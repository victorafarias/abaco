# General Code Review Checklist

Use this checklist for languages without specific guidance or as a baseline for all reviews.

## Correctness

- [ ] Code solves the stated problem
- [ ] Logic is sound and bug-free
- [ ] Edge cases are handled (null, empty, boundaries)
- [ ] No off-by-one errors
- [ ] Concurrency issues considered (if applicable)
- [ ] Data types are appropriate
- [ ] No hardcoded values that should be configurable

## Code Quality

- [ ] Functions are single-responsibility
- [ ] Naming is clear and descriptive
- [ ] Code is DRY (Don't Repeat Yourself)
- [ ] Cyclomatic complexity is reasonable (<10 per function)
- [ ] Nesting depth is manageable (<4 levels)
- [ ] Comments explain WHY, not WHAT
- [ ] No dead code or commented-out blocks
- [ ] Magic numbers are named constants

## Error Handling

- [ ] Errors are caught and handled appropriately
- [ ] Error messages are informative
- [ ] No silent failures
- [ ] Resources are properly cleaned up (files, connections)
- [ ] Graceful degradation where appropriate
- [ ] Logging includes sufficient context

## Security

- [ ] Input validation is present
- [ ] No SQL injection vulnerabilities
- [ ] No code injection risks
- [ ] Credentials not hardcoded
- [ ] Sensitive data not logged
- [ ] Authentication/authorization present where needed
- [ ] File paths are validated
- [ ] No unsafe deserialization

## Performance

- [ ] Algorithm complexity is appropriate
- [ ] No unnecessary iterations
- [ ] Resources released promptly
- [ ] Lazy loading where appropriate
- [ ] No obvious memory leaks
- [ ] Database queries are efficient
- [ ] Caching used appropriately

## Testability

- [ ] Code is modular and loosely coupled
- [ ] Dependencies are injectable
- [ ] No tight coupling to external services
- [ ] Side effects are minimized
- [ ] Global state avoided
- [ ] Time-dependent code is abstracted

## Documentation

- [ ] Public APIs are documented
- [ ] Complex logic is explained
- [ ] Assumptions are stated
- [ ] Dependencies are listed
- [ ] Setup instructions present (if needed)

## Language Conventions

- [ ] Follows standard naming conventions
- [ ] Consistent formatting
- [ ] Idiomatic patterns used
- [ ] Standard library used appropriately
- [ ] No anti-patterns

## AI-Specific Checks

- [ ] No hallucinated functions or libraries
- [ ] Library versions are compatible
- [ ] Patterns are current (not deprecated)
- [ ] Requirements understood correctly
- [ ] Solution complexity matches problem
- [ ] No copy-paste artifacts from training data
