package br.com.byop.aionlogbook.session.mapper;

import br.com.byop.aionlogbook.session.domain.SessionLog;
import br.com.byop.aionlogbook.session.dto.SessionLogResponse;
import org.springframework.stereotype.Component;

@Component
public class SessionLogMapper {

    public SessionLogResponse toResponse(SessionLog sessionLog) {
        return new SessionLogResponse(
                sessionLog.getId(),
                sessionLog.getUserId(),
                sessionLog.getPlanId(),
                sessionLog.getDirectionId(),
                sessionLog.getStartedAt(),
                sessionLog.getFinishedAt(),
                sessionLog.getDurationMinutes(),
                sessionLog.getResult(),
                sessionLog.getNotes(),
                sessionLog.getCreatedAt()
        );
    }
}