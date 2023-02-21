package com.cloudops.engine.dynamic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudops.engine.dynamic.model.WorkflowData;
import com.fasterxml.jackson.databind.JsonNode;

import io.serverlessworkflow.api.actions.Action;
import io.serverlessworkflow.api.events.OnEvents;
import io.serverlessworkflow.api.interfaces.State;
import io.serverlessworkflow.api.retry.RetryDefinition;
import io.serverlessworkflow.api.states.DefaultState;
import io.serverlessworkflow.api.states.EventState;
import io.serverlessworkflow.api.states.OperationState;
import io.serverlessworkflow.api.timeouts.StateExecTimeout;
import io.serverlessworkflow.api.timeouts.TimeoutsDefinition;
import io.serverlessworkflow.api.workflow.Retries;
import io.serverlessworkflow.utils.WorkflowUtils;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.common.converter.EncodedValues;
import io.temporal.workflow.ActivityStub;
import io.temporal.workflow.DynamicSignalHandler;
import io.temporal.workflow.DynamicWorkflow;
import io.temporal.workflow.Workflow;

/**
 * The definition of a DynamicWorkflow
 */
public class MyDynamicWorkflowImpl implements DynamicWorkflow {

   private static final Logger LOGGER = LoggerFactory.getLogger(MyDynamicWorkflowImpl.class);

   private io.serverlessworkflow.api.Workflow dslWorkflow;
   private ActivityStub activity;

   private boolean proceed;

   private final WorkflowData workflowData = new WorkflowData();

   // TODO We would need a global workflow data where the result of each activity can be inserted into.

   /**
    * A single point of entry for any Dynamic Workflow
    */
   @Override
   public Object execute(EncodedValues encodedValues) {

      // Get the most basic information
      String id = encodedValues.get(0, String.class);
      String version = encodedValues.get(1, String.class);

      // TODO Get any arguments for this particular workflow, for now, it is a simple string
      Object arguments = encodedValues.get(2, String.class);

      // Using the workflow-id + workflow version, find the correct workflow.
      dslWorkflow = fetchDslWorkflow(id, version);

      // Start listening to any signals.
      registerSignals();

      // Initialize the activity based on the dsl workflow
      ActivityOptions activityOptions = ActivityOptionsUtils.initializeActivityOptions(dslWorkflow);

      // Create a dynamic activities stub to be used for all states in dsl
      activity = Workflow.newUntypedActivityStub(activityOptions);

      // Execute the first state of this workflow.
      executeState(WorkflowUtils.getStartingState(dslWorkflow));

      return workflowData;
   }

   private void registerSignals() {
      Workflow.registerListener((DynamicSignalHandler) this::handleSignals);
   }

   private void handleSignals(String signalName, EncodedValues encodedValues) {
      // TODO Handle the signal
      System.out.println("signal: " + signalName);
      System.out.println("encoded values: " + encodedValues);
   }

   /**
    * This method execute the current state of the workflow
    *
    * Until the workflow has a next state, it'll continue
    *
    * @param workflowState The current DSL workflow state
    */
   private void executeState(State workflowState) {
      // Based on the Type of the workflow, execute it appropriately.
      if (workflowState != null) {
         switch (workflowState.getType()) {
            case OPERATION -> executeState(executeOperation(workflowState));
            case EVENT -> executeState(executeEvent(workflowState));
            default -> throw new IllegalStateException("Other workflow state aren't yet supported");
         }
      }
   }

   /**
    * Execute an operation
    * @param workflowState The workflow state to execute as an operation
    * @return The next state if any.
    */
   private State executeOperation(State workflowState) {
      if (!(workflowState instanceof OperationState operationState)) {
         throw new IllegalStateException("The type of the state " + workflowState.getType() + " does not match its implementation, please verify.");
      }

      // An operation state can have multiple actions:
      for (Action action : operationState.getActions()) {

         // TODO Make this Asynchronous, right now it is synchronous and thus blocking
         // BUT only if the actionMode is NOT sequential.

         Object data = activity.execute(
                 action.getFunctionRef().getRefName(),
                 String.class, // This will need to be generify to a generic payload returned by an activity.
                 action.getFunctionRef().getArguments());

         // Record the data returned by the activity
         workflowData.addResults(action.getName(), data);
      }

      // Check if there is a transition.
      if (operationState.getTransition() == null || operationState.getTransition().getNextState() == null) {
         return null;
      }

      // Proceed to the next state.
      return WorkflowUtils.getStateWithName(dslWorkflow, operationState.getTransition().getNextState());
   }

   /**
    * An event executes a method on the workflow itself.
    *
    * @param workflowState The current workflow state
    * @return The next workflow state, if any.
    */
   private State executeEvent(State workflowState) {
      if (!(workflowState instanceof EventState eventState)) {
         throw new IllegalStateException("The type of the state " + workflowState.getType() + " does not match its implementation, please verify.");
      }

      for (OnEvents onEvents : eventState.getOnEvents()) {
         for (Action action : onEvents.getActions()) {
            try {
               Method method = this.getClass().getMethod(action.getFunctionRef().getRefName(), JsonNode.class);
               method.invoke(this, action.getFunctionRef().getArguments());
            } catch (SecurityException | NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
               LOGGER.warn("An exception has been thrown: {}", ex.getMessage());
            }
         }
      }

      // Check if there is a transition.
      if (eventState.getTransition() == null || eventState.getTransition().getNextState() == null) {
         return null;
      }

      // Proceed to the next state.
      return WorkflowUtils.getStateWithName(dslWorkflow, eventState.getTransition().getNextState());
   }

   /**
    * Fetch the DSL workflow by its id and version
    */
   private io.serverlessworkflow.api.Workflow fetchDslWorkflow(String id, String version) {
      // Using the workflow-id + workflow version, find the correct workflow.
      // TODO this is where we would fetch the registered workflow from our db.
      return DynamicWorkflowUtils.getWorkflowFromFile("dsl/" + id + "-" + version + ".json");
   }

   public void waitForSignal(JsonNode jsonNode) {
      // TODO use a signal map to complexify this behavior
      while (!proceed) {
         System.out.println("Waiting for signal before proceeding....");
         Workflow.await(() -> proceed); // We wait for the Boolean condition to turn true
      }
   }
}
