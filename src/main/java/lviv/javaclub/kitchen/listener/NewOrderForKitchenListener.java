package lviv.javaclub.kitchen.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lviv.javaclub.integration.model.ExternalOrderEvent;
import lviv.javaclub.kitchen.service.KitchenService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class NewOrderForKitchenListener {

    private final KitchenService kitchenService;

    @EventListener(condition = "#e.source.equals('KITCHEN') && #e.action.equals('startCooking')")
    public void onNewOrderForKitchen(ExternalOrderEvent e) {
        UUID orderId = e.getOrderId();
        log.info("Kitchen has received order [orderId={}]", orderId);
        kitchenService.prepareOrderItems(orderId);
    }


}
