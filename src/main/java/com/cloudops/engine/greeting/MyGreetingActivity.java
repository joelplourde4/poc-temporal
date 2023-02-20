package com.cloudops.engine.greeting;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface MyGreetingActivity {

   // By default, the name of the method is capitalized (E.g: ComposeGreeting)
   @ActivityMethod(name = "composeGreeting")
   String composeGreeting(String greeting, String name);
}
