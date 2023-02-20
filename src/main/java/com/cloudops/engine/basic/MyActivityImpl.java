package com.cloudops.engine.basic;

import org.springframework.stereotype.Component;

import com.cloudops.engine.BaseActivity;

@Component
public class MyActivityImpl extends BaseActivity implements MyActivity {

   @Override
   public String doSomething() {
      return "yes";
   }
}
