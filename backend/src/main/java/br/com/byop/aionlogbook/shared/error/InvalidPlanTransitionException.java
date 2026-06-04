package br.com.byop.aionlogbook.shared.error;

public class InvalidPlanTransitionException extends InvalidTransitionException {

    public InvalidPlanTransitionException() {
        super("Transição inválida para o estado atual do plano.");
    }
}
