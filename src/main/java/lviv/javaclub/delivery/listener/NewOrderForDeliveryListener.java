package lviv.javaclub.delivery.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lviv.javaclub.delivery.model.Order;
import lviv.javaclub.delivery.service.DeliveryService;
import lviv.javaclub.integration.model.ExternalOrderEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class NewOrderForDeliveryListener {

    private final DeliveryService deliveryService;

    @EventListener(condition = "#e.source.equals('DELIVERY') && #e.action.equals('assignAgent')")
    public void onNewOrderForDelivery(ExternalOrderEvent e) {
        UUID orderId = e.getOrderId();
        log.info("Delivery has received order [orderId={}]", orderId);

        deliveryService.assignOrder(
                Order.builder()
                        .orderId(orderId)
                        .build()
        );
    }


}
