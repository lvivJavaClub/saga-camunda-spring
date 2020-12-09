package lviv.javaclub.kitchen.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Builder
@Getter
public class Order {

    private UUID orderId;
    private List<OrderItem> items;


}
