package easy.shop.repositories;

import easy.shop.entities.PurchaseContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PurchaseContractRepository extends JpaRepository<PurchaseContract, Long> {

    @Query("SELECT c FROM PurchaseContract c JOIN FETCH c.customer")
    List<PurchaseContract> findAllWithCustomer();

    List<PurchaseContract> findByCustomerId(Long customerId);

    @Query("SELECT c FROM PurchaseContract c LEFT JOIN FETCH c.installments WHERE c.id = :id")
    Optional<PurchaseContract> findByIdWithInstallments(Long id);
}
