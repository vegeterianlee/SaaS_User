package com.imcloud.saas_user.comment.dto;

import com.imcloud.saas_user.board.dto.BoardResponseDto;
import com.imcloud.saas_user.common.entity.Board;
import com.imcloud.saas_user.common.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CommentResponseDto {
    @Schema(type = "integer", example = "3")
    private Long id;

    @Schema(example = "문의사항 내용")
    private String content;

    @Schema(example = "작성자 닉네임")
    private String username;

    @Schema(example = "comment를 포함하는 boardId")
    private Long boardId;

    @Schema(example = "생성 날짜")
    private LocalDateTime createdAt;

    @Schema(example = "수정 날짜")
    private LocalDateTime modifiedAt;

    public static CommentResponseDto of (Comment comment){
        CommentResponseDto.CommentResponseDtoBuilder builder = CommentResponseDto.builder()
                .id(comment.getId())
                .boardId(comment.getBoard().getId())
                .username(comment.getMember().getUsername())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .modifiedAt(comment.getModifiedAt());
        return builder.build();
    }
}
