package lviv.javaclub.order.service;

import org.springframework.stereotype.Service;

@Service("orderService")
public class OrderServiceImpl {

    public void sayHello(String message) {
        System.out.println(message);
    }

}
