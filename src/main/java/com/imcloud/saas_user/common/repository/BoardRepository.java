package com.imcloud.saas_user.common.repository;

import com.imcloud.saas_user.common.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long>, JpaSpecificationExecutor<Board> {
    Optional<Board> findById(Long boardId);
    Optional<Board> findByTitle(String title);

    @Query("select b from boards b where b.member.id = :memberId ORDER BY b.id DESC")
    @EntityGraph(attributePaths = {
            "member"
    })
    Page<Board> findBoardsByMemberIdJPQL(Long memberId, Pageable pageable);

    @Query("select b from boards b where b.member.userId = :userId ORDER BY b.id DESC")
    @EntityGraph(attributePaths = {
            "member"
    })
    Page<Board> findBoardsByUserIdJPQL(String userId, Pageable pageable);


    @Query("select b from boards b ORDER BY b.id DESC")
    @EntityGraph(attributePaths = {
            "member"
    })
    Page<Board> findAll(Pageable pageable);

    @Query("select b from boards b where b.member.role = 'ADMIN' ORDER BY b.id DESC")
    @EntityGraph(attributePaths = {
            "member"
    })
    Page<Board> findBoardsByAdminRole(Pageable pageable);

    @Query("select b from boards b where b.member.role = 'USER' ORDER BY b.id DESC")
    @EntityGraph(attributePaths = {
            "member"
    })
    Page<Board> findBoardsByUserRole(Pageable pageable);

    @EntityGraph(attributePaths = {
            "member"
    })
    @Query("SELECT b FROM boards b WHERE " +
            "(:searchType = 'userId' AND b.member.userId LIKE %:keyword%) OR " +
            "(:searchType = 'hasAdminComment' AND b.hasAdminComment = CASE WHEN :keyword = 'true' THEN true ELSE false END) OR " +
            "(:searchType = 'title' AND b.title LIKE %:keyword%) OR " +
            "(:searchType = 'content' AND b.content LIKE %:keyword%) OR " +
            "(:searchType = 'userRole' AND b.member.role = :keyword)")
    Page<Board> searchBoards(String searchType, String keyword, Pageable pageable);
}
