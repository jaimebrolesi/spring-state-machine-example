package jaimebrolesi.springstatemachineexample.repository;

import jaimebrolesi.springstatemachineexample.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
