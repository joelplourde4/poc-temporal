package com.cloudops.engine.basic;

import java.time.Duration;

import com.cloudops.engine.BaseActivity;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

public class MyWorkflowImpl implements MyWorkflow {

   @Override
   public void helloWorld() {
      System.out.println("Hello World!");

      for (int i = 0; i < 5; i++) {
         System.out.println("count: " + i);

         // Define the Activity within the Workflow with a retry policy of 5 attempts.
         MyActivity myActivity = Workflow.newActivityStub(MyActivity.class, ActivityOptions.newBuilder()
                 .setStartToCloseTimeout(Duration.ofHours(1))
                 .setRetryOptions(RetryOptions.newBuilder()
                         .setMaximumAttempts(5)
                         .build())
                 .build());

         // Run the Activity
         String result = myActivity.doSomething();

         System.out.println("Activity 1: " + result);

         MyFlakyActivity myFlakyActivity = Workflow.newActivityStub(MyFlakyActivity.class, ActivityOptions.newBuilder()
                 .setStartToCloseTimeout(Duration.ofHours(1))
                 .setRetryOptions(RetryOptions.newBuilder()
                         .setMaximumAttempts(5)
                         .build())
                 .build());

         myFlakyActivity.flakyMethod();

         System.out.println("Activity 2: " + result);

         Workflow.sleep(4000);
      }

      System.out.println("The workflow successfully completed!");
   }
}
