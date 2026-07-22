package easy.shop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import easy.shop.entities.Customer;

import java.util.List;
import java.util.Optional;


public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT c FROM Customer c JOIN FETCH c.user")
    List<Customer> findAllWithUser();

    @Query("SELECT c FROM Customer c JOIN FETCH c.user WHERE LOWER(c.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    List<Customer> findByLastNameContainingIgnoreCase(String lastName);

    boolean existsByJmbg(String jmbg);

    Optional<Customer> findByJmbg(String jmbg);
}
