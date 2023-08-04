package com.imcloud.saas_user.kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.imcloud.saas_user.common.repository.MemberRepository;
import com.imcloud.saas_user.common.security.UserDetailsImpl;

import com.imcloud.saas_user.member.dto.MemberResponseDto;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class UserEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;


    private final String user_to_pay_topic = "user_to_pay_topic";
    private final String user_to_admin_active = "user_to_admin_active";
    private final String user_to_admin_dto = "user_to_admin_dto";

    public UserEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()); // Register JavaTimeModule
    }

    public void sendUserId(String deleteUserId) {
        try {
            String userId = deleteUserId;
            System.out.println("userId "+ userId);
            kafkaTemplate.send(user_to_pay_topic, userId);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to process message: " + e.getMessage());
        }
    }

    /*public void sendMemberDto(MemberResponseDto dto, String userId) {
        try {
            String message = objectMapper.writeValueAsString(dto);
            kafkaTemplate.send(user_to_admin_dto, userId, message);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to process message: " + e.getMessage());
        }
    }

    public void sendOk(String userId) {
        try {
            kafkaTemplate.send(user_to_admin_Ok, userId, "1");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to process message: " + e.getMessage());
        }
    }*/
}
