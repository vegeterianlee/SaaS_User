package com.imcloud.saas_user.board.service;

import com.imcloud.saas_user.board.dto.BoardRequestDto;
import com.imcloud.saas_user.board.dto.BoardResponseDto;
import com.imcloud.saas_user.common.dto.ErrorMessage;
import com.imcloud.saas_user.common.entity.Board;
import com.imcloud.saas_user.common.entity.Member;
import com.imcloud.saas_user.common.entity.enums.UserRole;
import com.imcloud.saas_user.common.repository.BoardRepository;
import com.imcloud.saas_user.common.repository.MemberRepository;
import com.imcloud.saas_user.common.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;


@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public BoardResponseDto createBoard(BoardRequestDto dto, UserDetailsImpl userDetails) {

        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        if (!member.getId().equals(userDetails.getUser().getId())) {
            throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED.getMessage());
        }

        Board board = Board.create(dto, member);
        board = boardRepository.save(board);
        return BoardResponseDto.of(board);
    }

    @Transactional
    public BoardResponseDto getSpecificBoard(Long boardId, UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        Board board = boardRepository.findById(boardId).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.BOARD_NOT_FOUND.getMessage())
        );
        return BoardResponseDto.of(board);
    }

    @Transactional(readOnly = true)
    public Page<BoardResponseDto> getBoard(Integer page, Integer size, UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );
        Pageable pageable = PageRequest.of(page-1, size);
        return boardRepository.findBoardsByUserIdJPQL(member.getUserId(), pageable).map(BoardResponseDto::of);
    }

    @Transactional(readOnly = true)
    public Page<BoardResponseDto> getBoards(Integer page, Integer size, UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        Pageable pageable = PageRequest.of(page-1, size);
        return boardRepository.findAll(pageable).map(BoardResponseDto::of);
    }

    @Transactional
    public BoardResponseDto updateBoard(
            Long boardId,
            BoardRequestDto dto,
            UserDetailsImpl userDetails
    ) {
        Board board = boardRepository.findById(boardId).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.BOARD_NOT_FOUND.getMessage())
        );

        if (!userDetails.getUser().getId().equals(board.getMember().getId())) {
            throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED.getMessage());
        }

        board.update(dto);
        return BoardResponseDto.of(board);
    }

    @Transactional
    public void deleteBoard(Long boardId, UserDetailsImpl userDetails) {
        Board board = boardRepository.findById(boardId).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.BOARD_NOT_FOUND.getMessage())
        );

        if (!userDetails.getUser().getId().equals(board.getMember().getId())) {
            throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED.getMessage());
        }
        boardRepository.deleteById(board.getId());
    }

    @Transactional
    public void deleteBoardByAdmin(Long boardId, UserDetailsImpl userDetails) {
        Board board = boardRepository.findById(boardId).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.BOARD_NOT_FOUND.getMessage())
        );

        if (!userDetails.getUser().getRole().equals(UserRole.Admin)){
            throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED.getMessage());
        }
        boardRepository.deleteById(board.getId());
    }
}
