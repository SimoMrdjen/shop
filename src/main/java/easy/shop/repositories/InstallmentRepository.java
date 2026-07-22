package easy.shop.repositories;

import easy.shop.entities.Installment;
import easy.shop.entities.InstallmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InstallmentRepository extends JpaRepository<Installment, Long> {

    List<Installment> findByPurchaseContractId(Long contractId);

    Optional<Installment> findByPurchaseContractIdAndInstallmentOrdinal(Long contractId, Integer ordinal);

    @Query("SELECT i FROM Installment i WHERE i.status = 'PENDING' AND i.maturityDate < :today")
    List<Installment> findOverdue(LocalDate today);

    @Query("SELECT i FROM Installment i JOIN i.purchaseContract c WHERE c.customer.id = :customerId AND i.status IN ('PENDING', 'PARTIAL') ORDER BY i.maturityDate ASC")
    List<Installment> findUnpaidByCustomer(Long customerId);
}
