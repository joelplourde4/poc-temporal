package com.cloudops.engine.greeting;

import java.time.Duration;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

// Hard-coded Greeting Workflow
public class MyGreetingWorkflowImpl implements MyGreetingWorkflow {

   @Override
   public String getGreeting(String name) {

      MyGreetingActivity myGreetingActivity = Workflow.newActivityStub(MyGreetingActivity.class, ActivityOptions.newBuilder()
              .setStartToCloseTimeout(Duration.ofHours(1))
              .setRetryOptions(RetryOptions.newBuilder()
                      .setMaximumAttempts(5)
                      .build())
              .build());

      return myGreetingActivity.composeGreeting("World!", name);
   }
}
