package easy.shop.mappers;

import easy.shop.dtos.UserRequest;
import easy.shop.dtos.UserResponse;
import easy.shop.entities.Customer;
import easy.shop.entities.Employee;
import easy.shop.entities.UserAccount;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Data
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public UserResponse toCustomerResponse(Customer customer) {
        return UserResponse.builder()
                .userId(customer.getUser().getId())
                .username(customer.getUser().getUsername())
                .role(customer.getUser().getRole())
                .profileId(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phoneNumber(customer.getPhoneNumber())
                .jmbg(customer.getJmbg())
                .address(customer.getAddress())
                .idCardNumber(customer.getIdCardNumber())
                .issuingAuthority(customer.getIssuingAuthority())
                .build();
    }

    public UserResponse toEmployeeResponse(Employee employee) {
        return UserResponse.builder()
                .userId(employee.getUser().getId())
                .username(employee.getUser().getUsername())
                .role(employee.getUser().getRole())
                .profileId(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .phoneNumber(employee.getPhoneNumber())
                .build();
    }

    public UserResponse toUserResponse(Employee employee) {
        return UserResponse.builder()
                .userId(employee.getUser().getId())
                .username(employee.getUser().getUsername())
                .role(employee.getUser().getRole())
                .build();
    }

    public Customer toCustomer(UserRequest request, UserAccount savedUser) {
       return  Customer.builder()
                .user(savedUser)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .jmbg(request.getJmbg())
                .address(request.getAddress())
                .idCardNumber(request.getIdCardNumber())
                .issuingAuthority(request.getIssuingAuthority())
                .build();
    }
    public Employee toEmployee(UserRequest request, UserAccount savedUser) {
        return  Employee.builder()
                .user(savedUser)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .build();
    }

    public UserAccount toUserAccount(UserRequest request) {

        UserAccount userAccount = UserAccount.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
        return userAccount;
    }

}
