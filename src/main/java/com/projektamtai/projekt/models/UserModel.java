package com.projektamtai.projekt.models;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserModel {
    @Id
    @Column(name = "Username", nullable = false)
    private String username;
    @Column(name = "Password", nullable = false)
    private String password;
    @Column(name = "Admin", nullable = false)
    private Long admin;

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public Long getRole() {
        return admin;
    }
    public void setRole(Long role) {
        this.admin = role;
    }
    public UserModel() {
    }
    public UserModel(String username, String password, Long admin) {
        this.username = username;
        this.password = password;
        this.admin = admin;
    }
    @Override
    public String toString() {
        return "UserModel{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", admin='" + admin + '\'' +
                '}';
    }
}
