package com.cloudops.engine.basic;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MyWorkflow {

   @WorkflowMethod
   void helloWorld();
}
