package com.cloudops.engine.notifier;

import com.fasterxml.jackson.databind.JsonNode;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface MySlackNotifierActivity {

   // By default, the name of the method is capitalized (E.g: SendSlackNotification)
   @ActivityMethod(name = "sendSlackNotification")
   void sendSlackNotification(JsonNode jsonNode);
}
