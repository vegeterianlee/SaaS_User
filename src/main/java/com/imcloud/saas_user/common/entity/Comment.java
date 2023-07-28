package com.imcloud.saas_user.common.entity;


import com.imcloud.saas_user.comment.dto.CommentRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private Board board;

    public void update(CommentRequestDto dto) {
        this.content = dto.getContent();
    }

    public static Comment create(CommentRequestDto dto, Board board, Member member) {
        return Comment.builder()
                .content(dto.getContent())
                //.viewCount(0L)
                .board(board)
                .member(member)
                .build();
    }
}
