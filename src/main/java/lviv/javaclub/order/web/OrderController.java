package lviv.javaclub.order.web;

import lombok.RequiredArgsConstructor;
import lviv.javaclub.order.model.Order;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class OrderController {

    private final RuntimeService runtimeService;


    @GetMapping("/orders")
    public String newOrder(Order order) {
        UUID orderId = UUID.randomUUID();
        order.setOrderId(orderId);

        runtimeService.startProcessInstanceByKey("Order",
            orderId.toString(),
            Map.of("paymentStatus", "pending",
                "orderId", orderId.toString())
        );

        return "done";
    }

}
