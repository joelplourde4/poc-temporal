package com.cloudops.engine.dynamic;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.serverlessworkflow.api.retry.RetryDefinition;
import io.serverlessworkflow.api.timeouts.StateExecTimeout;
import io.serverlessworkflow.api.timeouts.TimeoutsDefinition;
import io.serverlessworkflow.api.workflow.Retries;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;

public class ActivityOptionsUtils {

   private ActivityOptionsUtils() {}

   /**
    * Initialize the Activity Options based on the DSL workflow
    * @param dslWorkflow The DSL workflow
    * @return Activity Options
    */
   public static ActivityOptions initializeActivityOptions(io.serverlessworkflow.api.Workflow dslWorkflow) {
      // An example of a configuration
      return ActivityOptions.newBuilder()
              .setStartToCloseTimeout(ActivityOptionsUtils.initializeStartToCloseTimeout(dslWorkflow))
              .setRetryOptions(RetryOptions.newBuilder()
                      .setMaximumAttempts(ActivityOptionsUtils.initializeMaximumAttempts(dslWorkflow))
                      .validateBuildWithDefaults())
              .validateAndBuildWithDefaults();
   }

   /**
    * Example of a configuration of max attempts using the DSL workflow Retry policy at:
    * https://github.com/serverlessworkflow/specification/blob/main/specification.md#action-retries
    * @return Number of max attempts configured, else default to 5.
    */
   public static  int initializeMaximumAttempts(io.serverlessworkflow.api.Workflow dslWorkflow) {
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
   public static Duration initializeStartToCloseTimeout(io.serverlessworkflow.api.Workflow dslWorkflow) {
      return Duration.parse(Optional.ofNullable(dslWorkflow)
              .map(io.serverlessworkflow.api.Workflow::getTimeouts)
              .map(TimeoutsDefinition::getStateExecTimeout)
              .map(StateExecTimeout::getTotal)
              .orElse("PT30S") // Default to 30 seconds timeout
      );
   }
}
