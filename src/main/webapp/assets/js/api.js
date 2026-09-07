/* MegaMart Central Management System - API client helper. */
const Api = (() => {
    const TOKEN_KEY = 'mcms_token';
    const USER_KEY = 'mcms_user';

    let charts = [];

    function token() {
        return localStorage.getItem(TOKEN_KEY);
    }
    function user() {
        try { return JSON.parse(localStorage.getItem(USER_KEY) || 'null'); }
        catch (e) { return null; }
    }
    function setSession(token, user) {
        localStorage.setItem(TOKEN_KEY, token);
        localStorage.setItem(USER_KEY, JSON.stringify(user));
    }
    function clearSession() {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
    }

    async function request(method, path, body) {
        const headers = { 'Accept': 'application/json' };
        const t = token();
        if (t) headers['Authorization'] = 'Bearer ' + t;
        if (body !== undefined) headers['Content-Type'] = 'application/json';

        const res = await fetch('/mcms/api' + path, {
            method: method,
            headers: headers,
            body: body !== undefined ? JSON.stringify(body) : undefined
        });

        if (res.status === 401 && !path.startsWith('/auth/login')) {
            logout();
            throw new Error('Session expired. Please log in again.');
        }

        let data = null;
        const ct = res.headers.get('content-type') || '';
        if (ct.includes('application/json')) {
            data = await res.json();
        } else {
            data = await res.text();
        }

        if (!res.ok) {
            const msg = (data && data.message) ? data.message : ('Request failed (' + res.status + ')');
            const err = new Error(msg);
            err.status = res.status;
            throw err;
        }
        return data;
    }

    const get = (path) => request('GET', path);
    const post = (path, body) => request('POST', path, body);
    const put = (path, body) => request('PUT', path, body);
    const del = (path) => request('DELETE', path);

    function logout() {
        clearSession();
        location.hash = '';
        document.getElementById('app').classList.add('hidden');
        document.getElementById('login-screen').classList.remove('hidden');
    }

    /* Show a toast notification */
    function toast(message, type) {
        const container = document.getElementById('toast-container');
        const t = document.createElement('div');
        t.className = 'toast ' + (type === 'success' ? 'pop-success' : type === 'error' ? 'pop-error' : '');
        t.textContent = message;
        container.appendChild(t);
        setTimeout(() => t.remove(), 3800);
    }

    /* Destroy existing chart.js instances (re-render safety) */
    function resetCharts() {
        charts.forEach(c => { try { c.destroy(); } catch (e) {} });
        charts = [];
    }
    function registerChart(chart) {
        charts.push(chart);
        return chart;
    }

    function money(v) {
        const n = (v === null || v === undefined) ? 0 : Number(v);
        return 'R' + n.toLocaleString('en-ZA', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }
    function moneyShort(v) {
        const n = Number(v || 0);
        if (n >= 1000000) return 'R' + (n / 1000000).toFixed(1) + 'm';
        if (n >= 1000) return 'R' + (n / 1000).toFixed(1) + 'k';
        return 'R' + n.toFixed(0);
    }
    function esc(s) {
        return String(s == null ? '' : s)
            .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
    }
    function dateTime(s) {
        if (!s) return '';
        return String(s).replace('T', ' ').substring(0, 16);
    }
    function today() {
        return new Date().toISOString().slice(0, 10);
    }

    return { token, user, setSession, clearSession, get, post, put, del, logout,
        toast, resetCharts, registerChart, money, moneyShort, esc, dateTime, today };
})();
