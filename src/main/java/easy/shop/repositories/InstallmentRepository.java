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

    @Query("SELECT i FROM Installment i JOIN i.purchaseContract c WHERE i.status = 'PENDING' AND i.maturityDate < :today AND c.sentToLitigation = false")
    List<Installment> findOverdue(LocalDate today);

    // Namerno NE isključuje ugovore u utuženju - osoblje mora i dalje moći
    // da primi uplatu od kupca bez obzira na status utuženja.
    @Query("SELECT i FROM Installment i JOIN i.purchaseContract c WHERE c.customer.id = :customerId AND i.status IN ('PENDING', 'PARTIAL') ORDER BY i.maturityDate ASC")
    List<Installment> findUnpaidByCustomer(Long customerId);

    @Query("SELECT i FROM Installment i JOIN i.purchaseContract c WHERE i.maturityDate = :date AND i.status IN ('PENDING', 'PARTIAL') AND c.sentToLitigation = false")
    List<Installment> findUnpaidByMaturityDate(LocalDate date);

    @Query("SELECT i FROM Installment i JOIN i.purchaseContract c WHERE i.maturityDate <= :cutoffDate AND i.status IN ('PENDING', 'PARTIAL') AND c.sentToLitigation = false")
    List<Installment> findUnpaidWithMaturityDateOnOrBefore(LocalDate cutoffDate);

    @Query("SELECT i FROM Installment i JOIN i.purchaseContract c WHERE i.maturityDate < :cutoffDate AND i.status IN ('PENDING', 'PARTIAL') AND c.sentToLitigation = false")
    List<Installment> findUnpaidWithMaturityDateBefore(LocalDate cutoffDate);

    @Query("SELECT i FROM Installment i JOIN i.purchaseContract c WHERE i.status IN ('PENDING', 'PARTIAL') AND c.sentToLitigation = false AND i.maturityDate BETWEEN :fromDate AND :toDate ORDER BY i.maturityDate ASC")
    List<Installment> findForDebtorCallList(LocalDate fromDate, LocalDate toDate);

    @Query("SELECT i FROM Installment i JOIN i.purchaseContract c WHERE i.status IN ('PENDING', 'PARTIAL') AND c.sentToLitigation = false")
    List<Installment> findAllUnpaidExcludingLitigation();

    @Query("SELECT i FROM Installment i JOIN i.purchaseContract c WHERE i.status IN ('PENDING', 'PARTIAL') AND c.sentToLitigation = true")
    List<Installment> findAllUnpaidInLitigation();
}
