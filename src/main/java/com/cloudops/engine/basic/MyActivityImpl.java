package com.cloudops.engine.basic;

import org.springframework.stereotype.Component;

@Component
public class MyActivityImpl implements MyActivity {

   @Override
   public String doSomething() {
      return "yes";
   }
}
