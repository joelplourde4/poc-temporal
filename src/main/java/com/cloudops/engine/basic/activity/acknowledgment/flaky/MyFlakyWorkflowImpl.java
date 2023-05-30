package com.cloudops.engine.basic.activity.acknowledgment.flaky;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cloudops.engine.BaseWorkflow;
import com.cloudops.engine.basic.activity.flaky.MyFlakyActivity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

@Component
public class MyFlakyWorkflowImpl extends BaseWorkflow implements MyFlakyWorkflow {

   private static final Logger LOGGER = LoggerFactory.getLogger(MyFlakyWorkflowImpl.class);

   private static final float FAILING_PERCENTAGE = 0.5f;

   private static final long SLEEP_TIMER_MILLIS = 4000;

   private static final int RETRY_ATTEMPTS = 5;

   @Override
   public void flakyWorkflow() {
      for (int i = 0; i < RETRY_ATTEMPTS; i++) {
         MyFlakyActivity myFlakyActivity = Workflow.newActivityStub(MyFlakyActivity.class, ActivityOptions.newBuilder()
                 .setStartToCloseTimeout(Duration.ofHours(1))
                 .setRetryOptions(RetryOptions.newBuilder()
                         .setMaximumAttempts(RETRY_ATTEMPTS)
                         .build())
                 .build());

         ObjectNode jsonNode = new ObjectMapper().createObjectNode();
         jsonNode.put("failing_percentage", FAILING_PERCENTAGE);

         myFlakyActivity.flakyMethod(jsonNode);

         Workflow.sleep(SLEEP_TIMER_MILLIS);
      }

      LOGGER.info("The flaky workflow has been completed successfully!");
   }
}
