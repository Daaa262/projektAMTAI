package com.projektamtai.projekt.models;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private Long admin;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
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
    public UserModel(Long Id, String username, String password, Long admin) {
        this.id = Id;
        this.username = username;
        this.password = password;
        this.admin = admin;
    }
    @Override
    public String toString() {
        return "UserModel{" +
                "id=" + id + '\'' +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", admin='" + admin + '\'' +
                '}';
    }
}
