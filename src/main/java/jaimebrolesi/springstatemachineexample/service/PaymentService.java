package jaimebrolesi.springstatemachineexample.service;

import jaimebrolesi.springstatemachineexample.domain.Payment;
import jaimebrolesi.springstatemachineexample.domain.PaymentEvent;
import jaimebrolesi.springstatemachineexample.domain.PaymentState;
import jaimebrolesi.springstatemachineexample.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    public static final String PAYMENT_ID_HEADER = "payment_id";
    private final PaymentRepository paymentRepository;
    private final StateMachineFactory<PaymentState, PaymentEvent> smFactory;
    private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;

    Payment newPayment(Payment payment) {
        payment.setState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }

    @Transactional
    StateMachine<PaymentState, PaymentEvent> preAuth(final Long paymentId) {
        final StateMachine<PaymentState, PaymentEvent> sm = reprocessPaymentLastState(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.PRE_AUTHORIZE);
        return sm;
    }

    @Transactional
    StateMachine<PaymentState, PaymentEvent> authorizePayment(final Long paymentId) {
        final StateMachine<PaymentState, PaymentEvent> sm = reprocessPaymentLastState(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.AUTHORIZE);
        return sm;
    }

    @Deprecated
    @Transactional
    StateMachine<PaymentState, PaymentEvent> declinePayment(final Long paymentId) {
        final StateMachine<PaymentState, PaymentEvent> sm = reprocessPaymentLastState(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.AUTH_DECLINED);
        return sm;
    }

    private void sendEvent(final Long paymentId, final StateMachine<PaymentState, PaymentEvent> sm, final PaymentEvent event) {
        final Message<PaymentEvent> msg = MessageBuilder.withPayload(event)
                .setHeader(PAYMENT_ID_HEADER, paymentId)
                .build();

        sm.sendEvent(msg);
    }

    private StateMachine<PaymentState, PaymentEvent> reprocessPaymentLastState(final Long paymentId) {
        //Get last payment info from database
        final Payment payment = paymentRepository.getOne(paymentId);
        //Get state machine of the payment
        final StateMachine<PaymentState, PaymentEvent> stateMachine = smFactory.getStateMachine(Long.toString(payment.getId()));
        //Stop processing first
        stateMachine.stop();
        //Reset the state of payment state machine
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                        sma.addStateMachineInterceptor(paymentStateChangeInterceptor);
                        sma.resetStateMachine(new DefaultStateMachineContext<>(
                                payment.getState(), null, null, null));
                });
        //Start state machine process
        stateMachine.start();
        return stateMachine;
    }
}
