package lviv.javaclub.payment.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lviv.javaclub.integration.model.ExternalOrderEvent;
import lviv.javaclub.payment.service.PaymentService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChargeCustomerListener {

    private final PaymentService paymentService;

    @EventListener(condition = "#e.source.equals('PAYMENT') && #e.action.equals('CHARGE_CUSTOMER')")
    public void chargeCustomer(ExternalOrderEvent e) {
        paymentService.chargeCustomer(e.getOrderId());
    }

}
