package com.cloudops.engine.sleep;

import com.fasterxml.jackson.databind.JsonNode;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface MySleepActivity {

   @ActivityMethod(name = "sleep")
   void sleep(JsonNode jsonNode);
}
