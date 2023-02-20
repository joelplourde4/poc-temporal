package com.cloudops.engine.greeting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MyGreetingActivityImpl implements MyGreetingActivity {

   private static final Logger LOGGER = LoggerFactory.getLogger(MyGreetingActivityImpl.class);

   @Override
   public String composeGreeting(String greeting, String name) {
      LOGGER.info("Composing greeting...");
      return greeting + " " + name + "!";
   }
}
