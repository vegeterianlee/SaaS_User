package com.imcloud.saas_user.comment.service;

import com.imcloud.saas_user.comment.dto.CommentRequestDto;
import com.imcloud.saas_user.comment.dto.CommentResponseDto;
import com.imcloud.saas_user.common.dto.ErrorMessage;
import com.imcloud.saas_user.common.entity.Board;
import com.imcloud.saas_user.common.entity.Comment;
import com.imcloud.saas_user.common.entity.Member;
import com.imcloud.saas_user.common.entity.enums.UserRole;
import com.imcloud.saas_user.common.repository.BoardRepository;
import com.imcloud.saas_user.common.repository.CommentRepository;
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
public class CommentService {
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public CommentResponseDto createComment(Long boardId, CommentRequestDto dto, UserDetailsImpl userDetails) {

        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        if (!member.getId().equals(userDetails.getUser().getId())) {
            throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED.getMessage());
        }
        Board board = boardRepository.findById(boardId).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.BOARD_NOT_FOUND.getMessage())
        );

        Comment comment = Comment.create(dto, board, member);
        comment = commentRepository.save(comment);
        return CommentResponseDto.of(comment);
    }

    @Transactional
    public CommentResponseDto getSpecificComment(Long commentId, UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.BOARD_NOT_FOUND.getMessage())
        );
        return CommentResponseDto.of(comment);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getCommentInBoard(Long BoardId, Integer page, Integer size, UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );
        Pageable pageable = PageRequest.of(page-1, size);
        return commentRepository.findCommentsByBoardIdJPQL(BoardId, pageable).map(CommentResponseDto::of);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getCommentsInUser(Integer page, Integer size, UserDetailsImpl userDetails) {
        // 사용자 확인
        Member member = memberRepository.findByUserId(userDetails.getUser().getUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERID.getMessage())
        );

        Pageable pageable = PageRequest.of(page-1, size);
        return commentRepository.findCommentsByUserIdJPQL(member.getUserId(),pageable).map(CommentResponseDto::of);
    }

    @Transactional
    public CommentResponseDto updateComment(
            Long CommentId,
            CommentRequestDto dto,
            UserDetailsImpl userDetails
    ) {
        Comment comment = commentRepository.findById(CommentId).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.COMMENT_NOT_FOUND.getMessage())
        );

        if (!userDetails.getUser().getId().equals(comment.getMember().getId())) {
            throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED.getMessage());
        }

        comment.update(dto);
        return CommentResponseDto.of(comment);
    }

    @Transactional
    public void deleteComment(Long CommentId, UserDetailsImpl userDetails) {
        Comment comment = commentRepository.findById(CommentId).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.COMMENT_NOT_FOUND.getMessage())
        );

        if (!userDetails.getUser().getId().equals(comment.getMember().getId())) {
            throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED.getMessage());
        }
        commentRepository.deleteById(comment.getId());
    }

    @Transactional
    public void deleteCommentByAdmin(Long CommentId, UserDetailsImpl userDetails) {
        Comment comment = commentRepository.findById(CommentId).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.COMMENT_NOT_FOUND.getMessage())
        );

        if (!userDetails.getUser().getRole().equals(UserRole.ADMIN)){
            throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED.getMessage());
        }
        commentRepository.deleteById(comment.getId());
    }
}
