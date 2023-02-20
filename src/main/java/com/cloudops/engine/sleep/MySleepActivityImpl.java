package com.cloudops.engine.sleep;

import org.springframework.stereotype.Component;

import com.cloudops.engine.BaseActivity;

import io.temporal.workflow.Workflow;

@Component
public class MySleepActivityImpl extends BaseActivity implements MySleepActivity {

   @Override
   public void sleep(long millis) {
      Workflow.sleep(millis);
   }
}
