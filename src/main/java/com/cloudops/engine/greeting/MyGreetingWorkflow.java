package com.cloudops.engine.greeting;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MyGreetingWorkflow {

   @WorkflowMethod
   String getGreeting(String name);
}
