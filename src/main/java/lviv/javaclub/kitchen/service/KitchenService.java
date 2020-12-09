package lviv.javaclub.kitchen.service;


import java.util.UUID;

public interface KitchenService {

    void prepareOrderItems(UUID orderId);

    void cancelOrder(UUID orderId);

}
