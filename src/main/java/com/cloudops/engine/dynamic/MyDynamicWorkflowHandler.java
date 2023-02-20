package com.cloudops.engine.dynamic;

import javax.annotation.PostConstruct;

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
public class MyDynamicWorkflowHandler {

   @Autowired
   private WorkflowClient workflowClient;

   @Autowired
   private Worker worker;

   @PostConstruct
   public void init() {
      // Hardcode this information of which workflow to fetch from the datasource
      String id = "simple-workflow";
      String version = "v1";

      // Create a new Dynamic workflow on the Task queue: Task Queue
      WorkflowStub workflowStub = workflowClient.newUntypedWorkflowStub("MyDynamicWorkflowImpl", WorkflowOptions.newBuilder()
              .setTaskQueue("task-queue")
              .build()
      );

      // Start the Workflow with its id, version and any arguments
      workflowStub.start(id, version, "argument");

      // Wait for workflow to finish, it will return a WorkloadData (Eventually)
      JsonNode result = workflowStub.getResult(JsonNode.class);

      System.out.println("Result: " + result.toPrettyString());
   }
}
