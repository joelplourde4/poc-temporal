package com.cloudops.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cloudops.engine.greeting.MyGreetingWorkflow;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;

@Component
public class WorkflowHandler {

   @Autowired
   private WorkflowClient workflowClient;

   // @PostConstruct
   public void init() {
      System.out.println("Initializing the Workflow Handler!");

      // Using the Client, create a new Workflow
      //MyWorkflow myWorkflow = workflowClient.newWorkflowStub(MyWorkflow.class, WorkflowOptions.newBuilder()
      //        .setTaskQueue("task-queue") // It is important to define in which queue does this workflow needs to be execute
      //        .validateBuildWithDefaults()
      //);

      MyGreetingWorkflow myGreetingWorkflow = workflowClient.newWorkflowStub(MyGreetingWorkflow.class, WorkflowOptions.newBuilder()
                 .setTaskQueue("task-queue")
                 .validateBuildWithDefaults()
      );

      // Using the static method.
      WorkflowClient.start(() -> myGreetingWorkflow.getGreeting("World!"));

      System.out.println("Registering a workflow.");
   }
}
