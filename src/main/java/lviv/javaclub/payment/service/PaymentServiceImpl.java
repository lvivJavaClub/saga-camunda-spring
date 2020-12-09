package lviv.javaclub.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lviv.javaclub.integration.model.ExternalOrderEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void chargeCustomer(UUID orderId) {
        log.info("Debit customer account [orderId={}]", orderId);
        eventPublisher.publishEvent(
                new ExternalOrderEvent("PAYMENT", "paymentCompleted", orderId)
        );
    }

}
