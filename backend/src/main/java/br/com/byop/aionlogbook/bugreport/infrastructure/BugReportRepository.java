package br.com.byop.aionlogbook.bugreport.infrastructure;

import br.com.byop.aionlogbook.bugreport.domain.BugReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BugReportRepository extends JpaRepository<BugReport, UUID> {
}
