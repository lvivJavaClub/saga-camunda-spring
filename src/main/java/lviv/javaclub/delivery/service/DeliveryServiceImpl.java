package lviv.javaclub.delivery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lviv.javaclub.delivery.model.Order;
import lviv.javaclub.integration.model.ExternalOrderEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final ApplicationEventPublisher eventPublisher;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private ExternalOrderEvent deliveryEvent(String action, UUID orderId) {
        return new ExternalOrderEvent("DELIVERY", action, orderId);
    }

    @Override
    public void assignOrder(Order order) {
        log.info("Looking for free agent for New order [orderId={}]", order.getOrderId());
        eventPublisher.publishEvent(deliveryEvent("agentAssigned", order.getOrderId()));

        executorService.execute(() -> {
            try {
                Thread.sleep(3_000);
                log.info("Agent arrived to kitchen, waiting for order to be cooked");
                eventPublisher.publishEvent(deliveryEvent("agentArrivedToKitchen", order.getOrderId()));
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                Thread.interrupted();
            }
        });

    }

    @Override
    public void deliverOrder(UUID orderId) {
        eventPublisher.publishEvent(deliveryEvent("OUT_OF_WAY", orderId));
        executorService.execute(() -> {
            try {
                Thread.sleep(3_000);
                log.info("Agent arrived to customer");
                eventPublisher.publishEvent(deliveryEvent("agentArrivedToClient", orderId));
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                Thread.interrupted();
            }
        });
    }

    @Override
    public void cancelOrder(Order order) {
        log.info("Cancel order");
    }
}
