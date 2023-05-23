package com.cloudops.engine.dynamic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.cloudops.engine.dynamic.model.WorkflowData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.serverlessworkflow.api.actions.Action;
import io.serverlessworkflow.api.events.OnEvents;
import io.serverlessworkflow.api.interfaces.State;
import io.serverlessworkflow.api.states.DefaultState;
import io.serverlessworkflow.api.states.EventState;
import io.serverlessworkflow.api.states.ForEachState;
import io.serverlessworkflow.api.states.OperationState;
import io.serverlessworkflow.api.states.SwitchState;
import io.serverlessworkflow.api.switchconditions.DataCondition;
import io.serverlessworkflow.utils.WorkflowUtils;
import io.temporal.activity.ActivityOptions;
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

   // A signal map that waits
   private Map<String, Boolean> signalMap = new HashMap<>();

   // TODO We would need a global workflow data where the result of each activity can be inserted into.

   /**
    * A single point of entry for any Dynamic Workflow
    */
   @Override
   public Object execute(EncodedValues encodedValues) {

      // Get the most basic information
      String id = encodedValues.get(0, String.class);
      String version = encodedValues.get(1, String.class);

      if (StringUtils.isBlank(id) || StringUtils.isBlank(version)) {
         return null;
      }

      // TODO Get any arguments for this particular workflow, for now, it is a simple string
      Object arguments = encodedValues.get(2, String.class);

      // Using the workflow-id + workflow version, find the correct workflow.
      dslWorkflow = DynamicWorkflowUtils.getWorkflowFromFile("dsl/" + id + "-" + version + ".json");

      // Start listening to any signals.
      registerSignalHandler();

      // Initialize the activity based on the dsl workflow
      ActivityOptions activityOptions = ActivityOptionsUtils.initializeActivityOptions(dslWorkflow);

      // Create a dynamic activities stub to be used for all states in dsl
      activity = Workflow.newUntypedActivityStub(activityOptions);

      // Execute the first state of this workflow.
      executeState(WorkflowUtils.getStartingState(dslWorkflow));

      return workflowData;
   }

   /**
    * Register the Signal Handler
    */
   private void registerSignalHandler() {
      Workflow.registerListener((DynamicSignalHandler) this::handleSignals);
   }

   /**
    * Method that handle signals received
    *
    * This method may "wake" up this workflow if necessary.
    *
    * @param signalName The name of the signal
    * @param encodedValues The encoded values
    */
   private void handleSignals(String signalName, EncodedValues encodedValues) {
      // Only if the received signal is known.
      if (signalMap.containsKey(signalName)) {
         signalMap.replace(signalName, true);
      }

      // Check if all signals have been received
      if (signalMap.values().stream().allMatch(x -> x)) {
         this.proceed = true;
      }
   }

   /**
    * This method execute the current state of the workflow
    * <p>
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
            case SWITCH -> executeState(executeSwitch(workflowState));
            case FOREACH -> executeState(executeForEach(workflowState));
            default -> throw new IllegalStateException("Other workflow state aren't yet supported");
         }
      }
   }

   /**
    * Execute an operation
    *
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

      return transitionNextState(operationState);
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
            // TODO, from the eventRefs, build a signal map that can act as a counter, right now it is ON/OFF

            // For each event refs, add this key to the signal map.
            onEvents.getEventRefs().forEach(eventRef -> signalMap.put(eventRef, false));

            try {
               Method method = this.getClass().getMethod(action.getFunctionRef().getRefName(), JsonNode.class);
               method.invoke(this, action.getFunctionRef().getArguments());
            } catch (SecurityException | NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
               LOGGER.warn("An exception has been thrown: {}", ex.getMessage());
            }
         }
      }

      return transitionNextState(eventState);
   }

   /**
    * Execute a switch statement to determine, based on a condition, what is the next state to proceed to.
    *
    * @param workflowState The workflow state to execute as an operation
    * @return The next state that fulfill the condition, if any.
    */
   private State executeSwitch(State workflowState) {
      if (!(workflowState instanceof SwitchState switchState)) {
         throw new IllegalStateException("The type of the state " + workflowState.getType() + " does not match its implementation, please verify.");
      }

      String nextState = "";
      for (DataCondition dataCondition : switchState.getDataConditions()) {

         // TODO validate the condition for real.
         if (true) {
            nextState = dataCondition.getTransition().getNextState();

            // Stop at the first true condition.
            break;
         }
      }

      // If none of the condition was ok
      if (StringUtils.isBlank(nextState)) {

         // Try to fallback on the default condition
         if (switchState.getDefaultCondition().getTransition() != null && StringUtils.isNotBlank(switchState.getDefaultCondition().getTransition().getNextState())) {
            return WorkflowUtils.getStateWithName(dslWorkflow, switchState.getDefaultCondition().getTransition().getNextState());
         }

         // Else, return null.
         return null;
      }

      return WorkflowUtils.getStateWithName(dslWorkflow, nextState);
   }

   /**
    * Execute a forEach statement to execute multiple actions either sequentially or asynchronously
    *
    * @param workflowState The workflow state to execute as an operation
    * @return The next state to transition to, if any.
    */
   private State executeForEach(State workflowState) {
      if (!(workflowState instanceof ForEachState forEachState)) {
         throw new IllegalStateException("The type of the state " + workflowState.getType() + " does not match its implementation, please verify.");
      }

      ObjectMapper mapper = new ObjectMapper();
      JsonNode jsonNode;
      try {
         jsonNode = mapper.readTree(forEachState.getInputCollection());
      } catch (JsonProcessingException e) {
         throw new IllegalStateException("The manifest is malformed, this should never happen. Please verify.");
      }

      if (!jsonNode.isArray()) {
         throw new IllegalStateException("The input collections is malformed. Please verify.");
      }

      // A ForEach state can have multiple actions:
      for (Action action : forEachState.getActions()) {

//         if (forEachState.getMode() == ForEachState.Mode.PARALLEL) {
//            System.out.println("Executing in parallel!");
//            List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
//            for (final JsonNode object : jsonNode) {
//               completableFutures.add(CompletableFuture.supplyAsync(() -> {
//                  Object data = activity.execute(
//                          action.getFunctionRef().getRefName(),
//                          String.class, // This will need to be generify to a generic payload returned by an activity.
//                          object);
//                  workflowData.addResults(action.getName(), data);
//                  return null;
//               }));
//            }
//            completableFutures.forEach(CompletableFuture::join);
//         } else {
            System.out.println("Executing sequentially!");
            for (final JsonNode object : jsonNode) {
               Object data = activity.execute(
                       action.getFunctionRef().getRefName(),
                       String.class, // This will need to be generify to a generic payload returned by an activity.
                       object);

               workflowData.addResults(action.getName(), data);
//            }
         }
      }

      return transitionNextState(forEachState);
   }

   /**
    * This method blocks until a signal has been received.
    * @param jsonNode The arguments, if any.
    */
   public void waitForSignal(JsonNode jsonNode) {
      this.proceed = false;

      while (!proceed) {
         System.out.println("Waiting for signal before proceeding....");
         // TODO wait for complex condition from the arguments.
         Workflow.await(() -> proceed); // We wait for the Boolean condition to turn true
      }
   }

   /**
    * Transition to the next state
    *
    * @param currentState Current State
    * @return The next state, or null if it is the last state.
    */
   private State transitionNextState(DefaultState currentState) {
      // Check if there is a transition.
      if (currentState.getTransition() == null || currentState.getTransition().getNextState() == null) {
         // Else, return null.
         return null;
      }

      // Proceed to the next state.
      return WorkflowUtils.getStateWithName(dslWorkflow, currentState.getTransition().getNextState());
   }
}
