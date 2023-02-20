package com.cloudops.engine.sleep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cloudops.engine.BaseActivity;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class MySleepActivityImpl extends BaseActivity implements MySleepActivity {

   private static final Logger LOGGER = LoggerFactory.getLogger(MySleepActivityImpl.class);

   @Override
   public void sleep(JsonNode jsonNode) {
      long millis = jsonNode.get("millis").asLong();
      LOGGER.info("Sleeping for {}...", millis);
      try {
         Thread.sleep(millis);
      } catch(Exception ex) {
         LOGGER.warn("An exception occurred: {}", ex);
      }
   }
}
