package easy.shop.controllers;
import easy.shop.dtos.UpdateUserRequest;
import easy.shop.dtos.UserRequest;
import easy.shop.dtos.UserResponse;
import easy.shop.services.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserManagementService userManagementService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserRequest request) {
        return userManagementService.create(request);
    }

    @GetMapping("/customers")
    public List<UserResponse> getAllCustomers() {
        return userManagementService.getAllCustomers();
    }

    @GetMapping("/customers/search")
    public List<UserResponse> searchCustomers(@RequestParam String q) {
        return userManagementService.searchCustomers(q);
    }

    @GetMapping("/employees/search")
    public List<UserResponse> searchEmployees(@RequestParam String q) {
        return userManagementService.searchEmployees(q);
    }

    @GetMapping("/customers/{profileId}")
    public UserResponse getCustomerById(@PathVariable Long profileId) {
        return userManagementService.getCustomerById(profileId);
    }

    @GetMapping("/customers/by-jmbg/{jmbg}")
    public ResponseEntity<UserResponse> getCustomerByJmbg(@PathVariable String jmbg) {
        return userManagementService.findCustomerByJmbg(jmbg)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/customers/{userId}")
    public UserResponse updateCustomer(@PathVariable Long userId, @RequestBody UpdateUserRequest request) {
        return userManagementService.updateCustomerProfile(userId, request);
    }

}