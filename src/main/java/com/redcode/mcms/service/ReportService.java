package com.redcode.mcms.service;

import com.redcode.mcms.entity.*;
import com.redcode.mcms.repository.*;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Reporting module. Produces tabular report data for each department and can
 * render it as CSV for export. Reports support date filtering.
 */
@Stateless
public class ReportService {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private SaleRepository saleRepository;

    @Inject
    private ProductRepository productRepository;

    @Inject
    private PurchaseOrderRepository purchaseOrderRepository;

    @Inject
    private SupplierRepository supplierRepository;

    @Inject
    private CustomerRepository customerRepository;

    @Inject
    private EmployeeRepository employeeRepository;

    @Inject
    private PromotionRepository promotionRepository;

    @Inject
    private StockMovementRepository stockMovementRepository;

    /** Columns + rows pair for a report. */
    public static class Report {
        public List<String> columns;
        public List<List<String>> rows;
        public Report(List<String> columns, List<List<String>> rows) {
            this.columns = columns;
            this.rows = rows;
        }
    }

    public Report salesReport(LocalDate from, LocalDate to) {
        List<String> columns = List.of("Sale Number", "Date", "Customer", "Subtotal", "VAT", "Total", "Payment");
        List<List<String>> rows = new ArrayList<>();
        for (Sale s : saleRepository.findAllOrderedDesc()) {
            LocalDate d = s.getSaleDate().toLocalDate();
            if ((from == null || !d.isBefore(from)) && (to == null || !d.isAfter(to))) {
                String cust = s.getCustomer() != null ? s.getCustomer().getFullName() : "Walk-in";
                String pay = s.getPayment() != null ? s.getPayment().getMethod().name() : "";
                rows.add(List.of(s.getSaleNumber(), s.getSaleDate().toString(), cust,
                        money(s.getSubtotal()), money(s.getTax()), money(s.getTotal()), pay));
            }
        }
        return new Report(columns, rows);
    }

    public Report inventoryReport() {
        return new Report(
                List.of("Barcode", "Name", "Category", "Quantity", "Min", "Max", "Selling", "Cost", "Status"),
                productRepository.findAll().stream()
                        .map(p -> List.of(nullTo(p.getBarcode()), p.getName(),
                                p.getCategory() != null ? p.getCategory().getName() : "",
                                Integer.toString(p.getQuantity()),
                                Integer.toString(p.getMinStockLevel()),
                                Integer.toString(p.getMaxStockLevel()),
                                money(p.getSellingPrice()), money(p.getCostPrice()), p.getStatus().name()))
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
    }

    public Report lowStockReport() {
        return new Report(
                List.of("Name", "Category", "Quantity", "Minimum", "Shortfall"),
                productRepository.findLowStock().stream()
                        .map(p -> List.of(p.getName(),
                                p.getCategory() != null ? p.getCategory().getName() : "",
                                Integer.toString(p.getQuantity()),
                                Integer.toString(p.getMinStockLevel()),
                                Integer.toString(p.getMinStockLevel() - p.getQuantity())))
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
    }

    public Report procurementReport() {
        return new Report(
                List.of("PO Number", "Date", "Supplier", "Status", "Total", "Approved By"),
                purchaseOrderRepository.findAllOrderedDesc().stream()
                        .map(po -> List.of(po.getPoNumber(), po.getOrderDate().toString(),
                                po.getSupplier() != null ? po.getSupplier().getName() : "",
                                po.getStatus().name(), money(po.getTotal()), nullTo(po.getApprovedBy())))
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
    }

    public Report supplierReport() {
        return new Report(
                List.of("Name", "Contact Person", "Phone", "Email", "Address", "Status"),
                supplierRepository.findAll().stream()
                        .map(s -> List.of(s.getName(), nullTo(s.getContactPerson()),
                                nullTo(s.getPhone()), nullTo(s.getEmail()), nullTo(s.getAddress()),
                                s.getStatus().name()))
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
    }

    public Report customerReport() {
        return new Report(
                List.of("Name", "Email", "Phone", "Address", "Loyalty Points", "Status"),
                customerRepository.findAll().stream()
                        .map(c -> List.of(c.getFullName(), c.getEmail(), nullTo(c.getPhone()),
                                nullTo(c.getAddress()), Integer.toString(c.getLoyaltyPoints()),
                                c.getStatus().name()))
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
    }

    public Report employeeReport() {
        return new Report(
                List.of("Name", "Email", "Department", "Position", "Role", "Hire Date", "Status"),
                employeeRepository.findAll().stream()
                        .map(e -> List.of(e.getFullName(), e.getEmail(),
                                e.getDepartment() != null ? e.getDepartment().getName() : "",
                                nullTo(e.getPosition()), e.getRole() != null ? e.getRole().name() : "",
                                e.getHireDate() != null ? e.getHireDate().toString() : "",
                                e.isActive() ? "ACTIVE" : "INACTIVE"))
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
    }

    public Report financialReport(LocalDate from, LocalDate to) {
        Report salesRep = salesReport(from, to);
        Report procurementRep = procurementReport();
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("Metric", "Value"));
        rows.add(List.of("Total Sales Revenue", money(salesRevenueInRange(from, to))));
        rows.add(List.of("Total Number of Sales", Long.toString(countSalesInRange(from, to))));
        rows.add(List.of("Total Cost of Received POs", money(poCostInRange(from, to))));
        rows.add(List.of("Net Revenue (sales - po cost)", netRevenue(from, to)));
        List<String> columns = List.of("Metric", "Value");
        return new Report(columns, rows);
    }

    public Report promotionReport() {
        return new Report(
                List.of("Name", "Discount %", "Start", "End", "Product", "Category", "Status"),
                promotionRepository.findAllOrderedDesc().stream()
                        .map(p -> List.of(p.getName(), p.getDiscount().toPlainString(),
                                p.getStartDate() != null ? p.getStartDate().toString() : "",
                                p.getEndDate() != null ? p.getEndDate().toString() : "",
                                p.getProduct() != null ? p.getProduct().getName() : "",
                                p.getCategory() != null ? p.getCategory().getName() : "",
                                p.getStatus().name()))
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
    }

    public Report stockMovementReport() {
        return new Report(
                List.of("Date", "Product", "Type", "Qty Changed", "Balance", "Reference"),
                stockMovementRepository.findRecent(500).stream()
                        .map(m -> List.of(m.getCreatedAt().toString(), m.getProduct().getName(),
                                m.getType().name(), Integer.toString(m.getQuantityChanged()),
                                Integer.toString(m.getBalanceAfter()), m.getReference()))
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
    }

    // ---------- CSV export ----------

    public String toCsv(Report report) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", escapeAll(report.columns))).append("\n");
        for (List<String> row : report.rows) {
            sb.append(String.join(",", escapeAll(row))).append("\n");
        }
        return sb.toString();
    }

    private List<String> escapeAll(List<String> values) {
        List<String> escaped = new ArrayList<>();
        for (String v : values) {
            String s = v == null ? "" : v;
            if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
                s = "\"" + s.replace("\"", "\"\"") + "\"";
            }
            escaped.add(s);
        }
        return escaped;
    }

    private BigDecimal salesRevenueInRange(LocalDate from, LocalDate to) {
        return saleRepository.sumRevenueBetween(
                from == null ? LocalDate.of(2000, 1, 1).atStartOfDay() : from.atStartOfDay(),
                to == null ? LocalDate.now().plusDays(1).atStartOfDay() : to.plusDays(1).atStartOfDay());
    }

    private long countSalesInRange(LocalDate from, LocalDate to) {
        return saleRepository.countBetween(
                from == null ? LocalDate.of(2000, 1, 1).atStartOfDay() : from.atStartOfDay(),
                to == null ? LocalDate.now().plusDays(1).atStartOfDay() : to.plusDays(1).atStartOfDay());
    }

    private BigDecimal poCostInRange(LocalDate from, LocalDate to) {
        Object result = em.createQuery(
                        "SELECT COALESCE(SUM(po.total), 0) FROM PurchaseOrder po "
                                + "WHERE po.receivedAt IS NOT NULL AND po.receivedAt >= :start",
                        BigDecimal.class)
                .setParameter("start", LocalDateTime.of(2000, 1, 1, 0, 0))
                .getSingleResult();
        return (BigDecimal) result;
    }

    private String netRevenue(LocalDate from, LocalDate to) {
        return money(salesRevenueInRange(from, to).subtract(poCostInRange(from, to)));
    }

    private String money(BigDecimal value) {
        return value == null ? "0.00" : value.toPlainString();
    }

    private String nullTo(String value) {
        return value == null ? "" : value;
    }
}
