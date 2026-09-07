package com.redcode.mcms.service;

import com.redcode.mcms.dto.AttendanceDto;
import com.redcode.mcms.dto.EmployeeDto;
import com.redcode.mcms.entity.*;
import com.redcode.mcms.exception.BusinessException;
import com.redcode.mcms.exception.NotFoundException;
import com.redcode.mcms.repository.AttendanceRepository;
import com.redcode.mcms.repository.DepartmentRepository;
import com.redcode.mcms.repository.EmployeeRepository;
import com.redcode.mcms.repository.UserRepository;
import com.redcode.mcms.security.AuthContext;
import com.redcode.mcms.util.PasswordHasher;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Human Resources module: employee records, department assignment, roles and
 * basic attendance. Only HR, managers and administrators may access sensitive
 * employee data (enforced via {@link AuthContext#requireRole}).
 */
@Stateless
public class EmployeeService {

    @Inject
    private EmployeeRepository employeeRepository;

    @Inject
    private DepartmentRepository departmentRepository;

    @Inject
    private AttendanceRepository attendanceRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private NotificationService notificationService;

    @Inject
    private AuditService auditService;

    @Inject
    private AuthContext authContext;

    public List<EmployeeDto> list(String search) {
        authContext.requireRole(AuthContext.RolePermission.HR,
                AuthContext.RolePermission.MANAGER, AuthContext.RolePermission.ADMIN);
        return employeeRepository.search(search).stream().map(this::toDto).collect(Collectors.toList());
    }

    public EmployeeDto find(Long id) {
        authContext.requireRole(AuthContext.RolePermission.HR,
                AuthContext.RolePermission.MANAGER, AuthContext.RolePermission.ADMIN);
        return toDto(getEmployee(id));
    }

    public EmployeeDto create(EmployeeDto dto) {
        authContext.requireRole(AuthContext.RolePermission.HR,
                AuthContext.RolePermission.MANAGER, AuthContext.RolePermission.ADMIN);
        validate(dto);

        Employee e = new Employee();
        apply(dto, e);
        e.setActive(true);
        Employee saved = employeeRepository.save(e);

        // Provision a login account so the employee can access the system
        createUserForEmployee(saved);

        notificationService.notify("EMPLOYEE", "NEW EMPLOYEE",
                "New employee " + saved.getFullName() + " added.", null);
        auditService.log("CREATE", "Employee", "New employee added: " + saved.getFullName());
        return toDto(saved);
    }

    public EmployeeDto update(Long id, EmployeeDto dto) {
        authContext.requireRole(AuthContext.RolePermission.HR,
                AuthContext.RolePermission.MANAGER, AuthContext.RolePermission.ADMIN);
        validate(dto);
        Employee e = getEmployee(id);
        apply(dto, e);
        e.setActive(dto.isActive());
        employeeRepository.update(e);
        auditService.log("UPDATE", "Employee", "Employee " + e.getFullName() + " updated");
        return toDto(e);
    }

    /** Creates a User account (if none exists) so the new employee can log in. */
    private void createUserForEmployee(Employee employee) {
        if (employee.getRole() == null) {
            return;
        }
        String username = employee.getEmail().split("@")[0].toLowerCase();
        if (userRepository.findByUsername(username).isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setPasswordHash(PasswordHasher.hash("Password@123"));
            user.setRole(employee.getRole());
            user.setEmployee(employee);
            user.setActive(true);
            userRepository.save(user);
        }
    }

    private void validate(EmployeeDto dto) {
        if (dto.getFullName() == null || dto.getFullName().isBlank()) {
            throw new BusinessException("Employee full name is required.");
        }
        if (dto.getEmail() == null || !dto.getEmail().contains("@")) {
            throw new BusinessException("A valid employee email is required.");
        }
    }

    private void apply(EmployeeDto dto, Employee e) {
        e.setFullName(dto.getFullName());
        e.setEmail(dto.getEmail());
        e.setPhone(dto.getPhone());
        e.setPosition(dto.getPosition());
        e.setHireDate(dto.getHireDate());
        if (dto.getRole() != null) {
            e.setRole(Role.valueOf(dto.getRole()));
        }
        if (dto.getDepartmentId() != null) {
            e.setDepartment(departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new NotFoundException("Department not found.")));
        }
        if (dto.getHireDate() == null) {
            e.setHireDate(LocalDate.now());
        }
    }

    public List<AttendanceDto> listAttendance(Long employeeId, LocalDate date) {
        authContext.requireRole(AuthContext.RolePermission.HR,
                AuthContext.RolePermission.MANAGER, AuthContext.RolePermission.ADMIN);
        List<Attendance> records;
        if (employeeId != null) {
            records = attendanceRepository.findByEmployee(employeeId);
        } else {
            records = attendanceRepository.findByDate(date == null ? LocalDate.now() : date);
        }
        return records.stream().map(this::toAttendanceDto).collect(Collectors.toList());
    }

    public AttendanceDto recordAttendance(AttendanceDto dto) {
        authContext.requireRole(AuthContext.RolePermission.HR,
                AuthContext.RolePermission.MANAGER, AuthContext.RolePermission.ADMIN);
        Employee employee = getEmployee(dto.getEmployeeId());
        LocalDate day = dto.getDate() == null ? LocalDate.now() : dto.getDate();
        Attendance existing = attendanceRepository.findUnique(employee.getId(), day);
        if (existing != null) {
            existing.setClockIn(dto.getClockIn());
            existing.setClockOut(dto.getClockOut());
            existing.setPresent(dto.isPresent());
            existing.setNotes(dto.getNotes());
            attendanceRepository.update(existing);
            return toAttendanceDto(existing);
        }
        Attendance a = new Attendance();
        a.setEmployee(employee);
        a.setDate(day);
        a.setClockIn(dto.getClockIn());
        a.setClockOut(dto.getClockOut());
        a.setPresent(dto.isPresent());
        a.setNotes(dto.getNotes());
        attendanceRepository.save(a);
        auditService.log("ATTENDANCE", "Attendance", "Attendance recorded for " + employee.getFullName());
        return toAttendanceDto(a);
    }

    private Employee getEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found."));
    }

    public EmployeeDto toDto(Employee e) {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(e.getId());
        dto.setFullName(e.getFullName());
        dto.setEmail(e.getEmail());
        dto.setPhone(e.getPhone());
        dto.setPosition(e.getPosition());
        dto.setHireDate(e.getHireDate());
        dto.setActive(e.isActive());
        if (e.getRole() != null) {
            dto.setRole(e.getRole().name());
        }
        if (e.getDepartment() != null) {
            dto.setDepartmentId(e.getDepartment().getId());
            dto.setDepartmentName(e.getDepartment().getName());
        }
        return dto;
    }

    private AttendanceDto toAttendanceDto(Attendance a) {
        AttendanceDto dto = new AttendanceDto();
        dto.setId(a.getId());
        dto.setEmployeeId(a.getEmployee().getId());
        dto.setEmployeeName(a.getEmployee().getFullName());
        dto.setDate(a.getDate());
        dto.setClockIn(a.getClockIn());
        dto.setClockOut(a.getClockOut());
        dto.setPresent(a.isPresent());
        dto.setNotes(a.getNotes());
        return dto;
    }
}
