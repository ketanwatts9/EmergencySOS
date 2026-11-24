// ===== Dashboard: home, SOS, contacts, history, helplines =====
if (!getToken()) window.location.href = 'index.html';
initTheme();

const state = {
  user: localStorage.getItem('sosUser') || 'User',
  contacts: [],
  contactResults: [],      // result of the backend search
  categories: [],
  sosCategory: null,
  lastAlert: null,        // alert shown in the live tracker
  contactSearch: '',
  history: { category: 'ALL', status: 'ALL', limit: 0, query: '', list: [] }
};

const CAT_ICON = { MEDICAL: '🚑', FIRE: '🔥', ACCIDENT: '🚗', CRIME: '🛡️', WOMEN_SAFETY: '🙋‍♀️', NATURAL_DISASTER: '🌪️', OTHER: '❗' };
const SEVERITY_TEXT = ['', 'Low', 'Low', 'Moderate', 'High', 'Critical'];
const PRIORITY_TEXT = { 1: 'Notified first', 2: 'Notified second', 3: 'Normal', 4: 'Low', 5: 'Last' };

const QUICK_TEXT = {
  MEDICAL: ['Someone is unconscious', 'Chest pain or trouble breathing', 'Need an ambulance urgently'],
  FIRE: ['Fire in the building', 'Smoke everywhere, cannot get out', 'Smell of gas leak'],
  ACCIDENT: ['Vehicle accident, people injured', 'Road blocked, need help', 'Breakdown on a lonely road'],
  CRIME: ['My phone or bag was snatched', 'Someone is following me', 'Break-in at my home'],
  WOMEN_SAFETY: ['I feel unsafe, someone is following me', 'I am being harassed', 'Stuck in an unsafe place'],
  NATURAL_DISASTER: ['Flood water is rising', 'Earthquake, building damaged', 'Storm, need shelter'],
  OTHER: ['I need urgent help', 'I am lost and need assistance']
};

const HELPLINES = [
  { num: '112', name: 'National Emergency Number', note: 'Police, fire and ambulance in one number' },
  { num: '100', name: 'Police', note: 'Crime, theft, threats' },
  { num: '101', name: 'Fire Brigade', note: 'Fire and rescue' },
  { num: '102', name: 'Ambulance', note: 'Medical emergencies' },
  { num: '1091', name: 'Women Helpline', note: 'Harassment and safety' },
  { num: '1098', name: 'Child Helpline', note: 'Children in danger or distress' }
];

// ---------- shell: nav icons, user chip, logout ----------
$$('#nav a').forEach(a => { a.innerHTML = icon(a.dataset.icon, 20) + a.innerHTML; });
$('#userAvatar').innerHTML = avatar(state.user, true);
$('#userName').textContent = state.user.split(' ')[0];
$('#logoutBtn').innerHTML = icon('logout', 18);
$('#logoutBtn').onclick = async () => {
  try { await api('POST', '/api/auth/logout'); } catch (e) { /* ignore */ }
  clearSession();
  window.location.href = 'index.html';
};

// ---------- router (#home, #sos ...) ----------
const VIEWS = {
  home: { title: 'Dashboard', sub: () => 'Welcome back, ' + state.user.split(' ')[0], load: loadHome },
  sos: { title: 'Send SOS', sub: () => 'Tell us what is happening and we will alert your contacts', load: loadSos },
  contacts: { title: 'Emergency contacts', sub: () => 'The people who are notified when you send an SOS', load: loadContacts },
  history: { title: 'Alert history', sub: () => 'Every SOS you have raised', load: loadHistory },
  helplines: { title: 'Helplines', sub: () => 'Important numbers and safety checklist', load: loadHelplines }
};

function route() {
  const requested = (location.hash || '#home').slice(1);
  const key = VIEWS[requested] ? requested : 'home';
  $$('.view').forEach(v => v.classList.toggle('hidden', v.id !== 'view-' + key));
  $$('#nav a').forEach(a => a.classList.toggle('active', a.dataset.view === key));
  $('#pageTitle').textContent = VIEWS[key].title;
  $('#pageSub').textContent = VIEWS[key].sub();
  window.scrollTo(0, 0);
  VIEWS[key].load().catch(err => toast(err.message, 'error'));
}
window.addEventListener('hashchange', route);

function skeleton(count) {
  let html = '';
  for (let i = 0; i < count; i++) html += '<div class="skeleton"></div>';
  return html;
}

function formatDate(timestamp) {
  return new Date(timestamp).toLocaleString([], { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' });
}

function priorityChip(p) { return '<span class="p-chip p-' + p + '">P' + p + '</span>'; }

function deliveryRows(alert) {
  return alert.deliveries.map(d => {
    const done = d.status.startsWith('DELIVERED');
    return '<li><span>' + esc(d.contact) + '</span><span class="st ' + (done ? 'done' : 'wait') + '">' +
      (done ? icon('check', 14) + ' Delivered' : '<span class="spinner dark"></span> Sending') + '</span></li>';
  }).join('');
}

// =====================================================================
// HOME
// =====================================================================
async function loadHome() {
  const view = $('#view-home');
  if (!view.innerHTML) view.innerHTML = skeleton(3);

  const [contacts, alerts, stats] = await Promise.all([
    api('GET', '/api/contacts'), api('GET', '/api/alerts'), api('GET', '/api/alerts/stats')
  ]);
  state.contacts = contacts;

  const active = alerts.filter(a => !a.resolved).length;
  const maxCount = stats.reduce((m, s) => Math.max(m, s.count), 1);
  const ready = contacts.length > 0;

  const statusChip = ready
    ? '<span class="status-chip ok">' + icon('shield', 16) + ' SOS is ready</span>'
    : '<span class="status-chip warn">' + icon('alert', 16) + ' Add a contact to activate SOS</span>';
  const statusText = ready
    ? contacts.length + (contacts.length === 1 ? ' contact' : ' contacts') + ' will be notified, starting with <b>' + esc(contacts[0].name) + '</b>.'
    : 'SOS alerts need at least one emergency contact. It only takes a few seconds.';

  const bars = stats.length === 0
    ? '<div class="empty"><div class="big-ico">📊</div>No alerts yet. Your breakdown will appear here.</div>'
    : stats.map(s => '<div class="bar-row"><div class="bar-top"><span>' + esc(s.label) + '</span><span>' + s.count +
        '</span></div><div class="bar"><div style="width:' + Math.round(s.count * 100 / maxCount) + '%"></div></div></div>').join('');

  const recent = alerts.length === 0
    ? '<div class="empty"><div class="big-ico">✅</div>No alerts raised. Stay safe!</div>'
    : alerts.slice(0, 4).map(a => '<div class="mini-alert"><span class="m-ico">' + (CAT_ICON[a.category] || '❗') + '</span>' +
        '<div><b>' + esc(a.categoryLabel) + '</b><div class="muted small ellipsis">' + esc(a.location) + '</div></div>' +
        '<div style="text-align:right"><div class="muted small">' + formatDate(a.timestamp) + '</div>' +
        (a.resolved ? '<span class="badge ok">Resolved</span>' : '<span class="badge warn">Active</span>') + '</div></div>').join('');

  view.innerHTML =
    '<div class="card hero-card"><div>' + statusChip + '<h2>Your safety dashboard</h2><p class="muted">' + statusText + '</p>' +
    '<div style="margin-top:16px;display:flex;gap:10px;flex-wrap:wrap">' +
    (ready ? '<a class="btn primary" href="#sos">' + icon('alert', 18) + ' Send an SOS</a>'
           : '<a class="btn primary" href="#contacts">' + icon('plus', 18) + ' Add first contact</a>') +
    '<a class="btn ghost" href="#helplines">' + icon('phone', 18) + ' Helplines</a></div></div>' +
    '<button class="sos-btn" id="homeSos" aria-label="Open SOS page">SOS</button></div>' +

    '<div class="stats-grid">' +
    statCard('users', 's-blue', contacts.length + '<small class="muted" style="font-size:14px"> / 10</small>', 'Emergency contacts') +
    statCard('bell', 's-red', alerts.length, 'Total alerts sent') +
    statCard('activity', 's-amber', active, 'Active alerts') +
    statCard('target', 's-green', alerts.length ? formatDate(alerts[0].timestamp) : 'None yet', 'Last alert') + '</div>' +

    '<div class="two-col"><div class="card"><h3>Alerts by category</h3>' + bars + '</div>' +
    '<div class="card"><div style="display:flex;justify-content:space-between;align-items:center"><h3>Recent alerts</h3>' +
    '<a href="#history" class="small" style="color:var(--blue);font-weight:700;text-decoration:none">View all</a></div>' + recent + '</div></div>';

  $('#homeSos').onclick = () => { location.hash = '#sos'; };
}

function statCard(iconName, colorClass, value, label) {
  return '<div class="card stat"><span class="s-ico ' + colorClass + '">' + icon(iconName, 22) + '</span><div><b>' + value +
    '</b><span class="muted small">' + label + '</span></div></div>';
}

// =====================================================================
// SEND SOS
// =====================================================================
async function loadSos() {
  const view = $('#view-sos');
  view.innerHTML = skeleton(2);
  const results = await Promise.all([
    state.categories.length ? Promise.resolve(state.categories) : api('GET', '/api/alerts/categories'),
    api('GET', '/api/contacts')
  ]);
  state.categories = results[0];
  state.contacts = results[1];
  state.sosCategory = null;

  const hasContacts = state.contacts.length > 0;
  const catCards = state.categories.map(c =>
    '<button type="button" class="cat-card" data-key="' + c.key + '"><span class="c-ico">' + (CAT_ICON[c.key] || '❗') + '</span><b>' +
    esc(c.label) + '</b><div class="dots">' + [1, 2, 3, 4, 5].map(i => '<i class="' + (i <= c.severity ? 'on' : '') + '"></i>').join('') +
    '</div></button>').join('');

  const notifyList = hasContacts
    ? '<ol class="notify-list">' + state.contacts.map((c, i) =>
        '<li><span class="order">' + (i + 1) + '</span>' + avatar(c.name, true) + '<div class="info"><b>' + esc(c.name) +
        '</b><div class="muted small">' + esc(c.relation) + ' · ' + esc(c.phone) + '</div></div>' + priorityChip(c.priority) + '</li>').join('') + '</ol>'
    : '<div class="empty"><div class="big-ico">👥</div><b>No contacts yet</b>Add at least one contact to send an SOS.<br><br><a class="btn primary small" href="#contacts">Add a contact</a></div>';

  view.innerHTML =
    '<div class="grid-2"><div class="card">' +
    '<div class="step-title"><span>1</span> What is happening?</div><div class="cat-grid" id="catGrid">' + catCards + '</div>' +
    '<div id="tipBox" class="tip hidden"></div>' +
    '<div class="step-title"><span>2</span> Where are you?</div>' +
    '<div class="input-row"><input class="input" id="sosLocation" maxlength="100" placeholder="e.g. Sector 17, Chandigarh">' +
    '<button type="button" class="btn soft" id="geoBtn">' + icon('pin', 16) + ' Use my location</button></div>' +
    '<div class="step-title"><span>3</span> Tell us more</div>' +
    '<div class="field" style="margin:0"><textarea id="sosDescription" maxlength="300" placeholder="What happened? Add anything that can help."></textarea></div>' +
    '<div class="counter" id="descCount">0 / 300</div><div class="chips" id="quickChips"></div>' +
    '<div style="margin-top:22px"><button class="btn primary big" id="sosSend"' + (hasContacts ? '' : ' disabled') + '>' + icon('alert', 20) + ' SEND SOS</button></div>' +
    '</div>' +
    '<div class="side-stack"><div class="card"><h3>Who will be notified</h3>' + notifyList + '</div>' +
    '<div class="card hidden" id="trackerCard"></div></div></div>';

  $$('.cat-card').forEach(btn => { btn.onclick = () => selectCategory(btn.dataset.key); });
  $('#geoBtn').onclick = useMyLocation;
  $('#sosDescription').oninput = () => { $('#descCount').textContent = $('#sosDescription').value.length + ' / 300'; };
  $('#sosSend').onclick = sendSos;
  renderTracker();
}

function selectCategory(key) {
  state.sosCategory = key;
  $$('.cat-card').forEach(c => c.classList.toggle('selected', c.dataset.key === key));
  const cat = state.categories.find(c => c.key === key);
  const tip = $('#tipBox');
  tip.classList.remove('hidden');
  tip.innerHTML = '<b>Severity ' + cat.severity + '/5 (' + SEVERITY_TEXT[cat.severity] + '). Safety tip:</b> ' + esc(cat.advice);

  $('#quickChips').innerHTML = (QUICK_TEXT[key] || []).map(t => '<button type="button" class="chip">' + esc(t) + '</button>').join('');
  $$('#quickChips .chip').forEach(chip => {
    chip.onclick = () => {
      const box = $('#sosDescription');
      box.value = box.value.trim() ? box.value.trim() + '. ' + chip.textContent : chip.textContent;
      box.dispatchEvent(new Event('input'));
    };
  });
}

function useMyLocation() {
  if (!navigator.geolocation) { toast('Location is not supported in this browser', 'error'); return; }
  const button = $('#geoBtn');
  button.disabled = true;
  button.innerHTML = '<span class="spinner dark"></span> Locating...';
  navigator.geolocation.getCurrentPosition(
    pos => {
      $('#sosLocation').value = 'Lat ' + pos.coords.latitude.toFixed(5) + ', Lng ' + pos.coords.longitude.toFixed(5);
      button.disabled = false; button.innerHTML = icon('pin', 16) + ' Use my location';
    },
    () => {
      toast('Could not get your location. Please type it.', 'error');
      button.disabled = false; button.innerHTML = icon('pin', 16) + ' Use my location';
    },
    { timeout: 8000 });
}

// 5 second "are you sure?" countdown, so a wrong tap can be cancelled
function countdownModal(label) {
  return new Promise(resolve => {
    let seconds = 5;
    let decided = false;
    let timer = null;
    const finish = value => { if (!decided) { decided = true; clearInterval(timer); resolve(value); } };
    const modal = openModal(
      '<div class="countdown"><h3>Sending SOS: ' + esc(label) + '</h3><p class="muted">Your contacts will be alerted in</p>' +
      '<div class="count-ring"><span id="cdNum">5</span></div>' +
      '<div class="modal-actions" style="justify-content:center"><button class="btn ghost" data-cancel>Cancel</button>' +
      '<button class="btn primary" data-now>Send now</button></div></div>',
      () => finish(false));
    modal.querySelector('[data-cancel]').onclick = () => closeModal();
    modal.querySelector('[data-now]').onclick = () => { finish(true); closeModal(); };
    timer = setInterval(() => {
      seconds--;
      const num = $('#cdNum');
      if (num) num.textContent = seconds;
      if (seconds <= 0) { finish(true); closeModal(); }
    }, 1000);
  });
}

async function sendSos() {
  const category = state.sosCategory;
  const location = $('#sosLocation').value.trim();
  const description = $('#sosDescription').value.trim();
  if (!category) { toast('Choose the type of emergency first', 'error'); return; }
  if (!location) { toast('Enter your location', 'error'); $('#sosLocation').focus(); return; }
  if (!description) { toast('Describe what happened', 'error'); $('#sosDescription').focus(); return; }

  const cat = state.categories.find(c => c.key === category);
  const go = await countdownModal(cat.label);
  if (!go) { toast('SOS cancelled'); return; }

  const button = $('#sosSend');
  button.disabled = true;
  button.innerHTML = '<span class="spinner"></span> Sending...';
  try {
    const alert = await api('POST', '/api/alerts', { category: category, location: location, description: description });
    state.lastAlert = alert;
    renderTracker();
    toast('SOS raised. Notifying your contacts...', 'success');
    watchDelivery(alert.id);
  } catch (err) {
    toast(err.message, 'error');
  }
  const again = $('#sosSend');
  if (again) { again.disabled = false; again.innerHTML = icon('alert', 20) + ' SEND SOS'; }
}

// The backend thread notifies one contact at a time, so we check again every second.
async function watchDelivery(alertId) {
  try {
    for (let i = 0; i < 40; i++) {
      await sleep(1000);
      if (!state.lastAlert || state.lastAlert.id !== alertId) return;
      const latest = await api('GET', '/api/alerts?limit=1');
      if (!latest.length || latest[0].id !== alertId) return;
      state.lastAlert = latest[0];
      renderTracker();
      if (latest[0].allDelivered) { toast('All contacts have been notified (simulated)', 'success'); return; }
    }
  } catch (err) { /* polling is best effort */ }
}

function alertSummary(a) {
  return 'SOS ALERT (simulation)\nType: ' + a.categoryLabel + ' (severity ' + a.severity + '/5)\nDate: ' + a.date + '  Time: ' + a.clock +
    '\nLocation: ' + a.location + '\nDetails: ' + a.description + '\nMessage: ' + a.message + '\nContacts: ' +
    a.deliveries.map(d => d.contact).join('; ');
}

function renderTracker() {
  const card = $('#trackerCard');
  if (!card) return;
  const a = state.lastAlert;
  if (!a) { card.classList.add('hidden'); return; }

  const total = a.deliveries.length;
  const done = a.deliveries.filter(d => d.status.startsWith('DELIVERED')).length;
  const pct = total ? Math.round(done * 100 / total) : 100;

  card.classList.remove('hidden');
  card.innerHTML =
    '<div class="tracker-head"><h3 style="margin:0">Live alert status</h3><span class="sev sev-' + a.severity + '">' + esc(a.categoryLabel) + '</span></div>' +
    '<div class="progress"><div style="width:' + pct + '%"></div></div>' +
    '<p class="muted small">' + (a.allDelivered ? 'All ' + total + ' contacts notified (simulated)' : done + ' of ' + total + ' contacts notified...') + '</p>' +
    '<ul class="steps">' + deliveryRows(a) + '</ul>' +
    '<div class="msg-box"><b>Message sent to contacts (simulated)</b>' + esc(a.message) + '</div>' +
    '<div class="tip">' + esc(a.advice) + '</div>' +
    '<div class="tracker-actions">' +
    (a.resolved ? '<span class="badge ok">' + icon('check', 12) + ' Marked safe</span>'
                : '<button class="btn success small" id="safeBtn">' + icon('check', 15) + ' I am safe</button>') +
    '<button class="btn soft small" id="copyBtn">' + icon('copy', 15) + ' Copy summary</button>' +
    '<a class="btn ghost small" href="#history">History</a></div>';

  const copy = $('#copyBtn');
  if (copy) copy.onclick = () => copyText(alertSummary(a)).then(() => toast('Summary copied', 'success'));
  const safe = $('#safeBtn');
  if (safe) safe.onclick = async () => {
    safe.disabled = true;
    try {
      state.lastAlert = await api('PUT', '/api/alerts/' + a.id + '/resolve');
      renderTracker();
      toast('Glad you are safe. Alert marked as resolved.', 'success');
    } catch (err) { toast(err.message, 'error'); safe.disabled = false; }
  };
}

// =====================================================================
// CONTACTS
// =====================================================================
async function loadContacts() {
  const view = $('#view-contacts');
  if (!$('#contactGrid')) {
    view.innerHTML =
      '<div class="toolbar"><div class="search">' + icon('search', 16) + '<input id="contactSearch" placeholder="Search by name, relation or phone"></div>' +
      '<span class="badge" id="contactCount"></span>' +
      '<button class="btn primary" id="addContactBtn">' + icon('plus', 17) + ' Add contact</button></div>' +
      '<div class="legend"><span>' + priorityChip(1) + ' notified first</span><span>' + priorityChip(2) + ' second</span><span>' + priorityChip(3) +
      ' normal</span><span>' + priorityChip(4) + ' low</span><span>' + priorityChip(5) + ' last</span></div>' +
      '<div class="grid-cards" id="contactGrid">' + skeleton(3) + '</div>';
    $('#addContactBtn').onclick = () => contactModal(null);
    let searchTimer = null;
    $('#contactSearch').oninput = e => {
      clearTimeout(searchTimer);
      searchTimer = setTimeout(() => {
        state.contactSearch = e.target.value.trim();
        loadContacts().catch(err => toast(err.message, 'error'));
      }, 250);
    };
  }
  state.contacts = await api('GET', '/api/contacts');          // all contacts (for the counter)
  state.contactResults = state.contactSearch
    ? await api('GET', '/api/contacts?q=' + encodeURIComponent(state.contactSearch))   // searched in Java
    : state.contacts;
  drawContacts();
}

function drawContacts() {
  $('#contactCount').textContent = state.contacts.length + ' / 10 saved';
  const list = state.contactResults;
  const grid = $('#contactGrid');

  if (state.contacts.length === 0) {
    grid.innerHTML = '<div class="card empty" style="grid-column:1/-1"><div class="big-ico">👥</div><b>No emergency contacts yet</b>' +
      'Add the people who should be alerted in an emergency.<br><br><button class="btn primary" id="emptyAdd">' + icon('plus', 17) + ' Add your first contact</button></div>';
    $('#emptyAdd').onclick = () => contactModal(null);
    return;
  }
  if (list.length === 0) { grid.innerHTML = '<div class="card empty" style="grid-column:1/-1">No contacts match your search.</div>'; return; }

  grid.innerHTML = list.map(c =>
    '<div class="card c-card"><div class="c-top">' + avatar(c.name) + '<div><b>' + esc(c.name) + '</b><span class="muted small">' + esc(c.relation) +
    '</span></div>' + priorityChip(c.priority) + '</div>' +
    '<div class="muted" style="display:flex;gap:8px;align-items:center">' + icon('phone', 15) + ' ' + esc(c.phone) +
    '<span class="small">· ' + PRIORITY_TEXT[c.priority] + '</span></div>' +
    '<div class="c-actions"><a class="btn soft small" href="tel:' + esc(c.phone) + '">' + icon('phone', 14) + ' Call</a>' +
    '<button class="btn soft small" data-edit="' + c.id + '">' + icon('edit', 14) + ' Edit</button>' +
    '<button class="btn danger small" data-delete="' + c.id + '">' + icon('trash', 14) + ' Delete</button></div></div>').join('');

  $$('[data-edit]', grid).forEach(b => { b.onclick = () => contactModal(state.contacts.find(c => String(c.id) === b.dataset.edit)); });
  $$('[data-delete]', grid).forEach(b => { b.onclick = () => deleteContact(state.contacts.find(c => String(c.id) === b.dataset.delete)); });
}

function contactModal(contact) {
  const editing = !!contact;
  const current = contact ? contact.priority : 3;
  const modal = openModal(
    '<form id="contactForm" novalidate><h3>' + (editing ? 'Edit contact' : 'Add emergency contact') + '</h3>' +
    '<div class="field"><label>Name</label><input type="text" name="name" maxlength="50" placeholder="e.g. Mom" value="' + (editing ? esc(contact.name) : '') + '"></div>' +
    '<div class="field"><label>Phone (10 digits)</label><input type="text" name="phone" inputmode="numeric" maxlength="10" placeholder="9876543210" value="' + (editing ? esc(contact.phone) : '') + '"></div>' +
    '<div class="field"><label>Relation</label><input type="text" name="relation" list="relList" maxlength="30" placeholder="e.g. Mother" value="' + (editing ? esc(contact.relation) : '') + '">' +
    '<datalist id="relList"><option>Mother</option><option>Father</option><option>Sibling</option><option>Friend</option><option>Spouse</option><option>Neighbour</option><option>Colleague</option></datalist></div>' +
    '<div class="field"><label>Priority</label><div class="seg">' +
    [1, 2, 3, 4, 5].map(p => '<label><input type="radio" name="priority" value="' + p + '"' + (p === current ? ' checked' : '') + '><span>P' + p + '</span></label>').join('') +
    '</div><p class="hint" id="prioHint"></p></div>' +
    '<p class="form-error" id="contactError"></p>' +
    '<div class="modal-actions"><button type="button" class="btn ghost" data-cancel>Cancel</button><button type="submit" class="btn primary" id="contactSave">' + (editing ? 'Save changes' : 'Add contact') + '</button></div></form>');

  const hint = () => {
    const p = $('input[name=priority]:checked', modal).value;
    $('#prioHint').textContent = 'P' + p + ': ' + PRIORITY_TEXT[p] + (p === '1' ? ' (best for the person who can reach you fastest)' : '');
  };
  hint();
  $$('input[name=priority]', modal).forEach(r => { r.onchange = hint; });
  $('input[name=phone]', modal).oninput = e => { e.target.value = e.target.value.replace(/\D/g, ''); };
  $('[data-cancel]', modal).onclick = () => closeModal();

  $('#contactForm', modal).onsubmit = async e => {
    e.preventDefault();
    const form = e.target;
    // form.elements is used because form.name would return the form's own "name" attribute
    const field = fieldName => form.elements.namedItem(fieldName);
    const data = {
      name: field('name').value.trim(), phone: field('phone').value.trim(),
      relation: field('relation').value.trim(), priority: $('input[name=priority]:checked', form).value
    };
    const error = $('#contactError');
    error.textContent = '';
    if (!data.name || !data.phone || !data.relation) { error.textContent = 'Please fill in all the fields.'; return; }
    if (data.phone.length !== 10) { error.textContent = 'Phone number must be exactly 10 digits.'; return; }

    const save = $('#contactSave');
    save.disabled = true;
    try {
      if (editing) await api('PUT', '/api/contacts/' + contact.id, data);
      else await api('POST', '/api/contacts', data);
      closeModal();
      toast(editing ? 'Contact updated' : 'Contact added', 'success');
      loadContacts();
    } catch (err) {
      error.textContent = err.message;
      save.disabled = false;
    }
  };
}

async function deleteContact(contact) {
  if (!contact) return;
  const yes = await confirmDialog('Delete ' + contact.name + '?', 'They will no longer be notified when you send an SOS.', 'Delete', true);
  if (!yes) return;
  try {
    await api('DELETE', '/api/contacts/' + contact.id);
    toast('Contact deleted', 'success');
    loadContacts();
  } catch (err) { toast(err.message, 'error'); }
}

// =====================================================================
// HISTORY
// =====================================================================
async function loadHistory() {
  const view = $('#view-history');
  if (!$('#historyList')) {
    view.innerHTML =
      '<div class="toolbar"><div class="search">' + icon('search', 16) + '<input id="historySearch" placeholder="Search location or details"></div>' +
      '<select class="input" id="historyStatus" style="width:auto"><option value="ALL">All status</option><option value="ACTIVE">Active</option><option value="RESOLVED">Resolved</option></select>' +
      '<select class="input" id="historyLimit" style="width:auto"><option value="0">All alerts</option><option value="5">Last 5</option></select>' +
      '<button class="btn ghost" id="exportBtn">' + icon('download', 16) + ' Export CSV</button></div>' +
      '<div class="filters" id="historyChips"></div><div class="timeline" id="historyList">' + skeleton(3) + '</div>';

    let timer = null;
    $('#historySearch').oninput = e => {
      clearTimeout(timer);
      timer = setTimeout(() => { state.history.query = e.target.value.trim(); refreshHistory(); }, 300);
    };
    $('#historyStatus').onchange = e => { state.history.status = e.target.value; drawHistory(); };
    $('#historyLimit').onchange = e => { state.history.limit = e.target.value; refreshHistory(); };
    $('#exportBtn').onclick = exportHistory;
  }
  if (state.categories.length === 0) state.categories = await api('GET', '/api/alerts/categories');
  await refreshHistory();
}

async function refreshHistory() {
  const h = state.history;
  const params = '?category=' + h.category + '&limit=' + h.limit + '&q=' + encodeURIComponent(h.query);
  const results = await Promise.all([api('GET', '/api/alerts' + params), api('GET', '/api/alerts/stats')]);
  h.list = results[0];
  drawChips(results[1]);
  drawHistory();
}

function drawChips(stats) {
  const total = stats.reduce((sum, s) => sum + s.count, 0);
  const chips = ['<button class="chip ' + (state.history.category === 'ALL' ? 'active' : '') + '" data-cat="ALL">All (' + total + ')</button>'];
  state.categories.forEach(c => {
    const found = stats.find(s => s.label === c.label);
    if (!found) return;
    chips.push('<button class="chip ' + (state.history.category === c.key ? 'active' : '') + '" data-cat="' + c.key + '">' +
      (CAT_ICON[c.key] || '') + ' ' + esc(c.label) + ' (' + found.count + ')</button>');
  });
  const box = $('#historyChips');
  box.innerHTML = chips.join('');
  $$('.chip', box).forEach(chip => { chip.onclick = () => { state.history.category = chip.dataset.cat; refreshHistory().catch(err => toast(err.message, 'error')); }; });
}

function visibleHistory() {
  const status = state.history.status;
  return state.history.list.filter(a => status === 'ALL' || (status === 'RESOLVED' ? a.resolved : !a.resolved));
}

function drawHistory() {
  const list = visibleHistory();
  const box = $('#historyList');
  if (list.length === 0) {
    box.innerHTML = '<div class="card empty"><div class="big-ico">🗂️</div><b>No alerts found</b>' +
      (state.history.list.length === 0 && !state.history.query && state.history.category === 'ALL'
        ? 'When you send an SOS it will be recorded here.' : 'Try changing the filters or the search.') + '</div>';
    return;
  }
  box.innerHTML = list.map(a =>
    '<article class="t-item s' + a.severity + '"><span class="t-dot"></span><div class="card">' +
    '<div class="t-head"><div class="t-title"><span class="t-icon">' + (CAT_ICON[a.category] || '❗') + '</span><div><b>' + esc(a.categoryLabel) +
    '</b><div class="muted small">' + icon('clock', 12) + ' ' + esc(a.date) + ' · ' + esc(a.clock) + '</div></div></div><div><span class="sev sev-' + a.severity + '">Severity ' + a.severity + '/5</span> ' +
    (a.resolved ? '<span class="badge ok">Resolved</span>' : '<span class="badge warn">Active</span>') + '</div></div>' +
    '<p class="t-loc">' + icon('pin', 14) + ' ' + esc(a.location) + '</p><p class="t-desc">' + esc(a.description) + '</p>' +
    '<details class="t-details"><summary>Message and ' + a.deliveries.length + ' contacts notified</summary>' +
    '<div class="msg-box"><b>Message sent to contacts (simulated)</b>' + esc(a.message) + '</div><ul class="steps">' + deliveryRows(a) + '</ul></details>' +
    '<div class="t-actions">' + (a.resolved ? '' : '<button class="btn soft small" data-resolve="' + a.id + '">' + icon('check', 14) + ' Mark resolved</button>') +
    '</div></div></article>').join('');

  $$('[data-resolve]', box).forEach(b => {
    b.onclick = async () => {
      b.disabled = true;
      try {
        await api('PUT', '/api/alerts/' + b.dataset.resolve + '/resolve');
        toast('Alert marked as resolved', 'success');
        if (state.lastAlert && String(state.lastAlert.id) === b.dataset.resolve) state.lastAlert.resolved = true;
        refreshHistory();
      } catch (err) { toast(err.message, 'error'); b.disabled = false; }
    };
  });
}

function exportHistory() {
  const list = visibleHistory();
  if (list.length === 0) { toast('Nothing to export', 'error'); return; }
  const cell = v => '"' + String(v).replace(/"/g, '""') + '"';
  const rows = [['Date', 'Time', 'Category', 'Severity', 'Location', 'Details', 'Message', 'Status', 'Contacts notified']];
  list.forEach(a => rows.push([a.date, a.clock, a.categoryLabel, a.severity, a.location, a.description, a.message,
    a.resolved ? 'Resolved' : 'Active', a.deliveries.map(d => d.contact).join('; ')]));
  const csv = rows.map(r => r.map(cell).join(',')).join('\n');
  const link = document.createElement('a');
  link.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }));
  link.download = 'sos-history.csv';
  document.body.appendChild(link);
  link.click();
  link.remove();
  toast('History exported', 'success');
}

// =====================================================================
// HELPLINES
// =====================================================================
async function loadHelplines() {
  const view = $('#view-helplines');
  if (view.innerHTML) return;
  view.innerHTML =
    '<div class="grid-cards">' + HELPLINES.map(h =>
      '<div class="card help-card"><div class="num">' + h.num + '</div><div><b>' + h.name + '</b><div class="muted small">' + h.note + '</div></div>' +
      '<div class="help-actions"><a class="icon-btn" href="tel:' + h.num + '" title="Call ' + h.num + '">' + icon('phone', 17) +
      '</a><button class="icon-btn" data-copy="' + h.num + '" title="Copy number">' + icon('copy', 17) + '</button></div></div>').join('') + '</div>' +
    '<div class="card" style="margin-top:20px"><h3>Quick safety checklist</h3><ul class="checklist">' +
    ['Keep your phone charged and your emergency contacts up to date.',
     'Share your live location with at least one trusted person when travelling alone.',
     'Learn the nearest hospital, police station and fire station to your home.',
     'Keep a small first-aid kit at home and in your vehicle.',
     'Stay calm, move to a safe place first and then call for help.'].map(t => '<li>' + icon('check', 17) + '<span>' + t + '</span></li>').join('') + '</ul>' +
    '<p class="hint" style="margin-top:14px">Reference only. This app is a simulator and never places real calls or sends real messages.</p></div>';
  $$('[data-copy]', view).forEach(b => { b.onclick = () => copyText(b.dataset.copy).then(() => toast('Number ' + b.dataset.copy + ' copied', 'success')); });
}

// ---------- start ----------
route();
