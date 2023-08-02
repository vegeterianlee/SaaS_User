package com.imcloud.saas_user.kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imcloud.saas_user.common.security.UserDetailsImpl;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class UserEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String user_to_pay_topic = "user_to_pay_topic";
    private final String user_to_admin_topic = "user_to_admin_topic";

    public UserEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUserDetails(UserDetailsImpl userDetails) {
        try {
            String userEvent = objectMapper.writeValueAsString(userDetails); // Convert userDetails to a JSON String
            kafkaTemplate.send(user_to_pay_topic, userEvent);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            // Proper exception handling to be done
        }
    }
}
