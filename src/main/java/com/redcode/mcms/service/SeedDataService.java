package com.redcode.mcms.service;

import com.redcode.mcms.entity.*;
import com.redcode.mcms.repository.*;
import com.redcode.mcms.util.PasswordHasher;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Populates the database with realistic South African demo data on first start.
 *
 * Runs once only (guarded by checking whether any users exist). Provides:
 *  - All 8 roles with working login accounts
 *  - Departments, 20+ products, multiple suppliers and categories
 *  - Customers, employees, sample sales, purchase orders, payments
 *  - Promotions, campaigns, social posts and notifications
 *
 * Credentials (demo only):
 *   admin / admin123, manager / manager123, sales / sales123, inventory / inventory123,
 *   procurement / procurement123, finance / finance123, hr / hr123, marketing / marketing123
 */
@Singleton
@Startup
@TransactionManagement(TransactionManagementType.BEAN)
public class SeedDataService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private DepartmentRepository departmentRepository;

    @Inject
    private CategoryRepository categoryRepository;

    @Inject
    private SupplierRepository supplierRepository;

    @Inject
    private ProductRepository productRepository;

    @Inject
    private CustomerRepository customerRepository;

    @Inject
    private EmployeeRepository employeeRepository;

    @Inject
    private SaleRepository saleRepository;

    @Inject
    private PurchaseOrderRepository purchaseOrderRepository;

    @Inject
    private PromotionRepository promotionRepository;

    @Inject
    private CampaignRepository campaignRepository;

    @Inject
    private SocialPostRepository socialPostRepository;

    @Inject
    private NotificationRepository notificationRepository;

    @Inject
    private UserTransaction userTransaction;

    private List<Product> products = new ArrayList<>();
    private List<Supplier> suppliers = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();
    private Supplier defaultSupplier;

    @PostConstruct
    public void seed() {
        try {
            if (userRepository.count() > 0) {
                // Already seeded
                return;
            }
            userTransaction.begin();
            try {
                seedDepartments();
                seedCategories();
                seedSuppliers();
                seedEmployeesAndUsers();
                seedProducts();
                seedCustomers();
                seedSales();
                seedPurchaseOrders();
                seedMarketing();
                seedNotifications();
                userTransaction.commit();
            } catch (Exception e) {
                userTransaction.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to seed demo data", e);
        }
    }

    private void seedDepartments() {
        departmentRepository.save(new Department("Sales", "Point of sale and walk-in customers"));
        departmentRepository.save(new Department("Inventory", "Stock control and warehousing"));
        departmentRepository.save(new Department("Procurement", "Suppliers and purchasing"));
        departmentRepository.save(new Department("Finance", "Accounts, revenue and expenses"));
        departmentRepository.save(new Department("Human Resources", "Employees and attendance"));
        departmentRepository.save(new Department("Marketing", "Promotions and campaigns"));
        departmentRepository.save(new Department("Management", "Executive oversight"));
    }

    private void seedCategories() {
        categories.add(categoryRepository.save(new Category("Beverages", "Cold and hot drinks")));
        categories.add(categoryRepository.save(new Category("Dairy & Eggs", "Milk, cheese, eggs")));
        categories.add(categoryRepository.save(new Category("Bakery", "Bread and baked goods")));
        categories.add(categoryRepository.save(new Category("Pantry", "Rice, maize, sugar, oil")));
        categories.add(categoryRepository.save(new Category("Fresh Meat", "Chicken and beef")));
        categories.add(categoryRepository.save(new Category("Fruit & Veg", "Produce")));
        categories.add(categoryRepository.save(new Category("Household", "Soap and cleaning")));
        categories.add(categoryRepository.save(new Category("Personal Care", "Toiletries")));
        categories.add(categoryRepository.save(new Category("Snacks", "Biscuits and treats")));
        categories.add(categoryRepository.save(new Category("Hot Beverages", "Coffee and tea")));
    }

    private void seedSuppliers() {
        Supplier[] arr = {
                s("Coca-Cola Beverages SA", "Thabo Nkosi", "011 555 0101", "orders@ccbsa.co.za", "Gauteng"),
                s("PepsiCo SA", "Lerato Mokoena", "011 555 0102", "sales@pepsico.co.za", "KwaZulu-Natal"),
                s("Clover Dairy", "Pieter van der Merwe", "011 555 0103", "trade@clover.co.za", "Gauteng"),
                s("Sasko Bakeries", "Nomsa Dlamini", "011 555 0104", "orders@sasko.co.za", "Western Cape"),
                s("Tiger Brands", "Sipho Mahlangu", "011 555 0105", "supply@tigerbrands.co.za", "Gauteng"),
                s("Astral Foods", "Johan Botha", "011 555 0106", "b2b@astralfoods.co.za", "Mpumalanga"),
                s("Woolworths Fresh", "Refilwe Kgosi", "011 555 0107", "fresh@woolworths.co.za", "Western Cape"),
                s("Unilever SA", "Bongi Mthembu", "011 555 0108", "corporate@unilever.co.za", "KwaZulu-Natal")
        };
        for (Supplier su : arr) {
            suppliers.add(supplierRepository.save(su));
        }
        defaultSupplier = suppliers.get(0);
    }

    private Supplier s(String name, String cp, String phone, String email, String addr) {
        Supplier su = new Supplier();
        su.setName(name);
        su.setContactPerson(cp);
        su.setPhone(phone);
        su.setEmail(email);
        su.setAddress(addr);
        su.setStatus(Status.ACTIVE);
        return su;
    }

    private void seedEmployeesAndUsers() {
        Department depSales = departmentRepository.findByName("Sales");
        Department depInv = departmentRepository.findByName("Inventory");
        Department depProc = departmentRepository.findByName("Procurement");
        Department depFin = departmentRepository.findByName("Finance");
        Department depHr = departmentRepository.findByName("Human Resources");
        Department depMkt = departmentRepository.findByName("Marketing");
        Department depMgmt = departmentRepository.findByName("Management");

        createUser("admin", "admin123", Role.ADMIN, "System Administrator", depMgmt, "Administrator");
        createUser("manager", "manager123", Role.MANAGER, "Thandi Mabuza", depMgmt, "Store Manager");
        createUser("sales", "sales123", Role.SALES, "Kagiso Ndlovu", depSales, "Sales Associate");
        createUser("sales2", "sales123", Role.SALES, "Yolanda Cele", depSales, "Cashier");
        createUser("inventory", "inventory123", Role.INVENTORY, "Sibusiso Khumalo", depInv, "Inventory Controller");
        createUser("procurement", "procurement123", Role.PROCUREMENT, "Anele Zulu", depProc, "Procurement Officer");
        createUser("finance", "finance123", Role.FINANCE, "Priya Naidoo", depFin, "Accountant");
        createUser("hr", "hr123", Role.HR, "Michelle Potgieter", depHr, "HR Manager");
        createUser("marketing", "marketing123", Role.MARKETING, "Naledi Sithole", depMkt, "Marketing Coordinator");

        employee("Brenda Jacobs", "brenda.jacobs@megamart.co.za", depSales, "Cashier", Role.SALES, "071 222 1101");
        employee("Thabo Mokoena", "thabo.mokoena@megamart.co.za", depInv, "Warehouse Staff", Role.INVENTORY, "071 222 1102");
        employee("Lindiwe Ngcobo", "lindiwe.ngcobo@megamart.co.za", depProc, "Procurement Assistant", Role.PROCUREMENT, "071 222 1103");
        employee("Rajesh Govender", "rajesh.govender@megamart.co.za", depFin, "Junior Accountant", Role.FINANCE, "071 222 1104");
        employee("Zanele Motaung", "zanele.motaung@megamart.co.za", depMkt, "Social Media Specialist", Role.MARKETING, "071 222 1105");
    }

    private void createUser(String username, String password, Role role, String name, Department dep, String position) {
        Employee e = new Employee();
        e.setFullName(name);
        e.setEmail(username + "@megamart.co.za");
        e.setDepartment(dep);
        e.setPosition(position);
        e.setRole(role);
        e.setHireDate(LocalDate.now().minusYears(2));
        e.setActive(true);
        employeeRepository.save(e);

        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(PasswordHasher.hash(password));
        u.setRole(role);
        u.setEmployee(e);
        u.setActive(true);
        userRepository.save(u);
    }

    private void employee(String name, String email, Department dep, String pos, Role role, String phone) {
        Employee e = new Employee();
        e.setFullName(name);
        e.setEmail(email);
        e.setDepartment(dep);
        e.setPosition(pos);
        e.setRole(role);
        e.setPhone(phone);
        e.setHireDate(LocalDate.now().minusMonths(8));
        e.setActive(true);
        employeeRepository.save(e);
    }

    private void seedProducts() {
        p("6001234567890", "Coca-Cola 2L", 0, 26.99, 21.50, 48, 20, 120, cat("Beverages"), suppliers.get(0));
        p("6001234567891", "Pepsi 2L", 1, 24.99, 19.90, 60, 20, 120, cat("Beverages"), suppliers.get(1));
        p("6001234567892", "Full Cream Milk 2L", 2, 38.99, 31.20, 12, 15, 80, cat("Dairy & Eggs"), suppliers.get(2));
        p("6001234567893", "White Bread Loaf", 3, 18.49, 13.00, 45, 10, 90, cat("Bakery"), suppliers.get(3));
        p("6001234567894", "Long Grain Rice 5kg", 4, 89.99, 72.00, 30, 10, 60, cat("Pantry"), suppliers.get(4));
        p("6001234567895", "Maize Meal 5kg", 4, 58.99, 46.50, 35, 10, 70, cat("Pantry"), suppliers.get(4));
        p("6001234567896", "White Sugar 2.5kg", 4, 52.99, 41.00, 28, 10, 60, cat("Pantry"), suppliers.get(4));
        p("6001234567897", "Cooking Oil 2L", 4, 89.99, 71.00, 22, 10, 50, cat("Pantry"), suppliers.get(4));
        p("6001234567898", "Large Eggs Dozen", 1, 45.99, 36.00, 8, 10, 40, cat("Dairy & Eggs"), suppliers.get(2));
        p("6001234567899", "Frozen Chicken 2kg", 5, 119.99, 95.00, 18, 8, 40, cat("Fresh Meat"), suppliers.get(5));
        p("6001234567810", "Beef Mince 500g", 5, 79.99, 62.00, 20, 8, 40, cat("Fresh Meat"), suppliers.get(5));
        p("6001234567811", "Potatoes 5kg Bag", 6, 49.99, 38.00, 40, 10, 80, cat("Fruit & Veg"), suppliers.get(6));
        p("6001234567812", "Red Apples 1kg", 6, 29.99, 22.00, 50, 10, 80, cat("Fruit & Veg"), suppliers.get(6));
        p("6001234567813", "Bananas 1kg", 6, 24.99, 18.00, 14, 10, 70, cat("Fruit & Veg"), suppliers.get(6));
        p("6001234567814", "Dishwashing Liquid 750ml", 7, 27.99, 20.00, 35, 10, 60, cat("Household"), suppliers.get(7));
        p("6001234567815", "Toothpaste 100ml", 8, 32.99, 24.00, 42, 10, 70, cat("Personal Care"), suppliers.get(7));
        p("6001234567816", "Shampoo 400ml", 8, 55.99, 42.00, 25, 8, 50, cat("Personal Care"), suppliers.get(7));
        p("6001234567817", "Chocolate Biscuits 250g", 9, 24.99, 17.00, 55, 10, 90, cat("Snacks"), suppliers.get(4));
        p("6001234567818", "Instant Coffee 500g", 10, 149.99, 118.00, 3, 5, 30, cat("Hot Beverages"), suppliers.get(4));
        p("6001234567819", "Black Tea 250g", 10, 39.99, 30.00, 26, 8, 40, cat("Hot Beverages"), suppliers.get(4));
        p("6001234567820", "Cheddar Cheese 500g", 1, 88.99, 70.00, 16, 5, 30, cat("Dairy & Eggs"), suppliers.get(2));
    }

    private void p(String barcode, String name, int catIdx, double sell, double cost,
                   int qty, int min, int max, Category cat, Supplier sup) {
        Product pr = new Product();
        pr.setBarcode(barcode);
        pr.setName(name);
        pr.setCategory(cat);
        pr.setSellingPrice(BigDecimal.valueOf(sell));
        pr.setCostPrice(BigDecimal.valueOf(cost));
        pr.setQuantity(qty);
        pr.setMinStockLevel(min);
        pr.setMaxStockLevel(max);
        pr.setSupplier(sup);
        pr.setStatus(Status.ACTIVE);
        products.add(productRepository.save(pr));
    }

    private Category cat(String name) {
        return categories.stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
    }

    private void seedCustomers() {
        String[][] data = {
                {"Sipho Dlamini", "sipho.d@gmail.com", "082 111 2233", "12 Voortrekker St, Pretoria"},
                {"Anita Mokoena", "anita.m@yahoo.com", "083 222 3344", "45 Church Rd, Johannesburg"},
                {"David Nkosi", "david.nkosi@outlook.com", "084 333 4455", "8 Long St, Durban"},
                {"Fatima Patel", "fatima.patel@gmail.com", "085 444 5566", "23 Grove Ave, Cape Town"},
                {"George Adams", "george.adams@webmail.co.za", "086 555 6677", "77 Main Rd, Bloemfontein"},
                {"Helen Mavuso", "helen.mavuso@icloud.com", "087 666 7788", "5 Park Lane, East London"},
                {"Isaac Petersen", "isaac.p@protonmail.com", "088 777 8899", "90 Ocean Drive, Port Elizabeth"},
                {"Jade de Villiers", "jade.dv@gmail.com", "081 888 9900", "31 Kerk St, Polokwane"},
                {"Kabelo Sebata", "kabelo.s@outlook.com", "079 999 0011", "64 High St, Rustenburg"},
                {"Lerato Kubayi", "lerato.k@gmail.com", "078 111 2233", "19 Union Ave, Nelspruit"}
        };
        for (String[] row : data) {
            Customer c = new Customer();
            c.setFullName(row[0]);
            c.setEmail(row[1]);
            c.setPhone(row[2]);
            c.setAddress(row[3]);
            c.setLoyaltyPoints((int) (Math.random() * 400));
            c.setRegistrationDate(LocalDateTime.now().minusDays((long) (Math.random() * 300)));
            c.setStatus(Status.ACTIVE);
            customers.add(customerRepository.save(c));
        }
    }

    private void seedSales() {
        // Generate 40 sample sales over the last 30 days so charts and revenue have data.
        int totalSales = 40;
        for (int i = 0; i < totalSales; i++) {
            Sale sale = new Sale();
            sale.setSaleNumber("S-" + (1_000_000 + i));
            sale.setCustomer(customers.get(i % customers.size()));
            sale.setSaleDate(LocalDateTime.now().minusHours((long) (Math.random() * 720))
                    .withNano(0));
            // 1 to 3 random products
            int lineItems = 1 + (int) (Math.random() * 3);
            BigDecimal subtotal = BigDecimal.ZERO;
            for (int j = 0; j < lineItems; j++) {
                Product product = products.get((int) (Math.random() * products.size()));
                int qty = 1 + (int) (Math.random() * 3);
                SaleItem item = new SaleItem();
                item.setSale(sale);
                item.setProduct(product);
                item.setQuantity(qty);
                item.setUnitPrice(product.getSellingPrice());
                BigDecimal lineTotal = product.getSellingPrice().multiply(BigDecimal.valueOf(qty));
                item.setLineTotal(lineTotal);
                subtotal = subtotal.add(lineTotal);
                sale.getItems().add(item);
            }
            BigDecimal tax = subtotal.multiply(new BigDecimal("0.15")).setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal total = subtotal.add(tax).setScale(2, java.math.RoundingMode.HALF_UP);
            sale.setSubtotal(subtotal.setScale(2, java.math.RoundingMode.HALF_UP));
            sale.setTax(tax);
            sale.setTotal(total);
            sale.setAmountTendered(total);
            sale.setChangeGiven(BigDecimal.ZERO);
            sale.setStatus(Status.COMPLETED);
            saleRepository.save(sale);

            Payment payment = new Payment();
            PaymentMethod method = PaymentMethod.values()[(int) (Math.random() * PaymentMethod.values().length)];
            payment.setMethod(method);
            payment.setAmount(total);
            payment.setSale(sale);
            payment.setReference("PAY-" + sale.getSaleNumber());
            sale.setPayment(payment);
            saleRepository.update(sale);

            if (sale.getCustomer() != null) {
                int pts = total.divideToIntegralValue(BigDecimal.TEN).intValue();
                sale.getCustomer().setLoyaltyPoints(sale.getCustomer().getLoyaltyPoints() + pts);
                customerRepository.update(sale.getCustomer());
            }
        }
    }

    private void seedPurchaseOrders() {
        // A sample low-stock purchase order for Coffee (product id will be looked up by name)
        Product coffee = products.stream().filter(p -> p.getName().contains("Coffee")).findFirst().orElse(products.get(0));
        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber("PO-100001");
        po.setSupplier(defaultSupplier);
        po.setStatus(PurchaseOrderStatus.PENDING_APPROVAL);
        po.setNotes("Low stock top-up");
        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setPurchaseOrder(po);
        item.setProduct(coffee);
        item.setQuantity(50);
        item.setUnitCost(coffee.getCostPrice());
        item.setLineTotal(coffee.getCostPrice().multiply(BigDecimal.valueOf(50)).setScale(2, java.math.RoundingMode.HALF_UP));
        po.getItems().add(item);
        po.setTotal(item.getLineTotal());
        purchaseOrderRepository.save(po);
    }

    private void seedMarketing() {
        Promotion p1 = new Promotion();
        p1.setName("Winter Warmers");
        p1.setDescription("10% off coffee and tea");
        p1.setStartDate(LocalDate.now().minusDays(3));
        p1.setEndDate(LocalDate.now().plusDays(27));
        p1.setDiscount(new BigDecimal("10"));
        p1.setCategory(cat("Hot Beverages"));
        p1.setStatus(PromotionStatus.ACTIVE);
        promotionRepository.save(p1);

        Promotion p2 = new Promotion();
        p2.setName("Back to School Snacks");
        p2.setDescription("15% off biscuits");
        p2.setStartDate(LocalDate.now().plusDays(10));
        p2.setEndDate(LocalDate.now().plusDays(40));
        p2.setDiscount(new BigDecimal("15"));
        p2.setCategory(cat("Snacks"));
        p2.setStatus(PromotionStatus.SCHEDULED);
        promotionRepository.save(p2);

        Campaign c1 = new Campaign();
        c1.setName("MegaMart Fresh July");
        c1.setDescription("Monthly fresh food campaign");
        c1.setStartDate(LocalDate.now().withDayOfMonth(1));
        c1.setEndDate(LocalDate.now().with(java.time.temporal.TemporalAdjusters.lastDayOfMonth()));
        c1.setBudget(new BigDecimal("25000"));
        c1.setStatus(PromotionStatus.ACTIVE);
        campaignRepository.save(c1);

        post("Facebook", c1, "Fresh produce at unbeatable prices this month! #MegaMartFresh",
                LocalDateTime.now().minusDays(2), 154, 32, 48, 3200);
        post("Instagram", c1, "Look at today's specials at your local MegaMart 🛒",
                LocalDateTime.now().minusDays(1), 401, 22, 65, 5100);
        post("Twitter", c1, "Winter warmers are here. 10% off all hot beverages!",
                LocalDateTime.now().minusHours(5), 88, 14, 29, 1800);
        post("Facebook", c1, "Family braai essentials on special this weekend!",
                LocalDateTime.now().plusHours(10), 0, 0, 0, 0);
    }

    private void post(String platform, Campaign campaign, String content, LocalDateTime at,
                      int likes, int shares, int comments, int reach) {
        SocialPost sp = new SocialPost();
        sp.setPlatform(platform);
        sp.setCampaign(campaign);
        sp.setContent(content);
        sp.setStatus(at.isBefore(LocalDateTime.now()) ? "PUBLISHED" : "SCHEDULED");
        sp.setScheduledAt(at);
        sp.setLikes(likes);
        sp.setShares(shares);
        sp.setComments(comments);
        sp.setReach(reach);
        socialPostRepository.save(sp);
    }

    private void seedNotifications() {
        notificationRepository.save(n("LOW_STOCK", "LOW STOCK",
                "Instant Coffee 500g has reached its minimum stock level. Current: 3, Minimum: 5.", "PROCUREMENT"));
        notificationRepository.save(n("LOW_STOCK", "LOW STOCK",
                "Large Eggs Dozen has reached its minimum stock level. Current: 8, Minimum: 10.", "PROCUREMENT"));
        notificationRepository.save(n("PO_APPROVAL", "PURCHASE ORDER",
                "Purchase Order PO-100001 requires approval.", "MANAGER"));
        notificationRepository.save(n("SALE", "SALE COMPLETED",
                "Welcome to MegaMart - your shopping is recorded.", null));
        notificationRepository.save(n("EMPLOYEE", "NEW EMPLOYEE",
                "New employee Brenda Jacobs added.", null));
        notificationRepository.save(n("PROMOTION", "PROMOTION",
                "Promotion \"Winter Warmers\" is now active.", null));
    }

    private Notification n(String type, String title, String message, String role) {
        Notification n = new Notification();
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setTargetRole(role);
        return n;
    }
}
