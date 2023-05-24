package com.cloudops.engine.dynamic;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.worker.Worker;

/**
 * Class which serves as a point of entry to create DynamicWorkflow.
 */
@Component
public class DynamicWorkflowHandler {

   private static final Logger LOGGER = LoggerFactory.getLogger(DynamicWorkflowHandler.class);

   private static final String VERSION = "v1";

   @Autowired
   private WorkflowClient workflowClient;

   @Autowired
   private Worker worker;

   /* Uncomment this Annotation to enable this workflow handler */
   // @PostConstruct
   public void init() {
      // Scenario 1 --------------------------------
      String id = "simple-workflow";

      // Scenario 2 --------------------------------
//      String id = "sleep-action";

      // Scenario 3 --------------------------------
//      String id = "multiple-actions";

      // Create a new Dynamic workflow on the Task queue: Task Queue
      WorkflowStub workflowStub = workflowClient.newUntypedWorkflowStub("MyDynamicWorkflowImpl", WorkflowOptions.newBuilder()
              .setWorkflowId(id + "-" + VERSION)
              .setTaskQueue("task-queue")
              .build()
      );

      // Start the Workflow with its id, version and any arguments
      workflowStub.start(id, VERSION, "argument");

      // Wait for workflow to finish, it will return a WorkloadData (Eventually)
      JsonNode result = workflowStub.getResult(JsonNode.class);

      LOGGER.info("Result: {}", result.toPrettyString());
   }

   // @PostConstruct
   public void sendSignal() {
      sendSignal("09d92d72-c8bd-46af-8f7e-87aa7d7f49b7", "AcknowledgementSignal");
   }

   private void sendSignal(String workflowId, String signal) {
      // Fetch the current running workflow by its type, id and task-queue.
      // Using the client-side stub to a single Workflow instance
      WorkflowStub untypedWorkflowStub = workflowClient.newUntypedWorkflowStub("MyDynamicWorkflowImpl",
              WorkflowOptions.newBuilder()
                      .setWorkflowId(workflowId)
                      .setTaskQueue("task-queue")
                      .build());

      // You can pass any arguments,
      untypedWorkflowStub.signalWithStart(signal, new Object[]{}, new Object[]{});
   }
}
