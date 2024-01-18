package io.apicurio.registry.probe.monitoring;

import io.apicurio.registry.probe.persistence.CustomerEntity;
import io.apicurio.registry.probe.smoke.ProbeMonitoring;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadGenerator {

    private static final Logger log = LoggerFactory.getLogger(ProbeMonitoring.class);

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
                log.info("Generating customers...");
                generateCustomers();
            });
        }
    }

    private void generateCustomers() {
        while (true) {
            try {
                generateCustomer();
            } catch (Exception e) {
                log.error("Exception detected in the Probe application: {}", e.getCause(), e);
            }
        }
    }

    @Transactional
    protected void generateCustomer() throws InterruptedException {
        CustomerEntity customer = new CustomerEntity();
        String email = UUID.randomUUID() + "@apicurio.io";
        log.info("Generating customer with email: {}", email);
        customer.setEmail(email);
        customer.setFirstName(UUID.randomUUID().toString());
        customer.setLastName(UUID.randomUUID().toString());
        customer.persist();
        Thread.sleep(1000);
    }
}
