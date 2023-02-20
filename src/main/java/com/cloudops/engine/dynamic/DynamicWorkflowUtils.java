package com.cloudops.engine.dynamic;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudops.engine.EngineApplication;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.workflow.BaseWorkflow;

/**
 * Static class containing a bunch of methods to simplify testing.
 */
public class DynamicWorkflowUtils {

   private static final Logger LOGGER = LoggerFactory.getLogger(DynamicWorkflowUtils.class);

   private DynamicWorkflowUtils() {
   }

   public static Workflow getWorkflowFromFile(String filename) {
      try {
         File file = new File(Objects.requireNonNull(EngineApplication.class.getClassLoader().getResource(filename)).getFile());
         return BaseWorkflow.fromSource(Files.readString(file.toPath()));
      } catch (Exception e) {
         LOGGER.warn("Exception: {}", e.getMessage());
      }
      throw new IllegalStateException("Unsupported workflow: " + filename);
   }
}
