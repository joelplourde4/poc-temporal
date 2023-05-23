package com.cloudops.engine.service.monitoring;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.temporal.api.filter.v1.WorkflowTypeFilter;
import io.temporal.api.workflowservice.v1.ListOpenWorkflowExecutionsRequest;
import io.temporal.client.WorkflowClient;

@Component
public class WorkflowMonitoringServiceImpl implements WorkflowMonitoringService {

   @Autowired
   private WorkflowClient workflowClient;

   @Override
   public List<Object> getRunningWorkflows(String id, String version) {
      WorkflowTypeFilter workflowTypeFilter = WorkflowTypeFilter.newBuilder()
              .setName("MyDynamicWorkflowImpl")
              .build();

      ListOpenWorkflowExecutionsRequest listOpenWorkflowExecutionsRequest = ListOpenWorkflowExecutionsRequest.newBuilder()
              .setTypeFilter(workflowTypeFilter)
              .setNamespace("default")
              .build();

      return null;
   }
}
