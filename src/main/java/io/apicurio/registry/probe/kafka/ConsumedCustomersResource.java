package io.apicurio.registry.probe.kafka;

import io.apicurio.registry.probe.persistence.CustomerEntity;
import io.apicurio.registry.probe.smoke.ProbeMonitoring;
import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class ConsumedCustomersResource {

    private static final Logger log = LoggerFactory.getLogger(ProbeMonitoring.class);

    @Incoming("customers-from-kafka")
    @Transactional
    @Blocking
    public CompletionStage<Void> consume(Message<CustomerEntity> customerMessage) {
        final CustomerEntity customer = customerMessage.getPayload();
        try {
            log.info("Deleting customer with email: {}", customer.getEmail());
            customer.delete();
        } catch (Exception e) {
            log.error("Exception detected in the Probe application: {}", e.getCause(), e);
            return customerMessage.nack(e);
        }
        return customerMessage.ack();
    }
}

