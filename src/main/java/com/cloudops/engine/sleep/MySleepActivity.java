package com.cloudops.engine.sleep;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface MySleepActivity {

   @ActivityMethod(name = "sleep")
   void sleep(long millis);
}
