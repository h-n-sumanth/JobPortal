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
        if(!res.ok && res.status !== 401 && res.status !== 500) {
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
    div.innerText = msg;
    document.body.appendChild(div);
    setTimeout(() => div.remove(), 3000);
}
