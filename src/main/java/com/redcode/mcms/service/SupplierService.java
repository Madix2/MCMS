package com.redcode.mcms.service;

import com.redcode.mcms.dto.SupplierDto;
import com.redcode.mcms.entity.Status;
import com.redcode.mcms.entity.Supplier;
import com.redcode.mcms.exception.BusinessException;
import com.redcode.mcms.exception.NotFoundException;
import com.redcode.mcms.repository.SupplierRepository;
import com.redcode.mcms.security.AuthContext;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Supplier management used by the Procurement module.
 */
@Stateless
public class SupplierService {

    @Inject
    private SupplierRepository supplierRepository;

    @Inject
    private AuditService auditService;

    @Inject
    private AuthContext authContext;

    public List<SupplierDto> list(String search) {
        return supplierRepository.search(search).stream().map(this::toDto).collect(Collectors.toList());
    }

    public SupplierDto find(Long id) {
        return toDto(getSupplier(id));
    }

    public SupplierDto create(SupplierDto dto) {
        authContext.requireRole(AuthContext.RolePermission.PROCUREMENT, AuthContext.RolePermission.ADMIN);
        validate(dto);
        Supplier s = new Supplier();
        apply(dto, s);
        s.setStatus(Status.ACTIVE);
        Supplier saved = supplierRepository.save(s);
        auditService.log("CREATE", "Supplier", "Supplier \"" + saved.getName() + "\" added");
        return toDto(saved);
    }

    public SupplierDto update(Long id, SupplierDto dto) {
        authContext.requireRole(AuthContext.RolePermission.PROCUREMENT, AuthContext.RolePermission.ADMIN);
        validate(dto);
        Supplier s = getSupplier(id);
        apply(dto, s);
        if (dto.getStatus() != null) {
            s.setStatus(Status.valueOf(dto.getStatus()));
        }
        supplierRepository.update(s);
        auditService.log("UPDATE", "Supplier", "Supplier \"" + s.getName() + "\" updated");
        return toDto(s);
    }

    public void deactivate(Long id) {
        authContext.requireRole(AuthContext.RolePermission.PROCUREMENT, AuthContext.RolePermission.ADMIN);
        Supplier s = getSupplier(id);
        s.setStatus(Status.INACTIVE);
        supplierRepository.update(s);
        auditService.log("DEACTIVATE", "Supplier", "Supplier \"" + s.getName() + "\" deactivated");
    }

    private void validate(SupplierDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BusinessException("Supplier name is required.");
        }
    }

    private void apply(SupplierDto dto, Supplier s) {
        s.setName(dto.getName());
        s.setContactPerson(dto.getContactPerson());
        s.setPhone(dto.getPhone());
        s.setEmail(dto.getEmail());
        s.setAddress(dto.getAddress());
    }

    public Supplier getSupplier(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supplier not found."));
    }

    public SupplierDto toDto(Supplier s) {
        SupplierDto dto = new SupplierDto();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setContactPerson(s.getContactPerson());
        dto.setPhone(s.getPhone());
        dto.setEmail(s.getEmail());
        dto.setAddress(s.getAddress());
        dto.setStatus(s.getStatus().name());
        return dto;
    }
}
