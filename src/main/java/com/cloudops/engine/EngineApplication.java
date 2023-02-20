package com.cloudops.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.cloudops.engine.basic.MyActivityImpl;
import com.cloudops.engine.basic.MyFlakyActivityImpl;
import com.cloudops.engine.basic.MyWorkflowImpl;
import com.cloudops.engine.dynamic.MyDynamicWorkflowImpl;
import com.cloudops.engine.greeting.MyGreetingActivity;
import com.cloudops.engine.greeting.MyGreetingWorkflowImpl;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

@EnableAutoConfiguration
@SpringBootApplication
public class EngineApplication implements CommandLineRunner {

   private static final Logger LOGGER = LoggerFactory.getLogger(EngineApplication.class);

   private static final String TASK_QUEUE = "task-queue";

   private static final String NAMESPACE = "default";

   @Autowired
   private MyActivityImpl myActivity;

   @Autowired
   private MyFlakyActivityImpl myFlakyActivity;

   @Autowired
   private MyGreetingActivity myGreetingActivity;

   public static void main(String[] args) {
      SpringApplication.run(EngineApplication.class, args);
   }

   @Override
   public void run(String... args) throws Exception {
      LOGGER.info("Joining thread, you can press Ctrl+C to shutdown application");
      // Thread.currentThread().join();
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

      // For Dynamic DSL Workflow
      worker.registerWorkflowImplementationTypes(MyDynamicWorkflowImpl.class);

      worker.registerWorkflowImplementationTypes(MyWorkflowImpl.class);  // Needs to point to the implementation of the workflow
      worker.registerActivitiesImplementations(myActivity);
      worker.registerActivitiesImplementations(myFlakyActivity);

      // Greeting workflows
      worker.registerWorkflowImplementationTypes(MyGreetingWorkflowImpl.class);
      worker.registerActivitiesImplementations(myGreetingActivity);

      workerFactory.start();
      return worker;
   }
}
