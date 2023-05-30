package com.cloudops.engine.basic.workflow.acknowledgment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cloudops.engine.BaseWorkflow;

import io.temporal.workflow.Workflow;

@Component
public class MyAcknowledgementWorkflowImpl extends BaseWorkflow implements MyAcknowledgementWorkflow {

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
