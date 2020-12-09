package lviv.javaclub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lviv.javaclub.integration.model.ExternalOrderEvent;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.CallActivity;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaIn;
import org.camunda.bpm.spring.boot.starter.event.ExecutionEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class OrderSaga {

    ExecutorService executorService = Executors.newFixedThreadPool(10);

    private final ProcessEngine processEngine;
    private final RuntimeService runtimeService;
    private final RepositoryService repositoryService;

    private final ApplicationEventPublisher applicationEventPublisher;


    @EventListener
    public void init(ContextRefreshedEvent ctx) {
        DeploymentBuilder deployment = repositoryService.createDeployment().name("Online Cafe");

        BpmnModelInstance agentFlow = Bpmn.createExecutableProcess("AgentFlow")
            .name("AgentFlow")
                .startEvent("StartDeliveryFlow")
                    .serviceTask("lookingForAnAgent")
                        .camundaExpression("${orderSaga.deliveryCommand(deliveryOrderId, 'assignAgent')}")
                        .camundaAsyncBefore()
                    .receiveTask("deliveryWaitAssignment")
                        .message("agentAssigned")
                    .serviceTask("agentAssignedNotification")
                        .camundaExpression("${orderSaga.notifyCustomer(deliveryOrderId, 'Wait for premium delivery')}")
                        .camundaAsyncBefore()
                    .receiveTask("deliveryWaiForArrive")
                        .message("agentArrivedToKitchen")
                    .endEvent()
            .done();
        deployment.addModelInstance("agentFlow.bpmn", agentFlow);

        BpmnModelInstance kitchenFlow = Bpmn.createExecutableProcess("KitchenFlow")
            .name("KitchenFlow")
                 .startEvent("StartKitchenFlow")
                 .serviceTask("startKitchenProcess")
                     .camundaExpression("${orderSaga.kitchenEvent(kitchenOrderId, 'startCooking')}")
                     .camundaAsyncBefore()
                 .receiveTask("kitchenWaitForStart")
                     .message("cookingStarted")
                 .serviceTask("cookingStartedNotification")
                     .camundaExpression("${orderSaga.notifyCustomer(kitchenOrderId, 'we are cooking for you')}")
                 .camundaAsyncBefore()
                 .receiveTask("kitchenWhileCooking")
                     .message("cooked")
                .endEvent()
            .done();
        deployment.addModelInstance("kitchenFlow.bpmn", kitchenFlow);


        BpmnModelInstance proccess = Bpmn.createExecutableProcess("Order")
            .name("Order Flow")
                .startEvent("startOrderFlow")
                    .camundaAsyncBefore()

                .serviceTask("startPayment")
                    .camundaExpression("${orderSaga.paymentEvent(execution.businessKey, 'CHARGE_CUSTOMER')}")
                .receiveTask("waitForPayment")
                    .message("paymentCompleted")


                .exclusiveGateway("paymentCompleted")
                    .condition("isPayed", "#{paymentStatus == 'completed'}")
                    .serviceTask("thanksForOrdering")
                        .camundaExpression("${orderSaga.notifyCustomer(execution.businessKey, 'Thanks for purchase')}")

                    .parallelGateway("fork")
                        .callActivity("AgentFlow")
                            .camundaIn("orderId", "deliveryOrderId")
                            .calledElement("AgentFlow")
                    .parallelGateway("join")

                    .moveToNode("fork")
                        .callActivity("KitchenFlow")
                            .camundaIn("orderId", "kitchenOrderId")
                            .calledElement("KitchenFlow")
                        .connectTo("join")

                    .moveToNode("join")
                        .serviceTask("orderIsReady")
                            .camundaExpression("${orderSaga.notifyCustomer(execution.businessKey, 'Your order is ready. Wait for Delivery')}")
                        .serviceTask("startDelivery")
                            .camundaAsyncBefore()
                            .camundaExpression("${orderSaga.deliveryCommand(orderId, 'deliverOrder')}")
                        .receiveTask("waitForAgentArrive")
                            .message("agentArrivedToClient")

                        .serviceTask("handoverPackage")
                            .camundaExpression("${orderSaga.notifyCustomer(execution.businessKey, 'open the door')}")

                .moveToNode("paymentCompleted")
                    .condition("isPayed", "#{paymentStatus == 'nooo'}")
                    .serviceTask("sayNoooo")
                        .camundaExpression("${orderSaga.notifyCustomer(execution.businessKey, 'Sorry, please call us')}")

                .endEvent("OrderCompletedSuccessfully")
                .done();

        deployment.addModelInstance("order.bpmn", proccess);
        DeploymentWithDefinitions deploymentWithDefinitions = deployment.deployWithResult();
        log.info("Deployed processes [count={}]", deploymentWithDefinitions.getDeployedProcessDefinitions().size());
    }

    public void notifyCustomer(String orderId, String message) {
        log.info(orderId + ": " + message);
    }

    public void deliveryCommand(String orderId, String action) {
        executorService.submit(() -> applicationEventPublisher
            .publishEvent(new ExternalOrderEvent("DELIVERY", action, UUID.fromString(orderId)))
        );
    }

    public void paymentEvent(String orderId, String action) {
        executorService.submit(() -> applicationEventPublisher
            .publishEvent(new ExternalOrderEvent("PAYMENT", action, UUID.fromString(orderId)))
        );
    }

    public void kitchenEvent(String orderId, String action) {
        executorService.submit(() -> applicationEventPublisher
            .publishEvent(new ExternalOrderEvent("KITCHEN", action, UUID.fromString(orderId)))
        );
    }


    @EventListener(condition = "#e.source.equals('PAYMENT') && #e.action.equals('paymentCompleted')")
    public void onPaymentCompleted(ExternalOrderEvent e) {
        runtimeService
                .createMessageCorrelation(e.getAction())
                .processInstanceBusinessKey(e.getOrderId().toString())
                .setVariable("paymentStatus", "completed")
                .correlate();
    }

    @EventListener(condition = "#e.source.equals('DELIVERY') && #e.action.equals('agentAssigned')")
    public void onAgentAssigned(ExternalOrderEvent e) {
        messageCorrelationByOrder(e);
    }

    @EventListener(condition = "#e.source.equals('DELIVERY') && #e.action.equals('agentArrivedToKitchen')")
    public void onAgentArrivedToKitchen(ExternalOrderEvent e) {
        messageCorrelationByOrder(e);
    }

    @EventListener(condition = "#e.source.equals('DELIVERY') && #e.action.equals('agentArrivedToClient')")
    public void onAgentArrivedToCustomer(ExternalOrderEvent e) {
        runtimeService
                .createMessageCorrelation(e.getAction())
                .processInstanceBusinessKey(e.getOrderId().toString())
                .correlate();
    }

    @EventListener(condition = "#e.source.equals('KITCHEN') && #e.action.equals('cookingStarted')")
    public void onCookingStarted(ExternalOrderEvent e) {
        messageCorrelationByOrder(e);
    }

    @EventListener(condition = "#e.source.equals('KITCHEN') && #e.action.equals('cooked')")
    public void onCookingCompleted(ExternalOrderEvent e) {
        messageCorrelationByOrder(e);
    }

    private void messageCorrelationByOrder(ExternalOrderEvent e) {
        runtimeService
                .createMessageCorrelation(e.getAction())
                .processInstanceVariableEquals(e.getSource().toLowerCase() + "OrderId",e.getOrderId().toString())
                .correlate();
    }


    @EventListener
    public void executionListener(ExecutionEvent e) {
        log.info("event [bk={},event={}]",
            e.getCurrentActivityName(), e.getEventName());
    }

}
