package com.cloudops.engine.notifier;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cloudops.engine.BaseActivity;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class MySlackNotifierActivityImpl extends BaseActivity implements MySlackNotifierActivity {

   //@Autowired
   //private SlackClient slackClient;

   @Override
   public void sendSlackNotification(JsonNode jsonNode) {
      // Extract the Channel
      String channel = Optional.ofNullable(jsonNode.get("channel"))
              .map(JsonNode::textValue)
              .orElseThrow(() -> new IllegalStateException("You must provide the Slack channel"));

      // Extract the message
      String message = Optional.ofNullable(jsonNode.get("message"))
              .map(JsonNode::textValue)
              .orElse("No message");

      // Using the Slack client, send a notification
   }
}
