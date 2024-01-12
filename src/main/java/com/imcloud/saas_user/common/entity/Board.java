package com.imcloud.saas_user.common.entity;

import com.imcloud.saas_user.board.dto.BoardRequestDto;
import com.imcloud.saas_user.common.entity.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity(name = "boards")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Board extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long viewCount;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean hasAdminComment;

    // 삭제 플래그 (true: 삭제됨, false: 활성 상태)
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean deletedFlag;

    // 삭제된 날짜
    @Column
    private LocalDateTime deletedAt;

    @ManyToOne
    private Member member;

    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<Comment> commentSet;

    public void update(BoardRequestDto dto) {
        this.title = dto.getTitle();
        this.content = dto.getContent();
    }

    public void increaseViewCount() {
        this.viewCount += 1L;
    }

//    public void updateViewCount(Long viewCountDifference) {
//        this.viewCount = viewCountDifference;
//    }

    public static Board create(BoardRequestDto dto, Member member) {
        Boolean isCommentedByAdmin = (member.getRole() == UserRole.ADMIN) ? true : false;

        return Board.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .viewCount(0L)
                .hasAdminComment(isCommentedByAdmin)
                .deletedFlag(false)
                .member(member)
                .build();
    }


}
