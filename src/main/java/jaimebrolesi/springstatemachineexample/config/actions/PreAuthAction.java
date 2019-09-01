package jaimebrolesi.springstatemachineexample.config.actions;

import jaimebrolesi.springstatemachineexample.domain.PaymentEvent;
import jaimebrolesi.springstatemachineexample.domain.PaymentState;
import jaimebrolesi.springstatemachineexample.service.PaymentService;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class PreAuthAction implements Action<PaymentState, PaymentEvent> {

    @Override
    public void execute(StateContext<PaymentState, PaymentEvent> context) {
        System.out.println("PreAuth was called!!!");
        if (new Random().nextInt(10) < 8) {
            System.out.println("Approved");
            context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVED)
                    .setHeader(PaymentService.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentService.PAYMENT_ID_HEADER))
                    .build());
        } else {
            System.out.println("Declined! No Credit!!!!");
            context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_DECLINED)
                    .setHeader(PaymentService.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentService.PAYMENT_ID_HEADER))
                    .build());
        }
    }
}
