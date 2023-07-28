package com.imcloud.saas_user.common.entity;

import com.imcloud.saas_user.board.dto.BoardRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Entity(name = "boards")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Board extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "board", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<Comment> commentSet;

    /*@Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long viewCount;*/



    public void update(BoardRequestDto dto) {
        this.title = dto.getTitle();
        this.content = dto.getContent();
    }

//    public void updateViewCount(Long viewCountDifference) {
//        this.viewCount = viewCountDifference;
//    }

    public static Board create(BoardRequestDto dto, Member member) {
        return Board.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                //.viewCount(0L)
                .member(member)
                .build();
    }


}
