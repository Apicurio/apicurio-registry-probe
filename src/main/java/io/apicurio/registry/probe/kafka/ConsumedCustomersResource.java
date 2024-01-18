package io.apicurio.registry.probe.kafka;

import io.apicurio.registry.probe.persistence.CustomerEntity;
import io.apicurio.registry.probe.smoke.ProbeMonitoring;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class ConsumedCustomersResource {

    private static final Logger log = LoggerFactory.getLogger(ProbeMonitoring.class);

    @Channel("customers-from-kafka")
    Multi<CustomerEntity> customers;

    public void startMonitoring(@Observes StartupEvent startupEvent) {
        int concurrentTasks = 2;

        try {
            concurrentTasks = Integer.parseInt(System.getenv("CONCURRENT_TASKS"));
        } catch (Exception e) {
            log.warn("Cannot load concurrent tasks environment variable", e);
        }

        ExecutorService e = Executors.newFixedThreadPool(concurrentTasks);

        for (int i = 0; i < concurrentTasks; i++) {
            e.submit(() -> {
                log.info("Removing customers...");
                removeCustomers();
            });
        }
    }

    public void removeCustomers() {
        customers.map(customer -> {
            final String stringToReturn = String.format("'%s' from %s", customer.getFirstName(), customer.getEmail());
            try {
                log.info("Deleting customer with email: {}", customer.getEmail());
                customer.delete();
            } catch (Exception e) {
                log.error("Exception detected in the Probe application: {}", e.getCause(), e);
            }
            return stringToReturn;
        });
    }
}

