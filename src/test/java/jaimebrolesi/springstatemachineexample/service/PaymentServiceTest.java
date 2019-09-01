package jaimebrolesi.springstatemachineexample.service;

import jaimebrolesi.springstatemachineexample.domain.Payment;
import jaimebrolesi.springstatemachineexample.domain.PaymentEvent;
import jaimebrolesi.springstatemachineexample.domain.PaymentState;
import jaimebrolesi.springstatemachineexample.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@SpringBootTest
class PaymentServiceTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder().amount(new BigDecimal("12.99")).build();
    }

    @Test
    @Transactional
    void testPreAuth() {
        final Payment savedPayment = paymentService.newPayment(this.payment);
        paymentService.preAuth(savedPayment.getId());
        final Payment preAuthedPayment = paymentRepository.getOne(savedPayment.getId());
        System.out.println(preAuthedPayment);
    }

    @Transactional
    @RepeatedTest(10)
    void testAuthorize() {
        final Payment savedPayment = paymentService.newPayment(this.payment);
        final StateMachine<PaymentState, PaymentEvent> preAuthSM = paymentService.preAuth(savedPayment.getId());
        if(PaymentState.PRE_AUTH.equals(preAuthSM.getState().getId())) {
            System.out.println("Payment is PRE authorized.");
            final StateMachine<PaymentState, PaymentEvent> authorizeSM = paymentService.authorizePayment(savedPayment.getId());
            System.out.println("Authorization result: " + authorizeSM.getState().getId());
        } else {
            System.out.println("Payment failed on pre authorization. State: " + preAuthSM.getState().getId());
        }
    }
}