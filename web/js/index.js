if (getToken()) window.location.href = 'dashboard.html';
initTheme();
$$('[data-icon]').forEach(el => { el.innerHTML = icon(el.dataset.icon, 22); });

const loginForm = $('#loginForm');
const registerForm = $('#registerForm');

// ---------- Login / Register tabs ----------
function showTab(loginActive) {
  loginForm.classList.toggle('hidden', !loginActive);
  registerForm.classList.toggle('hidden', loginActive);
  $('#tabLogin').classList.toggle('active', loginActive);
  $('#tabRegister').classList.toggle('active', !loginActive);
  $('#formTitle').textContent = loginActive ? 'Welcome back' : 'Create your account';
  $('#formSub').textContent = loginActive
    ? 'Sign in to manage your emergency contacts.'
    : 'It takes less than a minute to get protected.';
  $('#loginError').textContent = '';
  $('#regError').textContent = '';
}
$('#tabLogin').onclick = () => showTab(true);
$('#tabRegister').onclick = () => showTab(false);

// ---------- Show / hide password ----------
$$('[data-toggle]').forEach(btn => {
  btn.onclick = () => {
    const input = $('#' + btn.dataset.toggle);
    const show = input.type === 'password';
    input.type = show ? 'text' : 'password';
    btn.textContent = show ? 'Hide' : 'Show';
  };
});

// ---------- Password strength meter ----------
$('#regPassword').oninput = e => {
  const p = e.target.value;
  let score = 0;
  if (p.length >= 6) score++;
  if (p.length >= 10) score++;
  if (/[A-Z]/.test(p) && /[a-z]/.test(p)) score++;
  if (/\d/.test(p) && /[^A-Za-z0-9]/.test(p)) score++;
  $('#meter').className = 'meter' + (p ? ' s' + Math.max(score, 1) : '');
  $('#meterText').textContent = !p ? 'Use letters, numbers and symbols for a stronger password.'
    : ['', 'Weak', 'Okay', 'Good', 'Strong'][Math.max(score, 1)] + ' password' + (p.length < 6 ? ' (minimum 6 characters)' : '');
};

function setLoading(button, loading, label) {
  button.disabled = loading;
  button.innerHTML = loading ? '<span class="spinner"></span> Please wait...' : label;
}

// ---------- Submit ----------
loginForm.onsubmit = async e => {
  e.preventDefault();
  const error = $('#loginError');
  error.textContent = '';
  const email = $('#loginEmail').value.trim();
  const password = $('#loginPassword').value;
  if (!email || !password) { error.textContent = 'Enter your email and password.'; return; }

  setLoading($('#loginBtn'), true);
  try {
    const data = await api('POST', '/api/auth/login', { email: email, password: password });
    localStorage.setItem('sosToken', data.token);
    localStorage.setItem('sosUser', data.user.name);
    window.location.href = 'dashboard.html';
  } catch (err) {
    error.textContent = err.message;
    setLoading($('#loginBtn'), false, 'Login');
  }
};

registerForm.onsubmit = async e => {
  e.preventDefault();
  const error = $('#regError');
  error.textContent = '';
  const name = $('#regName').value.trim();
  const email = $('#regEmail').value.trim();
  const password = $('#regPassword').value;
  if (!name || !email || !password) { error.textContent = 'Please fill in all the fields.'; return; }

  setLoading($('#regBtn'), true);
  try {
    await api('POST', '/api/auth/register', { name: name, email: email, password: password });
    registerForm.reset();
    $('#meter').className = 'meter';
    showTab(true);
    $('#loginEmail').value = email;
    $('#loginPassword').focus();
    toast('Account created. Please login.', 'success');
  } catch (err) {
    error.textContent = err.message;
  }
  setLoading($('#regBtn'), false, 'Create account');
};
