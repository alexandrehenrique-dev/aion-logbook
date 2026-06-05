package br.com.byop.aionlogbook.plan.domain;

public enum PlanEventType {
    CREATED,
    UPDATED,
    RESCHEDULED,
    STARTED,
    COMPLETED,
    PARTIAL_COMPLETED,
    POSTPONED,
    IGNORED,
    CANCELED,
    MISSED,
    DUE,
    NOTE_ADDED,
    MODIFIED
}