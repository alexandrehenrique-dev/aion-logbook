package br.com.byop.aionlogbook.bugreport.application;

import br.com.byop.aionlogbook.bugreport.domain.BugReport;

public interface BugReportNotifier {

    boolean isEnabled();

    void notify(BugReport bugReport);
}