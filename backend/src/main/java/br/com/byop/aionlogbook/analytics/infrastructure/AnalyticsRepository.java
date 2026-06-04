package br.com.byop.aionlogbook.analytics.infrastructure;

import br.com.byop.aionlogbook.analytics.domain.AnalyticsOverviewProjection;
import br.com.byop.aionlogbook.analytics.domain.PlannedVsExecutedProjection;
import br.com.byop.aionlogbook.analytics.domain.PlansByDayProjection;
import br.com.byop.aionlogbook.analytics.domain.StatusDistributionProjection;
import br.com.byop.aionlogbook.analytics.domain.TimeByDirectionProjection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public class AnalyticsRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public AnalyticsOverviewProjection overview(UUID userId, LocalDate dateFrom, LocalDate dateTo) {
        var query = entityManager.createNativeQuery("""
                select
                    count(p.id) as plans_created,
                    coalesce(sum(case when p.status = 'COMPLETED' then 1 else 0 end), 0) as plans_completed,
                    coalesce(sum(case when p.status = 'PARTIAL' then 1 else 0 end), 0) as plans_partial,
                    coalesce(sum(case when p.status = 'MISSED' then 1 else 0 end), 0) as plans_missed,
                    coalesce(sum(case when p.status = 'IGNORED' then 1 else 0 end), 0) as plans_ignored,
                    coalesce(sum(case when p.status = 'CANCELED' then 1 else 0 end), 0) as plans_canceled,
                    count(distinct p.direction_id) filter (where p.direction_id is not null) as active_directions,
                    (
                        select coalesce(sum(sl.duration_minutes), 0)
                        from session_logs sl
                        where sl.user_id = :userId
                          and (cast(:dateFrom as date) is null or sl.started_at::date >= cast(:dateFrom as date))
                          and (cast(:dateTo as date) is null or sl.started_at::date <= cast(:dateTo as date))
                    ) as total_time_minutes,
                    (
                        select coalesce(sum(sl.duration_minutes), 0)
                        from session_logs sl
                        where sl.user_id = :userId
                          and sl.started_at::date >= current_date - interval '6 days'
                          and sl.started_at::date <= current_date
                    ) as weekly_time_minutes
                from plans p
                where p.user_id = :userId
                  and (cast(:dateFrom as date) is null or p.planned_date >= cast(:dateFrom as date))
                  and (cast(:dateTo as date) is null or p.planned_date <= cast(:dateTo as date))
                """);

        setCommonParameters(query, userId, dateFrom, dateTo);

        var row = (Object[]) query.getSingleResult();

        return new AnalyticsOverviewProjection(
                toLong(row[0]),
                toLong(row[1]),
                toLong(row[2]),
                toLong(row[3]),
                toLong(row[4]),
                toLong(row[5]),
                toLong(row[6]),
                toLong(row[7]),
                toLong(row[8])
        );
    }

    public List<PlansByDayProjection> plansByDay(UUID userId, LocalDate dateFrom, LocalDate dateTo) {
        var query = entityManager.createNativeQuery("""
                select
                    p.planned_date as date,
                    count(p.id) as planned,
                    coalesce(sum(case when p.status in ('COMPLETED', 'PARTIAL') then 1 else 0 end), 0) as executed
                from plans p
                where p.user_id = :userId
                  and p.planned_date is not null
                  and (cast(:dateFrom as date) is null or p.planned_date >= cast(:dateFrom as date))
                  and (cast(:dateTo as date) is null or p.planned_date <= cast(:dateTo as date))
                group by p.planned_date
                order by p.planned_date
                """);

        setCommonParameters(query, userId, dateFrom, dateTo);

        return query.getResultList()
                .stream()
                .map(row -> {
                    var values = (Object[]) row;

                    return new PlansByDayProjection(
                            toLocalDate(values[0]),
                            toLong(values[1]),
                            toLong(values[2])
                    );
                })
                .toList();
    }

    public List<StatusDistributionProjection> statusDistribution(UUID userId, LocalDate dateFrom, LocalDate dateTo) {
        var query = entityManager.createNativeQuery("""
                select
                    p.status as status,
                    count(p.id) as count
                from plans p
                where p.user_id = :userId
                  and (cast(:dateFrom as date) is null or p.planned_date >= cast(:dateFrom as date))
                  and (cast(:dateTo as date) is null or p.planned_date <= cast(:dateTo as date))
                group by p.status
                order by count(p.id) desc, p.status
                """);

        setCommonParameters(query, userId, dateFrom, dateTo);

        return query.getResultList()
                .stream()
                .map(row -> {
                    var values = (Object[]) row;

                    return new StatusDistributionProjection(
                            String.valueOf(values[0]),
                            toLong(values[1])
                    );
                })
                .toList();
    }

    public List<TimeByDirectionProjection> timeByDirection(UUID userId, LocalDate dateFrom, LocalDate dateTo) {
        var query = entityManager.createNativeQuery("""
                select
                    sl.direction_id as direction_id,
                    coalesce(d.name, 'Sem direção') as direction_name,
                    d.color as color,
                    coalesce(sum(sl.duration_minutes), 0) as total_minutes,
                    count(sl.id) as sessions_count
                from session_logs sl
                left join directions d
                       on d.id = sl.direction_id
                      and d.user_profile_id = sl.user_id
                where sl.user_id = :userId
                  and (cast(:dateFrom as date) is null or sl.started_at::date >= cast(:dateFrom as date))
                  and (cast(:dateTo as date) is null or sl.started_at::date <= cast(:dateTo as date))
                group by sl.direction_id, d.name, d.color
                order by total_minutes desc, direction_name
                """);

        setCommonParameters(query, userId, dateFrom, dateTo);

        return query.getResultList()
                .stream()
                .map(row -> {
                    var values = (Object[]) row;

                    return new TimeByDirectionProjection(
                            toUuid(values[0]),
                            String.valueOf(values[1]),
                            values[2] == null ? null : String.valueOf(values[2]),
                            toLong(values[3]),
                            toLong(values[4])
                    );
                })
                .toList();
    }

    public List<PlannedVsExecutedProjection> plannedVsExecuted(UUID userId, LocalDate dateFrom, LocalDate dateTo) {
        var query = entityManager.createNativeQuery("""
                with planned as (
                    select
                        p.planned_date as date,
                        coalesce(sum(coalesce(p.estimated_minutes, 0)), 0) as planned_minutes
                    from plans p
                    where p.user_id = :userId
                      and p.planned_date is not null
                      and (cast(:dateFrom as date) is null or p.planned_date >= cast(:dateFrom as date))
                      and (cast(:dateTo as date) is null or p.planned_date <= cast(:dateTo as date))
                    group by p.planned_date
                ),
                executed as (
                    select
                        sl.started_at::date as date,
                        coalesce(sum(coalesce(sl.duration_minutes, 0)), 0) as executed_minutes
                    from session_logs sl
                    where sl.user_id = :userId
                      and (cast(:dateFrom as date) is null or sl.started_at::date >= cast(:dateFrom as date))
                      and (cast(:dateTo as date) is null or sl.started_at::date <= cast(:dateTo as date))
                    group by sl.started_at::date
                )
                select
                    coalesce(planned.date, executed.date) as date,
                    coalesce(planned.planned_minutes, 0) as planned_minutes,
                    coalesce(executed.executed_minutes, 0) as executed_minutes
                from planned
                full outer join executed
                  on executed.date = planned.date
                order by date
                """);

        setCommonParameters(query, userId, dateFrom, dateTo);

        return query.getResultList()
                .stream()
                .map(row -> {
                    var values = (Object[]) row;

                    return new PlannedVsExecutedProjection(
                            toLocalDate(values[0]),
                            toLong(values[1]),
                            toLong(values[2])
                    );
                })
                .toList();
    }

    private void setCommonParameters(Query query, UUID userId, LocalDate dateFrom, LocalDate dateTo) {
        query.setParameter("userId", userId);
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("dateTo", dateTo);
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        return Long.parseLong(value.toString());
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof LocalDate localDate) {
            return localDate;
        }

        if (value instanceof Date date) {
            return date.toLocalDate();
        }

        return LocalDate.parse(value.toString());
    }

    private UUID toUuid(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof UUID uuid) {
            return uuid;
        }

        return UUID.fromString(value.toString());
    }
}
