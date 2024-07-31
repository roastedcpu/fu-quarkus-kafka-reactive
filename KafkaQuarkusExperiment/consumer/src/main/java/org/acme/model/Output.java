package org.acme.model;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

import java.util.UUID;

public record Output (
        @NonNull UUID id,
        @NotBlank String name,
        String key
) {}
