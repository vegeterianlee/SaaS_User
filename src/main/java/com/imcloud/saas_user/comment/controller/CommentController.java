package com.imcloud.saas_user.comment.controller;

import com.imcloud.saas_user.board.dto.BoardResponseDto;
import com.imcloud.saas_user.comment.dto.CommentRequestDto;
import com.imcloud.saas_user.comment.dto.CommentResponseDto;
import com.imcloud.saas_user.comment.service.CommentService;
import com.imcloud.saas_user.common.dto.ApiResponse;
import com.imcloud.saas_user.common.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "Comment")
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/fromBoard")
    @Operation(summary = "하나의 board에 포함되는 모든 comment들 조회 (all comments belonging to OneToMany in the entered boardId)", description = "Page starts with 1, sort in descending order")
    public ApiResponse<Page<CommentResponseDto>> getCommentFromBoard(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Long boardId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ApiResponse.successOf(HttpStatus.OK, commentService.getCommentInBoard(boardId,page, size, userDetails));
    }

}
