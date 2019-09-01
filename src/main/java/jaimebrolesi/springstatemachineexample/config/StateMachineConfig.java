package jaimebrolesi.springstatemachineexample.config;

import jaimebrolesi.springstatemachineexample.domain.PaymentEvent;
import jaimebrolesi.springstatemachineexample.domain.PaymentState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;

import static jaimebrolesi.springstatemachineexample.domain.PaymentState.*;

@Slf4j
@EnableStateMachineFactory
@Configuration
@RequiredArgsConstructor
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    private final Guard<PaymentState, PaymentEvent> paymentIdGuard;
    private final Action<PaymentState, PaymentEvent> authAction;
    private final Action<PaymentState, PaymentEvent> authApprovedAction;
    private final Action<PaymentState, PaymentEvent> authDeclinedAction;
    private final Action<PaymentState, PaymentEvent> preAuthAction;
    private final Action<PaymentState, PaymentEvent> preAuthApprovedAction;
    private final Action<PaymentState, PaymentEvent> preAuthDeclinedAction;

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states.withStates()
                .initial(NEW)
                .states(EnumSet.allOf(PaymentState.class))
                .end(AUTH)
                .end(PRE_AUTH_ERROR)
                .end(AUTH_ERROR);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions.withExternal()
                .source(NEW).target(NEW).event(PaymentEvent.PRE_AUTHORIZE)
                .action(preAuthAction)
                .guard(paymentIdGuard)
                .and()
                .withExternal().source(NEW).target(PRE_AUTH).event(PaymentEvent.PRE_AUTH_APPROVED)
                .action(preAuthApprovedAction)
                .and()
                .withExternal().source(NEW).target(PRE_AUTH_ERROR).event(PaymentEvent.PRE_AUTH_DECLINED)
                .action(preAuthDeclinedAction)
                .and()
                .withExternal().source(PRE_AUTH).target(PRE_AUTH).event(PaymentEvent.AUTHORIZE)
                .action(authAction)
                .and()
                .withExternal().source(PRE_AUTH).target(AUTH).event(PaymentEvent.AUTH_APPROVED)
                .action(authApprovedAction)
                .and()
                .withExternal().source(PRE_AUTH).target(AUTH_ERROR).event(PaymentEvent.AUTH_DECLINED)
                .action(authDeclinedAction);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                log.info(String.format("stateChanged(from: %s, to: %s", from, to));
            }
        };
        config.withConfiguration()
                .listener(adapter);
    }
}
