package lviv.javaclub.kitchen.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lviv.javaclub.integration.model.ExternalOrderEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class KitchenServiceImpl implements KitchenService {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void prepareOrderItems(UUID orderId) {
        eventPublisher.publishEvent(new ExternalOrderEvent("KITCHEN", "cookingStarted", orderId));
        new Thread(() -> {
            try {
                Thread.sleep(5_000);
                log.info("Order has been cooked. Agent can take it for delivery [orderId={}]", orderId);
                eventPublisher.publishEvent(new ExternalOrderEvent("KITCHEN", "cooked", orderId));
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                Thread.interrupted();
            }
        }).start();
    }

    @Override
    public void cancelOrder(UUID orderId) {
        log.info("Cancel order [orderId={}]", orderId);
    }

}
