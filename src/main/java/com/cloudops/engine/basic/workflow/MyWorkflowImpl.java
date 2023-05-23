package com.cloudops.engine.basic.workflow;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudops.engine.basic.activity.MyActivity;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

public class MyWorkflowImpl implements MyWorkflow {

   private static final Logger LOGGER = LoggerFactory.getLogger(MyWorkflowImpl.class);

   @Override
   public void helloWorld() {
      // Define the Activity within the Workflow with a retry policy of 5 attempts.
      MyActivity myActivity = Workflow.newActivityStub(MyActivity.class, ActivityOptions.newBuilder()
              .setStartToCloseTimeout(Duration.ofHours(1))
              .setRetryOptions(RetryOptions.newBuilder()
                      .setMaximumAttempts(5)
                      .build())
              .build());

      // Run the Activity
      String result = myActivity.helloWorld();

      LOGGER.info("Result: {}", result);
   }
}
