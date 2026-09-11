package com.example.project.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.example.project.Entity.TripGoal;
import com.example.project.dto.request.TripDepositRequest;
import com.example.project.dto.request.TripGoalRequest;
import com.example.project.dto.response.TripGoalResponse;
import com.example.project.mapper.TripGoalMapper;
import com.example.project.repository.TripGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripGoalService {

    private final TripGoalRepository repository;
    private final Cloudinary cloudinary;
    private final TripGoalMapper mapper;
    private final NotificationService notificationService;

    public List<TripGoalResponse> getAllGoals() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public TripGoalResponse getGoalById(Long id) {
        TripGoal goal = findGoal(id);
        return mapper.toResponse(goal);
    }

    @Transactional
    public TripGoalResponse createGoal(TripGoalRequest request) throws IOException {
        TripGoal goal = mapper.toEntity(request);
        handleImageUpload(goal, request);
        TripGoal saved = repository.save(goal);
        notificationService.sendDeadlineAlertForGoal(saved);
        return mapper.toResponse(saved);
    }

    @Transactional
    public TripGoalResponse updateGoal(Long id, TripGoalRequest request) throws IOException {
        TripGoal goal = findGoal(id);
        mapper.updateEntityFromRequest(request, goal);

        if ((request.getFile() != null && !request.getFile().isEmpty()) ||
                (request.getImageUrl() != null && !request.getImageUrl().trim().isEmpty())) {
            deleteCloudinaryImage(goal.getImagePublicId());
            handleImageUpload(goal, request);
        }

        TripGoal saved = repository.save(goal);
        notificationService.sendDeadlineAlertForGoal(saved);
        return mapper.toResponse(saved);
    }

    @Transactional
    public TripGoalResponse deposit(Long id, TripDepositRequest request) {
        TripGoal goal = findGoal(id);
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }
        goal.setSaved(goal.getSaved().add(request.getAmount()));
        return mapper.toResponse(repository.save(goal));
    }

    @Transactional
    public void deleteGoal(Long id) {
        TripGoal goal = findGoal(id);
        deleteCloudinaryImage(goal.getImagePublicId());
        repository.delete(goal);
    }

    private TripGoal findGoal(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found with ID: " + id));
    }

    private void handleImageUpload(TripGoal goal, TripGoalRequest request) throws IOException {
        if (request.getFile() != null && !request.getFile().isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(
                    request.getFile().getBytes(),
                    ObjectUtils.asMap(
                            "folder", "trip_goals",
                            "transformation", new Transformation<>().quality("auto").fetchFormat("auto")));
            goal.setImage((String) uploadResult.get("secure_url"));
            goal.setImagePublicId((String) uploadResult.get("public_id"));
        } else if (request.getImageUrl() != null && !request.getImageUrl().trim().isEmpty()) {
            goal.setImage(request.getImageUrl().trim());
            goal.setImagePublicId(null);
        }
    }

    private void deleteCloudinaryImage(String publicId) {
        if (publicId != null && !publicId.trim().isEmpty()) {
            try {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            } catch (IOException e) {
                System.err.println("Failed to delete image from Cloudinary: " + e.getMessage());
            }
        }
    }
}