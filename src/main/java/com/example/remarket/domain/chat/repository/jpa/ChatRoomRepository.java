package com.example.remarket.domain.chat.repository.jpa;

import com.example.remarket.domain.chat.entity.ChatRoom;
import com.example.remarket.domain.item.entity.Item;
import com.example.remarket.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    List<ChatRoom> findAllByItem(Item item);

    Optional<ChatRoom> findByItemAndBuyer(Item item, Member buyer);

    List<ChatRoom> findAllByItemOrderByCreatedAtAsc(Item item);

//    @Query("""
//            SELECT cr
//            FROM ChatRoom cr
//            WHERE cr.buyer.id = :memberId OR cr.seller.id = :memberId
//    """)
//    List<ChatRoom> findMyChatRooms(@Param("memberId") Long memberId);
    @Query("""
            SELECT cr
            FROM ChatRoom cr
            JOIN FETCH cr.item i     
            JOIN FETCH cr.seller s   
            JOIN FETCH cr.buyer b    
            WHERE b.id = :memberId OR s.id = :memberId
    """)
    List<ChatRoom> findMyChatRooms(@Param("memberId") Long memberId);

    /**
     * 특정 상품에 대해 채팅을 건 사용자 목록 조회
     */
    @Query("""
        SELECT cr.buyer
        FROM ChatRoom cr
        JOIN cr.buyer
        WHERE cr.item.id = :itemId
    """)
    List<Member> findBuyersByItemId(@Param("itemId") Long itemId);

}
