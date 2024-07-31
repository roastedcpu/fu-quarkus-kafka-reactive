package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.acme.model.Output;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.util.concurrent.CompletionStage;

@Slf4j
@ApplicationScoped
public class EgressService {
    @Inject
    @Channel("egress")
    Emitter<Output> farmsenseEmitter;

    @SneakyThrows
    public CompletionStage<Void> sendToEgress(@NonNull Output outputMsg) {
        return farmsenseEmitter.send(outputMsg).thenRun(() -> log.debug("Message {} sent to egress topic", outputMsg));
    }
}