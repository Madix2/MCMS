package com.redcode.mcms.service;

import com.redcode.mcms.dto.CustomerDto;
import com.redcode.mcms.entity.Customer;
import com.redcode.mcms.entity.Status;
import com.redcode.mcms.exception.BusinessException;
import com.redcode.mcms.exception.NotFoundException;
import com.redcode.mcms.repository.CustomerRepository;
import com.redcode.mcms.security.AuthContext;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Customer management: registration, profile editing, loyalty points and status.
 */
@Stateless
public class CustomerService {

    @Inject
    private CustomerRepository customerRepository;

    @Inject
    private AuditService auditService;

    @Inject
    private AuthContext authContext;

    public List<CustomerDto> list(String search) {
        return customerRepository.search(search).stream().map(this::toDto).collect(Collectors.toList());
    }

    public CustomerDto find(Long id) {
        return toDto(getCustomer(id));
    }

    public CustomerDto create(CustomerDto dto) {
        authContext.requireRole(AuthContext.RolePermission.SALES,
                AuthContext.RolePermission.MARKETING, AuthContext.RolePermission.MANAGER,
                AuthContext.RolePermission.ADMIN);
        validate(dto);
        if (customerRepository.search(dto.getEmail()).stream()
                .anyMatch(c -> c.getEmail().equalsIgnoreCase(dto.getEmail()))) {
            throw new BusinessException("A customer with this email already exists.");
        }
        Customer c = new Customer();
        apply(dto, c);
        c.setLoyaltyPoints(0);
        c.setStatus(Status.ACTIVE);
        Customer saved = customerRepository.save(c);
        auditService.log("CREATE", "Customer", "Customer \"" + saved.getFullName() + "\" registered");
        return toDto(saved);
    }

    public CustomerDto update(Long id, CustomerDto dto) {
        validate(dto);
        Customer c = getCustomer(id);
        apply(dto, c);
        customerRepository.update(c);
        auditService.log("UPDATE", "Customer", "Customer \"" + c.getFullName() + "\" updated");
        return toDto(c);
    }

    public void deactivate(Long id) {
        Customer c = getCustomer(id);
        c.setStatus(Status.INACTIVE);
        customerRepository.update(c);
        auditService.log("DEACTIVATE", "Customer", "Customer \"" + c.getFullName() + "\" deactivated");
    }

    private void validate(CustomerDto dto) {
        if (dto.getFullName() == null || dto.getFullName().isBlank()) {
            throw new BusinessException("Customer full name is required.");
        }
        if (dto.getEmail() == null || !dto.getEmail().contains("@")) {
            throw new BusinessException("A valid customer email is required.");
        }
    }

    private void apply(CustomerDto dto, Customer c) {
        c.setFullName(dto.getFullName());
        c.setEmail(dto.getEmail());
        c.setPhone(dto.getPhone());
        c.setAddress(dto.getAddress());
    }

    public Customer getCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found."));
    }

    public CustomerDto toDto(Customer c) {
        CustomerDto dto = new CustomerDto();
        dto.setId(c.getId());
        dto.setFullName(c.getFullName());
        dto.setEmail(c.getEmail());
        dto.setPhone(c.getPhone());
        dto.setAddress(c.getAddress());
        dto.setLoyaltyPoints(c.getLoyaltyPoints());
        dto.setRegistrationDate(c.getRegistrationDate());
        dto.setStatus(c.getStatus().name());
        return dto;
    }
}
