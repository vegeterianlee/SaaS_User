package com.imcloud.saas_user.common.entity;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.imcloud.saas_user.common.entity.enums.FileActionType;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;



@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "file_action_history")
public class FileActionHistory extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String userId;

    @Column
    private String fileName;

    @Column(nullable = false)
    private String objectKey;

    @Column
    @Enumerated(EnumType.STRING)
    private FileActionType actionType; // 파일 액션 타입

    @Column
    private LocalDateTime actionTime;

    @Column
    private Long fileActionId;

    // 삭제 플래그 (true: 삭제됨, false: 활성 상태)
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean deletedFlag;

    // 삭제된 날짜
    @Column
    private LocalDateTime deletedAt;


    // 정적 팩토리 메소드 추가
    public static FileActionHistory create(FileAction fileAction, FileActionType actionType,
                                           String userId) {
        return FileActionHistory.builder()
                .userId(userId)
                .fileActionId(fileAction.getId())
                .fileName(fileAction.getFileName())
                .objectKey(fileAction.getObjectKey())
                .actionType(actionType)
                .deletedFlag(false)
                .actionTime(LocalDateTime.now())
                .build();
    }
}
