package com.imcloud.saas_user.common.entity;


import com.imcloud.saas_user.comment.dto.CommentRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "comments")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Comment extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    // 삭제 플래그 (true: 삭제됨, false: 활성 상태)
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean deletedFlag;

    // 삭제된 날짜
    @Column
    private LocalDateTime deletedAt;

    @ManyToOne
    private Member member;

    @ManyToOne
    private Board board;

    public void update(CommentRequestDto dto) {
        this.content = dto.getContent();
    }

    public static Comment create(CommentRequestDto dto, Board board, Member member) {
        return Comment.builder()
                .content(dto.getContent())
                //.viewCount(0L)
                .board(board)
                .deletedFlag(false)
                .member(member)
                .build();
    }
}
