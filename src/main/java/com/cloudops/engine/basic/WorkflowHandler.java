package com.cloudops.engine.basic;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cloudops.engine.basic.activity.acknowledgment.flaky.MyFlakyWorkflow;
import com.cloudops.engine.basic.workflow.acknowledgment.MyAcknowledgementWorkflow;
import com.cloudops.engine.basic.workflow.greeting.MyGreetingWorkflow;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;

@Component
public class WorkflowHandler {

   @Autowired
   private WorkflowClient workflowClient;

   /* Uncomment this Annotation to enable this workflow handler */
   // @PostConstruct
   public void init() {
      System.out.println("Initializing the Workflow Handler!");

      // Scenario 1 --------------------------------

//      MyWorkflow myWorkflow = workflowClient.newWorkflowStub(MyWorkflow.class, WorkflowOptions.newBuilder()
//              .setTaskQueue("task-queue") // It is important to define in which queue does this workflow needs to be execute
//              .validateBuildWithDefaults()
//      );
//
//      // Using the static method.
//      WorkflowClient.start(myWorkflow::helloWorld);

      // Scenario 2 --------------------------------

      MyGreetingWorkflow myGreetingWorkflow = workflowClient.newWorkflowStub(MyGreetingWorkflow.class, WorkflowOptions.newBuilder()
                 .setTaskQueue("task-queue")
                 .validateBuildWithDefaults()
      );

      // Using the static method.
      WorkflowClient.start(() -> myGreetingWorkflow.getGreeting("Hello", "Michael Wazowski"));

      // Scenario 3 --------------------------------

//      MyFlakyWorkflow myFlakyWorkflow = workflowClient.newWorkflowStub(MyFlakyWorkflow.class, WorkflowOptions.newBuilder()
//                 .setTaskQueue("task-queue")
//                 .validateBuildWithDefaults()
//      );
//
//      WorkflowClient.start(myFlakyWorkflow::flakyWorkflow);

      // Scenario 4 --------------------------------
//      MyAcknowledgementWorkflow myInputWorkflow = workflowClient.newWorkflowStub(MyAcknowledgementWorkflow.class, WorkflowOptions.newBuilder()
//              .setTaskQueue("task-queue")
//              .validateBuildWithDefaults()
//      );
//
//      WorkflowClient.start(myInputWorkflow::waitingForAcknowledgement);
      // Scenario 4 (continued) ----------------------

//      sendSignal("2f9d4543-33d8-4911-a817-238735e38ef5");

      //------------------------------------------
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
