package com.imcloud.saas_user.common.repository;

import com.imcloud.saas_user.common.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
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

}
