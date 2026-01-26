package com.example.secondhandmarket.domain.member.repository;

import com.example.secondhandmarket.domain.member.entity.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("""
        SELECT m 
        FROM Member m 
        WHERE m.username = :username
    """)
    Optional<Member> findByUsername(@Param("username") String username);

    @Query("""
        SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END 
        FROM Member m
        WHERE m.username = :username
    """)
    boolean existsByUsername(@Param("username") String username);

    // 낙관적 락
    @Lock(LockModeType.OPTIMISTIC)
    @Query("select m from Member m where m.id = :id")
    Optional<Member> findByIdWithOptimisticLock(Long id);
}
