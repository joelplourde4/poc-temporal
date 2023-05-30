package com.cloudops.engine.basic.workflow.greeting;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cloudops.engine.BaseWorkflow;
import com.cloudops.engine.basic.activity.greeting.MyGreetingActivity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

// Hard-coded Greeting Workflow
@Component
public class MyGreetingWorkflowImpl extends BaseWorkflow implements MyGreetingWorkflow {

   private static final Logger LOGGER = LoggerFactory.getLogger(MyGreetingWorkflowImpl.class);

   @Override
   public String getGreeting(String greeting, String name) {
      MyGreetingActivity myGreetingActivity = Workflow.newActivityStub(MyGreetingActivity.class, ActivityOptions.newBuilder()
              .setStartToCloseTimeout(Duration.ofHours(1))
              .setRetryOptions(RetryOptions.newBuilder()
                      .setMaximumAttempts(5)
                      .build())
              .build());

      ObjectNode jsonNode = new ObjectMapper().createObjectNode();
      jsonNode.put("greeting", greeting);
      jsonNode.put("name", name);

      String result = myGreetingActivity.composeGreeting(jsonNode);

      LOGGER.info("{}", result);

      return result;
   }
}
