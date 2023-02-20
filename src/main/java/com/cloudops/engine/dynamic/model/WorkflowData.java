package com.cloudops.engine.dynamic.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Class containing all of the results from the Activity, if any.
 */
public class WorkflowData {

   private static final ObjectMapper mapper = new ObjectMapper();

   private final ObjectNode value;

   public WorkflowData() {
      value = mapper.createObjectNode();
      value.putArray("results");

      // TODO Add a section for exceptions message.
   }

   /**
    * Get the current value  of the WorkflowData
    * @return
    */
   public ObjectNode getValue() {
      return value;
   }

   /**
    * Record the results
    *
    * @param data The data to record
    */
   public void addResults(String key, Object data) {
      ObjectNode result = mapper.createObjectNode()
              .put("name", key)
              .putPOJO("result", mapper.valueToTree(data));

      ((ArrayNode) this.value.get("results")).add(result);
   }

   /**
    * Format the value to a pretty string :)
    *
    * @return A pretty string
    */
   public String valueToString() {
      return value.toPrettyString();
   }
}
