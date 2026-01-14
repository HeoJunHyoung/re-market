package com.example.secondhandmarket.domain.item.entity;

import com.example.secondhandmarket.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "favorites",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"member_id", "item_id"})
        }
)
@Getter
public class Favorite {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    protected Favorite() { }
}
