package com.imcloud.saas_user.common.entity;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
@Entity(name = "sessions")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSession extends Timestamped{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private String browserInfo;

    @Column(nullable = false)
    private LocalDateTime loginTime;

    @Column(nullable = false)
    private Boolean userStatus; // Active or Inactive

    @Column(nullable = false)
    private LocalDateTime expirationTime;

}
