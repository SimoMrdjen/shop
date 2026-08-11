package easy.shop.repositories;

import easy.shop.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p " +
            "JOIN FETCH p.installment i " +
            "JOIN FETCH i.purchaseContract c " +
            "JOIN FETCH c.customer cu " +
            "WHERE p.paymentDate = :date " +
            "ORDER BY p.createdAt ASC")
    List<Payment> findByPaymentDate(LocalDate date);

    @Query("SELECT p FROM Payment p JOIN FETCH p.installment WHERE p.installment.id IN :installmentIds")
    List<Payment> findByInstallmentIdIn(List<Long> installmentIds);

    @Query("SELECT p FROM Payment p JOIN FETCH p.installment i " +
            "WHERE p.paymentGroupId = :groupId ORDER BY i.installmentOrdinal ASC")
    List<Payment> findByPaymentGroupId(String groupId);
}
