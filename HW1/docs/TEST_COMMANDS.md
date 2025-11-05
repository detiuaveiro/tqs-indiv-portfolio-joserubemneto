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

---

## BDD/Acceptance Tests

### Prerequisites for BDD Tests
1. Backend must be running on `http://localhost:8080`
2. Frontend must be running on `http://localhost:5173`

### Option 1: Automated Script (Recommended)

```bash
# From HW1 directory
cd HW1
./run-bdd-tests.sh test
```

This script automatically:
- Starts backend
- Starts frontend  
- Waits for services to be ready
- Runs BDD tests
- Generates HTML report
- Cleans up processes

### Option 2: Manual Execution

```bash
# Terminal 1: Start backend
cd HW1/backend
./mvnw spring-boot:run

# Terminal 2: Start frontend
cd HW1/frontend
npm run dev

# Terminal 3: Run BDD tests (wait for services to start first)
cd HW1/backend
./mvnw test -Dtest=CucumberTestRunner
```

### Option 3: Selective BDD Tests

```bash
# Run only smoke tests
./mvnw test -Dtest=CucumberTestRunner -Dcucumber.filter.tags="@smoke"

# Run only critical tests
./mvnw test -Dtest=CucumberTestRunner -Dcucumber.filter.tags="@critical"

# Run only validation tests
./mvnw test -Dtest=CucumberTestRunner -Dcucumber.filter.tags="@validation"

# Run only workflow tests
./mvnw test -Dtest=CucumberTestRunner -Dcucumber.filter.tags="@workflow"

# Run citizen tests only (exclude staff)
./mvnw test -Dtest=CucumberTestRunner -Dcucumber.filter.tags="not @staff"

# Run staff tests only
./mvnw test -Dtest=CucumberTestRunner -Dcucumber.filter.tags="@staff"
```

### View BDD Reports

```bash
# Open HTML report
open target/cucumber-reports/cucumber.html
```

**Expected time:** 3-5 minutes

---

## Performance Tests (Gatling)

### Prerequisites
Backend must be running on `http://localhost:8080`

### Run All Performance Tests

```bash
# Run all simulations
./mvnw gatling:test
```

### Run Specific Simulation

```bash
# Smoke test (quick sanity check, ~30 seconds)
./mvnw gatling:test -Dgatling.simulationClass=com.zeremonos.wastecollection.performance.BasicSmokeTestSimulation

# Load test (realistic load, 5 minutes)
./mvnw gatling:test -Dgatling.simulationClass=com.zeremonos.wastecollection.performance.LoadTestSimulation

# Stress test (extreme load, 3 minutes)
./mvnw gatling:test -Dgatling.simulationClass=com.zeremonos.wastecollection.performance.StressTestSimulation

# Spike test (sudden spikes, ~45 seconds)
./mvnw gatling:test -Dgatling.simulationClass=com.zeremonos.wastecollection.performance.SpikeTestSimulation

# Endurance test (long duration, 30 minutes)
./mvnw gatling:test -Dgatling.simulationClass=com.zeremonos.wastecollection.performance.EnduranceTestSimulation
```

### Customize Parameters

```bash
# Custom number of users
./mvnw gatling:test -Dgatling.simulationClass=com.zeremonos.wastecollection.performance.LoadTestSimulation -Dusers=100

# Custom duration (minutes)
./mvnw gatling:test -Dgatling.simulationClass=com.zeremonos.wastecollection.performance.LoadTestSimulation -Dduration=10

# Custom base URL
./mvnw gatling:test -DbaseUrl=http://production-server:8080
```

### View Performance Reports

```bash
# Open latest Gatling report
open target/gatling/*/index.html

# Or find specific report
ls target/gatling/
open target/gatling/<simulation-name>-<timestamp>/index.html
```

**Expected time:** Varies (30s to 30 minutes depending on simulation)

---

## All Tests Together

Run complete test suite:

```bash
# 1. Unit + Integration tests
./mvnw test

# 2. Generate coverage report
./mvnw jacoco:report

# 3. Start services for BDD (if not running)
# See BDD section above

# 4. Run BDD tests
./run-bdd-tests.sh test

# 5. Run performance tests
./mvnw gatling:test
```

**Total expected time:** ~10-15 minutes

---

## Maven Verify (Recommended for CI/CD)

Run full Maven lifecycle including tests:

```bash
# Clean, compile, test, and generate reports
./mvnw clean verify

# This runs:
# - Compilation
# - Unit tests
# - Integration tests  
# - JaCoCo coverage report
# - Package generation
```

---

## Test Results Location

After running tests, find results at:

```bash
# JUnit test results (XML)
target/surefire-reports/

# JaCoCo coverage report (HTML)
target/site/jacoco/index.html

# Cucumber BDD report (HTML)
target/cucumber-reports/cucumber.html

# Gatling performance reports (HTML)
target/gatling/*/index.html
```

---

## Quick Test Commands Summary

| Test Type | Command | Time |
|-----------|---------|------|
| **Unit only** | `./mvnw test -Dtest='!*IT,!CucumberTestRunner'` | ~15s |
| **Integration only** | `./mvnw test -Dtest='*IT'` | ~45s |
| **Unit + Integration** | `./mvnw test` | ~1m |
| **With coverage** | `./mvnw clean test jacoco:report` | ~1m |
| **BDD (automated)** | `./run-bdd-tests.sh test` | 3-5m |
| **BDD (manual)** | `./mvnw test -Dtest=CucumberTestRunner` | 3-5m |
| **BDD smoke only** | `./mvnw test -Dtest=CucumberTestRunner -Dcucumber.filter.tags="@smoke"` | 1-2m |
| **Performance smoke** | `./mvnw gatling:test -Dgatling.simulationClass=...BasicSmokeTestSimulation` | ~30s |
| **Performance all** | `./mvnw gatling:test` | varies |
| **Full suite** | `./mvnw clean verify` | ~5m |

---

## Troubleshooting

### Tests Fail to Connect to Database

```bash
# Clean and rebuild
./mvnw clean test
```

### BDD Tests Can't Connect to Frontend/Backend

```bash
# Check services are running
curl http://localhost:8080/api/municipalities
curl http://localhost:5173

# Restart services if needed
```

### Port Already in Use

```bash
# Kill processes on port 8080
lsof -ti:8080 | xargs kill -9

# Kill processes on port 5173
lsof -ti:5173 | xargs kill -9
```

### ChromeDriver Issues (BDD Tests)

```bash
# WebDriverManager handles this automatically
# But if issues persist, install manually:
brew install chromedriver

# Allow ChromeDriver to run
xattr -d com.apple.quarantine /opt/homebrew/bin/chromedriver
```

---

## Continuous Integration Example

```bash
#!/bin/bash
# CI pipeline test execution

set -e  # Exit on error

echo "Running Unit and Integration Tests..."
./mvnw clean test

echo "Generating Coverage Report..."
./mvnw jacoco:report

echo "Starting Services for BDD Tests..."
./mvnw spring-boot:run &
BACKEND_PID=$!
cd ../frontend && npm run dev &
FRONTEND_PID=$!

echo "Waiting for services..."
sleep 15

echo "Running BDD Tests..."
cd ../backend
./mvnw test -Dtest=CucumberTestRunner

echo "Running Performance Smoke Test..."
./mvnw gatling:test -Dgatling.simulationClass=com.zeremonos.wastecollection.performance.BasicSmokeTestSimulation

echo "Cleanup..."
kill $BACKEND_PID $FRONTEND_PID

echo "All tests passed!"
```

---

## Best Practices

1. **Run unit tests frequently** during development (fast feedback)
2. **Run integration tests before committing** (verify component interaction)
3. **Run BDD tests before pull request** (ensure user workflows work)
4. **Run performance tests before deployment** (validate non-functional requirements)
5. **Run full suite in CI/CD pipeline** (comprehensive quality gate)

---

For more details on each test type, see:
- Unit/Integration: `docs/REPORT_UNIT_INTEGRATION_TESTING.md`
- Acceptance (BDD): `docs/REPORT_ACCEPTANCE_TESTING.md`
- Performance: `docs/PERFORMANCE_TESTING.md`

