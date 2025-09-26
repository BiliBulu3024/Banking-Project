package com.banking.backend.entity;

import jakarta.persistence.*;


    @Entity                // đánh dấu đây là Entity
    @Table(name = "users") // tên bảng trong MySQL
    public class User {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY) // khóa chính auto tăng
        private Long id;

        @Column(nullable = false)
        private String role;  // "ADMIN" hoặc "USER"

        @Column(nullable = false, unique = true)
        private String username;

        @Column(nullable = false, unique = true)
        private String email;

        @Column(nullable = false)
        private String password;

        // ===== GETTER & SETTER =====
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

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
