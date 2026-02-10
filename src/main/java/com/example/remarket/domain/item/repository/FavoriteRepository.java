package com.example.remarket.domain.item.repository;

import com.example.remarket.domain.item.entity.Favorite;
import com.example.remarket.domain.item.entity.Item;
import com.example.remarket.domain.member.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    @Query("""
        SELECT f 
        FROM Favorite f 
        WHERE f.member = :member AND f.item = :item
    """)
    Optional<Favorite> findByMemberAndItem(Member member, Item item);

    @Query("""
        SELECT f.item
        FROM Favorite f
        WHERE f.member.id = :memberId
        ORDER BY f.id DESC
    """)
    Slice<Item> findMyFavoriteItems(@Param("memberId") Long memberId, Pageable pageable);
}
