# PHP Code Review Checklist

## PHP-Specific Correctness

- [ ] Proper use of `===` vs `==` (strict vs loose comparison)
- [ ] Variables properly initialized before use
- [ ] Array keys exist before access (use `isset()` or `array_key_exists()`)
- [ ] Null coalescing operator used appropriately (`??`)
- [ ] Type declarations used (PHP 7.0+)
- [ ] Return type declarations present (PHP 7.0+)
- [ ] Proper use of `include` vs `require` (and `_once` variants)
- [ ] No variable variables unless absolutely necessary
- [ ] Proper handling of superglobals ($_GET, $_POST, etc.)
- [ ] Sessions managed correctly (`session_start()`, `session_destroy()`)

## Code Quality

- [ ] PSR-12 coding standards followed
- [ ] Namespaces used properly
- [ ] Autoloading configured (PSR-4)
- [ ] Class names in PascalCase
- [ ] Method names in camelCase
- [ ] Constants in UPPER_SNAKE_CASE
- [ ] Proper indentation (4 spaces)
- [ ] One class per file
- [ ] DocBlocks present for classes and methods
- [ ] No short PHP tags (`<?` should be `<?php`)
- [ ] Closing `?>` tag omitted in pure PHP files

## Object-Oriented Design

- [ ] Proper use of visibility (public, protected, private)
- [ ] Interfaces used for contracts
- [ ] Abstract classes used appropriately
- [ ] Traits used sparingly and wisely
- [ ] Dependency injection over global state
- [ ] Single responsibility per class
- [ ] Composition over inheritance
- [ ] Magic methods used judiciously (`__construct`, `__get`, etc.)
- [ ] No God objects (classes that do too much)

## Error Handling

- [ ] Exceptions used for error handling (not error codes)
- [ ] Custom exceptions extend appropriate base class
- [ ] Try-catch blocks at appropriate levels
- [ ] Finally blocks used for cleanup
- [ ] Error messages are informative
- [ ] Errors logged appropriately
- [ ] No suppressed errors with `@` operator
- [ ] Set proper error reporting level
- [ ] Exception messages don't expose sensitive data

## Security

### Input Validation & Sanitization
- [ ] All user input validated and sanitized
- [ ] `filter_var()` and `filter_input()` used appropriately
- [ ] FILTER_SANITIZE_* and FILTER_VALIDATE_* used correctly
- [ ] Never trust $_GET, $_POST, $_COOKIE, $_FILES, $_REQUEST

### SQL Injection Prevention
- [ ] Prepared statements used (PDO or MySQLi)
- [ ] No direct SQL string concatenation with user input
- [ ] Parameter binding used correctly
- [ ] ORM (Eloquent, Doctrine) used properly if applicable

### XSS Prevention
- [ ] Output escaped with `htmlspecialchars()` or `htmlentities()`
- [ ] ENT_QUOTES flag used
- [ ] Templating engine auto-escaping enabled (Twig, Blade)
- [ ] No raw user input in HTML attributes
- [ ] Content Security Policy headers set

### CSRF Protection
- [ ] CSRF tokens implemented for forms
- [ ] Tokens validated on submission
- [ ] Framework's CSRF protection used (Laravel, Symfony)
- [ ] SameSite cookie attribute set

### Other Security
- [ ] Passwords hashed with `password_hash()` (bcrypt/argon2)
- [ ] Never use MD5 or SHA1 for passwords
- [ ] `password_verify()` used for validation
- [ ] File uploads validated (type, size, extension)
- [ ] Uploaded files moved to secure location
- [ ] File paths validated (no directory traversal)
- [ ] `eval()` avoided completely
- [ ] `unserialize()` only on trusted data
- [ ] Credentials in environment variables (not code)
- [ ] Secrets not in version control
- [ ] SSL/TLS enforced for sensitive operations
- [ ] Session IDs regenerated after login
- [ ] Session cookies set as HttpOnly and Secure

## Performance

- [ ] Database queries optimized (no N+1 problem)
- [ ] Indexes used appropriately
- [ ] Caching implemented (Redis, Memcached)
- [ ] Opcode caching enabled (OPcache)
- [ ] Lazy loading used for heavy operations
- [ ] Connection pooling for databases
- [ ] Avoid queries in loops
- [ ] Use generators for large datasets
- [ ] Assets minified and compressed
- [ ] HTTP/2 and gzip compression enabled

## Database (PDO/MySQLi)

- [ ] PDO or MySQLi used (not deprecated mysql_*)
- [ ] Connection errors handled gracefully
- [ ] Transactions used for multi-query operations
- [ ] Proper use of `beginTransaction()`, `commit()`, `rollback()`
- [ ] Database connections closed appropriately
- [ ] Prepared statements reused when possible
- [ ] Result sets freed after use
- [ ] No SELECT * (specify columns)

## Laravel Specific (if applicable)

- [ ] Eloquent ORM used correctly
- [ ] Mass assignment protection configured
- [ ] Route model binding used appropriately
- [ ] Middleware for authentication/authorization
- [ ] Form requests for validation
- [ ] Resource controllers for REST APIs
- [ ] Queues for long-running tasks
- [ ] Events and listeners for decoupling
- [ ] Service providers for binding
- [ ] Facades used appropriately (testability)
- [ ] Artisan commands for CLI tasks
- [ ] Database migrations for schema changes
- [ ] Seeders for test data
- [ ] `.env` file for configuration
- [ ] Config caching in production

## Testing

- [ ] PHPUnit tests present
- [ ] Unit tests for business logic
- [ ] Feature/integration tests for workflows
- [ ] Mocking used for external dependencies
- [ ] Test database separate from production
- [ ] Assertions are meaningful
- [ ] Edge cases covered
- [ ] Test names are descriptive
- [ ] Code coverage reasonable (>70%)
- [ ] Tests are isolated and independent

## Composer Dependencies

- [ ] `composer.json` present and valid
- [ ] Dependencies have version constraints
- [ ] `composer.lock` committed
- [ ] Unused dependencies removed
- [ ] `require` vs `require-dev` properly separated
- [ ] Autoloading configured (PSR-4)
- [ ] No deprecated packages
- [ ] Security vulnerabilities checked (`composer audit`)

## API Development

- [ ] RESTful conventions followed
- [ ] HTTP status codes used correctly
- [ ] JSON responses properly formatted
- [ ] API versioning implemented
- [ ] Rate limiting configured
- [ ] API authentication (OAuth, JWT)
- [ ] CORS headers set correctly
- [ ] Input validation on all endpoints
- [ ] Pagination for large datasets
- [ ] API documentation present (OpenAPI/Swagger)

## Modern PHP Features (7.0+)

- [ ] Scalar type declarations used
- [ ] Return type declarations used
- [ ] Null coalescing operator (`??`) used
- [ ] Spaceship operator (`<=>`) where appropriate
- [ ] Anonymous classes used judiciously
- [ ] Generators used for memory efficiency
- [ ] Traits used for code reuse
- [ ] Named arguments (PHP 8.0+)
- [ ] Constructor property promotion (PHP 8.0+)
- [ ] Match expressions (PHP 8.0+)
- [ ] Nullsafe operator (`?->`) (PHP 8.0+)
- [ ] Union types (PHP 8.0+)
- [ ] Attributes/Annotations (PHP 8.0+)

## File Operations

- [ ] File existence checked before operations
- [ ] Proper file permissions set
- [ ] Resources closed after use (or use `finally`)
- [ ] File locking used for concurrent access
- [ ] Error handling for file operations
- [ ] Path traversal vulnerabilities prevented
- [ ] Upload directory outside web root
- [ ] File types validated (not just extension)
- [ ] File size limits enforced

## Sessions & Cookies

- [ ] `session_start()` called before output
- [ ] Session data sanitized
- [ ] Session IDs regenerated on privilege change
- [ ] Session timeout configured
- [ ] Cookies set with HttpOnly flag
- [ ] Cookies set with Secure flag (HTTPS)
- [ ] SameSite attribute set
- [ ] Cookie expiration set appropriately

## Logging & Debugging

- [ ] PSR-3 logger interface used
- [ ] Appropriate log levels (debug, info, warning, error)
- [ ] Sensitive data not logged
- [ ] Stack traces captured for errors
- [ ] Monolog or similar logging library used
- [ ] Debug mode disabled in production
- [ ] Display errors off in production
- [ ] Error logs reviewed regularly

## Common AI Agent Issues in PHP

- [ ] Not confusing PHP 5 vs PHP 7/8 syntax
- [ ] Not using deprecated functions (mysql_*, split(), etc.)
- [ ] Not mixing procedural and OOP styles unnecessarily
- [ ] Understanding array vs object access
- [ ] Proper namespace usage (not mixing with global)
- [ ] Not assuming register_globals is enabled
- [ ] Understanding pass-by-reference vs pass-by-value
- [ ] Correct use of static vs self vs parent
- [ ] Not using deprecated error suppression (@)

## WordPress Specific (if applicable)

- [ ] WordPress coding standards followed
- [ ] Proper use of WordPress functions (wp_*, get_*, the_*)
- [ ] Database queries use $wpdb
- [ ] Data sanitization with sanitize_*() functions
- [ ] Output escaped with esc_*() functions
- [ ] Hooks and filters used properly
- [ ] Nonces used for form security
- [ ] Capabilities checked for authorization
- [ ] No direct file access (ABSPATH check)
- [ ] Localization functions used (_e, __, _n)
- [ ] Custom post types registered correctly
- [ ] Enqueue scripts/styles properly

## Email Handling

- [ ] PHPMailer or Swift Mailer used (not mail())
- [ ] Email addresses validated
- [ ] Email headers sanitized (prevent injection)
- [ ] SMTP authentication configured
- [ ] Email queue for bulk sending
- [ ] Unsubscribe links in marketing emails
- [ ] HTML emails have plain text alternative

## Configuration

- [ ] Configuration in environment variables
- [ ] Different configs for dev/staging/production
- [ ] Sensitive data not in version control
- [ ] `.env` file in `.gitignore`
- [ ] Application key properly generated
- [ ] Debug mode off in production
- [ ] Error reporting appropriate per environment

## Code Organization

- [ ] MVC pattern followed (if using framework)
- [ ] Business logic in services/models
- [ ] Controllers thin (no business logic)
- [ ] Reusable code in libraries/helpers
- [ ] Constants in config files
- [ ] No logic in views (only presentation)
- [ ] Dependency injection used
- [ ] Repository pattern for data access (optional)

## Headers & Output

- [ ] Headers sent before any output
- [ ] Content-Type header set correctly
- [ ] Security headers set (X-Frame-Options, etc.)
- [ ] No whitespace before `<?php` or after `?>`
- [ ] UTF-8 encoding used consistently
- [ ] Proper HTTP status codes
- [ ] Cache headers set appropriately
