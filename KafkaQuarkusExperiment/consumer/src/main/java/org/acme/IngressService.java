package org.acme;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import io.smallrye.common.annotation.Identifier;
import io.smallrye.reactive.messaging.kafka.DeserializationFailureHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.acme.external.User;
import org.acme.external.UserService;
import org.acme.model.Output;
import org.acme.model.Person;
import org.apache.kafka.common.header.Headers;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Slf4j
@ApplicationScoped
@Startup
@Identifier("ingress-service")
public class IngressService implements DeserializationFailureHandler<Person> {
    @RestClient
    UserService userService;

    @Inject
    EgressService egressService;

    ObjectMapper objectMapper = new ObjectMapper();

    @Incoming("person-create")
    public CompletionStage<Void> consumeLorawanIngressMsg(Message<Person> message) {
        CompletionStage<Person> getMessagePayload = CompletableFuture.supplyAsync(() -> {
            try {
                Log.debug("Receiving message on ingress: " + objectMapper.writeValueAsString(message.getPayload()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return message.getPayload();
        }).thenCompose(msg -> CompletableFuture.supplyAsync(() -> {
            if (msg==null) {
                throw new IllegalArgumentException("Error deserializing message.");
            }
            return msg;
        }));

        CompletionStage<User> getUser = userService.getRandomUser(); // used to be a blocking call, now it is not, anymore
        /*
                                                                                                                import jakarta.validation.constraints.NotNull;
                                                                                                                import jakarta.ws.rs.GET;
                                                                                                                import jakarta.ws.rs.Path;
                                                                                                                import jakarta.ws.rs.PathParam;
                                                                                                                import jakarta.ws.rs.QueryParam;
                                                                                                                import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

                                                                                                                import java.util.List;
                                                                                                                import java.util.concurrent.CompletionStage;

                                                                                                                @Path("/api")
                                                                                                                @RegisterRestClient(configKey = "external-api")
                                                                                                                public interface UserService {
                                                                                                                    @GET
                                                                                                                    @Path("/user")
                                                                                                                    CompletionStage<User> getRandomUser();
                                                                                                                }

         */

        /*
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
         */

        CompletionStage<Void> chain = getMessagePayload
                .thenCombine(getUser, (msg, user) -> egressService.sendToEgress(new Output(
                        UUID.randomUUID(),
                        msg.name(),
                        user.key()
                )))
                .handle((res, ex) -> {
                    if (ex == null) {
                        return res;
                    }

                    try {
                        Log.error(String.format("Error while processing message on ingress: %s", objectMapper.writeValueAsString(message.getPayload())), ex);
                    } catch (JsonProcessingException e) {
                        Log.error("Error while processing message on ingress", ex);
                        throw new RuntimeException(e);
                    } finally {
                        return message.nack(ex);  // For kafka dead letter strategy to take place this whole thing needs to fail.
                    }
                })
                .thenCompose(result -> result)  // flatten
                .thenAccept(_void -> message.ack());

        return chain;
    }

    @Override
    public Person handleDeserializationFailure(String topic, boolean isKey, String deserializer, byte[] data, Exception exception, Headers headers) {
        log.error("Failed to parse message on topic " + topic);
        return null;
    }
}
