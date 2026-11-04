package dev.noby.administrator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateExchangeRequest(@NotBlank @Size(max = 80) String name, @NotBlank String type) {
}
