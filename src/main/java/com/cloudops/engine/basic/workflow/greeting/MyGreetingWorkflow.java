package com.cloudops.engine.basic.workflow.greeting;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MyGreetingWorkflow {

   @WorkflowMethod
   String getGreeting(String firstName, String lastName);
}
