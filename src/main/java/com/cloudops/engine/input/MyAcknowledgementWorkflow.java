package com.cloudops.engine.input;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MyAcknowledgementWorkflow {

   @WorkflowMethod
   void waitingForAcknowledgement();

   @SignalMethod
   void receiveAcknowledgment();
}
