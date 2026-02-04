# JavaScript/TypeScript Code Review Checklist

## JavaScript-Specific Correctness

- [ ] `var` not used (use `let`/`const`)
- [ ] `const` preferred over `let` when possible
- [ ] `===` used instead of `==` (strict equality)
- [ ] Array methods used correctly (map, filter, reduce)
- [ ] Promise chains handled properly (no missing `.catch()`)
- [ ] `async`/`await` used instead of nested callbacks
- [ ] Template literals used for string interpolation
- [ ] Destructuring used appropriately

## TypeScript-Specific (if applicable)

- [ ] Types defined for function parameters and returns
- [ ] No use of `any` type (or justified)
- [ ] Interfaces/types defined for data structures
- [ ] Enums used where appropriate
- [ ] Type guards used for narrowing
- [ ] Generics used for reusable components
- [ ] `strict` mode enabled in tsconfig
- [ ] No type assertion abuse (`as` keyword)

## Code Quality

- [ ] Functions are pure when possible
- [ ] Arrow functions used appropriately
- [ ] Spread operator used for immutability
- [ ] ESLint rules followed
- [ ] Prettier formatting applied
- [ ] Comments use JSDoc format for functions
- [ ] No console.log in production code
- [ ] No commented-out code blocks

## Error Handling

- [ ] Try-catch used for async operations
- [ ] Promise rejections handled
- [ ] Error objects thrown (not strings)
- [ ] Custom error classes extend Error properly
- [ ] Unhandled promise rejections prevented
- [ ] Error boundaries used (React)
- [ ] Logging includes stack traces

## Security

- [ ] No eval() or Function() constructor
- [ ] User input sanitized (XSS prevention)
- [ ] CSRF tokens used (if applicable)
- [ ] CORS configured properly
- [ ] Secrets not in client-side code
- [ ] Dependencies scanned for vulnerabilities
- [ ] SQL injection prevented (parameterized queries)
- [ ] Content Security Policy considered

## Performance

- [ ] Debouncing/throttling used for frequent events
- [ ] Memoization applied where beneficial
- [ ] Lazy loading for large modules
- [ ] Bundle size considered
- [ ] No unnecessary re-renders (React)
- [ ] Event listeners cleaned up properly
- [ ] WeakMap/WeakSet used to prevent memory leaks
- [ ] Async operations don't block UI

## React-Specific (if applicable)

- [ ] Hooks rules followed (order, conditions)
- [ ] useEffect dependencies correct
- [ ] Keys used properly in lists
- [ ] Props validated with PropTypes or TypeScript
- [ ] State updates are immutable
- [ ] Controlled components used correctly
- [ ] Context not overused
- [ ] Custom hooks extracted for reusable logic
- [ ] No inline function definitions in JSX (when avoidable)

## Node.js-Specific (if applicable)

- [ ] Async I/O used (not blocking)
- [ ] Streams used for large data
- [ ] Proper error handling in middleware
- [ ] Environment variables for configuration
- [ ] No sensitive data in logs
- [ ] Clustering considered for CPU-intensive tasks
- [ ] Memory leaks prevented (listeners, timers)
- [ ] `process.exit()` avoided in libraries

## Testing

- [ ] Jest/Mocha tests written
- [ ] Unit tests for business logic
- [ ] Integration tests for APIs
- [ ] Mocks used for external dependencies
- [ ] Coverage reports generated
- [ ] Snapshot testing used appropriately (React)
- [ ] Async tests use done() or return promise
- [ ] Test data is isolated

## Dependencies

- [ ] package.json has exact versions or range
- [ ] package-lock.json committed
- [ ] Dependencies are actively maintained
- [ ] No unused dependencies
- [ ] Dev dependencies separated from production
- [ ] Vulnerabilities checked (npm audit)

## Browser Compatibility

- [ ] Polyfills included if needed
- [ ] Babel configuration appropriate
- [ ] Target browsers specified
- [ ] Feature detection used (not browser sniffing)
- [ ] Progressive enhancement applied

## Common AI Agent Issues

- [ ] Not confusing CommonJS and ES modules
- [ ] Not using deprecated React lifecycle methods
- [ ] Not mixing promise and callback patterns
- [ ] Not assuming browser-only or Node-only APIs
- [ ] Correct async/await syntax (no missing await)
- [ ] Event handler this binding handled correctly
- [ ] No outdated jQuery patterns

## Code Organization

- [ ] Components/modules in separate files
- [ ] Named exports for utilities
- [ ] Default exports for components (React)
- [ ] Folder structure follows conventions
- [ ] Barrel exports used appropriately (index.js)
- [ ] Circular dependencies avoided

## Accessibility (Web)

- [ ] Semantic HTML used
- [ ] ARIA labels where needed
- [ ] Keyboard navigation supported
- [ ] Focus management handled
- [ ] Color contrast sufficient
- [ ] Alt text for images
