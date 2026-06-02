package br.com.byop.aionlogbook.shared.error;

public record ApiFieldError(
        String field,
        String message,
        Object rejectedValue
) {
}
