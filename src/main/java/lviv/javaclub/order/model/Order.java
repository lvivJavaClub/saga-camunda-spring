package lviv.javaclub.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Order {

    private UUID orderId;
    private String customerName;
    private String phone;
    private DeliveryAddress deliveryAddress;
    private UUID deliveryAgentId;
    private OrderStatus status;

    private List<OrderItem> items;

}
