package com.example.secondhandmarket.domain.trade.entity;

import com.example.secondhandmarket.domain.item.entity.Item;
import com.example.secondhandmarket.domain.member.entity.Member;
import com.example.secondhandmarket.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "trades")
@Getter
public class Trade extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Member seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    private Member buyer;

    // 생성자
    protected Trade() { }

    private Trade(Item item, Member seller, Member buyer) {
        this.item = item;
        this.seller = seller;
        this.buyer = buyer;
    }

    public static Trade completeTrade(Item item, Member seller, Member buyer) {
        return new Trade(item, seller, buyer);
    }

}
