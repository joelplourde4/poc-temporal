package com.cloudops.engine.basic.activity.flaky;

import com.fasterxml.jackson.databind.JsonNode;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface MyFlakyActivity {

   @ActivityMethod(name = "flaky")
   void flakyMethod(JsonNode argument);
}
