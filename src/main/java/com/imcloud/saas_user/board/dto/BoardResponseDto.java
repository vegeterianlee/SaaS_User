package com.imcloud.saas_user.board.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.imcloud.saas_user.common.entity.Board;
import com.imcloud.saas_user.common.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashSet;

@Getter
@Setter
@Builder
public class BoardResponseDto {
    @Schema(type = "integer", example = "3")
    private Long id;

    @Schema(example = "문의사항 제목")
    private String title;

    @Schema(example = "문의사항 내용")
    private String content;

  /*  @Schema(example = "게시글 좋아요 수")
    private Integer wishCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isLike;

    private Long viewCount;*/

    @Schema(example = "작성자 아이디")
    private String userId;

    @Schema(example = "작성자 닉네임")
    private String username;

    @Schema(example = "문의 사항 처리 상태")
    private Boolean hasAdminComment;

    @Schema(example = "문의 사항 조회 수")
    private Long viewCount;

    @Schema(example = "생성 날짜")
    private LocalDateTime createdAt;

    @Schema(example = "수정 날짜")
    private LocalDateTime modifiedAt;

   /* public static BoardResponseDto of(Board board, Member member ){
        BoardResponseDtoBuilder builder = BoardResponseDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .createdAt(board.getCreated_at())
                .modifiedAt(board.getModified_at());
//                .viewCount(board.getViewCount())
//                .wishCount(ObjectUtils.defaultIfNull(board.getWishes(), new HashSet<>()).size());

        return builder.build();
    }*/


    public static BoardResponseDto of (Board board){
        BoardResponseDtoBuilder builder = BoardResponseDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .hasAdminComment(board.getHasAdminComment())
                .userId(board.getMember().getUserId())
                .username(board.getMember().getUsername())
                .content(board.getContent())
                .viewCount(board.getViewCount())
                .createdAt(board.getCreatedAt())
                .modifiedAt(board.getModifiedAt());
//               .wishCount(ObjectUtils.defaultIfNull(board.getWishes(), new HashSet<>()).size());
        return builder.build();
    }
}
