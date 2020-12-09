package lviv.javaclub.integration.listener;

import lombok.extern.slf4j.Slf4j;
import lviv.javaclub.integration.model.ExternalOrderEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IntegrationEventLogListener {

    @EventListener
    public void processEvent(ExternalOrderEvent event) {
        log.info("Processing event [event={}]", event.toString());
    }

}
