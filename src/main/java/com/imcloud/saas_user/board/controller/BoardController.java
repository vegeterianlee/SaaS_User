package com.imcloud.saas_user.board.controller;

import com.imcloud.saas_user.board.dto.BoardRequestDto;
import com.imcloud.saas_user.board.dto.BoardResponseDto;
import com.imcloud.saas_user.board.service.BoardService;
import com.imcloud.saas_user.common.dto.ApiResponse;
import com.imcloud.saas_user.common.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.concurrent.CompletableFuture;

@Tag(name = "Board")
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @PostMapping
    @Operation(summary = "게시글 작성", description = "건의사항 등의 게시글 작성")
    public ApiResponse<BoardResponseDto> createBoard(
            @RequestBody @Valid BoardRequestDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ApiResponse.successOf(HttpStatus.CREATED, boardService.createBoard(dto, userDetails));
    }

    @GetMapping("/specific")
    @Operation(summary = "게시글 조회", description ="특정 boardId를 갖는 단일 게시글 조회")
    public ApiResponse<BoardResponseDto> getSpecificBoard(
            @RequestParam Long boardId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ApiResponse.successOf(HttpStatus.OK, boardService.getSpecificBoard(boardId, userDetails));
    }

    @GetMapping("/search")
    @Operation(summary = "유저의 게시글 조회", description = "유저의 게시글 목록 조회, page는 1부터 시작")
    public ApiResponse<Page<BoardResponseDto>> searchBoards(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ApiResponse.successOf(HttpStatus.OK, boardService.getBoard(page, size, userDetails));
    }

    @GetMapping("/allsearch")
    @Operation(summary = "모든 게시글 조회", description = "모든 유저의 게시글 조회, page는 1부터 시작")
    public ApiResponse<Page<BoardResponseDto>> getBoard(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ApiResponse.successOf(HttpStatus.OK, boardService.getBoards(page, size, userDetails));
    }

    @PutMapping("/specific")
    @Operation(summary = "게시글 수정", description = "특정 boardId의 게시글 수정")
    public ApiResponse<BoardResponseDto> updateBoard(
            @RequestParam Long boardId,
            @RequestBody @Valid BoardRequestDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ApiResponse.successOf(HttpStatus.CREATED, boardService.updateBoard(boardId, dto, userDetails));
    }

    @DeleteMapping("/specific")
    @Operation(summary = "게시글 삭제", description = "특정 boardId의 게시글 삭제")
    public ApiResponse<BoardResponseDto> deleteBoard(
            @RequestParam Long boardId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        boardService.deleteBoard(boardId, userDetails);
        return ApiResponse.successOf(HttpStatus.NO_CONTENT, null);
    }

    @DeleteMapping("/admin")
    @Operation(summary = "게시글 삭제", description = "특정 boardId의 admin 권한으로 게시글 삭제")
    public ApiResponse<BoardResponseDto> deleteBoardByAdmin(
            @RequestParam Long boardId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        boardService.deleteBoardByAdmin(boardId, userDetails);
        return ApiResponse.successOf(HttpStatus.NO_CONTENT, null);
    }
}