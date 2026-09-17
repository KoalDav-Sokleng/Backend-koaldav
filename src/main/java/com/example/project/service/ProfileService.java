package com.example.project.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.project.Entity.User;
import com.example.project.dto.ProfileRequest;
import com.example.project.dto.ProfileResponse;
import com.example.project.dto.exception.ResourceNotFoundException;
import com.example.project.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final Cloudinary cloudinary;

    public ProfileService(UserRepository userRepository, Cloudinary cloudinary) {
        this.userRepository = userRepository;
        this.cloudinary = cloudinary;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String email) {
        return toResponse(findUser(email));
    }

    @Transactional
    public ProfileResponse updateProfile(String email, ProfileRequest request) {
        User user = findUser(email);
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());

        if (request.getAvatar() != null && !request.getAvatar().isBlank()) {
            user.setAvatar(request.getAvatar());
            user.setProfileImageUrl(request.getAvatar());
        }

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public ProfileResponse uploadImage(String email, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Profile image is required");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Profile image must be an image file");
        }

        User user = findUser(email);
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "koaldav/profiles",
                    "public_id", "user_" + user.getId(),
                    "overwrite", true,
                    "resource_type", "image"));

            String imageUrl = (String) result.get("secure_url");
            user.setAvatar(imageUrl);
            user.setProfileImageUrl(imageUrl);
            user.setProfileImagePublicId((String) result.get("public_id"));
            return toResponse(userRepository.save(user));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to upload profile image", exception);
        }
    }

    @Transactional
    public ProfileResponse deleteImage(String email) {
        User user = findUser(email);
        if (user.getProfileImagePublicId() != null && !user.getProfileImagePublicId().isBlank()) {
            try {
                cloudinary.uploader().destroy(user.getProfileImagePublicId(), ObjectUtils.emptyMap());
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to delete profile image", exception);
            }
        }

        user.setAvatar(null);
        user.setProfileImageUrl(null);
        user.setProfileImagePublicId(null);
        return toResponse(userRepository.save(user));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ProfileResponse toResponse(User user) {
        String avatar = user.getAvatar() != null ? user.getAvatar() : user.getProfileImageUrl();
        return new ProfileResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                avatar,
                user.getProfileImageUrl(),
                user.getCreatedAt());
    }
}