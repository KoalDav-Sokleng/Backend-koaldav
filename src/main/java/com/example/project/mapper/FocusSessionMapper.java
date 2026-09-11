package com.example.project.mapper;





import com.example.project.Entity.FocusSession;
import com.example.project.dto.request.FocusSessionRequest;
import com.example.project.dto.response.FocusSessionResponse;
import org.springframework.stereotype.Component;

@Component
public class FocusSessionMapper {

    public FocusSession toEntity(FocusSessionRequest request) {
        FocusSession session = new FocusSession();
        session.setDurationMinutes(request.getDurationMinutes());
        // focusedDate is set server-side in the service, not here
        return session;
    }

    public FocusSessionResponse toResponse(FocusSession session) {
        return new FocusSessionResponse(
                session.getId(),
                session.getDurationMinutes(),
                session.getFocusedDate() // entity field -> DTO field renamed to "date"
        );
    }
}