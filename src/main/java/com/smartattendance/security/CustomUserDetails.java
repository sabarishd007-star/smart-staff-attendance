package com.smartattendance.security;

import com.smartattendance.model.Staff;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final Staff staff;

    public CustomUserDetails(Staff staff) {
        this.staff = staff;
    }

    public Staff getStaff() {
        return staff;
    }

    public Long getStaffId() {
        return staff.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // e.g. ROLE_STAFF or ROLE_ADMIN
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + staff.getRole().name()));
    }

    @Override
    public String getPassword() {
        return staff.getPassword();
    }

    @Override
    public String getUsername() {
        return staff.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
