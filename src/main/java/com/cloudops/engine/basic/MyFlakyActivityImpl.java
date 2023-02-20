package com.cloudops.engine.basic;

import org.springframework.stereotype.Component;

@Component
public class MyFlakyActivityImpl implements MyFlakyActivity {

   @Override
   public void flakyMethod() {
      if (Math.random() >= 0.5) {
         System.out.println("Let's divide by zero, shall we?");
         int number = 1 / 0;
      }
      System.out.println("Not today it seems...");
   }
}
