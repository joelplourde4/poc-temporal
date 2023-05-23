package com.cloudops.engine.basic.activity;

import org.springframework.stereotype.Component;

import com.cloudops.engine.BaseActivity;

@Component
public class MyActivityImpl extends BaseActivity implements MyActivity {

   @Override
   public String helloWorld() {
      return "Hello World!";
   }
}
