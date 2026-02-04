# Python Code Review Checklist

## Python-Specific Correctness

- [ ] Correct use of mutable default arguments (avoid `def func(x=[])`)
- [ ] Proper iterator/generator usage
- [ ] List comprehensions used appropriately (not overly complex)
- [ ] Dictionary/set operations used efficiently
- [ ] `is` vs `==` used correctly (identity vs equality)
- [ ] String formatting follows modern conventions (f-strings preferred)
- [ ] Context managers used for resource management (`with` statements)

## Code Quality

- [ ] PEP 8 style guide followed (mostly)
- [ ] Functions/methods have docstrings
- [ ] Type hints used (Python 3.5+)
- [ ] Meaningful variable names (no single letters except counters)
- [ ] List comprehensions not nested more than 2 levels
- [ ] Lambda functions kept simple (single expression)
- [ ] Classes use `__init__` properly
- [ ] `@property` decorator used for getters when appropriate

## Error Handling

- [ ] Specific exceptions caught (avoid bare `except:`)
- [ ] Custom exceptions inherit from appropriate base
- [ ] `finally` clause used for cleanup
- [ ] Context managers handle resource cleanup
- [ ] No swallowed exceptions without logging
- [ ] `raise ... from` used for exception chaining

## Security

- [ ] `eval()` and `exec()` avoided
- [ ] SQL queries use parameterization
- [ ] File operations use safe paths (no direct user input)
- [ ] `pickle` avoided for untrusted data
- [ ] Secrets managed via environment variables or secrets manager
- [ ] Input validation using appropriate libraries (pydantic, etc.)
- [ ] Shell commands avoid direct string interpolation (use `shlex`)

## Performance

- [ ] Generators used for large datasets
- [ ] Sets/dicts used for membership testing (not lists)
- [ ] `join()` used instead of string concatenation in loops
- [ ] List comprehensions preferred over `map()`/`filter()` (readability)
- [ ] `__slots__` used for memory optimization (if needed)
- [ ] Unnecessary copies avoided (use views, references)
- [ ] `itertools` used for efficient iteration patterns

## Testing

- [ ] pytest or unittest framework used
- [ ] Fixtures used for test data
- [ ] Mocking used for external dependencies
- [ ] Tests are isolated and independent
- [ ] Edge cases covered
- [ ] `@pytest.mark.parametrize` for test variations

## Dependencies

- [ ] `requirements.txt` or `pyproject.toml` present
- [ ] Version constraints specified
- [ ] Virtual environment usage implied/documented
- [ ] Standard library used where possible (avoid dependencies)
- [ ] No circular imports

## Python Idioms

- [ ] EAFP (Easier to Ask Forgiveness than Permission) over LBYL
- [ ] Duck typing leveraged appropriately
- [ ] `enumerate()` used instead of manual counters
- [ ] `zip()` used for parallel iteration
- [ ] Unpacking used effectively (`a, b = get_values()`)
- [ ] `collections` module used (defaultdict, Counter, etc.)
- [ ] Pathlib used for file paths (not string manipulation)

## Common AI Agent Issues in Python

- [ ] Not confusing Python 2 vs Python 3 syntax
- [ ] Not using deprecated libraries (e.g., `imp` instead of `importlib`)
- [ ] Not mixing async and sync code incorrectly
- [ ] Not assuming libraries are installed (check imports)
- [ ] Type hints match actual types
- [ ] No incorrect assumptions about GIL behavior

## Async Code (if applicable)

- [ ] `async`/`await` used correctly
- [ ] Not mixing blocking and async code
- [ ] Event loop managed properly
- [ ] `asyncio.create_task()` vs `await` understood
- [ ] Async context managers used (`async with`)
- [ ] Cancellation handled appropriately

## Data Science (if applicable)

- [ ] NumPy vectorization used (avoid loops)
- [ ] Pandas chaining used appropriately
- [ ] Memory-efficient data types chosen
- [ ] Copy vs view distinction understood
- [ ] Index operations used correctly
- [ ] Plotting code separated from computation
