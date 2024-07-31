package org.acme.getting.started;

import java.util.UUID;

public record User(
        UUID id,
        String key
) {
}
