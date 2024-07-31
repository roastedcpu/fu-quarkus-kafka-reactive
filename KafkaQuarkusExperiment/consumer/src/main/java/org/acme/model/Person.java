package org.acme.model;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record Person(
        UUID id,
        @NotBlank String name,
        String surname,
        int age,
        Profession profession
) {
    public Person(@NotBlank String name, String surname, int age, Profession profession) {
        this(UUID.randomUUID(), name, surname, age, profession);
    }
}
