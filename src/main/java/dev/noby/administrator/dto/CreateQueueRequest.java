package dev.noby.administrator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateQueueRequest(@NotBlank @Size(max = 80) String name) {
}
