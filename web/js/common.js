// ===== Shared helpers: icons, API calls, theme, toasts, modals =====

const ICONS = {
  home: '<path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/>',
  alert: '<path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>',
  users: '<path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>',
  clock: '<circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>',
  phone: '<path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/>',
  moon: '<path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>',
  sun: '<circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/>',
  logout: '<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/>',
  search: '<circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>',
  plus: '<line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>',
  edit: '<path d="M17 3a2.83 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z"/>',
  trash: '<polyline points="3 6 5 6 21 6"/><path d="M19 6l-2 14a2 2 0 0 1-2 2H9a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>',
  check: '<polyline points="20 6 9 17 4 12"/>',
  pin: '<path d="M21 10c0 7-9 13-9 13S3 17 3 10a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>',
  download: '<path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>',
  copy: '<rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>',
  shield: '<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>',
  bell: '<path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/>',
  activity: '<polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>',
  target: '<circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="6"/><circle cx="12" cy="12" r="2"/>'
};

function icon(name, size) {
  const s = size || 18;
  return '<svg class="ico" width="' + s + '" height="' + s + '" viewBox="0 0 24 24" fill="none" stroke="currentColor" ' +
    'stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">' + (ICONS[name] || '') + '</svg>';
}

function $(selector, root) { return (root || document).querySelector(selector); }
function $$(selector, root) { return Array.from((root || document).querySelectorAll(selector)); }
function sleep(ms) { return new Promise(resolve => setTimeout(resolve, ms)); }

// Escape text before putting it into innerHTML (stops HTML injection)
function esc(text) {
  const div = document.createElement('div');
  div.textContent = text == null ? '' : String(text);
  return div.innerHTML.replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}

// ---------- session + backend calls ----------
function getToken() { return localStorage.getItem('sosToken'); }
function clearSession() { localStorage.removeItem('sosToken'); localStorage.removeItem('sosUser'); }

// One function for every backend call. Data is sent as a normal form (key=value&key=value).
async function api(method, path, data) {
  const headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
  const token = getToken();
  if (token) headers['Authorization'] = 'Bearer ' + token;

  const response = await fetch(path, {
    method: method,
    headers: headers,
    body: data ? new URLSearchParams(data).toString() : undefined
  });
  const result = await response.json().catch(() => ({}));

  if (!response.ok) {
    if (response.status === 401 && document.body.dataset.page === 'dashboard') {
      clearSession();
      window.location.href = 'index.html';     // session lost (e.g. server restarted)
    }
    throw new Error(result.error || 'Something went wrong');
  }
  return result;
}

// ---------- theme (light / dark) ----------
function applyTheme(theme) {
  document.documentElement.setAttribute('data-theme', theme);
  localStorage.setItem('sosTheme', theme);
  const button = $('#themeBtn');
  if (button) button.innerHTML = icon(theme === 'dark' ? 'sun' : 'moon', 18);
}

function initTheme() {
  const saved = localStorage.getItem('sosTheme');
  const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
  applyTheme(saved || (prefersDark ? 'dark' : 'light'));
  const button = $('#themeBtn');
  if (button) button.onclick = () =>
    applyTheme(document.documentElement.getAttribute('data-theme') === 'dark' ? 'light' : 'dark');
}

// ---------- toasts ----------
function toast(message, type) {
  let root = $('#toasts');
  if (!root) { root = document.createElement('div'); root.id = 'toasts'; document.body.appendChild(root); }
  const item = document.createElement('div');
  item.className = 'toast ' + (type || 'info');
  item.textContent = message;
  root.appendChild(item);
  setTimeout(() => { item.classList.add('out'); setTimeout(() => item.remove(), 260); }, 3600);
}

// ---------- modals ----------
let activeModal = null;
let activeOnClose = null;

function openModal(html, onClose) {
  closeModal();
  const backdrop = document.createElement('div');
  backdrop.className = 'modal-backdrop';
  backdrop.innerHTML = '<div class="modal" role="dialog" aria-modal="true">' + html + '</div>';
  backdrop.addEventListener('mousedown', e => { if (e.target === backdrop) closeModal(); });
  document.body.appendChild(backdrop);
  activeModal = backdrop;
  activeOnClose = onClose || null;
  const firstField = backdrop.querySelector('input:not([type=hidden]), textarea');
  if (firstField) firstField.focus();
  return backdrop.firstElementChild;
}

function closeModal() {
  if (!activeModal) return;
  const callback = activeOnClose;
  activeModal.remove();
  activeModal = null;
  activeOnClose = null;
  if (callback) callback();
}
document.addEventListener('keydown', e => { if (e.key === 'Escape') closeModal(); });

// Returns a Promise<boolean>: true = confirmed
function confirmDialog(title, text, okLabel, isDanger) {
  return new Promise(resolve => {
    let answered = false;
    const finish = value => { if (!answered) { answered = true; resolve(value); } };
    const modal = openModal(
      '<h3>' + esc(title) + '</h3><p class="muted" style="margin-bottom:18px">' + esc(text) + '</p>' +
      '<div class="modal-actions"><button class="btn ghost" data-cancel>Cancel</button>' +
      '<button class="btn ' + (isDanger ? 'primary' : 'success') + '" data-ok>' + esc(okLabel || 'Confirm') + '</button></div>',
      () => finish(false));
    modal.querySelector('[data-cancel]').onclick = () => closeModal();
    modal.querySelector('[data-ok]').onclick = () => { finish(true); closeModal(); };
  });
}

// ---------- small shared helpers ----------
function initials(name) {
  return String(name || '?').trim().split(/\s+/).slice(0, 2).map(w => w.charAt(0).toUpperCase()).join('') || '?';
}

function avatarColor(name) {
  let hue = 0;
  const text = String(name || '');
  for (let i = 0; i < text.length; i++) hue = (hue * 31 + text.charCodeAt(i)) % 360;
  return 'hsl(' + hue + ' 62% 46%)';
}

function avatar(name, small) {
  return '<span class="avatar' + (small ? ' sm' : '') + '" style="background:' + avatarColor(name) + '">' + esc(initials(name)) + '</span>';
}

function copyText(text) {
  if (navigator.clipboard && navigator.clipboard.writeText) return navigator.clipboard.writeText(text);
  return new Promise(resolve => {
    const area = document.createElement('textarea');
    area.value = text; document.body.appendChild(area); area.select();
    document.execCommand('copy'); area.remove(); resolve();
  });
}
