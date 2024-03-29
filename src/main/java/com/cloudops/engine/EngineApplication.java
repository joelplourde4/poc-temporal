package com.cloudops.engine;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

@EnableAutoConfiguration
@SpringBootApplication
public class EngineApplication {

   private static final String TASK_QUEUE = "task-queue";

   private static final String NAMESPACE = "default";

   @Autowired
   private List<BaseWorkflow> workflows;

   @Autowired
   private List<BaseActivity> activities;

   public static void main(String[] args) {
      SpringApplication.run(EngineApplication.class, args);
   }

   /*
      WorkflowService and WorkflowClient creation is a heavyweight operation, and will be resource-intensive if created each time you start a Workflow or send a Signal to it.
      The recommended way is to create them once and reuse where possible.
    */
   @Bean
   public WorkflowServiceStubs workflowServiceStubs() {
		/*
		return WorkflowServiceStubs.newInstance(
                    WorkflowServiceStubsOptions.newBuilder()
                     .setTarget(TARGET_ENDPOINT)
                            .build())
		 */
      return WorkflowServiceStubs.newLocalServiceStubs();
   }

   @Bean
   public WorkflowClient workflowClient(WorkflowServiceStubs workflowServiceStubs) {
      return WorkflowClient.newInstance(
              workflowServiceStubs,
              WorkflowClientOptions.newBuilder()
                      .setNamespace(NAMESPACE)
                      .build()
      );
   }

   @Bean
   public WorkerFactory workerFactory(WorkflowClient workflowClient) {
      return WorkerFactory.newInstance(workflowClient);
   }

   @Bean
   public Worker worker(WorkerFactory workerFactory) {
      Worker worker = workerFactory.newWorker(TASK_QUEUE);

      // Register all workflows
      workflows.forEach(workflow -> worker.registerWorkflowImplementationTypes(workflow.getClass()));

      // Register all Activities
      activities.forEach(worker::registerActivitiesImplementations);

      workerFactory.start();
      return worker;
   }
}
