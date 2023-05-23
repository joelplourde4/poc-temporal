package com.cloudops.engine.basic.activity.flaky;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cloudops.engine.BaseActivity;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class MyFlakyActivityImpl extends BaseActivity implements MyFlakyActivity {

   private static final Logger LOGGER = LoggerFactory.getLogger(MyFlakyActivityImpl.class);

   @Override
   public void flakyMethod(JsonNode jsonNode) {
      double percentage = Optional.ofNullable(jsonNode.get("failing_percentage"))
              .map(JsonNode::asDouble)
              .orElse(0.5);

      LOGGER.info("Entering a flaky method ...");
      if (Math.random() < percentage) {
         LOGGER.info("Let's divide by zero, shall we?");
         int number = 1 / 0;
      }
      LOGGER.info("All good!");
   }
}
