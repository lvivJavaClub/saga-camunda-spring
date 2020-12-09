package lviv.javaclub.delivery.service;


import lviv.javaclub.delivery.model.Order;

import java.util.UUID;

public interface DeliveryService {

    void assignOrder(Order order);

    void deliverOrder(UUID orderId);

    void cancelOrder(Order order);

}
