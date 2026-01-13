package com.mindbloom.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import javax.persistence.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "person")
public class Person implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String role;

    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "reset_code")
    private String resetCode;

    @Column(name = "reset_code_expiry")
    private LocalDateTime resetCodeExpiry;

    /* =========================
       USER DETAILS (SECURITY)
       ========================= */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
            new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
        );
    }

    @Override
    public String getUsername() {
        return email;
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

    /* =========================
       GETTERS & SETTERS
       ========================= */

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
 
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
 
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPassword() {
        return password;
    }
 
    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }
 
    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }
 
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
 
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getResetCode() {
        return resetCode;
    }

    public void setResetCode(String resetCode) {
        this.resetCode = resetCode;
    }

    public LocalDateTime getResetCodeExpiry() {
        return resetCodeExpiry;
    }

    public void setResetCodeExpiry(LocalDateTime resetCodeExpiry) {
        this.resetCodeExpiry = resetCodeExpiry;
    }
}