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
import server1.inventory.customers.Envelope;
import server1.inventory.customers.Value;

import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class ConsumedCustomersResource {

    private static final Logger log = LoggerFactory.getLogger(ProbeMonitoring.class);

    @Incoming("customers-from-kafka")
    @Transactional
    @Blocking
    public CompletionStage<Void> consume(Message<Envelope> customerMessage) {
        try {
            if (customerMessage.getPayload() != null && customerMessage.getPayload().getAfter() != null) {
                final Value customer = customerMessage.getPayload().getAfter();

                log.info(customerMessage.getPayload().toString());

                log.info("Deleting customer with email: {}", customer.getEmail());
                CustomerEntity customerEntity = new CustomerEntity();
                customerEntity.setId((long) customer.getId());
                customerEntity.setEmail(customer.getEmail());
                customerEntity.setFirstName(customer.getFirstName());
                customerEntity.setLastName(customer.getLastName());
                customerEntity.delete();
            } else {
                //Tombstone message, just ack
                customerMessage.ack();
            }
        } catch (Exception e) {
            log.error("Exception detected in the Probe application: {}", e.getCause(), e);
            return customerMessage.nack(e);
        }
        return customerMessage.ack();
    }
}

