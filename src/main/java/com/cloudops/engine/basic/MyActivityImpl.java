package com.cloudops.engine.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cloudops.engine.BaseActivity;

@Component
public class MyActivityImpl extends BaseActivity implements MyActivity {

   private static final Logger LOGGER = LoggerFactory.getLogger(MyActivityImpl.class);

   @Override
   public String doSomething() {
      LOGGER.info("DAD! DON'T TALK TO MY FRIENDS");
      return "O.M.G YOU ARE SOOOOO LAME";
   }
}
