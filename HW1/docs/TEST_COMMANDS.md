## Unit Tests

Run only unit tests (fastest):

```bash
# All unit tests (excluding integration and BDD)
./mvnw test -Dtest='!*IT,!CucumberTestRunner'

# Specific unit test class
./mvnw test -Dtest=ServiceRequestServiceTest
./mvnw test -Dtest=ServiceRequestValidationTest
```

## Integration Tests

Run only integration tests:

```bash
# All integration tests (classes ending with IT)
./mvnw test -Dtest='*IT'

# Specific integration test
./mvnw test -Dtest=ServiceRequestControllerIT
./mvnw test -Dtest=ServiceRequestRepositoryTest
```

---

## Unit + Integration Tests

Run both unit and integration tests (default):

```bash
# All tests except BDD
./mvnw test

# With code coverage report
./mvnw clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```
