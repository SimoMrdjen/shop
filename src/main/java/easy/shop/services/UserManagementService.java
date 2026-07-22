package easy.shop.services;

import easy.shop.dtos.UpdateUserRequest;
import easy.shop.dtos.UserRequest;
import easy.shop.dtos.UserResponse;
import easy.shop.entities.Customer;
import easy.shop.entities.Employee;
import easy.shop.entities.Role;
import easy.shop.entities.UserAccount;
import easy.shop.exceptions.BadRequestException;
import easy.shop.mappers.UserMapper;
import easy.shop.repositories.CustomerRepository;
import easy.shop.repositories.EmployeeRepository;
import easy.shop.repositories.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static easy.shop.entities.Role.CUSTOMER;
import static easy.shop.entities.Role.EMPLOYEE;

@Service
@RequiredArgsConstructor
public class UserManagementService {
    private final UserAccountRepository userAccountRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse create(UserRequest request) {


        validateBusinessRulesForCreate(request);

        UserAccount savedUser = userAccountRepository.save(userMapper.toUserAccount(request));

        return switch (request.getRole()) {
            case CUSTOMER -> createCustomer(savedUser, request);
            case EMPLOYEE -> createEmployee(savedUser, request);
            case ADMIN -> UserResponse.builder().userId(savedUser.getId())
                    .role(savedUser.getRole())
                    .build();
        };
    }

    private UserResponse createEmployee(UserAccount savedUser, UserRequest request) {

        Employee employee = userMapper.toEmployee(request, savedUser);
        Employee savedEmployee = employeeRepository.save(employee);
        return userMapper.toEmployeeResponse(savedEmployee);
    }

    private UserResponse createCustomer(UserAccount savedUser, UserRequest request) {

        Customer customer = userMapper.toCustomer(request, savedUser);
        Customer savedCustomer = customerRepository.save(customer);
        return userMapper.toCustomerResponse(savedCustomer);
    }

    private void validateBusinessRulesForCreate(UserRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Request must not be null.");
        }
        if (requiresProfile(request.getRole())) {
            if (isBlank(request.getFirstName())) {
                throw new IllegalArgumentException("Ime je obavezno !");
            }
            if (isBlank(request.getLastName())) {
                throw new IllegalArgumentException("Prezime je obavezno !");
            }
        }
        if (request.getRole() == CUSTOMER) {
            if (isBlank(request.getJmbg())) {
                throw new IllegalArgumentException("JMBG je obavezan!");
            } else if (request.getJmbg().length() != 13) {
                throw new IllegalArgumentException("JMBG mora sadrzati 13 brojeva!");
            } else if (customerRepository.existsByJmbg(request.getJmbg())) {
                throw new IllegalArgumentException("Kupac sa ovim JMBG-om već postoji: " + request.getJmbg());
            } else if (userAccountRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username vec postoji: " + request.getUsername());
            }
        }
        if (request.getRole() != CUSTOMER) {
            if (isBlank(request.getUsername())) {
                throw new IllegalArgumentException("Username je obavezan!");
            } else if (userAccountRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username vec postoji: " + request.getUsername());
            }
        }

    }

    private boolean requiresProfile(Role role) {
        return role == CUSTOMER || role == EMPLOYEE;
    }

    boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Transactional(readOnly = true)
    public Optional<UserResponse> findCustomerByJmbg(String jmbg) {
        return customerRepository.findByJmbg(jmbg).map(userMapper::toCustomerResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getCustomerById(Long profileId) {
        Customer customer = customerRepository.findById(profileId)
                .orElseThrow(() -> new BadRequestException("Kupac nije pronađen: " + profileId));
        return userMapper.toCustomerResponse(customer);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllCustomers() {

        return customerRepository.findAllWithUser().stream()
                .map(userMapper::toCustomerResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> searchCustomers(String term) {

        return customerRepository.findByLastNameContainingIgnoreCase(term)
                .stream()
                .map(userMapper::toCustomerResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> searchEmployees(String term) {
        return employeeRepository
                .findByLastNameContainingIgnoreCase(term)
                .stream()
                .map(userMapper::toEmployeeResponse)
                .toList();
    }

    @Transactional
    public UserResponse updateCustomerProfile(Long userId, UpdateUserRequest request) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Korisnik nije pronađen: " + userId));

        Customer customer = Optional.ofNullable(user.getCustomer())
                .orElseThrow(() -> new BadRequestException("Profil kupca nije pronađen za userId: " + userId));

        validateJmbgNotTaken(request.getJmbg(), customer.getId());

        setIfPresent(request.getFirstName(), customer::setFirstName);
        setIfPresent(request.getLastName(), customer::setLastName);
        setIfPresent(request.getPhoneNumber(), customer::setPhoneNumber);
        setIfPresent(request.getJmbg(), customer::setJmbg);
        setIfPresent(request.getAddress(), customer::setAddress);
        setIfPresent(request.getIdCardNumber(), customer::setIdCardNumber);
        setIfPresent(request.getIssuingAuthority(), customer::setIssuingAuthority);

        return userMapper.toCustomerResponse(customerRepository.save(customer));
    }

    private void validateJmbgNotTaken(String jmbg, Long currentCustomerId) {
        if (jmbg == null || jmbg.isBlank()) {
            return;
        }
        customerRepository.findByJmbg(jmbg)
                .filter(existing -> !existing.getId().equals(currentCustomerId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Kupac sa ovim JMBG-om već postoji: " + jmbg);
                });
    }

    @Transactional
    public UserResponse update(Long userId, UserRequest request) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

        Role currentRole = user.getRole();
        Role targetRole = Optional.ofNullable(request.getRole()).orElse(currentRole);

        if (currentRole != targetRole) {
            throw new BadRequestException("Changing role through update is not supported.");
        }
        if (targetRole == CUSTOMER) {
            return updateCustomer(user, request);
        }
        if (targetRole == EMPLOYEE) {
            return updateEmployee(user, request);
        }
        throw new IllegalArgumentException("Unsupported role: " + targetRole);
    }


    private UserResponse updateEmployee(UserAccount user, UserRequest request) {

        Employee employee = Optional.ofNullable(user.getEmployee())
                .orElseThrow(() -> new BadRequestException("Employee profile not found for user id: " + user.getId()));

        setIfPresent(request.getFirstName(), employee::setFirstName);
        setIfPresent(request.getLastName(), employee::setLastName);
        setIfPresent(request.getPhoneNumber(), employee::setPhoneNumber);

        return userMapper.toEmployeeResponse(employeeRepository.save(employee));
    }

    private UserResponse updateCustomer(UserAccount user, UserRequest request) {

        Customer customer = Optional.ofNullable(user.getCustomer())
                .orElseThrow(() -> new BadRequestException("Customer profile not found for user id: " + user.getId()));

        validateJmbgNotTaken(request.getJmbg(), customer.getId());

        setIfPresent(request.getFirstName(), customer::setFirstName);
        setIfPresent(request.getLastName(), customer::setLastName);
        setIfPresent(request.getPhoneNumber(), customer::setPhoneNumber);
        setIfPresent(request.getJmbg(), customer::setJmbg);
        setIfPresent(request.getAddress(), customer::setAddress);
        setIfPresent(request.getIdCardNumber(), customer::setIdCardNumber);
        setIfPresent(request.getIssuingAuthority(), customer::setIssuingAuthority);

        return userMapper.toCustomerResponse(customerRepository.save(customer));
    }

    private void setIfPresent(String value, Consumer<String> setter) {
        Optional.ofNullable(value)
                .filter(v -> !v.isBlank())
                .ifPresent(setter);
    }
}
