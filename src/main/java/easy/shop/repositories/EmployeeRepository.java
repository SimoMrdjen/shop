package easy.shop.repositories;


import easy.shop.entities.Customer;
import easy.shop.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT e FROM Employee e JOIN FETCH e.user")
    List<Employee> findAllWithUser();

    @Query("SELECT e FROM Employee e JOIN FETCH e.user WHERE LOWER(e.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    List<Employee> findByLastNameContainingIgnoreCase(String lastName);
}
