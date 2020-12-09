package lviv.javaclub.integration.model;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class ExternalOrderEvent extends ApplicationEvent {

    private final String action;
    private final UUID orderId;

    public ExternalOrderEvent(String source, String action, UUID orderId) {
        super(source);
        this.action = action;
        this.orderId = orderId;
    }

    public String getSource() {
        return (String) source;
    }

    @Override
    public String toString() {
        return "ExternalOrderEvent{" +
                "action='" + action + '\'' +
                ", orderId=" + orderId +
                ", source=" + source +
                '}';
    }
}
