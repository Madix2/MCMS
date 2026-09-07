/* MegaMart Central Management System - Single Page Application */
(function () {
    'use strict';

    /* ---------------- Navigation definition ---------------- */
    const NAV = [
        { key: 'dashboard', label: 'Dashboard', icon: '🏠', roles: ['ADMIN','MANAGER','SALES','INVENTORY','PROCUREMENT','FINANCE','HR','MARKETING'] },
        { label: 'Sales', icon: '🛒', group: true, roles: ['ADMIN','MANAGER','SALES'] },
        { key: 'new-sale', label: 'New Sale', icon: '➕', roles: ['ADMIN','MANAGER','SALES'] },
        { key: 'sales', label: 'Sales History', icon: '🧾', roles: ['ADMIN','MANAGER','SALES'] },
        { label: 'Inventory', icon: '📦', group: true, roles: ['ADMIN','MANAGER','INVENTORY'] },
        { key: 'products', label: 'Products', icon: '🏷️', roles: ['ADMIN','MANAGER','INVENTORY'] },
        { key: 'low-stock', label: 'Low Stock', icon: '⚠️', roles: ['ADMIN','MANAGER','INVENTORY','PROCUREMENT'] },
        { key: 'movements', label: 'Stock Movements', icon: '🔄', roles: ['ADMIN','MANAGER','INVENTORY'] },
        { label: 'Procurement', icon: '📦', group: true, roles: ['ADMIN','MANAGER','PROCUREMENT'] },
        { key: 'suppliers', label: 'Suppliers', icon: '🏢', roles: ['ADMIN','MANAGER','PROCUREMENT'] },
        { key: 'purchase-orders', label: 'Purchase Orders', icon: '📋', roles: ['ADMIN','MANAGER','PROCUREMENT'] },
        { key: 'approvals', label: 'Approvals', icon: '✅', roles: ['ADMIN','MANAGER'] },
        { label: 'Finance', icon: '💰', group: true, roles: ['ADMIN','MANAGER','FINANCE'] },
        { key: 'finance', label: 'Finance Dashboard', icon: '📊', roles: ['ADMIN','MANAGER','FINANCE'] },
        { label: 'Customers', icon: '👥', group: true, roles: ['ADMIN','MANAGER','SALES','MARKETING'] },
        { key: 'customers', label: 'Customers', icon: '👤', roles: ['ADMIN','MANAGER','SALES','MARKETING'] },
        { label: 'Human Resources', icon: '🧑‍💼', group: true, roles: ['ADMIN','MANAGER','HR'] },
        { key: 'employees', label: 'Employees', icon: '🧑‍💼', roles: ['ADMIN','MANAGER','HR'] },
        { key: 'attendance', label: 'Attendance', icon: '🕐', roles: ['ADMIN','MANAGER','HR'] },
        { label: 'Marketing', icon: '📣', group: true, roles: ['ADMIN','MANAGER','MARKETING'] },
        { key: 'promotions', label: 'Promotions', icon: '🎉', roles: ['ADMIN','MANAGER','MARKETING'] },
        { key: 'campaigns', label: 'Campaigns', icon: '📣', roles: ['ADMIN','MANAGER','MARKETING'] },
        { key: 'social', label: 'Social Media', icon: '🕸️', roles: ['ADMIN','MANAGER','MARKETING'] },
        { key: 'reports', label: 'Reports', icon: '📈', roles: ['ADMIN','MANAGER','FINANCE','INVENTORY','PROCUREMENT'] },
        { key: 'audit', label: 'Audit Logs', icon: '📜', roles: ['ADMIN','MANAGER'] },
        { key: 'settings', label: 'Settings', icon: '⚙️', roles: ['ADMIN','MANAGER','SALES','INVENTORY','PROCUREMENT','FINANCE','HR','MARKETING'] }
    ];

    const TITLES = {
        'dashboard': 'Dashboard', 'new-sale': 'New Sale', 'sales': 'Sales History',
        'products': 'Products', 'low-stock': 'Low Stock', 'movements': 'Stock Movements',
        'suppliers': 'Suppliers', 'purchase-orders': 'Purchase Orders', 'approvals': 'Purchase Order Approvals',
        'finance': 'Finance Dashboard', 'customers': 'Customers',
        'employees': 'Employees', 'attendance': 'Attendance',
        'promotions': 'Promotions', 'campaigns': 'Campaigns', 'social': 'Social Media',
        'reports': 'Reports', 'audit': 'Audit Logs', 'settings': 'Settings'
    };

    let currentRoute = 'dashboard';
    let cart = [];

    const $ = (id) => document.getElementById(id);

    /* ---------------- Login ---------------- */
    function initLogin() {
        if (Api.user()) {
            showApp();
            return;
        }
        $('login-screen').classList.remove('hidden');
        $('app').classList.add('hidden');

        $('login-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = $('login-btn');
            btn.disabled = true; btn.textContent = 'Signing in...';
            $('login-error').classList.add('hidden');
            try {
                const data = await Api.post('/auth/login', {
                    username: $('username').value.trim(),
                    password: $('password').value
                });
                Api.setSession(data.token, { username: data.username, role: data.role, displayName: data.displayName });
                showApp();
            } catch (err) {
                const el = $('login-error');
                el.textContent = err.message;
                el.classList.remove('hidden');
            } finally {
                btn.disabled = false; btn.textContent = 'Sign In';
            }
        });
    }

    function loggedIn() { return !!Api.user(); }

    /* ---------------- App shell ---------------- */
    function showApp() {
        $('login-screen').classList.add('hidden');
        $('app').classList.remove('hidden');
        renderSidebar();
        bindShellEvents();
        navigate('dashboard');
    }

    function renderSidebar() {
        const nav = $('sidebar-nav');
        const roles = Api.user().role;
        let html = '';
        NAV.forEach(item => {
            if (!item.roles.includes(roles) && roles !== 'ADMIN' && !roles.includes && !item.roles.includes(roles)) return;
            if (item.group) {
                html += `<div class="nav-group-label">${item.label}</div>`;
            } else {
                html += `<a class="nav-item" data-route="${item.key}"><span class="ico">${item.icon}</span>${item.label}</a>`;
            }
        });
        // simpler authoritative role filter
        nav.innerHTML = html;
        nav.querySelectorAll('.nav-item').forEach(el => {
            const item = NAV.find(n => n.key === el.dataset.route);
            if (item && !item.roles.includes(roles)) { el.remove(); return; }
            el.addEventListener('click', () => navigate(el.dataset.route));
        });

        const u = Api.user();
        $('user-name-top').textContent = u.displayName || u.username;
        $('user-role').textContent = u.role;
        $('sidebar-user').innerHTML = `<strong>${Api.esc(u.username)}</strong><br>${Api.esc(u.displayName || '')}`;
    }

    function bindShellEvents() {
        $('logout-btn').onclick = () => Api.logout();
        $('hamburger').onclick = () => $('sidebar').classList.toggle('open');
        $('notif-btn').onclick = () => { $('notif-drawer').classList.remove('hidden'); $('drawer-backdrop').classList.remove('hidden'); loadDrawerNotifications(); };
        $('notif-close').onclick = closeDrawer;
        $('drawer-backdrop').onclick = closeDrawer;
    }
    function closeDrawer() {
        $('notif-drawer').classList.add('hidden');
        $('drawer-backdrop').classList.add('hidden');
    }

    /* ---------------- Navigation ---------------- */
    function navigate(route) {
        currentRoute = route;
        $('page-title').textContent = TITLES[route] || 'Dashboard';
        document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
        const active = document.querySelector(`.nav-item[data-route="${route}"]`);
        if (active) active.classList.add('active');

        Api.resetCharts();

        const view = $('view');
        const loader = $('loader');
        const load = (fn) => {
            loader.classList.remove('hidden');
            view.innerHTML = '';
            Promise.resolve().then(fn)
                .catch(err => view.innerHTML = `<div class="card"><div class="card-body empty"><div class="big">⚠️</div>${Api.esc(err.message)}</div></div>`)
                .finally(() => loader.classList.add('hidden'));
        };

        switch (route) {
            case 'dashboard': load(() => viewDashboard(view)); break;
            case 'new-sale': load(() => viewNewSale(view)); break;
            case 'sales': load(() => viewSales(view)); break;
            case 'products': load(() => viewProducts(view)); break;
            case 'low-stock': load(() => viewLowStock(view)); break;
            case 'movements': load(() => viewMovements(view)); break;
            case 'suppliers': load(() => viewSuppliers(view)); break;
            case 'purchase-orders': load(() => viewPurchaseOrders(view)); break;
            case 'approvals': load(() => viewApprovals(view)); break;
            case 'finance': load(() => viewFinance(view)); break;
            case 'customers': load(() => viewCustomers(view)); break;
            case 'employees': load(() => viewEmployees(view)); break;
            case 'attendance': load(() => viewAttendance(view)); break;
            case 'promotions': load(() => viewPromotions(view)); break;
            case 'campaigns': load(() => viewCampaigns(view)); break;
            case 'social': load(() => viewSocial(view)); break;
            case 'reports': load(() => viewReports(view)); break;
            case 'audit': load(() => viewAudit(view)); break;
            case 'settings': load(() => viewSettings(view)); break;
            default: viewDashboard(view);
        }
    }

    /* ================= DASHBOARD ================= */
    async function viewDashboard(view) {
        const [summary, topProducts, invByCat, revenue, activity, notifs] = await Promise.all([
            Api.get('/dashboard/summary'),
            Api.get('/dashboard/top-products'),
            Api.get('/dashboard/inventory-by-category'),
            Api.get('/dashboard/revenue-trend?days=14'),
            Api.get('/dashboard/activity?limit=8'),
            Api.get('/dashboard/notifications?limit=6')
        ]);

        const badge = (n) => n > 0 ? `<span class="badge badge-red">${n}</span>` : '';
        view.innerHTML = `
            <div class="grid grid-4 mb-2">
                <div class="stat red"><div class="stat-icon">🛒</div><div class="stat-label">Today's Sales</div><div class="stat-value">${Api.moneyShort(summary.todaysSales)}</div><div class="stat-sub">${summary.todaysSalesCount} transactions</div></div>
                <div class="stat green"><div class="stat-icon">📈</div><div class="stat-label">Monthly Revenue</div><div class="stat-value">${Api.moneyShort(summary.monthlyRevenue)}</div><div class="stat-sub">This month</div></div>
                <div class="stat blue"><div class="stat-icon">📦</div><div class="stat-label">Total Products</div><div class="stat-value">${summary.totalProducts}</div><div class="stat-sub">across categories</div></div>
                <div class="stat amber"><div class="stat-icon">⚠️</div><div class="stat-label">Low Stock</div><div class="stat-value">${summary.lowStockProducts}</div><div class="stat-sub">reorder required</div></div>
                <div class="stat blue"><div class="stat-icon">🧾</div><div class="stat-label">Pending Purchase Orders</div><div class="stat-value">${summary.pendingPurchaseOrders}</div><div class="stat-sub">awaiting approval</div></div>
                <div class="stat green"><div class="stat-icon">👥</div><div class="stat-label">Total Customers</div><div class="stat-value">${summary.totalCustomers}</div><div class="stat-sub">registered</div></div>
                <div class="stat red"><div class="stat-icon">🧑‍💼</div><div class="stat-label">Active Employees</div><div class="stat-value">${summary.activeEmployees}</div></div>
                <div class="stat amber"><div class="stat-icon">⚡</div><div class="stat-label">Pending Transactions</div><div class="stat-value">${summary.pendingTransactions}</div></div>
            </div>
            <div class="grid grid-2">
                <div class="card"><div class="card-body">
                    <div class="card-title">Revenue Trend (14 days)</div>
                    <div class="chart-box"><canvas id="ch-rev"></canvas></div>
                </div></div>
                <div class="card"><div class="card-body">
                    <div class="card-title">Top Selling Products</div>
                    <div class="chart-box"><canvas id="ch-top"></canvas></div>
                </div></div>
                <div class="card"><div class="card-body">
                    <div class="card-title">Inventory by Category</div>
                    <div class="chart-box"><canvas id="ch-inv"></canvas></div>
                </div></div>
                <div class="card"><div class="card-body">
                    <div class="card-title">Notifications &amp; Activity</div>
                    ${notifs.map(n => `<div class="notif-item ${n.read ? '' : 'unread'}"><div class="n-title">${Api.esc(n.type)}</div><div class="n-msg">${Api.esc(n.message)}</div><div class="n-time">${Api.dateTime(n.createdAt)}</div></div>`).join('') || '<div class="empty">No notifications</div>'}
                    <div class="card-title mt-2">Recent Activity</div>
                    <ul class="activity-list">${activity.map(a => `<li>${Api.esc(a)}</li>`).join('') || '<li class="text-muted">No activity yet</li>'}</ul>
                </div></div>
            </div>`;

        if (window.Chart) {
            Api.registerChart(new Chart($('ch-rev'), { type: 'line', data: {
                labels: revenue.map(p => p.label), datasets: [{ label: 'Revenue', data: revenue.map(p => p.value), borderColor: '#c8102e', backgroundColor: 'rgba(200,16,46,.1)', fill: true, tension: .35 }]
            }, options: simpleChart }));
            Api.registerChart(new Chart($('ch-top'), { type: 'bar', data: {
                labels: topProducts.map(p => p.label), datasets: [{ label: 'Units sold', data: topProducts.map(p => p.value), backgroundColor: '#c8102e' }]
            }, options: simpleChart }));
            Api.registerChart(new Chart($('ch-inv'), { type: 'doughnut', data: {
                labels: invByCat.map(p => p.label), datasets: [{ data: invByCat.map(p => p.value), backgroundColor: palette(invByCat.length) }]
            }, options: { plugins: { legend: { position: 'bottom' } } } }));
        } else {
            $('ch-rev').parentElement.innerHTML = '<div class="empty">Chart.js failed to load from CDN</div>';
        }
    }

    const simpleChart = { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true } } };
    function palette(n) {
        const c = ['#c8102e','#2563eb','#16a34a','#d97706','#7c3aed','#0891b2','#be185d','#4d7c0f'];
        return Array.from({ length: n }, (_, i) => c[i % c.length]);
    }

    /* ================= SALES / NEW SALE (POS) ================= */
    let posProducts = [];
    async function viewNewSale(view) {
        const [products, customers] = await Promise.all([
            Api.get('/products?q='), Api.get('/customers?q=')]);
        posProducts = products;
        cart = [];
        view.innerHTML = `
            <div class="pos-layout">
                <div class="card"><div class="card-body">
                    <div class="card-title">Add Products</div>
                    <div class="toolbar">
                        <input type="text" id="pos-search" placeholder="Search products...">
                    </div>
                    <div class="mt-2"><label>Customer type</label>
                        <div class="flex" style="gap:18px;margin-top:6px">
                            <label style="display:flex;align-items:center;gap:6px"><input type="radio" name="pos-cust-type" value="walkin" checked> Walk-in (no points)</label>
                            <label style="display:flex;align-items:center;gap:6px"><input type="radio" name="pos-cust-type" value="registered"> Registered customer</label>
                        </div>
                    </div>
                    <div id="pos-customer-wrap" class="hidden mt-2">
                        <label>Registered customer</label>
                        <select id="pos-customer"><option value="">Select customer…</option>${customers.map(c => `<option value="${c.id}" data-pts="${c.loyaltyPoints}">${Api.esc(c.fullName)} — ${c.loyaltyPoints} pts</option>`).join('')}</select>
                        <div class="flex between" style="align-items:center;margin-top:8px">
                            <span class="text-muted" id="pos-cust-pts">—</span>
                            <label style="display:flex;align-items:center;gap:6px"><input type="checkbox" id="pos-use-points"> Use points (R0.10 / point)</label>
                        </div>
                    </div>
                    <div id="pos-results"></div>
                </div></div>
                <div class="card"><div class="card-body">
                    <div class="card-title">Cart</div>
                    <div id="pos-cart"></div>
                    <div class="flex between mt-2"><span class="text-muted">Subtotal</span><span id="cart-subtotal">R0.00</span></div>
                    <div class="flex between"><span class="text-muted">Points discount</span><span id="cart-discount">−R0.00</span></div>
                    <div class="flex between"><span class="text-muted">VAT (15%)</span><span id="cart-tax">R0.00</span></div>
                    <div class="flex between"><strong>Total</strong><strong id="cart-total">R0.00</strong></div>
                    <div class="toolbar mt-2">
                        <select id="pos-pay-method"><option value="CASH">Cash</option><option value="CARD">Card</option><option value="EFT">EFT</option><option value="DIGITAL">Digital</option></select>
                        <input type="number" id="pos-tendered" placeholder="Amount tendered" min="0" step="0.01">
                    </div>
                    <button class="btn btn-primary btn-block mt-2" id="pos-checkout">Complete Sale</button>
                </div></div>
            </div>
            <div id="receipt-modal"></div>`;

        $('pos-search').addEventListener('input', (e) => renderPosResults(e.target.value));
        $('pos-checkout').addEventListener('click', checkout);
        document.querySelectorAll('input[name="pos-cust-type"]').forEach(r => r.onchange = () => {
            const registered = document.querySelector('input[name="pos-cust-type"]:checked').value === 'registered';
            $('pos-customer-wrap').classList.toggle('hidden', !registered);
            $('pos-use-points').checked = false;
            updateCustomerPoints();
            renderCart();
        });
        $('pos-customer').addEventListener('change', () => { updateCustomerPoints(); renderCart(); });
        $('pos-use-points').addEventListener('change', renderCart);
        renderPosResults('');
        renderCart();
        $('pos-search').focus();
    }

    function updateCustomerPoints() {
        const opt = $('pos-customer').selectedOptions[0];
        const registered = document.querySelector('input[name="pos-cust-type"]:checked').value === 'registered';
        $('pos-cust-pts').textContent = (registered && opt && opt.value)
            ? `${opt.textContent.split('—')[0].trim()} has ${opt.dataset.pts} pts (≈ ${Api.money(Number(opt.dataset.pts) * 0.10)})`
            : '—';
    }

    async function renderPosResults(term) {
        const termL = (term || '').toLowerCase();
        let matches = posProducts;
        if (termL) matches = posProducts.filter(p => (p.name || '').toLowerCase().includes(termL) || (p.barcode || '').toLowerCase().includes(termL));
        const el = $('pos-results');
        el.innerHTML = matches.slice(0, 12).map(p => `
            <div class="search-result" data-id="${p.id}">
                <div><strong>${Api.esc(p.name)}</strong><br><span class="text-muted">Qty in stock: ${p.quantity}</span></div>
                <div class="result-price">${Api.money(p.sellingPrice)}</div>
            </div>`).join('') || '<div class="empty">No products found</div>';
        el.querySelectorAll('.search-result').forEach(div => {
            div.onclick = () => {
                const p = posProducts.find(x => x.id === Number(div.dataset.id));
                if (!p || p.quantity <= 0) { Api.toast('Product out of stock', 'error'); return; }
                const line = cart.find(c => c.productId === p.id);
                if (line) { if (line.quantity < p.quantity) line.quantity++; }
                else cart.push({ productId: p.id, quantity: 1, name: p.name, price: p.sellingPrice });
                renderCart();
            };
        });
    }

    function renderCart() {
        const el = $('pos-cart');
        let subtotal = 0;
        el.innerHTML = cart.map(line => {
            subtotal += line.price * line.quantity;
            return `<div class="cart-line">
                <div style="flex:1"><strong>${Api.esc(line.name)}</strong><br><span class="text-muted">${Api.money(line.price)}</span></div>
                <div class="qty-controls">
                    <button class="btn btn-outline btn-sm" data-a="dec" data-id="${line.productId}">−</button>
                    <span style="min-width:32px;text-align:center"><strong>${line.quantity}</strong></span>
                    <button class="btn btn-outline btn-sm" data-a="inc" data-id="${line.productId}">+</button>
                </div>
                <strong style="min-width:70px;text-align:right">${Api.money(line.price * line.quantity)}</strong>
                <button class="btn btn-danger btn-sm" data-a="del" data-id="${line.productId}">✕</button>
            </div>`;
        }).join('') || '<div class="empty">Cart is empty</div>';

        subtotal = Number(subtotal.toFixed(2));
        const discount = computePointsDiscount(subtotal);
        const taxableBase = Number((subtotal - discount).toFixed(2));
        const tax = Number((taxableBase * 0.15).toFixed(2));
        const total = Number((taxableBase + tax).toFixed(2));
        $('cart-subtotal').textContent = Api.money(subtotal);
        $('cart-discount').textContent = '−' + Api.money(discount);
        $('cart-tax').textContent = Api.money(tax);
        $('cart-total').textContent = Api.money(total);
        renderCartTotals = { subtotal, discount, tax, total };

        el.querySelectorAll('[data-a]').forEach(btn => {
            btn.onclick = () => {
                const id = Number(btn.dataset.id);
                const line = cart.find(c => c.productId === id);
                const p = posProducts.find(x => x.id === id);
                if (btn.dataset.a === 'inc' && line && p && line.quantity < p.quantity) line.quantity++;
                if (btn.dataset.a === 'dec' && line) { line.quantity--; if (line.quantity <= 0) cart = cart.filter(c => c.productId !== id); }
                if (btn.dataset.a === 'del') cart = cart.filter(c => c.productId !== id);
                renderCart();
            };
        });
    }
    function computePointsDiscount(subtotal) {
        const registered = document.querySelector('input[name="pos-cust-type"]:checked').value === 'registered';
        const opt = $('pos-customer').selectedOptions[0];
        const usePoints = $('pos-use-points').checked;
        if (!registered || !usePoints || !opt || !opt.value) return 0;
        const pts = Number(opt.dataset.pts) || 0;
        const value = Number((pts * 0.10).toFixed(2));
        return Math.min(value, subtotal);
    }
    let renderCartTotals = { subtotal: 0, discount: 0, tax: 0, total: 0 };
    let renderTimer;

    async function checkout() {
        if (!cart.length) { Api.toast('Cart is empty', 'error'); return; }
        const tendered = parseFloat($('pos-tendered').value) || 0;
        if (tendered < renderCartTotals.total) {
            // allow exact/over pay only
            Api.toast('Amount tendered is less than total', 'error');
            return;
        }
        const registered = document.querySelector('input[name="pos-cust-type"]:checked').value === 'registered';
        const payload = {
            customerId: registered && $('pos-customer').value ? Number($('pos-customer').value) : null,
            items: cart.map(l => ({ productId: l.productId, quantity: l.quantity })),
            paymentMethod: $('pos-pay-method').value,
            amountTendered: tendered,
            usePoints: registered && $('pos-use-points').checked
        };
        $('pos-checkout').disabled = true;
        try {
            const sale = await Api.post('/sales', payload);
            Api.toast(`Sale ${sale.saleNumber} completed`, 'success');
            cart = [];
            showReceipt(sale);
            renderCart();
        } catch (err) {
            Api.toast(err.message, 'error');
        } finally {
            $('pos-checkout').disabled = false;
        }
    }

    function showReceipt(sale) {
        const money = Api.money;
        const rows = sale.items.map(i => `<tr><td>${i.quantity} × ${Api.esc(i.productName)}</td><td class="num">${money(i.lineTotal)}</td></tr>`).join('');
        const change = Math.max(0, sale.changeGiven);
        const modal = document.createElement('div');
        modal.className = 'modal-backdrop';
        modal.id = 'receipt-modal-el';
        modal.innerHTML = `<div class="modal">
            <div class="receipt">
                <h3>MegaMart Retail</h3>
                <div class="center">123 Market Street, Johannesburg<br>Tel: 011 000 1234</div>
                <div class="line"></div>
                <div class="center"><strong>${sale.saleNumber}</strong></div>
                <div class="center">${Api.dateTime(sale.saleDate)}<br>${sale.paymentMethod || ''} · customer: ${Api.esc(sale.customerName || 'Walk-in')}</div>
                <table>${rows}
                    <tr><td>Subtotal</td><td class="num">${money(sale.subtotal)}</td></tr>
                    ${sale.discount && Number(sale.discount) > 0 ? `<tr><td>Points discount</td><td class="num">−${money(sale.discount)}</td></tr>` : ''}
                    <tr><td>VAT (15%)</td><td class="num">${money(sale.tax)}</td></tr>
                    <tr class="total-line"><td>TOTAL</td><td class="num">${money(sale.total)}</td></tr>
                    <tr><td>Tendered</td><td class="num">${money(sale.amountTendered)}</td></tr>
                    <tr><td>Change</td><td class="num">${money(change)}</td></tr>
                </table>
                <div class="line"></div>
                <div class="center">Thank you for shopping at MegaMart!</div>
                <button class="btn btn-primary btn-block mt-2" id="receipt-close">Done</button>
            </div>
        </div>`;
        document.body.appendChild(modal);
        $('receipt-close').onclick = () => modal.remove();
        modal.addEventListener('click', (e) => { if (e.target === modal) modal.remove(); });
    }

    /* ================= SALES HISTORY ================= */
    async function viewSales(view) {
        view.innerHTML = `<div class="page-header"><div><h2>Sales History</h2></div>
            <div class="toolbar"><input type="text" id="sales-search" placeholder="Search by number or customer..."></div></div>
            <div class="card"><div class="card-body"><div class="table-wrap"><table class="tbl"><thead><tr>
            <th>Sale #</th><th>Date</th><th>Customer</th><th class="num">Items</th><th class="num">Total</th><th>Payment</th><th>Status</th><th></th>
            </tr></thead><tbody id="sales-tbody"></tbody></table></div></div></div>
            <div id="receipt-modal"></div>`;
        const load = async (term) => {
            const sales = await Api.get('/sales?q=' + (term || ''));
            const tbody = $('sales-tbody');
            tbody.innerHTML = sales.map(s => `<tr>
                <td><strong>${Api.esc(s.saleNumber)}</strong></td>
                <td>${Api.dateTime(s.saleDate)}</td>
                <td>${Api.esc(s.customerName || 'Walk-in')}</td>
                <td class="num">${s.items ? s.items.reduce((a, i) => a + i.quantity, 0) : 0}</td>
                <td class="num"><strong>${Api.money(s.total)}</strong></td>
                <td>${s.paymentMethod || ''}</td>
                <td><span class="badge badge-green">${s.status || 'COMPLETED'}</span></td>
                <td><button class="btn btn-outline btn-sm" data-id="${s.id}">Receipt</button></td>
            </tr>`).join('') || '<tr><td colspan="8" class="empty">No sales found</td></tr>';
            tbody.querySelectorAll('[data-id]').forEach(b => b.onclick = async () => {
                const sale = await Api.get('/sales/' + b.dataset.id);
                showReceipt(sale);
            });
        };
        load();
        $('sales-search').addEventListener('input', (e) => { clearTimeout(renderTimer); renderTimer = setTimeout(() => load(e.target.value), 350); });
    }

    /* ================= PRODUCTS ================= */
    async function viewProducts(view) {
        view.innerHTML = `<div class="page-header"><div><h2>Products</h2></div><div class="actions">
            <button class="btn btn-primary" id="btn-add-product">+ Add Product</button></div></div>
            <div class="toolbar"><input type="text" id="prod-search" placeholder="Search products...">
            <button class="btn btn-outline" data-view="low-stock">Low Stock</button>
            <button class="btn btn-outline" data-view="products">All</button></div>
            <div class="card"><div class="card-body"><div class="table-wrap"><table class="tbl"><thead><tr>
            <th>Barcode</th><th>Name</th><th>Category</th><th>Supplier</th><th class="num">Qty</th>
            <th class="num">Selling</th><th class="num">Cost</th><th>Status</th><th></th>
            </tr></thead><tbody id="prod-tbody"></tbody></table></div></div></div>`;

        const load = async (term) => {
            const products = await Api.get('/products?q=' + (term || ''));
            const tbody = $('prod-tbody');
            tbody.innerHTML = products.map(p => `<tr>
                <td>${Api.esc(p.barcode || '')}</td>
                <td><strong>${Api.esc(p.name)}</strong>${p.lowStock ? ' <span class="badge badge-red">LOW</span>' : ''}</td>
                <td>${Api.esc(p.categoryName || '')}</td>
                <td>${Api.esc(p.supplierName || '')}</td>
                <td class="num">${p.quantity}</td>
                <td class="num">${Api.money(p.sellingPrice)}</td>
                <td class="num">${Api.money(p.costPrice)}</td>
                <td><span class="badge ${p.status === 'ACTIVE' ? 'badge-green' : 'badge-gray'}">${p.status}</span></td>
                <td><div class="flex">
                    <button class="btn btn-outline btn-sm" data-a="edit" data-id="${p.id}">Edit</button>
                    <button class="btn btn-outline btn-sm" data-a="stock" data-id="${p.id}">+ Stock</button>
                    <button class="btn btn-danger btn-sm" data-a="del" data-id="${p.id}">✕</button>
                </div></td>
            </tr>`).join('') || '<tr><td colspan="9" class="empty">No products found</td></tr>';

            tbody.querySelectorAll('[data-a]').forEach(btn => {
                btn.onclick = () => {
                    const id = Number(btn.dataset.id);
                    const p = products.find(x => x.id === id);
                    if (btn.dataset.a === 'edit') productModal(p);
                    if (btn.dataset.a === 'stock') stockModal(p);
                    if (btn.dataset.a === 'del') del('deactivate', Deactivate => doDeactivate('product', id, p.name));
                };
            });
        };
        load();
        $('btn-add-product').onclick = () => productModal(null);
        $('prod-search').addEventListener('input', (e) => { clearTimeout(renderTimer); renderTimer = setTimeout(() => load(e.target.value), 350); });
        view.querySelectorAll('[data-view]').forEach(b => b.onclick = () => navigate(b.dataset.view));
    }

    async function doDeactivate(type, id, name) {
        if (!confirm(`Deactivate ${name}?`)) return;
        try {
            await Api.del('/' + type + 's/' + id);
            Api.toast(`${name} deactivated`, 'success');
            navigate(currentRoute);
        } catch (e) { Api.toast(e.message, 'error'); }
    }

    async function productModal(product) {
        const [lookups, suppliers] = await Promise.all([Api.get('/lookups/categories'), Api.get('/suppliers?q=')]);
        const isEdit = !!product;
        modal(`
            <div class="modal-head"><h3>${isEdit ? 'Edit Product' : 'Add Product'}</h3></div>
            <div class="modal-body">
                <div class="grid grid-2">
                    <div><label>Name *</label><input id="p-name" value="${Api.esc(product ? product.name : '')}"></div>
                    <div><label>Barcode</label><input id="p-barcode" value="${Api.esc(product ? product.barcode : '')}"></div>
                    <div><label>Category</label><select id="p-cat">${lookups.map(c => `<option value="${c.id}" ${product && product.categoryId === c.id ? 'selected' : ''}>${Api.esc(c.name)}</option>`).join('')}</select></div>
                    <div><label>Supplier</label><select id="p-supp">${suppliers.map(s => `<option value="${s.id}" ${product && product.supplierId === s.id ? 'selected' : ''}>${Api.esc(s.name)}</option>`).join('')}</select></div>
                    <div><label>Selling Price *</label><input type="number" step="0.01" id="p-sell" value="${product ? product.sellingPrice : ''}"></div>
                    <div><label>Cost Price *</label><input type="number" step="0.01" id="p-cost" value="${product ? product.costPrice : ''}"></div>
                    <div><label>Quantity</label><input type="number" id="p-qty" value="${product ? product.quantity : '0'}"></div>
                    <div><label>Minimum Level</label><input type="number" id="p-min" value="${product ? product.minStockLevel : '0'}"></div>
                    <div><label>Maximum Level</label><input type="number" id="p-max" value="${product ? product.maxStockLevel : '100'}"></div>
                </div>
                <div class="mt-2"><label>Description</label><textarea id="p-desc">${Api.esc(product ? product.description : '')}</textarea></div>
            </div>
            <div class="modal-foot"><button class="btn btn-ghost" id="m-cancel">Cancel</button>
            <button class="btn btn-primary" id="m-save">${isEdit ? 'Save Changes' : 'Create Product'}</button></div>`,
            async () => {
                const body = {
                    name: $('p-name').value, barcode: $('p-barcode').value,
                    categoryId: $('p-cat').value ? Number($('p-cat').value) : null,
                    supplierId: $('p-supp').value ? Number($('p-supp').value) : null,
                    sellingPrice: Number($('p-sell').value), costPrice: Number($('p-cost').value),
                    quantity: Number($('p-qty').value) || 0, minStockLevel: Number($('p-min').value) || 0,
                    maxStockLevel: Number($('p-max').value) || 100, description: $('p-desc').value
                };
                if (isEdit) { await Api.put('/products/' + product.id, body); Api.toast('Product updated', 'success'); }
                else { await Api.post('/products', body); Api.toast('Product created', 'success'); }
                closeModal(); navigate(currentRoute);
            });
    }

    function stockModal(product) {
        modal(`
            <div class="modal-head"><h3>Adjust Stock — ${Api.esc(product.name)}</h3></div>
            <div class="modal-body">
                <p class="text-muted">Current stock: <strong>${product.quantity}</strong></p>
                <label>Change (+/-)</label><input type="number" id="s-delta" value="0">
                <div class="mt-2"><label>Reason</label><input id="s-reason" placeholder="e.g. damaged goods"></div>
            </div>
            <div class="modal-foot"><button class="btn btn-ghost" id="m-cancel">Cancel</button>
            <button class="btn btn-primary" id="m-save">Apply Adjustment</button></div>`,
            async () => {
                await Api.post(`/products/${product.id}/adjust`, { delta: Number($('s-delta').value), reason: $('s-reason').value });
                Api.toast('Stock adjusted', 'success'); closeModal(); navigate(currentRoute);
            });
    }

    /* ================= LOW STOCK ================= */
    async function viewLowStock(view) {
        const low = await Api.get('/products/low-stock');
        view.innerHTML = `<div class="page-header"><div><h2>Low Stock Products</h2>
            <div class="sub">Items at or below their minimum stock level</div></div>
            <button class="btn btn-primary" data-goto="purchase-orders">Create Purchase Order</button></div>
            <div class="card"><div class="card-body"><div class="table-wrap"><table class="tbl"><thead><tr>
            <th>Product</th><th>Category</th><th class="num">Qty</th><th class="num">Min Level</th><th class="num">Shortfall</th><th>Status</th>
            </tr></thead><tbody>${low.map(p => `<tr>
            <td><strong>${Api.esc(p.name)}</strong></td><td>${Api.esc(p.categoryName || '')}</td>
            <td class="num"><span class="badge badge-red">${p.quantity}</span></td>
            <td class="num">${p.minStockLevel}</td><td class="num">${p.minStockLevel - p.quantity}</td>
            <td><span class="badge badge-amber">LOW STOCK</span></td></tr>`).join('') || '<tr><td colspan="6" class="empty">All products above minimum stock</td></tr>'}
            </tbody></table></div></div></div>`;
        view.querySelector('[data-goto]') && (view.querySelector('[data-goto]').onclick = () => navigate('purchase-orders'));
    }

    /* ================= STOCK MOVEMENTS ================= */
    async function viewMovements(view) {
        const products = await Api.get('/products?q=');
        view.innerHTML = `<div class="page-header"><div><h2>Stock Movements</h2></div>
            <div class="toolbar"><select id="mv-product"><option value="">All products</option>${products.map(p => `<option value="${p.id}">${Api.esc(p.name)}</option>`).join('')}</select></div></div>
            <div class="card"><div class="card-body"><div class="table-wrap"><table class="tbl"><thead><tr>
            <th>Date</th><th>Product</th><th>Type</th><th class="num">Qty Change</th><th class="num">Balance</th><th>Reference</th>
            </tr></thead><tbody id="mv-tbody"></tbody></table></div></div></div>`;
        const load = async () => {
            const id = $('mv-product').value;
            const list = id ? await Api.get(`/products/${id}/movements`) : [];
            const tbody = $('mv-tbody');
            let html = '';
            if (!id) {
                const all = products;
                for (const p of all.slice(0, 20)) {
                    const m = await Api.get(`/products/${p.id}/movements`);
                    m.forEach(x => { html += row(x); });
                }
            } else {
                list.forEach(x => html += row(x));
            }
            tbody.innerHTML = html || '<tr><td colspan="6" class="empty">No movements</td></tr>';
        };
        const row = (m) => `<tr><td>${Api.dateTime(m.createdAt)}</td><td>${Api.esc(m.productName)}</td>
            <td><span class="badge badge-blue">${m.type}</span></td>
            <td class="num">${m.quantityChanged > 0 ? '+' : ''}${m.quantityChanged}</td>
            <td class="num">${m.balanceAfter}</td><td>${Api.esc(m.reference)}</td></tr>`;
        load();
        $('mv-product').addEventListener('change', load);
    }

    /* ================= SUPPLIERS ================= */
    async function viewSuppliers(view) {
        view.innerHTML = `<div class="page-header"><div><h2>Suppliers</h2></div><button class="btn btn-primary" id="btn-add-supplier">+ Add Supplier</button></div>
            <div class="toolbar"><input type="text" id="supp-search" placeholder="Search suppliers..."></div>
            <div class="card"><div class="card-body"><div class="table-wrap"><table class="tbl"><thead><tr>
            <th>Name</th><th>Contact Person</th><th>Phone</th><th>Email</th><th>Address</th><th>Status</th><th></th>
            </tr></thead><tbody id="supp-tbody"></tbody></table></div></div></div>`;
        const load = async (term) => {
            const list = await Api.get('/suppliers?q=' + (term || ''));
            $('supp-tbody').innerHTML = list.map(s => `<tr>
                <td><strong>${Api.esc(s.name)}</strong></td><td>${Api.esc(s.contactPerson || '')}</td>
                <td>${Api.esc(s.phone || '')}</td><td>${Api.esc(s.email || '')}</td><td>${Api.esc(s.address || '')}</td>
                <td><span class="badge ${s.status === 'ACTIVE' ? 'badge-green' : 'badge-gray'}">${s.status}</span></td>
                <td><div class="flex"><button class="btn btn-outline btn-sm" data-a="edit" data-id="${s.id}">Edit</button>
                <button class="btn btn-danger btn-sm" data-a="del" data-id="${s.id}">✕</button></div></td></tr>`).join('') || '<tr><td colspan="7" class="empty">No suppliers</td></tr>';
            $('supp-tbody').querySelectorAll('[data-a]').forEach(b => b.onclick = () => {
                const s = list.find(x => x.id === Number(b.dataset.id));
                if (b.dataset.a === 'edit') supplierModal(s);
                if (b.dataset.a === 'del') doDeactivate('supplier', s.id, s.name);
            });
        };
        load();
        $('btn-add-supplier').onclick = () => supplierModal(null);
        $('supp-search').addEventListener('input', (e) => { clearTimeout(renderTimer); renderTimer = setTimeout(() => load(e.target.value), 350); });
    }

    function supplierModal(supplier) {
        const isEdit = !!supplier;
        modal(`
            <div class="modal-head"><h3>${isEdit ? 'Edit Supplier' : 'Add Supplier'}</h3></div>
            <div class="modal-body">
                <div class="grid grid-2">
                    <div><label>Name *</label><input id="s-name" value="${Api.esc(supplier ? supplier.name : '')}"></div>
                    <div><label>Contact Person</label><input id="s-cp" value="${Api.esc(supplier ? supplier.contactPerson : '')}"></div>
                    <div><label>Phone</label><input id="s-phone" value="${Api.esc(supplier ? supplier.phone : '')}"></div>
                    <div><label>Email</label><input id="s-email" value="${Api.esc(supplier ? supplier.email : '')}"></div>
                </div>
                <div class="mt-2"><label>Address</label><input id="s-address" value="${Api.esc(supplier ? supplier.address : '')}"></div>
            </div>
            <div class="modal-foot"><button class="btn btn-ghost" id="m-cancel">Cancel</button>
            <button class="btn btn-primary" id="m-save">${isEdit ? 'Save' : 'Create'}</button></div>`,
            async () => {
                const body = { name: $('s-name').value, contactPerson: $('s-cp').value, phone: $('s-phone').value, email: $('s-email').value, address: $('s-address').value };
                if (isEdit) { await Api.put('/suppliers/' + supplier.id, body); Api.toast('Supplier updated', 'success'); }
                else { await Api.post('/suppliers', body); Api.toast('Supplier created', 'success'); }
                closeModal(); navigate(currentRoute);
            });
    }

    /* ================= PURCHASE ORDERS ================= */
    async function viewPurchaseOrders(view) {
        const suppliers = await Api.get('/suppliers?q=');
        const products = await Api.get('/products?q=');
        view.innerHTML = `<div class="page-header"><div><h2>Purchase Orders</h2></div><button class="btn btn-primary" id="btn-add-po">+ New Purchase Order</button></div>
            <div class="toolbar"><select id="po-filter"><option value="">All statuses</option>
            ${['DRAFT','PENDING_APPROVAL','APPROVED','REJECTED','ORDERED','PARTIALLY_RECEIVED','RECEIVED','CANCELLED'].map(s => `<option>${s}</option>`).join('')}</select></div>
            <div class="card"><div class="card-body"><div class="table-wrap"><table class="tbl"><thead><tr>
            <th>PO #</th><th>Date</th><th>Supplier</th><th>Status</th><th class="num">Total</th><th>Approved By</th><th></th>
            </tr></thead><tbody id="po-tbody"></tbody></table></div></div></div>`;
        const color = (s) => ({ 'APPROVED':'badge-green','RECEIVED':'badge-green','PENDING_APPROVAL':'badge-amber','REJECTED':'badge-red','DRAFT':'badge-gray','ORDERED':'badge-blue','PARTIALLY_RECEIVED':'badge-blue','CANCELLED':'badge-gray' }[s] || 'badge-gray');
        const load = async () => {
            const status = $('po-filter').value;
            const list = await Api.get('/purchase-orders?status=' + (status || ''));
            $('po-tbody').innerHTML = list.map(po => `<tr>
                <td><strong>${Api.esc(po.poNumber)}</strong></td><td>${Api.dateTime(po.orderDate)}</td>
                <td>${Api.esc(po.supplierName)}</td>
                <td><span class="badge ${color(po.status)}">${po.status}</span></td>
                <td class="num"><strong>${Api.money(po.total)}</strong></td><td>${Api.esc(po.approvedBy || '—')}</td>
                <td><button class="btn btn-outline btn-sm" data-a="view" data-id="${po.id}">View</button>
                ${po.status === 'DRAFT' ? `<button class="btn btn-secondary btn-sm" data-a="submit" data-id="${po.id}">Submit</button>` : ''}
                ${po.status === 'ORDERED' || po.status === 'APPROVED' ? `<button class="btn btn-success btn-sm" data-a="receive" data-id="${po.id}">Receive</button>` : ''}
                </div></td></tr>`).join('') || '<tr><td colspan="7" class="empty">No purchase orders</td></tr>';
            $('po-tbody').querySelectorAll('[data-a]').forEach(b => b.onclick = async () => {
                const id = Number(b.dataset.id);
                const po = list.find(x => x.id === id);
                if (b.dataset.a === 'view') poModal(po);
                if (b.dataset.a === 'submit') { await Api.post(`/purchase-orders/${id}/submit`); Api.toast('Submitted for approval', 'success'); load(); }
                if (b.dataset.a === 'receive') { await Api.post(`/purchase-orders/${id}/receive`); Api.toast('Order received — inventory updated', 'success'); load(); navigate('inventory')||load(); }
            });
        };
        load();
        $('btn-add-po').onclick = () => poModal(null);
        $('po-filter').addEventListener('change', load);
    }

    function poModal(po) {
        Api.get('/suppliers?q=').then(suppliers => {
            const isEdit = !!po;
            modal(`
                <div class="modal-head"><h3>${isEdit ? `Purchase Order ${po.poNumber}` : 'New Purchase Order'}</h3></div>
                <div class="modal-body">
                    ${isEdit ? `<div class="flex between mb-2"><span>Status:</span><span class="badge badge-amber">${po.status}</span></div><div class="table-wrap"><table class="tbl"><thead><tr><th>Product</th><th class="num">Qty</th><th class="num">Unit Cost</th><th class="num">Line Total</th></tr></thead><tbody>${po.items.map(i => `<tr><td>${Api.esc(i.productName)}</td><td class="num">${i.quantity}</td><td class="num">${Api.money(i.unitCost)}</td><td class="num">${Api.money(i.lineTotal)}</td></tr>`).join('')}</tbody></table></div><h3 style="margin:12px 0">Total: ${Api.money(po.total)}</h3>`
                    : `<div><label>Supplier *</label><select id="po-supplier">${suppliers.map(s => `<option value="${s.id}">${Api.esc(s.name)}</option>`).join('')}</select></div>
                    <div class="toolbar mt-2"><select id="po-product" disabled><option value="">Select a supplier first…</option></select>
                    <button class="btn btn-secondary btn-sm" id="po-add" disabled>Add</button></div>
                    <div id="po-items"></div>`}
                    ${isEdit ? '' : '<div class="mt-2"><label>Notes</label><input id="po-notes"></div>'}
                </div>
                <div class="modal-foot">
                    <button class="btn btn-ghost" id="m-cancel">Close</button>
                    ${isEdit ? '' : '<button class="btn btn-primary" id="m-save">Create Purchase Order</button>'}
                </div>`
            );
            if (!isEdit) {
                const cartLines = [];
                const productSelect = $('po-product');

                const renderProducts = (supplierId) => {
                    productSelect.innerHTML = '<option value="">Loading products…</option>';
                    productSelect.disabled = true;
                    $('po-add').disabled = true;
                    Api.get('/products?supplierId=' + supplierId).then(products => {
                        productSelect.innerHTML = products.length
                            ? '<option value="">Select a product…</option>' + products.map(p => `<option value="${p.id}" data-cost="${p.costPrice}" data-name="${Api.esc(p.name)}">${Api.esc(p.name)}</option>`).join('')
                            : '<option value="">No products for this supplier</option>';
                        productSelect.disabled = products.length === 0;
                        $('po-add').disabled = products.length === 0;
                    }).catch(e => {
                        productSelect.innerHTML = '<option value="">Failed to load products</option>';
                        Api.toast(e.message, 'error');
                    });
                };

                $('po-supplier').addEventListener('change', (e) => {
                    renderProducts(e.target.value);
                });

                const renderItems = () => {
                    let total = 0;
                    $('po-items').innerHTML = cartLines.map(l => {
                        total += l.cost * l.qty;
                        return `<div class="cart-line"><strong style="flex:1">${Api.esc(l.name)}</strong>
                            <input type="number" style="width:70px" value="${l.qty}" min="1" data-line="${l.productId}">
                            <strong>${Api.money(l.cost * l.qty)}</strong>
                            <button class="btn btn-danger btn-sm" data-del="${l.productId}">✕</button></div>`;
                    }).join('') || '<div class="empty">No products added yet</div>';
                    $('po-items').insertAdjacentHTML('beforeend', `<div class="flex between mt-2"><strong>Total</strong><strong id="po-total">${Api.money(total)}</strong></div>`);
                    $('po-items').querySelectorAll('[data-del]').forEach(btn => btn.onclick = () => {
                        const pid = Number(btn.dataset.del);
                        const i = cartLines.findIndex(l => l.productId === pid);
                        if (i > -1) cartLines.splice(i, 1);
                        renderItems();
                    });
                };

                $('po-add').onclick = () => {
                    const opt = productSelect.selectedOptions[0];
                    if (!opt || !opt.value) return;
                    const p = { productId: Number(opt.value), name: opt.dataset.name, cost: Number(opt.dataset.cost) };
                    const ex = cartLines.find(l => l.productId === p.productId);
                    if (ex) ex.qty++;
                    else cartLines.push({ ...p, qty: 1 });
                    renderItems();
                };

                $('m-save').onclick = async () => {
                    document.querySelectorAll('#po-items [data-line]').forEach(inp => {
                        const l = cartLines.find(x => x.productId === Number(inp.dataset.line));
                        if (l) l.qty = Number(inp.value) || 1;
                    });
                    const items = cartLines.map(l => ({ productId: l.productId, quantity: l.qty }));
                    try {
                        await Api.post('/purchase-orders', { supplierId: Number($('po-supplier').value), items, notes: $('po-notes').value });
                        Api.toast('Purchase order created', 'success'); closeModal(); navigate(currentRoute);
                    } catch (e) { Api.toast(e.message, 'error'); }
                };
                renderItems();
            }
        });
    }

    /* ================= APPROVALS ================= */
    async function viewApprovals(view) {
        const list = await Api.get('/purchase-orders/pending-approval');
        view.innerHTML = `<div class="page-header"><div><h2>Purchase Order Approvals</h2>
            <div class="sub">Only managers/administrators can approve or reject</div></div></div>
            <div class="card"><div class="card-body"><div class="table-wrap"><table class="tbl"><thead><tr>
            <th>PO #</th><th>Date</th><th>Supplier</th><th class="num">Total</th><th>Notes</th><th>Actions</th>
            </tr></thead><tbody>${list.map(po => `<tr>
            <td><strong>${Api.esc(po.poNumber)}</strong></td><td>${Api.dateTime(po.orderDate)}</td>
            <td>${Api.esc(po.supplierName)}</td><td class="num">${Api.money(po.total)}</td><td>${Api.esc(po.notes || '')}</td>
            <td><div class="flex"><button class="btn btn-success btn-sm" data-ap="approve" data-id="${po.id}">Approve</button>
            <button class="btn btn-danger btn-sm" data-ap="reject" data-id="${po.id}">Reject</button></div></td></tr>`).join('') || '<tr><td colspan="6" class="empty">No orders pending approval</td></tr>'}
            </tbody></table></div></div></div>`;
        view.querySelectorAll('[data-ap]').forEach(b => b.onclick = async () => {
            const id = Number(b.dataset.id);
            try {
                if (b.dataset.ap === 'approve') { await Api.put(`/purchase-orders/${id}/approve`); Api.toast('Order approved', 'success'); }
                else { await Api.put(`/purchase-orders/${id}/reject`, {}); Api.toast('Order rejected', 'success'); }
                navigate('approvals');
            } catch (e) { Api.toast(e.message, 'error'); }
        });
    }

    /* ================= FINANCE ================= */
    async function viewFinance(view) {
        const f = await Api.get('/finance/summary');
        view.innerHTML = `
            <div class="page-header"><div><h2>Finance Dashboard</h2></div></div>
            <div class="grid grid-4 mb-2">
                <div class="stat green"><div class="stat-icon">📆</div><div class="stat-label">Daily Revenue</div><div class="stat-value">${Api.moneyShort(f.dailyRevenue)}</div></div>
                <div class="stat blue"><div class="stat-icon">🗓️</div><div class="stat-label">Weekly Revenue</div><div class="stat-value">${Api.moneyShort(f.weeklyRevenue)}</div></div>
                <div class="stat red"><div class="stat-icon">💵</div><div class="stat-label">Monthly Revenue</div><div class="stat-value">${Api.moneyShort(f.monthlyRevenue)}</div></div>
                <div class="stat amber"><div class="stat-icon">🧾</div><div class="stat-label">Total Expenses (POs)</div><div class="stat-value">${Api.moneyShort(f.totalExpenses)}</div></div>
                <div class="stat"><div class="stat-icon">📊</div><div class="stat-label">Net Revenue (month)</div><div class="stat-value">${Api.moneyShort(f.netRevenue)}</div></div>
                <div class="stat"><div class="stat-icon">💵</div><div class="stat-label">Cash</div><div class="stat-value">${Api.moneyShort(f.cashTotal)}</div></div>
                <div class="stat"><div class="stat-icon">💳</div><div class="stat-label">Card</div><div class="stat-value">${Api.moneyShort(f.cardTotal)}</div></div>
                <div class="stat"><div class="stat-icon">📱</div><div class="stat-label">EFT / Digital</div><div class="stat-value">${Api.moneyShort(f.eftTotal + f.digitalTotal)}</div></div>
            </div>
            <div class="grid grid-2">
                <div class="card"><div class="card-body"><div class="card-title">Daily Revenue</div><div class="chart-box"><canvas id="ch-fdaily"></canvas></div></div></div>
                <div class="card"><div class="card-body"><div class="card-title">Monthly Revenue</div><div class="chart-box"><canvas id="ch-fmonth"></canvas></div></div></div>
            </div>
            <div class="card mt-2"><div class="card-body"><div class="card-title">Payment Methods (this month)</div>
                <div class="grid grid-4">
                    <div class="stat"><div class="stat-label">Cash</div><div class="stat-value">${Api.money(f.cashTotal)}</div></div>
                    <div class="stat"><div class="stat-label">Card</div><div class="stat-value">${Api.money(f.cardTotal)}</div></div>
                    <div class="stat"><div class="stat-label">EFT</div><div class="stat-value">${Api.money(f.eftTotal)}</div></div>
                    <div class="stat"><div class="stat-label">Digital</div><div class="stat-value">${Api.money(f.digitalTotal)}</div></div>
                </div>
            </div></div>`;
        if (window.Chart) {
            Api.registerChart(new Chart($('ch-fdaily'), { type: 'bar', data: { labels: f.dailySeries.map(p => p.label), datasets: [{ label: 'Revenue', data: f.dailySeries.map(p => p.value), backgroundColor: '#2563eb' }] }, options: simpleChart }));
            Api.registerChart(new Chart($('ch-fmonth'), { type: 'bar', data: { labels: f.monthlySeries.map(p => p.label), datasets: [{ label: 'Revenue', data: f.monthlySeries.map(p => p.value), backgroundColor: '#c8102e' }] }, options: simpleChart }));
        }
    }

    /* ================= CUSTOMERS ================= */
    async function viewCustomers(view) {
        view.innerHTML = `<div class="page-header"><div><h2>Customers</h2></div><button class="btn btn-primary" id="btn-add-cust">+ Register Customer</button></div>
            <div class="toolbar"><input type="text" id="cust-search" placeholder="Search customers..."></div>
            <div class="card"><div class="card-body"><div class="table-wrap"><table class="tbl"><thead><tr>
            <th>Name</th><th>Email</th><th>Phone</th><th class="num">Loyalty Points</th><th>Registered</th><th>Status</th><th></th>
            </tr></thead><tbody id="cust-tbody"></tbody></table></div></div></div>`;
        const load = async (term) => {
            const list = await Api.get('/customers?q=' + (term || ''));
            $('cust-tbody').innerHTML = list.map(c => `<tr>
                <td><strong>${Api.esc(c.fullName)}</strong></td><td>${Api.esc(c.email)}</td><td>${Api.esc(c.phone || '')}</td>
                <td class="num"><span class="badge badge-amber">${c.loyaltyPoints} pts</span></td>
                <td>${Api.dateTime(c.registrationDate)}</td>
                <td><span class="badge ${c.status === 'ACTIVE' ? 'badge-green' : 'badge-gray'}">${c.status}</span></td>
                <td><div class="flex"><button class="btn btn-outline btn-sm" data-a="edit" data-id="${c.id}">Edit</button>
                <button class="btn btn-outline btn-sm" data-a="purchases" data-id="${c.id}">Purchases</button></div></td></tr>`).join('') || '<tr><td colspan="7" class="empty">No customers</td></tr>';
            $('cust-tbody').querySelectorAll('[data-a]').forEach(b => b.onclick = async () => {
                const c = list.find(x => x.id === Number(b.dataset.id));
                if (b.dataset.a === 'edit') customerModal(c);
                if (b.dataset.a === 'purchases') { const sales = await Api.get(`/customers/${c.id}/purchases`); purchasesModal(c, sales); }
            });
        };
        load();
        $('btn-add-cust').onclick = () => customerModal(null);
        $('cust-search').addEventListener('input', (e) => { clearTimeout(renderTimer); renderTimer = setTimeout(() => load(e.target.value), 350); });
    }

    function customerModal(cust) {
        const isEdit = !!cust;
        modal(`
            <div class="modal-head"><h3>${isEdit ? 'Edit Customer' : 'Register Customer'}</h3></div>
            <div class="modal-body">
                <div class="grid grid-2">
                    <div><label>Full Name *</label><input id="c-name" value="${Api.esc(cust ? cust.fullName : '')}"></div>
                    <div><label>Email *</label><input id="c-email" value="${Api.esc(cust ? cust.email : '')}"></div>
                    <div><label>Phone</label><input id="c-phone" value="${Api.esc(cust ? cust.phone : '')}"></div>
                    <div><label>Address</label><input id="c-address" value="${Api.esc(cust ? cust.address : '')}"></div>
                </div>
            </div>
            <div class="modal-foot"><button class="btn btn-ghost" id="m-cancel">Cancel</button><button class="btn btn-primary" id="m-save">${isEdit ? 'Save' : 'Register'}</button></div>`,
            async () => {
                const body = { fullName: $('c-name').value, email: $('c-email').value, phone: $('c-phone').value, address: $('c-address').value };
                if (isEdit) { await Api.put('/customers/' + cust.id, body); Api.toast('Customer updated', 'success'); }
                else { await Api.post('/customers', body); Api.toast('Customer registered', 'success'); }
                closeModal(); navigate(currentRoute);
            });
    }

    function purchasesModal(cust, sales) {
        modal(`<div class="modal-head"><h3>Purchase History — ${Api.esc(cust.fullName)}</h3></div>
            <div class="modal-body"><div class="table-wrap"><table class="tbl"><thead><tr><th>Sale #</th><th>Date</th><th class="num">Total</th></tr></thead><tbody>${sales.map(s => `<tr><td>${Api.esc(s.saleNumber)}</td><td>${Api.dateTime(s.saleDate)}</td><td class="num">${Api.money(s.total)}</td></tr>`).join('') || '<tr><td colspan="3" class="empty">No purchases</td></tr>'}</tbody></table></div></div>
            <div class="modal-foot"><button class="btn btn-ghost" id="m-cancel">Close</button></div>`, () => {});
    }

    /* ================= EMPLOYEES ================= */
    async function viewEmployees(view) {
        const [deps] = await Promise.all([Api.get('/lookups/departments')]);
        view.innerHTML = `<div class="page-header"><div><h2>Employees</h2><div class="sub">Restricted to HR, managers &amp; admins</div></div>
            <button class="btn btn-primary" id="btn-add-emp">+ Add Employee</button></div>
            <div class="toolbar"><input type="text" id="emp-search" placeholder="Search employees..."></div>
            <div class="card"><div class="card-body"><div class="table-wrap"><table class="tbl"><thead><tr>
            <th>Name</th><th>Email</th><th>Department</th><th>Position</th><th>Role</th><th>Hire Date</th><th>Status</th><th></th>
            </tr></thead><tbody id="emp-tbody"></tbody></table></div></div></div>`;
        const load = async (term) => {
            const list = await Api.get('/hr/employees?q=' + (term || ''));
            $('emp-tbody').innerHTML = list.map(e => `<tr>
                <td><strong>${Api.esc(e.fullName)}</strong></td><td>${Api.esc(e.email)}</td><td>${Api.esc(e.departmentName || '')}</td>
                <td>${Api.esc(e.position || '')}</td><td><span class="badge badge-blue">${e.role || ''}</span></td>
                <td>${e.hireDate || ''}</td>
                <td><span class="badge ${e.active ? 'badge-green' : 'badge-gray'}">${e.active ? 'ACTIVE' : 'INACTIVE'}</span></td>
                <td><button class="btn btn-outline btn-sm" data-a="edit" data-id="${e.id}">Edit</button></td></tr>`).join('') || '<tr><td colspan="8" class="empty">No employees</td></tr>';
            $('emp-tbody').querySelectorAll('[data-a]').forEach(b => b.onclick = () => {
                const e = list.find(x => x.id === Number(b.dataset.id));
                if (b.dataset.a === 'edit') employeeModal(e);
            });
        };
        load();
        $('btn-add-emp').onclick = () => employeeModal(null);
        $('emp-search').addEventListener('input', (e) => { clearTimeout(renderTimer); renderTimer = setTimeout(() => load(e.target.value), 350); });
    }

    async function employeeModal(emp) {
        const [deps, roles] = await Promise.all([Api.get('/lookups/departments'), Api.get('/lookups/roles')]);
        const isEdit = !!emp;
        modal(`
            <div class="modal-head"><h3>${isEdit ? 'Edit Employee' : 'Add Employee'}</h3></div>
            <div class="modal-body">
                <div class="grid grid-2">
                    <div><label>Full Name *</label><input id="e-name" value="${Api.esc(emp ? emp.fullName : '')}"></div>
                    <div><label>Email *</label><input id="e-email" value="${Api.esc(emp ? emp.email : '')}"></div>
                    <div><label>Department</label><select id="e-dep">${deps.map(d => `<option value="${d.id}" ${emp && emp.departmentId === d.id ? 'selected' : ''}>${Api.esc(d.name)}</option>`).join('')}</select></div>
                    <div><label>Position</label><input id="e-pos" value="${Api.esc(emp ? emp.position : '')}"></div>
                    <div><label>Role</label><select id="e-role">${roles.map(r => `<option ${emp && emp.role === r ? 'selected' : ''}>${r}</option>`).join('')}</select></div>
                    <div><label>Phone</label><input id="e-phone" value="${Api.esc(emp ? emp.phone : '')}"></div>
                </div>
                ${isEdit ? `<div class="toolbar mt-2"><label class="flex" style="gap:8px"><input type="checkbox" id="e-active" style="width:auto" ${emp.active ? 'checked' : ''}> Active</label></div>` : ''}
            </div>
            <div class="modal-foot"><button class="btn btn-ghost" id="m-cancel">Cancel</button><button class="btn btn-primary" id="m-save">${isEdit ? 'Save' : 'Add Employee'}</button></div>`,
            async () => {
                const body = { fullName: $('e-name').value, email: $('e-email').value, phone: $('e-phone').value, departmentId: $('e-dep').value ? Number($('e-dep').value) : null, position: $('e-pos').value, role: $('e-role').value, active: isEdit ? $('e-active').checked : true };
                if (isEdit) { await Api.put('/hr/employees/' + emp.id, body); Api.toast('Employee updated', 'success'); }
                else { await Api.post('/hr/employees', body); Api.toast('Employee added (login: username@domain / Password@123)', 'success'); }
                closeModal(); navigate(currentRoute);
            });
    }

    /* ================= ATTENDANCE ================= */
    async function viewAttendance(view) {
        const [emps, today] = await Promise.all([Api.get('/hr/employees?q='), Promise.resolve(Api.today())]);
        view.innerHTML = `<div class="page-header"><div><h2>Attendance</h2></div>
            <div class="toolbar"><input type="date" id="att-date" value="${today}">
            <button class="btn btn-primary" id="btn-add-att">+ Record / Update</button></div></div>
            <div class="card"><div class="card-body"><div class="table-wrap"><table class="tbl"><thead><tr>
            <th>Employee</th><th>Date</th><th>Clock In</th><th>Clock Out</th><th>Present</th><th>Notes</th>
            </tr></thead><tbody id="att-tbody"></tbody></table></div></div></div>`;
        const load = async () => {
            const date = $('att-date').value;
            const list = await Api.get('/hr/attendance?date=' + date);
            $('att-tbody').innerHTML = list.map(a => `<tr>
                <td><strong>${Api.esc(a.employeeName)}</strong></td><td>${a.date}</td><td>${Api.esc(a.clockIn || '—')}</td>
                <td>${Api.esc(a.clockOut || '—')}</td>
                <td><span class="badge ${a.present ? 'badge-green' : 'badge-red'}">${a.present ? 'PRESENT' : 'ABSENT'}</span></td>
                <td>${Api.esc(a.notes || '')}</td></tr>`).join('') || '<tr><td colspan="6" class="empty">No attendance for this date</td></tr>';
        };
        load();
        $('att-date').addEventListener('change', load);
        $('btn-add-att').onclick = () => attendanceModal(emps);
    }

    function attendanceModal(emps) {
        modal(`
            <div class="modal-head"><h3>Record Attendance</h3></div>
            <div class="modal-body">
                <div><label>Employee</label><select id="a-emp">${emps.map(e => `<option value="${e.id}">${Api.esc(e.fullName)}</option>`).join('')}</select></div>
                <div class="grid grid-3 mt-2">
                    <div><label>Clock In</label><input type="time" id="a-in" value="08:00"></div>
                    <div><label>Clock Out</label><input type="time" id="a-out" value="17:00"></div>
                    <div><label class="flex" style="gap:6px"><input type="checkbox" id="a-present" style="width:auto" checked> Present</label></div>
                </div>
                <div class="mt-2"><label>Notes</label><input id="a-notes"></div>
            </div>
            <div class="modal-foot"><button class="btn btn-ghost" id="m-cancel">Cancel</button><button class="btn btn-primary" id="m-save">Save</button></div>`,
            async () => {
                await Api.post('/hr/attendance', {
                    employeeId: Number($('a-emp').value), date: $('att-date').value,
                    clockIn: $('a-in').value, clockOut: $('a-out').value, present: $('a-present').checked, notes: $('a-notes').value
                });
                Api.toast('Attendance recorded', 'success'); closeModal(); navigate('attendance');
            });
    }

    /* ================= PROMOTIONS ================= */
    async function viewPromotions(view) {
        view.innerHTML = `<div class="page-header"><div><h2>Promotions</h2></div><button class="btn btn-primary" id="btn-add-promo">+ Create Promotion</button></div>
            <div class="card"><div class="card-body"><div class="table-wrap"><table class="tbl"><thead><tr>
            <th>Name</th><th>Discount</th><th>Start</th><th>End</th><th>Applied To</th><th>Status</th><th></th>
            </tr></thead><tbody id="promo-tbody"></tbody></table></div></div></div>`;
        const list = await Api.get('/marketing/promotions');
        const color = (s) => ({ 'ACTIVE':'badge-green','SCHEDULED':'badge-blue','EXPIRED':'badge-gray','PAUSED':'badge-amber','DRAFT':'badge-gray' }[s] || 'badge-gray');
        $('promo-tbody').innerHTML = list.map(p => `<tr>
            <td><strong>${Api.esc(p.name)}</strong></td><td><span class="badge badge-red">${p.discount}%</span></td>
            <td>${p.startDate || '—'}</td><td>${p.endDate || '—'}</td>
            <td>${Api.esc(p.productName || p.categoryName || 'All')}</td>
            <td><span class="badge ${color(p.status)}">${p.status}</span></td>
            <td><div class="flex"><button class="btn btn-outline btn-sm" data-a="edit" data-id="${p.id}">Edit</button>
            <button class="btn btn-danger btn-sm" data-a="del" data-id="${p.id}">✕</button></div></td></tr>`).join('') || '<tr><td colspan="7" class="empty">No promotions</td></tr>';
        $('promo-tbody').querySelectorAll('[data-a]').forEach(b => b.onclick = () => {
            const p = list.find(x => x.id === Number(b.dataset.id));
            if (b.dataset.a === 'edit') promotionModal(p);
            if (b.dataset.a === 'del') { if (confirm('Deactivate promotion?')) { Api.del('/marketing/promotions/' + p.id + '?pause=true').then(() => { Api.toast('Promotion paused', 'success'); navigate('promotions'); }).catch(e => Api.toast(e.message, 'error')); } }
        });
        $('btn-add-promo').onclick = () => promotionModal(null);
    }

    async function promotionModal(promotion) {
        const [products, cats] = await Promise.all([Api.get('/products?q='), Api.get('/lookups/categories')]);
        const isEdit = !!promotion;
        modal(`
            <div class="modal-head"><h3>${isEdit ? 'Edit Promotion' : 'Create Promotion'}</h3></div>
            <div class="modal-body">
                <div class="grid grid-2">
                    <div><label>Name *</label><input id="pr-name" value="${Api.esc(promotion ? promotion.name : '')}"></div>
                    <div><label>Discount % *</label><input type="number" step="0.01" id="pr-disc" value="${promotion ? promotion.discount : ''}"></div>
                    <div><label>Start Date</label><input type="date" id="pr-start" value="${promotion ? promotion.startDate : Api.today()}"></div>
                    <div><label>End Date</label><input type="date" id="pr-end" value="${promotion ? promotion.endDate : ''}"></div>
                    <div><label>Product (optional)</label><select id="pr-product"><option value="">— All / Category —</option>${products.map(p => `<option value="${p.id}" ${promotion && promotion.productId === p.id ? 'selected' : ''}>${Api.esc(p.name)}</option>`).join('')}</select></div>
                    <div><label>Category (optional)</label><select id="pr-cat"><option value="">— None —</option>${cats.map(c => `<option value="${c.id}" ${promotion && promotion.categoryId === c.id ? 'selected' : ''}>${Api.esc(c.name)}</option>`).join('')}</select></div>
                </div>
                <div class="mt-2"><label>Description</label><textarea id="pr-desc">${Api.esc(promotion ? promotion.description : '')}</textarea></div>
            </div>
            <div class="modal-foot"><button class="btn btn-ghost" id="m-cancel">Cancel</button><button class="btn btn-primary" id="m-save">${isEdit ? 'Save' : 'Create'}</button></div>`,
            async () => {
                const body = {
                    name: $('pr-name').value, description: $('pr-desc').value, discount: Number($('pr-disc').value),
                    startDate: $('pr-start').value, endDate: $('pr-end').value,
                    productId: $('pr-product').value ? Number($('pr-product').value) : null,
                    categoryId: $('pr-cat').value ? Number($('pr-cat').value) : null
                };
                if (isEdit) { await Api.put('/marketing/promotions/' + promotion.id, body); Api.toast('Promotion updated', 'success'); }
                else { await Api.post('/marketing/promotions', body); Api.toast('Promotion created', 'success'); }
                closeModal(); navigate('promotions');
            });
    }

    /* ================= CAMPAIGNS ================= */
    async function viewCampaigns(view) {
        view.innerHTML = `<div class="page-header"><div><h2>Campaigns</h2></div><button class="btn btn-primary" id="btn-add-camp">+ New Campaign</button></div>
            <div class="card"><div class="card-body"><div class="table-wrap"><table class="tbl"><thead><tr>
            <th>Name</th><th>Description</th><th>Start</th><th>End</th><th class="num">Budget</th><th>Status</th><th></th>
            </tr></thead><tbody id="camp-tbody"></tbody></table></div></div></div>`;
        const list = await Api.get('/marketing/campaigns');
        $('camp-tbody').innerHTML = list.map(c => `<tr>
            <td><strong>${Api.esc(c.name)}</strong></td><td>${Api.esc(c.description || '')}</td>
            <td>${c.startDate || '—'}</td><td>${c.endDate || '—'}</td>
            <td class="num">${Api.money(c.budget)}</td>
            <td><span class="badge ${c.status === 'ACTIVE' ? 'badge-green' : 'badge-blue'}">${c.status}</span></td>
            <td><button class="btn btn-outline btn-sm" data-a="edit" data-id="${c.id}">Edit</button></td></tr>`).join('') || '<tr><td colspan="7" class="empty">No campaigns</td></tr>';
        $('camp-tbody').querySelectorAll('[data-a]').forEach(b => b.onclick = () => {
            const c = list.find(x => x.id === Number(b.dataset.id));
            if (b.dataset.a === 'edit') campaignModal(c);
        });
        $('btn-add-camp').onclick = () => campaignModal(null);
    }

    function campaignModal(camp) {
        const isEdit = !!camp;
        modal(`
            <div class="modal-head"><h3>${isEdit ? 'Edit Campaign' : 'New Campaign'}</h3></div>
            <div class="modal-body">
                <div><label>Name *</label><input id="cm-name" value="${Api.esc(camp ? camp.name : '')}"></div>
                <div class="grid grid-2 mt-2">
                    <div><label>Start Date</label><input type="date" id="cm-start" value="${camp ? camp.startDate : Api.today()}"></div>
                    <div><label>End Date</label><input type="date" id="cm-end" value="${camp ? camp.endDate : ''}"></div>
                    <div><label>Budget</label><input type="number" step="0.01" id="cm-budget" value="${camp ? camp.budget : ''}"></div>
                    <div><label>Status</label><select id="cm-status">${['ACTIVE','DRAFT','PAUSED','EXPIRED'].map(s => `<option ${camp && camp.status === s ? 'selected' : ''}>${s}</option>`).join('')}</select></div>
                </div>
                <div class="mt-2"><label>Description</label><textarea id="cm-desc">${Api.esc(camp ? camp.description : '')}</textarea></div>
            </div>
            <div class="modal-foot"><button class="btn btn-ghost" id="m-cancel">Cancel</button><button class="btn btn-primary" id="m-save">${isEdit ? 'Save' : 'Create'}</button></div>`,
            async () => {
                const body = { name: $('cm-name').value, description: $('cm-desc').value, startDate: $('cm-start').value, endDate: $('cm-end').value, budget: Number($('cm-budget').value) || 0, status: $('cm-status').value };
                if (isEdit) { await Api.put('/marketing/campaigns/' + camp.id, body); Api.toast('Campaign updated', 'success'); }
                else { await Api.post('/marketing/campaigns', body); Api.toast('Campaign created', 'success'); }
                closeModal(); navigate('campaigns');
            });
    }

    /* ================= SOCIAL MEDIA ================= */
    async function viewSocial(view) {
        const [posts, camps] = await Promise.all([Api.get('/marketing/social-posts'), Api.get('/marketing/campaigns')]);
        view.innerHTML = `<div class="page-header"><div><h2>Social Media</h2>
            <div class="sub">Simulated integration — external APIs can connect here later</div></div>
            <button class="btn btn-primary" id="btn-add-post">+ New Post</button></div>
            <div class="card"><div class="card-body"><div class="table-wrap"><table class="tbl"><thead><tr>
            <th>Platform</th><th>Content</th><th>Campaign</th><th>Status</th><th>Scheduled</th>
            <th class="num">Likes</th><th class="num">Shares</th><th class="num">Reach</th>
            </tr></thead><tbody>${posts.map(p => `<tr>
            <td><span class="badge badge-blue">${Api.esc(p.platform)}</span></td><td>${Api.esc(p.content)}</td>
            <td>${Api.esc(p.campaignName || '—')}</td>
            <td><span class="badge ${p.status === 'PUBLISHED' ? 'badge-green' : 'badge-amber'}">${p.status}</span></td>
            <td>${Api.dateTime(p.scheduledAt)}</td>
            <td class="num">${p.likes}</td><td class="num">${p.shares}</td><td class="num">${p.reach}</td></tr>`).join('') || '<tr><td colspan="8" class="empty">No posts</td></tr>'}
            </tbody></table></div></div></div>`;
        $('btn-add-post').onclick = () => socialPostModal(camps);
    }

    function socialPostModal(camps) {
        modal(`
            <div class="modal-head"><h3>Schedule Social Post</h3></div>
            <div class="modal-body">
                <div><label>Platform</label><select id="sp-platform">${['Facebook','Instagram','Twitter','TikTok'].map(p => `<option>${p}</option>`).join('')}</select></div>
                <div class="mt-2"><label>Campaign</label><select id="sp-camp">${camps.map(c => `<option value="${c.id}">${Api.esc(c.name)}</option>`).join('')}</select></div>
                <div class="mt-2"><label>Content</label><textarea id="sp-content"></textarea></div>
                <div class="mt-2"><label>Scheduled Date/Time</label><input type="datetime-local" id="sp-when"></div>
            </div>
            <div class="modal-foot"><button class="btn btn-ghost" id="m-cancel">Cancel</button><button class="btn btn-primary" id="m-save">Schedule</button></div>`,
            async () => {
                await Api.post('/marketing/social-posts', {
                    platform: $('sp-platform').value, campaignId: Number($('sp-camp').value),
                    content: $('sp-content').value, scheduledAt: $('sp-when').value
                });
                Api.toast('Post scheduled', 'success'); closeModal(); navigate('social');
            });
    }

    /* ================= REPORTS ================= */
    async function viewReports(view) {
        view.innerHTML = `<div class="page-header"><div><h2>Reports</h2></div>
            <div class="toolbar">
                <select id="rep-name"></select>
                <input type="date" id="rep-from"><input type="date" id="rep-to">
                <button class="btn btn-primary" id="rep-run">Generate</button>
                <button class="btn btn-outline" id="rep-csv">Export CSV</button>
            </div></div>
            <div class="card"><div class="card-body"><div class="table-wrap"><table class="tbl" id="rep-table"></table></div></div></div>`;
        const names = ['sales','inventory','low-stock','procurement','supplier','customer','employee','financial','promotion','stock-movement'];
        $('rep-name').innerHTML = names.map(n => `<option>${n}</option>`).join('');
        const run = async (format) => {
            const name = $('rep-name').value, from = $('rep-from').value, to = $('rep-to').value;
            if (format === 'csv') { window.open(`/mcms/api/reports?name=${name}&from=${from}&to=${to}&format=csv`, '_blank'); return; }
            const rep = await Api.get(`/reports?name=${name}&from=${from}&to=${to}`);
            let html = `<thead><tr>${rep.columns.map(c => `<th>${Api.esc(c)}</th>`).join('')}</tr></thead><tbody>`;
            html += rep.rows.map(r => `<tr>${r.map(c => `<td>${Api.esc(c)}</td>`).join('')}</tr>`).join('');
            html += '</tbody>';
            $('rep-table').innerHTML = html || '<tbody><tr><td class="empty">No data</td></tr></tbody>';
        };
        $('rep-run').onclick = () => run('json');
        $('rep-csv').onclick = () => run('csv');
        run('json');
    }
    // note: CSV uses query params (no auth header) -> download via token not carried; acceptable for demo

    /* ================= AUDIT LOGS ================= */
    async function viewAudit(view) {
        const logs = await Api.get('/audit?limit=200');
        view.innerHTML = `<div class="page-header"><div><h2>Audit Logs</h2><div class="sub">Immutable record of user actions</div></div></div>
            <div class="card"><div class="card-body"><div class="table-wrap"><table class="tbl"><thead><tr>
            <th>Timestamp</th><th>User</th><th>Action</th><th>Entity</th><th>Description</th>
            </tr></thead><tbody>${logs.map(l => `<tr>
            <td>${Api.dateTime(l.timestamp)}</td><td><strong>${Api.esc(l.username)}</strong></td>
            <td><span class="badge badge-blue">${Api.esc(l.action)}</span></td><td>${Api.esc(l.entity)}</td>
            <td>${Api.esc(l.description)}</td></tr>`).join('') || '<tr><td colspan="5" class="empty">No audit logs</td></tr>'}
            </tbody></table></div></div></div>`;
    }

    /* ================= SETTINGS ================= */
    function viewSettings(view) {
        view.innerHTML = `<div class="page-header"><div><h2>Settings</h2></div></div>
            <div class="card"><div class="card-body">
                <div class="card-title">Change Password</div>
                <div style="max-width:380px">
                    <label>New Password</label><input type="password" id="set-pass">
                    <button class="btn btn-primary mt-2" id="set-save">Update Password</button>
                </div>
            </div></div>
            <div class="card mt-2"><div class="card-body">
                <div class="card-title">About</div>
                <p class="text-muted">MegaMart Central Management System (MCMS) — Red-Code Company LTD</p>
                <p class="text-muted">Logged in as <strong>${Api.esc(Api.user().username)}</strong> (${Api.esc(Api.user().role)})</p>
            </div></div>`;
        $('set-save').onclick = async () => {
            try { await Api.post('/auth/change-password', { newPassword: $('set-pass').value }); Api.toast('Password updated', 'success'); $('set-pass').value = ''; }
            catch (e) { Api.toast(e.message, 'error'); }
        };
    }

    /* ================= NOTIFICATIONS DRAWER ================= */
    async function loadDrawerNotifications() {
        const list = await Api.get('/dashboard/notifications?limit=20');
        $('notif-list').innerHTML = list.map(n => `<div class="notif-item ${n.read ? '' : 'unread'}" data-id="${n.id}">
            <div class="n-title">${Api.esc(n.type)}</div><div class="n-msg">${Api.esc(n.message)}</div>
            <div class="n-time">${Api.dateTime(n.createdAt)}</div></div>`).join('') || '<div class="empty">No notifications</div>';
        $('notif-list').querySelectorAll('.notif-item').forEach(el => el.onclick = () => {
            Api.put(`/notifications/${el.dataset.id}/read`).catch(() => {});
            el.classList.remove('unread');
        });
    }

    /* ---------------- Modal helpers ---------------- */
    let modalRoot = null;
    function modal(html, onSave) {
        closeModal();
        modalRoot = document.createElement('div');
        modalRoot.className = 'modal-backdrop';
        modalRoot.innerHTML = `<div class="modal">${html}</div>`;
        document.body.appendChild(modalRoot);
        const cancelBtn = modalRoot.querySelector('#m-cancel');
        if (cancelBtn) cancelBtn.onclick = closeModal;
        const saveBtn = modalRoot.querySelector('#m-save');
        if (saveBtn) saveBtn.onclick = async () => {
            saveBtn.disabled = true;
            try { await onSave(); } catch (e) { Api.toast(e.message, 'error'); }
            finally { saveBtn.disabled = false; }
        };
        modalRoot.addEventListener('click', (e) => { if (e.target === modalRoot) closeModal(); });
    }
    function closeModal() {
        if (modalRoot) { modalRoot.remove(); modalRoot = null; }
    }

    /* ---------------- Init ---------------- */
    document.addEventListener('DOMContentLoaded', () => {
        initLogin();
    });

    window.addEventListener('hashchange', () => {});
})();
