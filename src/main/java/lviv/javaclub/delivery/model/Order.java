package lviv.javaclub.delivery.model;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class Order {

    private UUID orderId;
    private OrderStatus orderStatus;
    private DeliveryAddress deliveryAddress;

}
