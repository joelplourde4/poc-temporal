package com.cloudops.engine.dynamic;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.cloudops.engine.dynamic.model.WorkflowData;

import io.serverlessworkflow.api.actions.Action;
import io.serverlessworkflow.api.interfaces.State;
import io.serverlessworkflow.api.retry.RetryDefinition;
import io.serverlessworkflow.api.states.OperationState;
import io.serverlessworkflow.api.timeouts.StateExecTimeout;
import io.serverlessworkflow.api.timeouts.TimeoutsDefinition;
import io.serverlessworkflow.api.workflow.Retries;
import io.serverlessworkflow.utils.WorkflowUtils;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.common.converter.EncodedValues;
import io.temporal.workflow.ActivityStub;
import io.temporal.workflow.DynamicWorkflow;
import io.temporal.workflow.Workflow;

/**
 * The definition of a DynamicWorkflow
 */
public class MyDynamicWorkflowImpl implements DynamicWorkflow {

   private io.serverlessworkflow.api.Workflow dslWorkflow;
   private ActivityStub activity;

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
      Object arguments = encodedValues.get(0, String.class);

      // Using the workflow-id + workflow version, find the correct workflow.
      dslWorkflow = fetchDslWorkflow(id, version);

      // Initialize the activity based on the dsl workflow
      ActivityOptions activityOptions = initializeActivityOptions(dslWorkflow);

      // Create a dynamic activities stub to be used for all states in dsl
      activity = Workflow.newUntypedActivityStub(activityOptions);

      // Execute the first state of this workflow.
      executeState(WorkflowUtils.getStartingState(dslWorkflow));

      return workflowData;
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
         switch (workflowState.getType().value()) {
            case "operation": {
               executeState(executeOperation(workflowState));
               break;
            }
            default:
               throw new IllegalStateException("Other workflow state aren't yet supported");
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
    * Fetch the DSL workflow by its id and version
    */
   private io.serverlessworkflow.api.Workflow fetchDslWorkflow(String id, String version) {
      // Using the workflow-id + workflow version, find the correct workflow.
      // TODO this is where we would fetch the registered workflow from our db.
      return DynamicWorkflowUtils.getWorkflowFromFile("dsl/" + id + "-" + version + ".json");
   }

   /**
    * Initialize the Activity Options based on the DSL workflow
    * @param dslWorkflow The DSL workflow
    * @return Activity Options
    */
   private ActivityOptions initializeActivityOptions(io.serverlessworkflow.api.Workflow dslWorkflow) {
      // An example of a configuration
      return ActivityOptions.newBuilder()
              .setStartToCloseTimeout(initializeStartToCloseTimeout())
              .setRetryOptions(RetryOptions.newBuilder()
                      .setMaximumAttempts(initializeMaximumAttempts())
                      .validateBuildWithDefaults())
              .validateAndBuildWithDefaults();
   }

   /**
    * Example of a configuration of max attempts using the DSL workflow Retry policy at:
    * https://github.com/serverlessworkflow/specification/blob/main/specification.md#action-retries
    * @return Number of max attempts configured, else default to 5.
    */
   private int initializeMaximumAttempts() {
      List<RetryDefinition> retryDefinitions = Optional.ofNullable(dslWorkflow)
              .map(io.serverlessworkflow.api.Workflow::getRetries)
              .map(Retries::getRetryDefs)
              .orElse(new ArrayList<>());

      // Default to 5 retry attempts.
      if (retryDefinitions.isEmpty()) {
         return 5;
      }

      return Integer.parseInt(retryDefinitions.get(0).getMaxAttempts());
   }

   /**
    * Example of a configuration of start to close timeout for an activity using the DSL workflow Timeout policy at:
    * https://github.com/serverlessworkflow/specification/blob/main/specification.md#states-timeout-definition
    * @return The duration if provided, else default to 30 seconds.
    */
   private Duration initializeStartToCloseTimeout() {
      return Duration.parse(Optional.ofNullable(dslWorkflow)
              .map(io.serverlessworkflow.api.Workflow::getTimeouts)
              .map(TimeoutsDefinition::getStateExecTimeout)
              .map(StateExecTimeout::getTotal)
              .orElse("PT30S") // Default to 30 seconds timeout
      );
   }
}
