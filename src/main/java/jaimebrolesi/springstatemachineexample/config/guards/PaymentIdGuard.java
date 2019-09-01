package jaimebrolesi.springstatemachineexample.config.guards;

import jaimebrolesi.springstatemachineexample.domain.PaymentEvent;
import jaimebrolesi.springstatemachineexample.domain.PaymentState;
import jaimebrolesi.springstatemachineexample.service.PaymentService;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;
import org.springframework.stereotype.Component;

@Component
public class PaymentIdGuard implements Guard<PaymentState, PaymentEvent> {

    @Override
    public boolean evaluate(StateContext<PaymentState, PaymentEvent> context) {
        return context.getMessageHeader(PaymentService.PAYMENT_ID_HEADER) != null;
    }
}
