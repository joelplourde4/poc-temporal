package com.cloudops.engine.input;

import com.fasterxml.jackson.databind.JsonNode;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface MyAcknowledgementActivity {

   // By default, the name of the method is capitalized (E.g: ComposeGreeting)
   @ActivityMethod(name = "waitingForAcknowledgement")
   void waitingForAcknowledgement(JsonNode jsonNode);
}
