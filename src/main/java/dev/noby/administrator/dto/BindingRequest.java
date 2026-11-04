package dev.noby.administrator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BindingRequest(@NotBlank @Size(max = 80) String exchange,
                             @NotBlank @Size(max = 80) String queue,
                             @NotBlank @Size(max = 128) String routingKey) {
}
