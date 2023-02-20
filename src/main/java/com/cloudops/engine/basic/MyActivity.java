package com.cloudops.engine.basic;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface MyActivity {

   @ActivityMethod(name = "doSomething")
   String doSomething();
}
