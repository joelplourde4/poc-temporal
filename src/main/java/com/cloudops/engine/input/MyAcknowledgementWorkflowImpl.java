package com.cloudops.engine.input;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.temporal.workflow.Workflow;

public class MyAcknowledgementWorkflowImpl implements MyAcknowledgementWorkflow {

   private static final Logger LOGGER = LoggerFactory.getLogger(MyAcknowledgementWorkflowImpl.class);

   private boolean proceed = false;

   @Override
   public void waitingForAcknowledgement() {
      while (!proceed) {
         Workflow.await(() -> proceed);
      }
      LOGGER.info("Continue forth! let's go!");
   }

   @Override
   public void receiveAcknowledgment() {
      this.proceed = true;

      LOGGER.info("Received the acknowledgement.");
   }
}
