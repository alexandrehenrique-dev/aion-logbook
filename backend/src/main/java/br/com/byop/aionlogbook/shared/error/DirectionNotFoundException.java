package br.com.byop.aionlogbook.shared.error;

public class DirectionNotFoundException extends ResourceNotFoundException {

    public DirectionNotFoundException() {
        super("Direção não encontrada.");
    }
}
