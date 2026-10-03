const API_BASE = window.location.pathname.substring(0, window.location.pathname.lastIndexOf('/html/'));

async function fetchAPI(endpoint, method = 'GET', body = null) {
    const options = {
        method,
        headers: {
            'Accept': 'application/json'
        }
    };

    // For form submissions requiring file uploads, use FormData directly
    if (body instanceof FormData) {
        options.body = body;
    } else if (body) {
        // Standard forms
        const params = new URLSearchParams();
        for (let key in body) {
            params.append(key, body[key]);
        }
        options.body = params;
        options.headers['Content-Type'] = 'application/x-www-form-urlencoded';
    }

    try {
        const url = `${API_BASE}/${endpoint}`;
        const res = await fetch(url.replace(/\/\//g, '/'), options);
        if (!res.ok && res.status !== 401 && res.status !== 500) {
            console.warn(`Request to ${endpoint} returned ${res.status}`);
        }
        const text = await res.text();
        return text ? JSON.parse(text) : {};
    } catch (err) {
        console.error('API Error:', err);
        return { status: 'error', message: err.message };
    }
}

function showMessage(msg, isError = false) {
    const div = document.createElement('div');
    div.className = `glass-panel animate-fade p-4 fixed top-4 right-4 z-50 ${isError ? 'text-danger' : 'text-success'}`;
    div.style.position = 'fixed';
    div.style.top = '20px';
    div.style.right = '20px';
    div.style.padding = '15px 25px';
    div.style.zIndex = '9999';
    div.style.background = isError ? 'rgba(239, 68, 68, 0.9)' : 'rgba(16, 185, 129, 0.9)';
    div.style.color = '#fff';
    div.style.borderRadius = '12px';
    div.style.fontWeight = '600';
    div.style.boxShadow = '0 10px 30px rgba(0,0,0,0.3)';
    div.innerText = msg;
    document.body.appendChild(div);
    setTimeout(() => div.remove(), 3500);
}

// In-App Notification System Helper
let notificationDrawerOpen = false;

async function initNotificationSystem(bellButtonId = 'notifBellBtn', badgeId = 'notifBadge', drawerId = 'notifDrawer') {
    const bellBtn = document.getElementById(bellButtonId);
    if (!bellBtn) return;

    bellBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        toggleNotificationDrawer(drawerId);
    });

    document.addEventListener('click', (e) => {
        const drawer = document.getElementById(drawerId);
        if (drawer && !drawer.contains(e.target) && !bellBtn.contains(e.target)) {
            drawer.hidden = true;
            notificationDrawerOpen = false;
        }
    });

    await refreshNotifications(badgeId, drawerId);
    // Refresh notifications every 30 seconds
    setInterval(() => refreshNotifications(badgeId, drawerId), 30000);
}

async function refreshNotifications(badgeId = 'notifBadge', drawerId = 'notifDrawer') {
    const res = await fetchAPI('notifications');
    const badge = document.getElementById(badgeId);
    const drawer = document.getElementById(drawerId);

    if (res.status === 'success') {
        const count = res.unreadCount || 0;
        if (badge) {
            badge.innerText = count;
            badge.hidden = count === 0;
        }
        if (drawer) {
            renderNotificationDrawerContent(drawer, res.data || []);
        }
    }
}

function toggleNotificationDrawer(drawerId) {
    const drawer = document.getElementById(drawerId);
    if (!drawer) return;
    notificationDrawerOpen = !notificationDrawerOpen;
    drawer.hidden = !notificationDrawerOpen;
}

function renderNotificationDrawerContent(drawer, items) {
    if (!items.length) {
        drawer.innerHTML = `
            <div class="notif-header">
                <h4>In-App Notifications</h4>
            </div>
            <p style="font-size:0.85rem; color:var(--text-muted); text-align:center; padding:1rem 0;">No notifications yet.</p>`;
        return;
    }

    const htmlItems = items.map(item => `
        <div class="notif-item ${item.isRead ? '' : 'unread'}" onclick="markNotifRead(${item.id})">
            <div class="notif-item-title">${escapeHTML(item.title)}</div>
            <div class="notif-item-msg">${escapeHTML(item.message)}</div>
            <span class="notif-item-time">${escapeHTML(item.createdAt || '')}</span>
        </div>
    `).join('');

    drawer.innerHTML = `
        <div class="notif-header">
            <h4>In-App Notifications</h4>
            <button type="button" onclick="clearAllNotifications()">Clear all</button>
        </div>
        <div class="notif-list">${htmlItems}</div>`;
}

async function markNotifRead(id) {
    await fetchAPI('notifications', 'POST', { action: 'markRead', id });
    refreshNotifications();
}

async function clearAllNotifications() {
    await fetchAPI('notifications', 'POST', { action: 'clear' });
    refreshNotifications();
}

function escapeHTML(value) {
    const div = document.createElement('div');
    div.textContent = value ?? '';
    return div.innerHTML;
}
