package org.acme.external;

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
