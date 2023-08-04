package com.imcloud.saas_user.common.entity;


import com.imcloud.saas_user.common.entity.enums.Product;
import com.imcloud.saas_user.common.entity.enums.UserRole;
import com.imcloud.saas_user.member.dto.SignupRequestDto;
import lombok.*;

import javax.persistence.*;
import java.util.Optional;
import java.util.Set;

@Entity(name = "members")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String email;

    @Column(nullable = true)
    private String institution;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Product product;


    @OneToMany(fetch = FetchType.EAGER, mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<Board> boardSet;



    public static Member create(SignupRequestDto signupRequestDto, String encodedPassword) {
        return Member.builder()
                .userId(signupRequestDto.getUserId())
                .username(signupRequestDto.getUsername())
                .email(signupRequestDto.getEmail())
                .password(encodedPassword)
                .phone(signupRequestDto.getPhone())
                .institution(signupRequestDto.getInstitution())
                .role(UserRole.User)
                .product(Product.ENTERPRISE)
                .build();
    }

    public void setPassword(String newPassword) {
        this.password = newPassword;
    }
    public void setRole(UserRole role) {
        this.role = role;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
