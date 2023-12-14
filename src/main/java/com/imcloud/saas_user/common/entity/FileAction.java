package com.imcloud.saas_user.common.entity;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.imcloud.saas_user.common.entity.enums.FileActionType;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "file_actions")
public class FileAction extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String objectKey;

    @Column
    private String fileName;

    @Column(nullable = false)
    private String userId;

    /*@Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FileActionType actionType; // 액션 타입 (예: "DOWNLOADED", "DELETED", "PENDING_DEIDENTIFICATION")*/

    @Column
    private Boolean toBeDeidentified;

    @Column(nullable = false)
    private LocalDateTime storedAt;

    @Column
    private LocalDateTime isDeidentifiedAt;

    public static FileAction create(String fileName, String objectKey,
                                    String userId) {
        return FileAction.builder()
                .fileName(fileName)
                .userId(userId)
                .objectKey(objectKey)
                .toBeDeidentified(false)
                .storedAt(LocalDateTime.now())
               // .actionType(actionType)
                .build();
    }
}
