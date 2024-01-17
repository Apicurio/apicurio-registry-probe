package io.apicurio.registry.probe.kafka;

import io.apicurio.registry.probe.persistence.CustomerEntity;
import io.apicurio.registry.probe.smoke.ProbeMonitoring;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.reactive.RestSseElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Path("/consumed-customers")
public class ConsumedCustomersResource {

    private static final Logger log = LoggerFactory.getLogger(ProbeMonitoring.class);

    @Channel("customers-from-kafka")
    Multi<CustomerEntity> customers;

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestSseElementType(MediaType.TEXT_PLAIN)
    public Multi<String> stream() {
        return customers.map(customer -> {
            final String stringToReturn = String.format("'%s' from %s", customer.getFirstName(), customer.getEmail());
            try {
                customer.delete();
            } catch (Exception e) {
                log.error("Exception detected in the Probe application: {}", e.getCause(), e);
            }
            return stringToReturn;
        });
    }
}

