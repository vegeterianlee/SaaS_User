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

    @PostMapping
    @Operation(summary = "comment 작성", description = "건의사항 등의 게시글의 comment 작성")
    public ApiResponse<CommentResponseDto> createBoard(
            @RequestBody @Valid CommentRequestDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam Long boardId) {
        return ApiResponse.successOf(HttpStatus.CREATED, commentService.createComment(boardId, dto, userDetails));
    }

    @GetMapping("/specific")
    @Operation(summary = "단일 comment 조회", description ="특정 commentId를 갖는 단일 comment 조회")
    public ApiResponse<CommentResponseDto> getSpecificComment(
            @RequestParam Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ApiResponse.successOf(HttpStatus.OK, commentService.getSpecificComment(commentId, userDetails));
    }

    @GetMapping("/fromUser")
    @Operation(summary = "유저가 작성한 모든 comment들 조회", description = "유저가 작성한 모든 comment들 조회, page는 1부터 시작")
    public ApiResponse<Page<CommentResponseDto>> getCommentsFromUser(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ApiResponse.successOf(HttpStatus.OK, commentService.getCommentsInUser(page, size, userDetails));
    }

    @GetMapping("/fromBoard")
    @Operation(summary = "하나의 board에 해당하는 모든 comment들 조회", description = "하나의 board에 해당하는 모든 comment들 조회, page는 1부터 시작")
    public ApiResponse<Page<CommentResponseDto>> getCommentFromBoard(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Long boardId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ApiResponse.successOf(HttpStatus.OK, commentService.getCommentInBoard(boardId,page, size, userDetails));
    }

    @PutMapping("/specific")
    @Operation(summary = "댓글 수정", description = "특정 댓글 수정")
    public ApiResponse<CommentResponseDto> updateComment(
            @RequestParam Long commentId,
            @RequestBody @Valid CommentRequestDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ApiResponse.successOf(HttpStatus.CREATED, commentService.updateComment(commentId, dto, userDetails));
    }

    @DeleteMapping("/specific")
    @Operation(summary = "댓글 삭제", description = "특정 댓글 삭제")
    public ApiResponse<CommentResponseDto> deleteComment(
            @RequestParam Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        commentService.deleteComment(commentId, userDetails);
        return ApiResponse.successOf(HttpStatus.NO_CONTENT, null);
    }

    @DeleteMapping("/admin")
    @Operation(summary = "댓글 삭제", description = "특정 CommentId의 admin 권한으로 comment 삭제")
    public ApiResponse<BoardResponseDto> deleteCommentByAdmin(
            @RequestParam Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        commentService.deleteCommentByAdmin(commentId, userDetails);
        return ApiResponse.successOf(HttpStatus.NO_CONTENT, null);
    }
}
