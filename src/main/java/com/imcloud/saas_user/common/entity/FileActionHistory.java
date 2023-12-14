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
public class FileActionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String fileName;

    @Column(nullable = false)
    private String objectKey;

    @Column
    @Enumerated(EnumType.STRING)
    private FileActionType actionType; // 파일 액션 타입

    @Column
    private LocalDateTime actionTime;

    @ManyToOne
    private FileAction fileAction;


    // 정적 팩토리 메소드 추가
    public static FileActionHistory create(FileAction fileAction, FileActionType actionType) {
        return FileActionHistory.builder()
                .fileAction(fileAction)
                .fileName(fileAction.getFileName())
                .objectKey(fileAction.getObjectKey())
                .actionType(actionType)
                .actionTime(LocalDateTime.now())
                .build();
    }
}
