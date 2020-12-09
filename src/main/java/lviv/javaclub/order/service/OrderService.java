package lviv.javaclub.order.service;

import lviv.javaclub.order.model.Order;

public interface OrderService {

    Order newOrder();

    void cancelOrder();

}
