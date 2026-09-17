package com.example.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String avatar;
    private String profileImageUrl;
    private java.time.LocalDateTime createdAt;
}