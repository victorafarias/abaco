# Security Patterns & Anti-Patterns

Common security vulnerabilities to check for when reviewing AI-generated code.

## Input Validation

### ❌ Vulnerable Pattern
```python
# Python
user_id = request.args.get('id')
user = db.execute(f"SELECT * FROM users WHERE id = {user_id}")
```

```javascript
// JavaScript
const userId = req.query.id;
const user = await db.query(`SELECT * FROM users WHERE id = ${userId}`);
```

### ✅ Secure Pattern
```python
# Python
user_id = request.args.get('id')
if not user_id.isdigit():
    raise ValueError("Invalid user ID")
user = db.execute("SELECT * FROM users WHERE id = ?", (user_id,))
```

```javascript
// JavaScript
const userId = parseInt(req.query.id);
if (isNaN(userId)) {
    throw new Error("Invalid user ID");
}
const user = await db.query("SELECT * FROM users WHERE id = $1", [userId]);
```

## SQL Injection

### ❌ Vulnerable
```python
# String formatting
query = "SELECT * FROM users WHERE email = '%s'" % email
query = f"SELECT * FROM users WHERE email = '{email}'"
```

### ✅ Secure
```python
# Parameterized queries
query = "SELECT * FROM users WHERE email = ?"
cursor.execute(query, (email,))

# ORM
user = User.objects.filter(email=email).first()
```

## XSS (Cross-Site Scripting)

### ❌ Vulnerable
```javascript
// Direct HTML insertion
div.innerHTML = userInput;
element.outerHTML = `<div>${userData}</div>`;
```

### ✅ Secure
```javascript
// Text content only
div.textContent = userInput;

// Sanitized HTML
import DOMPurify from 'dompurify';
div.innerHTML = DOMPurify.sanitize(userInput);
```

## Command Injection

### ❌ Vulnerable
```python
# Direct shell execution
import os
filename = request.args.get('file')
os.system(f'cat {filename}')
```

### ✅ Secure
```python
# Avoid shell when possible
import subprocess
import shlex

filename = request.args.get('file')
# Validate filename first
if not is_safe_filename(filename):
    raise ValueError("Invalid filename")
subprocess.run(['cat', filename], check=True)
```

## Path Traversal

### ❌ Vulnerable
```python
# Direct path construction
import os
filename = request.args.get('file')
filepath = os.path.join('/uploads', filename)
with open(filepath, 'r') as f:
    content = f.read()
```

### ✅ Secure
```python
# Validate and normalize
import os
from pathlib import Path

filename = request.args.get('file')
base_dir = Path('/uploads')
filepath = (base_dir / filename).resolve()

# Ensure path is within base directory
if not filepath.is_relative_to(base_dir):
    raise ValueError("Invalid file path")

with open(filepath, 'r') as f:
    content = f.read()
```

## Hardcoded Secrets

### ❌ Vulnerable
```python
# Secrets in code
API_KEY = "sk_live_123456789abcdef"
DB_PASSWORD = "mypassword123"

# Secrets in version control
config = {
    'api_key': 'hardcoded_key_here'
}
```

### ✅ Secure
```python
# Environment variables
import os
API_KEY = os.environ.get('API_KEY')
DB_PASSWORD = os.environ.get('DB_PASSWORD')

# Secret management service
from secretmanager import get_secret
API_KEY = get_secret('api_key')
```

## Insecure Deserialization

### ❌ Vulnerable
```python
# Pickle untrusted data
import pickle
user_data = pickle.loads(request.data)
```

### ✅ Secure
```python
# JSON for untrusted data
import json
user_data = json.loads(request.data)

# Or use schema validation
from pydantic import BaseModel
class UserData(BaseModel):
    name: str
    age: int
user_data = UserData.parse_raw(request.data)
```

## Authentication & Authorization

### ❌ Vulnerable
```python
# No authentication check
@app.route('/admin/users')
def get_users():
    return User.query.all()

# Client-side only checks
if (user.role === 'admin') {
    showAdminPanel();
}
```

### ✅ Secure
```python
# Server-side authentication
from functools import wraps

def require_auth(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        if not current_user.is_authenticated:
            abort(401)
        if not current_user.has_role('admin'):
            abort(403)
        return f(*args, **kwargs)
    return decorated

@app.route('/admin/users')
@require_auth
def get_users():
    return User.query.all()
```

## Cryptography

### ❌ Vulnerable
```python
# Weak hashing
import hashlib
password_hash = hashlib.md5(password.encode()).hexdigest()

# Custom crypto
def encrypt(text, key):
    return ''.join(chr(ord(c) ^ ord(k)) for c, k in zip(text, key))
```

### ✅ Secure
```python
# Strong hashing with salt
import bcrypt
password_hash = bcrypt.hashpw(password.encode(), bcrypt.gensalt())

# Verify
if bcrypt.checkpw(password.encode(), stored_hash):
    # Password correct
    pass

# Use established libraries
from cryptography.fernet import Fernet
key = Fernet.generate_key()
cipher = Fernet(key)
encrypted = cipher.encrypt(plaintext.encode())
```

## Session Management

### ❌ Vulnerable
```javascript
// Session ID in URL
const sessionId = req.query.session;

// No expiration
sessions[userId] = { loggedIn: true };
```

### ✅ Secure
```javascript
// Secure cookies
res.cookie('session', sessionId, {
    httpOnly: true,
    secure: true,
    sameSite: 'strict',
    maxAge: 3600000 // 1 hour
});

// Server-side session with expiration
session.set(userId, {
    loggedIn: true,
    expiresAt: Date.now() + 3600000
});
```

## CSRF Protection

### ❌ Vulnerable
```python
# No CSRF token
@app.route('/transfer', methods=['POST'])
def transfer_money():
    amount = request.form['amount']
    to_account = request.form['to_account']
    transfer(amount, to_account)
```

### ✅ Secure
```python
# CSRF token validation
from flask_wtf.csrf import CSRFProtect
csrf = CSRFProtect(app)

@app.route('/transfer', methods=['POST'])
@csrf.exempt  # Only if using custom CSRF
def transfer_money():
    # Token verified automatically by Flask-WTF
    amount = request.form['amount']
    to_account = request.form['to_account']
    transfer(amount, to_account)
```

## File Upload

### ❌ Vulnerable
```python
# No validation
file = request.files['upload']
file.save(f'/uploads/{file.filename}')
```

### ✅ Secure
```python
# Validation and sanitization
import os
from werkzeug.utils import secure_filename

ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'gif'}
MAX_FILE_SIZE = 10 * 1024 * 1024  # 10MB

def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

file = request.files['upload']
if not file or not allowed_file(file.filename):
    abort(400)

if len(file.read()) > MAX_FILE_SIZE:
    abort(400)
file.seek(0)

filename = secure_filename(file.filename)
filepath = os.path.join('/uploads', filename)
file.save(filepath)
```

## CORS Configuration

### ❌ Vulnerable
```javascript
// Allow all origins
app.use(cors({
    origin: '*',
    credentials: true
}));
```

### ✅ Secure
```javascript
// Whitelist specific origins
const allowedOrigins = ['https://trusted-site.com'];

app.use(cors({
    origin: (origin, callback) => {
        if (allowedOrigins.includes(origin) || !origin) {
            callback(null, true);
        } else {
            callback(new Error('Not allowed by CORS'));
        }
    },
    credentials: true
}));
```

## Logging Sensitive Data

### ❌ Vulnerable
```python
# Logging passwords/tokens
logger.info(f"User logged in: {username}, password: {password}")
logger.debug(f"API call with token: {api_token}")
```

### ✅ Secure
```python
# Never log secrets
logger.info(f"User logged in: {username}")
logger.debug(f"API call authenticated")

# Sanitize before logging
def sanitize_for_log(data):
    sensitive_fields = {'password', 'token', 'api_key', 'ssn'}
    return {k: '***' if k in sensitive_fields else v 
            for k, v in data.items()}

logger.info(f"Request data: {sanitize_for_log(request_data)}")
```

## Rate Limiting

### ❌ Vulnerable
```python
# No rate limiting
@app.route('/api/login', methods=['POST'])
def login():
    # Brute force attack possible
    return authenticate(request.form)
```

### ✅ Secure
```python
# Rate limiting
from flask_limiter import Limiter

limiter = Limiter(app, key_func=get_remote_address)

@app.route('/api/login', methods=['POST'])
@limiter.limit("5 per minute")
def login():
    return authenticate(request.form)
```

## Security Headers

### ❌ Missing Headers
```javascript
// No security headers
app.get('/', (req, res) => {
    res.send('<h1>Hello</h1>');
});
```

### ✅ Security Headers
```javascript
// Add security headers
const helmet = require('helmet');
app.use(helmet());

// Or manually
app.use((req, res, next) => {
    res.setHeader('X-Content-Type-Options', 'nosniff');
    res.setHeader('X-Frame-Options', 'DENY');
    res.setHeader('X-XSS-Protection', '1; mode=block');
    res.setHeader('Strict-Transport-Security', 'max-age=31536000');
    next();
});
```

## Common AI Code Gen Security Mistakes

1. **Assuming sanitized input**: AI often generates code assuming inputs are safe
2. **Using deprecated crypto**: May use MD5, SHA1, or weak algorithms
3. **Missing authentication**: Focuses on functionality, forgets security
4. **Hardcoded secrets in examples**: Training data includes hardcoded keys
5. **No rate limiting**: Doesn't consider abuse scenarios
6. **Trusting client-side validation**: Only validates in frontend
7. **Using eval/exec**: Quick but dangerous solutions
8. **No HTTPS enforcement**: Generates HTTP-only examples
