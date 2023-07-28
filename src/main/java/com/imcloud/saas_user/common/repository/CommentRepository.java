package com.imcloud.saas_user.common.repository;



import com.imcloud.saas_user.common.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
public interface CommentRepository extends JpaRepository<Comment, Long>{
    @Query("select c from comments c where c.board.id = :boardId")
    @EntityGraph(attributePaths = {
            "board", "member"
    })
    Page<Comment> findCommentsByBoardIdJPQL(Long boardId, Pageable pageable);


    @Query("select c from comments c where c.member.userId = :userId")
    @EntityGraph(attributePaths = {
            "board", "member"
    })
    Page<Comment> findCommentsByUserIdJPQL(String userId, Pageable pageable);
}
