package br.com.byop.aionlogbook.direction.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateDirectionRequest(
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description,

        @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "must be a valid hex color")
        String color,

        @Size(max = 100)
        String icon,

        @Size(max = 200)
        String identityPhrase
) {
}
