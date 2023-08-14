package com.imcloud.saas_user.activityLog.service;

import com.imcloud.saas_user.activityLog.dto.ActivityLogResponseDto;
import com.imcloud.saas_user.common.dto.ErrorMessage;
import com.imcloud.saas_user.common.entity.ActivityLog;
import com.imcloud.saas_user.common.entity.Member;
import com.imcloud.saas_user.common.repository.ActivityLogRepository;
import com.imcloud.saas_user.common.repository.MemberRepository;
import com.imcloud.saas_user.common.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityLogService {
    private final MemberRepository memberRepository;
    private final ActivityLogRepository activityLogRepository;

    @Transactional(readOnly = true)
    public Page<ActivityLogResponseDto> getActivityLogs(Integer page, Integer size, UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        Pageable pageable = PageRequest.of(page-1, size);
        Page<ActivityLog> logs = activityLogRepository.findActivityLogsByUserId(member.getUserId(), pageable);

        // LogDetail 엔터티 로드
        logs.getContent().forEach(log -> log.getLogDetailSet().size());
        return logs.map(this::toDto);
    }

    // 추가된 private 메소드, of 정적 메서드로 호출해도 상관없음
    private ActivityLogResponseDto toDto(ActivityLog activityLog) {
        return ActivityLogResponseDto.of(activityLog);
    }

}