package br.com.byop.aionlogbook.logbook.infrastructure;

import br.com.byop.aionlogbook.logbook.application.LogEntrySearchCriteria;
import br.com.byop.aionlogbook.logbook.domain.LogEntry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LogEntryRepositoryImpl implements LogEntryRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<LogEntry> findByFilters(UUID userId, LogEntrySearchCriteria criteria, Pageable pageable) {
        var conditions = buildConditions(criteria);

        var selectSql = "select * from log_entries log where log.user_id = :userId" + conditions + " order by log.created_at desc";
        var countSql = "select count(*) from log_entries log where log.user_id = :userId" + conditions;

        var query = entityManager.createNativeQuery(selectSql, LogEntry.class);
        var countQuery = entityManager.createNativeQuery(countSql);

        bindParams(query, userId, criteria);
        bindParams(countQuery, userId, criteria);

        long total = ((Number) countQuery.getSingleResult()).longValue();

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<LogEntry> results = query.getResultList();

        return new PageImpl<>(results, pageable, total);
    }

    private String buildConditions(LogEntrySearchCriteria criteria) {
        var sb = new StringBuilder();
        if (criteria.type() != null) {
            sb.append(" and log.type = :type");
        }
        if (criteria.directionId() != null) {
            sb.append(" and log.direction_id = :directionId");
        }
        if (criteria.planId() != null) {
            sb.append(" and log.plan_id = :planId");
        }
        if (criteria.dateFrom() != null) {
            sb.append(" and log.created_at >= :dateFrom");
        }
        if (criteria.dateTo() != null) {
            sb.append(" and log.created_at <= :dateTo");
        }
        if (criteria.tags() != null && !criteria.tags().isEmpty()) {
            sb.append(" and log.tags @> cast(:tagsJson as jsonb)");
        }
        if (StringUtils.hasText(criteria.q())) {
            sb.append(" and (lower(log.title) like lower(concat('%', :q, '%')) or lower(log.content) like lower(concat('%', :q, '%')))");
        }
        return sb.toString();
    }

    private void bindParams(jakarta.persistence.Query query, UUID userId, LogEntrySearchCriteria criteria) {
        query.setParameter("userId", userId);
        if (criteria.type() != null) {
            query.setParameter("type", criteria.type().name());
        }
        if (criteria.directionId() != null) {
            query.setParameter("directionId", criteria.directionId());
        }
        if (criteria.planId() != null) {
            query.setParameter("planId", criteria.planId());
        }
        if (criteria.dateFrom() != null) {
            query.setParameter("dateFrom", criteria.dateFrom());
        }
        if (criteria.dateTo() != null) {
            query.setParameter("dateTo", criteria.dateTo());
        }
        if (criteria.tags() != null && !criteria.tags().isEmpty()) {
            query.setParameter("tagsJson", toTagsJson(criteria.tags()));
        }
        if (StringUtils.hasText(criteria.q())) {
            query.setParameter("q", criteria.q().trim());
        }
    }

    private String toTagsJson(List<String> tags) {
        String elements = tags.stream()
                .map(t -> "\"" + t.replace("\\", "\\\\").replace("\"", "\\\"") + "\"")
                .collect(Collectors.joining(","));
        return "[" + elements + "]";
    }
}
