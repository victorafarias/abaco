# Java Code Review Checklist

## Java-Specific Correctness

- [ ] Proper use of equals() and hashCode() (override both or neither)
- [ ] Null checks where needed (or use Optional)
- [ ] Correct exception hierarchy (checked vs unchecked)
- [ ] Generics used with proper type parameters
- [ ] AutoCloseable resources managed with try-with-resources
- [ ] Immutable objects truly immutable (final fields, no setters)
- [ ] Thread safety considered for shared state
- [ ] Serialization handled correctly (serialVersionUID if Serializable)

## Code Quality

- [ ] Package naming follows reverse domain convention
- [ ] Class names are nouns (PascalCase)
- [ ] Method names are verbs (camelCase)
- [ ] Constants in UPPER_SNAKE_CASE
- [ ] Code follows Java conventions (Oracle style guide)
- [ ] Methods have single responsibility
- [ ] Classes are cohesive
- [ ] Javadoc present for public APIs
- [ ] No magic numbers (use constants)
- [ ] Proper access modifiers (private, protected, public)

## Object-Oriented Design

- [ ] Appropriate use of inheritance vs composition
- [ ] Interface segregation (small, focused interfaces)
- [ ] Dependency inversion (depend on abstractions)
- [ ] Encapsulation maintained (no exposed internals)
- [ ] Liskov substitution principle respected
- [ ] Single responsibility per class
- [ ] Open/closed principle (open for extension, closed for modification)

## Error Handling

- [ ] Exceptions caught at appropriate level
- [ ] Custom exceptions extend appropriate base class
- [ ] Exception messages are informative
- [ ] Resources cleaned up in finally or try-with-resources
- [ ] Checked exceptions declared in method signature
- [ ] No empty catch blocks
- [ ] Logging includes context and stack traces
- [ ] Don't catch Throwable or Error

## Security

- [ ] Input validation present
- [ ] SQL queries parameterized (PreparedStatement)
- [ ] No SQL injection vulnerabilities
- [ ] Sensitive data not logged
- [ ] Deserialization only from trusted sources
- [ ] File paths validated
- [ ] No hardcoded credentials
- [ ] Cryptography uses secure algorithms (not DES, MD5)
- [ ] Random numbers use SecureRandom for security

## Performance

- [ ] StringBuilder used for string concatenation in loops
- [ ] Collections sized appropriately
- [ ] Streams not overused (simple loops sometimes better)
- [ ] Database queries optimized
- [ ] Lazy loading used appropriately
- [ ] Caching implemented where beneficial
- [ ] No unnecessary object creation in loops
- [ ] Primitive types used when possible (not wrappers)

## Concurrency (if applicable)

- [ ] Thread-safe collections used (ConcurrentHashMap, etc.)
- [ ] Synchronization minimal and correct
- [ ] No race conditions
- [ ] Volatile keyword used correctly
- [ ] Atomic classes used for simple atomicity
- [ ] Executor framework used (not raw Threads)
- [ ] Proper shutdown of executor services
- [ ] Deadlock potential avoided

## Memory Management

- [ ] Collections cleared when no longer needed
- [ ] Listeners and observers unregistered
- [ ] Static collections don't cause leaks
- [ ] Weak/Soft references used appropriately
- [ ] Large objects released promptly
- [ ] No circular references preventing GC

## Testing

- [ ] JUnit or TestNG tests present
- [ ] Unit tests for business logic
- [ ] Integration tests for persistence/external services
- [ ] Mocking used for external dependencies (Mockito)
- [ ] Test data isolated
- [ ] Assertions are meaningful
- [ ] Edge cases tested
- [ ] Test methods follow naming convention (should_X_when_Y)

## Dependencies

- [ ] pom.xml (Maven) or build.gradle (Gradle) present
- [ ] Dependencies have explicit versions
- [ ] Unused dependencies removed
- [ ] No conflicting dependency versions
- [ ] Latest stable versions used (security)
- [ ] Scope appropriately set (compile, test, provided)

## Spring Boot (if applicable)

- [ ] Proper use of @Component, @Service, @Repository
- [ ] @Autowired used appropriately (constructor injection preferred)
- [ ] Configuration externalized (application.properties/yml)
- [ ] Profiles used for different environments
- [ ] REST endpoints follow conventions (GET, POST, PUT, DELETE)
- [ ] Request/response DTOs used
- [ ] Exception handling with @ControllerAdvice
- [ ] Validation annotations used (@Valid, @NotNull, etc.)

## Hibernate/JPA (if applicable)

- [ ] Entities properly annotated
- [ ] Lazy loading configured correctly
- [ ] N+1 query problem avoided
- [ ] Cascading operations used carefully
- [ ] Proper transaction boundaries
- [ ] Native queries only when necessary
- [ ] Entity relationships mapped correctly

## Logging

- [ ] SLF4J with Logback/Log4j2 used
- [ ] Appropriate log levels (TRACE, DEBUG, INFO, WARN, ERROR)
- [ ] No System.out.println in production code
- [ ] Structured logging where beneficial
- [ ] Sensitive data not logged
- [ ] Log messages informative

## Common AI Agent Issues in Java

- [ ] Not confusing Java 8 vs newer versions
- [ ] Not using deprecated APIs (Date vs LocalDateTime)
- [ ] Not mixing old and new I/O (File vs Path)
- [ ] Correct lambda syntax
- [ ] Proper Stream API usage
- [ ] Understanding checked vs unchecked exceptions
- [ ] Not assuming default visibility is good
- [ ] Package-private vs public distinction understood

## Java Best Practices

- [ ] Prefer interfaces to abstract classes
- [ ] Favor composition over inheritance
- [ ] Use enums for fixed sets of constants
- [ ] Implement Comparable for natural ordering
- [ ] Override toString() for debugging
- [ ] Use @Override annotation
- [ ] Minimize mutability
- [ ] Minimize accessibility
- [ ] Use varargs appropriately

## Build & Deployment

- [ ] Main class specified in manifest/config
- [ ] Resources in correct directory (src/main/resources)
- [ ] Properties files for configuration
- [ ] Dockerfile present (if containerized)
- [ ] Health check endpoint (Spring Actuator)
- [ ] Metrics collection configured
