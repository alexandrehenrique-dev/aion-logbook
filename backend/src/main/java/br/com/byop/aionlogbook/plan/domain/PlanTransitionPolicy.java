package br.com.byop.aionlogbook.plan.domain;

import java.util.EnumSet;
import java.util.Set;

public final class PlanTransitionPolicy {

    private static final Set<PlanStatus> TERMINAL = EnumSet.of(
            PlanStatus.COMPLETED,
            PlanStatus.PARTIAL,
            PlanStatus.IGNORED,
            PlanStatus.CANCELED
    );

    private static final Set<PlanStatus> CAN_START = EnumSet.of(
            PlanStatus.SCHEDULED,
            PlanStatus.PENDING,
            PlanStatus.DUE,
            PlanStatus.MISSED,
            PlanStatus.POSTPONED
    );

    private static final Set<PlanStatus> CAN_PARTIAL = EnumSet.of(
            PlanStatus.IN_PROGRESS,
            PlanStatus.DUE,
            PlanStatus.PENDING,
            PlanStatus.MISSED
    );

    private static final Set<PlanStatus> CAN_POSTPONE = EnumSet.of(
            PlanStatus.SCHEDULED,
            PlanStatus.PENDING,
            PlanStatus.DUE,
            PlanStatus.MISSED,
            PlanStatus.POSTPONED
    );

    private static final Set<PlanStatus> CAN_IGNORE = EnumSet.of(
            PlanStatus.SCHEDULED,
            PlanStatus.PENDING,
            PlanStatus.DUE,
            PlanStatus.MISSED
    );

    private PlanTransitionPolicy() {}

    public static boolean isTerminal(PlanStatus status) {
        return TERMINAL.contains(status);
    }

    public static boolean canStart(PlanStatus status) {
        return CAN_START.contains(status);
    }

    public static boolean canComplete(PlanStatus status) {
        return status == PlanStatus.IN_PROGRESS;
    }

    public static boolean canPartial(PlanStatus status) {
        return CAN_PARTIAL.contains(status);
    }

    public static boolean canPostpone(PlanStatus status) {
        return CAN_POSTPONE.contains(status);
    }

    public static boolean canIgnore(PlanStatus status) {
        return CAN_IGNORE.contains(status);
    }

    public static boolean canCancel(PlanStatus status) {
        return !isTerminal(status);
    }

    public static boolean canModify(PlanStatus status) {
        return !isTerminal(status);
    }
}