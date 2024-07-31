package org.acme.external;

import java.util.UUID;

public record User(
    UUID uuid,
    String key
) {

}
