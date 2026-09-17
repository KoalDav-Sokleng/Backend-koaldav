package com.example.project.controller;

import com.example.project.dto.ProfileRequest;
import com.example.project.dto.ProfileResponse;
import com.example.project.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ProfileResponse getProfile(Principal principal) {
        return profileService.getProfile(principal.getName());
    }

    @PutMapping
    public ProfileResponse updateProfile(Principal principal, @Valid @RequestBody ProfileRequest request) {
        return profileService.updateProfile(principal.getName(), request);
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ProfileResponse uploadImage(Principal principal, @RequestParam("file") MultipartFile file) {
        return profileService.uploadImage(principal.getName(), file);
    }

    @DeleteMapping("/image")
    public ProfileResponse deleteImage(Principal principal) {
        return profileService.deleteImage(principal.getName());
    }
}