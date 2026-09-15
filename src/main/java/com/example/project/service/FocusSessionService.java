package com.example.project.service;

import com.example.project.Entity.FocusSession;
import com.example.project.Entity.Milestone;
import com.example.project.dto.request.FocusSessionRequest;
import com.example.project.dto.response.FocusSessionResponse;
import com.example.project.mapper.FocusSessionMapper;
import com.example.project.repository.FocusSessionRepository;
import com.example.project.repository.MilestoneRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FocusSessionService {

    private final FocusSessionRepository focusSessionRepository;
    private final MilestoneRepository milestoneRepository;
    private final FocusSessionMapper mapper;

    public FocusSessionService(FocusSessionRepository focusSessionRepository,
                               MilestoneRepository milestoneRepository,
                               FocusSessionMapper mapper) {
        this.focusSessionRepository = focusSessionRepository;
        this.milestoneRepository = milestoneRepository;
        this.mapper = mapper;
    }

    public FocusSessionResponse create(Long milestoneId, FocusSessionRequest request) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found: " + milestoneId));

        FocusSession session = mapper.toEntity(request);
        session.setMilestone(milestone);
        session.setFocusedDate(LocalDate.now());

        FocusSession saved = focusSessionRepository.save(session);
        return mapper.toResponse(saved);
    }

    public List<FocusSessionResponse> getSessionsByMilestoneId(Long milestoneId) {
        return focusSessionRepository.findAll().stream()
                .filter(fs -> fs.getMilestone() != null && fs.getMilestone().getId().equals(milestoneId))
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}