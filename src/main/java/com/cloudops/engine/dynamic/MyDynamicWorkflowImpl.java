package com.cloudops.engine.dynamic;

import java.time.Duration;

import io.serverlessworkflow.api.actions.Action;
import io.serverlessworkflow.api.interfaces.State;
import io.serverlessworkflow.api.states.OperationState;
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
   private ActivityStub activities;

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

      // Create a dynamic activities stub to be used for all actions in dsl
      activities = Workflow.newUntypedActivityStub(activityOptions);

      // Execute the first state of this workflow.
      executeState(WorkflowUtils.getStartingState(dslWorkflow));

      // TODO return the Workload data built overtime here.
      return null;
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
         Object result = activities.execute(
                 action.getFunctionRef().getRefName(),
                 String.class, // This will need to be generify to a generic payload returned by an activity.
                 action.getFunctionRef().getArguments());

         // TODO Add the result to the global workload data.
         System.out.println("Result from the activity: " + result);
      }

      // Check if there is a transition.
      if (operationState.getTransition() == null || operationState.getTransition().getNextState() == null) {
         System.out.println("No next state, we are done.");
         return null;
      }

      System.out.println("Proceed to the next state of this workflow.");
      return WorkflowUtils.getStateWithName(dslWorkflow, operationState.getTransition().getNextState());
   }

   /**
    * Fetch the DSL workflow by its id and version
    */
   private io.serverlessworkflow.api.Workflow fetchDslWorkflow(String id, String version) {
      System.out.println("Fetching the workflow using: id: " + id + " version: " + version);
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
      int maximumAttempts = dslWorkflow.isAutoRetries() ? 5 : 0;

      return ActivityOptions.newBuilder()
              .setStartToCloseTimeout(Duration.ofSeconds(10))
              .setRetryOptions(RetryOptions.newBuilder()
                      .setMaximumAttempts(maximumAttempts)
                      .validateBuildWithDefaults())
              .validateAndBuildWithDefaults();
   }
}
