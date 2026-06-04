package br.com.byop.aionlogbook.scheduler;

import br.com.byop.aionlogbook.plan.application.PlanSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PlanStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(PlanStatusScheduler.class);

    private final PlanSchedulerService planSchedulerService;

    public PlanStatusScheduler(PlanSchedulerService planSchedulerService) {
        this.planSchedulerService = planSchedulerService;
    }

    @Scheduled(fixedDelayString = "${aion.scheduler.due.fixed-delay:60000}")
    public void runDueJob() {
        run("DUE", planSchedulerService::markDuePlans);
    }

    @Scheduled(fixedDelayString = "${aion.scheduler.missed.fixed-delay:300000}")
    public void runMissedJob() {
        run("MISSED", planSchedulerService::markMissedPlans);
    }

    private void run(String jobName, SchedulerJob job) {
        var startedAt = System.nanoTime();

        log.info("scheduler.job.start name={}", jobName);

        int count = job.execute();

        var durationMs = (System.nanoTime() - startedAt) / 1_000_000;

        log.info(
                "scheduler.job.finish name={} count={} durationMs={}",
                jobName,
                count,
                durationMs
        );
    }

    @FunctionalInterface
    private interface SchedulerJob {
        int execute();
    }
}
