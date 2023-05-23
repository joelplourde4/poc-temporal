package com.cloudops.engine.service.monitoring;

import java.util.List;

public interface WorkflowMonitoringService {

   List<Object> getRunningWorkflows(String id, String version);
}
