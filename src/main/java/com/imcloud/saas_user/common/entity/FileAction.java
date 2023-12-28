package com.imcloud.saas_user.common.entity;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;


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

    @Column
    private Boolean isDeidentifiedTarget;

    @Column
    private Boolean toBeDeidentified;

    @Column(nullable = false)
    private LocalDateTime storedAt;

    @Column
    private LocalDateTime isDeidentifiedAt;

    /*@OneToMany(mappedBy = "fileAction", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<FileActionHistory> fileActionHistorySet;*/

    public static FileAction create(String fileName, String objectKey,
                                    String userId) {
        return FileAction.builder()
                .fileName(fileName)
                .userId(userId)
                .objectKey(objectKey)
                .toBeDeidentified(false)
                .isDeidentifiedTarget(false)
                .storedAt(LocalDateTime.now())
                .build();
    }
}
