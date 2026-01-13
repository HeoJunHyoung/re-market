package com.example.secondhandmarket.global.security.principal;

import com.example.secondhandmarket.domain.member.entity.enumerate.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class AuthMember implements UserDetails {

    private Long memberId; // Member 객체의 AUTO INCREMENT값
    private String username;
    private String password;
    private Role role;

    // 로그인용 생성자
    public AuthMember(Long memberId, String username, String password, Role role) {
        this.memberId = memberId;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // jwt용 생성자
    public AuthMember(Long memberId, Role role) {
        this.memberId = memberId;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
