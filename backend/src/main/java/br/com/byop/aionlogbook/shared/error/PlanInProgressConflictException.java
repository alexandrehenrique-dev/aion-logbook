package br.com.byop.aionlogbook.shared.error;

public class PlanInProgressConflictException extends ConflictException {

    public PlanInProgressConflictException() {
        super("Já existe um plano em andamento para este usuário.");
    }
}
