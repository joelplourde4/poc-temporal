package com.cloudops.engine.basic.activity.greeting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cloudops.engine.BaseActivity;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class MyGreetingActivityImpl extends BaseActivity implements MyGreetingActivity {

   private static final Logger LOGGER = LoggerFactory.getLogger(MyGreetingActivityImpl.class);

   @Override
   public String composeGreeting(JsonNode jsonNode) {
      LOGGER.info("Composing greeting...");
      return jsonNode.get("greeting") + " " + jsonNode.get("name") + "!";
   }
}
