package lviv.javaclub.payment.service;

import java.util.UUID;

public interface PaymentService {

    void chargeCustomer(UUID orderId);

}
