package org.acme.getting.started;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.SneakyThrows;

import java.util.Random;
import java.util.UUID;

@Path("/api")
public class GreetingResource {
    @SneakyThrows
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/user")
    public User random1234() {
        Random r = new Random();
        int low = 100;
        int high = 10000;
        int randomDl = r.nextInt(high-low) + low;

        Thread.sleep(randomDl);
        return new User(UUID.randomUUID(), UUID.randomUUID().toString());
    }
}