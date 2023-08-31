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
    @Operation(summary = "게시글 작성 (Create Boards)", description = "문의사항 등의 게시글 작성 (Writing posts such as inquiries)")
    public ApiResponse<BoardResponseDto> createBoard(
            @RequestBody @Valid BoardRequestDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ApiResponse.successOf(HttpStatus.CREATED, boardService.createBoard(dto, userDetails));
    }

    @GetMapping("/specificBoard")
    @Operation(summary = "게시글 조회 (get a board by boardId) ", description ="특정 boardId를 갖는 단일 게시글 조회")
    public ApiResponse<BoardResponseDto> getSpecificBoard(
            @RequestParam Long boardId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ApiResponse.successOf(HttpStatus.OK, boardService.getSpecificBoard(boardId, userDetails));
    }

    @GetMapping("/adminBoards")
    @Operation(summary = "ADMIN 권한을 가진 사용자의 게시글 조회 (made by ADMIN)", description = "Page starts with 1, sort in descending order")
    public ApiResponse<Page<BoardResponseDto>> getAdminBoards(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ApiResponse.successOf(HttpStatus.OK, boardService.getAdminBoards(page, size, userDetails));
    }

    @GetMapping("/userBoards")
    @Operation(summary = "USER 권한을 가진 사용자의 게시글 조회 (made by USER)", description = "Page starts with 1, sort in descending order")
    public ApiResponse<Page<BoardResponseDto>> getUserBoards(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ApiResponse.successOf(HttpStatus.OK, boardService.getUserBoards(page, size, userDetails));
    }

    @GetMapping("/userBoards")
    @Operation(summary = "유저의 게시글 조회 (get all user's boards)", description = "Page starts with 1, sort in descending order")
    public ApiResponse<Page<BoardResponseDto>> searchBoards(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ApiResponse.successOf(HttpStatus.OK, boardService.getBoard(page, size, userDetails));
    }

    @GetMapping("/allusersBoards")
    @Operation(summary = "모든 게시글 조회 (get all boards)", description = "Page starts with 1, sort in descending order")
    public ApiResponse<Page<BoardResponseDto>> getBoard(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ApiResponse.successOf(HttpStatus.OK, boardService.getBoards(page, size, userDetails));
    }

    @PutMapping("/specific")
    @Operation(summary = "게시글 수정 (Modify a board on specific boardId)", description = "특정 boardId의 게시글 수정")
    public ApiResponse<BoardResponseDto> updateBoard(
            @RequestParam Long boardId,
            @RequestBody @Valid BoardRequestDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ApiResponse.successOf(HttpStatus.CREATED, boardService.updateBoard(boardId, dto, userDetails));
    }

    @DeleteMapping("/specific")
    @Operation(summary = "게시글 삭제 (Delete a board on specific boardId)", description = "특정 boardId의 게시글 삭제")
    public ApiResponse<String> deleteBoard(
            @RequestParam Long boardId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        boardService.deleteBoard(boardId, userDetails);
        return ApiResponse.successOf(HttpStatus.NO_CONTENT, "게시글 삭제 완료");
    }


}