package com.cloudops.engine.basic.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface MyActivity {

   @ActivityMethod(name = "helloWorld")
   String helloWorld();
}
