package com.cloudops.engine.basic.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MyWorkflow {

   @WorkflowMethod
   void helloWorld();
}
