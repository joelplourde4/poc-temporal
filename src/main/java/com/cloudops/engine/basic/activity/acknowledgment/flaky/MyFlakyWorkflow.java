package com.cloudops.engine.basic.activity.acknowledgment.flaky;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MyFlakyWorkflow {

   @WorkflowMethod
   void flakyWorkflow();
}
