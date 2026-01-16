package com.example.secondhandmarket.domain.chat.repository.jpa;

import com.example.secondhandmarket.domain.chat.entity.ChatRoom;
import com.example.secondhandmarket.domain.item.entity.Item;
import com.example.secondhandmarket.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByItemAndBuyer(Item item, Member buyer);

    @Query("""
            SELECT cr
            FROM ChatRoom cr
            WHERE cr.buyer.id = :memberId OR cr.seller.id = :memberId
            """)
    List<ChatRoom> findMyChatRooms(@Param("memberId") Long memberId);

}
