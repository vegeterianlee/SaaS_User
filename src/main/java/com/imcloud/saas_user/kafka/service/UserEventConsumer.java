package com.imcloud.saas_user.kafka.service;

import com.imcloud.saas_user.common.dto.ErrorMessage;
import com.imcloud.saas_user.common.entity.Member;
import com.imcloud.saas_user.common.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
public class UserEventConsumer {

    private final MemberRepository memberRepository;
    private final String pay_to_user_topic = "pay_to_user_topic";


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
}
