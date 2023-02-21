package com.cloudops.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;

@Component
public class WorkflowHandler {

   @Autowired
   private WorkflowClient workflowClient;

   //@PostConstruct
   public void init() {
      System.out.println("Initializing the Workflow Handler!");

      // Using the Client, create a new Workflow
      //MyWorkflow myWorkflow = workflowClient.newWorkflowStub(MyWorkflow.class, WorkflowOptions.newBuilder()
      //        .setTaskQueue("task-queue") // It is important to define in which queue does this workflow needs to be execute
      //        .validateBuildWithDefaults()
      //);

//      MyGreetingWorkflow myGreetingWorkflow = workflowClient.newWorkflowStub(MyGreetingWorkflow.class, WorkflowOptions.newBuilder()
//                 .setTaskQueue("task-queue")
//                 .validateBuildWithDefaults()
//      );

      // Using the static method.
      // WorkflowClient.start(() -> myGreetingWorkflow.getGreeting("World!"));

      //MyAcknowledgementWorkflow myInputWorkflow = workflowClient.newWorkflowStub(MyAcknowledgementWorkflow.class, WorkflowOptions.newBuilder()
      //        .setTaskQueue("task-queue")
      //        .validateBuildWithDefaults()
      //);

      // Wait for Acknowledgement
      //WorkflowClient.start(myInputWorkflow::waitingForAcknowledgement);

      sendSignal("d27612b5-b0a5-442b-9d2a-59516a3c8bd3");

      System.out.println("Registering a workflow.");
   }

   private void sendSignal(String workflowId) {
      // Fetch the current running workflow by its type, id and task-queue.
      // Using the client-side stub to a single Workflow instance
      WorkflowStub untypedWorkflowStub = workflowClient.newUntypedWorkflowStub("MyAcknowledgementWorkflow",
              WorkflowOptions.newBuilder()
                      .setWorkflowId(workflowId)
                      .setTaskQueue("task-queue")
                      .build());

      // You can pass any arguments,
      untypedWorkflowStub.signalWithStart("receiveAcknowledgment", new Object[]{}, new Object[]{});
   }
}
