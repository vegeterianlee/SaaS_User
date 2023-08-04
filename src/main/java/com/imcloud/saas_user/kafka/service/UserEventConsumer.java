package com.imcloud.saas_user.kafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imcloud.saas_user.common.dto.ApiResponse;
import com.imcloud.saas_user.common.dto.ErrorMessage;
import com.imcloud.saas_user.common.entity.Member;
import com.imcloud.saas_user.common.repository.MemberRepository;
import com.imcloud.saas_user.member.dto.MemberResponseDto;
import com.imcloud.saas_user.member.dto.ProfileUpdateRequestDto;
import com.imcloud.saas_user.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
public class UserEventConsumer {

    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final UserEventProducer userEventProducer;
    private final String pay_to_user_topic = "pay_to_user_topic";
    private final String admin_to_user_topic = "admin_to_user_topic";

    @KafkaListener(topics = pay_to_user_topic)
    public void handleDeleteMember(String userId) {
        try {
            // 사용자 확인
            Member member = memberRepository.findByUserId(userId).orElseThrow(
                    () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
            );

            // 회원 정보 삭제
            memberRepository.delete(member);
            System.out.println("Success in deleting user with userId: "+ userId);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to process delete user message: " + e.getMessage());
        }
    }



    /*@KafkaListener(topics = admin_to_user_topic)
    public void handleUpdateMember(ConsumerRecord<String, String> record) {
        try {
            String userId = record.key();
            ProfileUpdateRequestDto dto = objectMapper.readValue(record.value(), ProfileUpdateRequestDto.class);

            // 사용자 확인
            Member member = memberRepository.findByUserId(userId).orElseThrow(
                    () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
            );
            dto.getUsername().ifPresent(member::setUsername);
            dto.getPhone().ifPresent(member::setPhone);
            dto.getEmail().ifPresent(member::setEmail);
            dto.getInstitution().ifPresent(member::setInstitution);

            if (dto.getNewPassword().isPresent()) {
                member.setPassword(passwordEncoder.encode(dto.getNewPassword().get()));
            }
            memberRepository.save(member);
            //memberService.kafkaMemberResponse(member);
            //userEventProducer.sendOk(member.getUserId());
            //userEventProducer.sendMemberDto(MemberResponseDto.of(member), member.getUserId());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to process delete user message: " + e.getMessage());

        }
    }*/
}
