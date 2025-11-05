#!/bin/bash

# BDD Test Runner Script for ZeroMonos Waste Collection System
# This script starts the backend, frontend, and runs BDD tests

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR/backend"
FRONTEND_DIR="$SCRIPT_DIR/frontend"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if processes are running
check_backend() {
    curl -s http://localhost:8080/api/municipalities > /dev/null 2>&1
    return $?
}

check_frontend() {
    curl -s http://localhost:5173 > /dev/null 2>&1
    return $?
}

# Start backend
start_backend() {
    log_info "Starting backend..."
    cd "$BACKEND_DIR"
    
    # Build backend
    ./mvnw clean install -DskipTests
    
    # Start backend in background
    ./mvnw spring-boot:run > /tmp/zeremonos-backend.log 2>&1 &
    BACKEND_PID=$!
    echo $BACKEND_PID > /tmp/zeremonos-backend.pid
    
    # Wait for backend to start
    log_info "Waiting for backend to start..."
    for i in {1..30}; do
        if check_backend; then
            log_info "Backend is running on http://localhost:8080"
            return 0
        fi
        sleep 2
    done
    
    log_error "Backend failed to start"
    return 1
}

# Start frontend
start_frontend() {
    log_info "Starting frontend..."
    cd "$FRONTEND_DIR"
    
    # Install dependencies if needed
    if [ ! -d "node_modules" ]; then
        log_info "Installing frontend dependencies..."
        npm install
    fi
    
    # Start frontend in background
    npm run dev > /tmp/zeremonos-frontend.log 2>&1 &
    FRONTEND_PID=$!
    echo $FRONTEND_PID > /tmp/zeremonos-frontend.pid
    
    # Wait for frontend to start
    log_info "Waiting for frontend to start..."
    for i in {1..30}; do
        if check_frontend; then
            log_info "Frontend is running on http://localhost:5173"
            return 0
        fi
        sleep 2
    done
    
    log_error "Frontend failed to start"
    return 1
}

# Stop services
stop_services() {
    log_info "Stopping services..."
    
    # Stop backend
    if [ -f /tmp/zeremonos-backend.pid ]; then
        BACKEND_PID=$(cat /tmp/zeremonos-backend.pid)
        if ps -p $BACKEND_PID > /dev/null 2>&1; then
            kill $BACKEND_PID
            log_info "Backend stopped"
        fi
        rm /tmp/zeremonos-backend.pid
    fi
    
    # Stop frontend
    if [ -f /tmp/zeremonos-frontend.pid ]; then
        FRONTEND_PID=$(cat /tmp/zeremonos-frontend.pid)
        if ps -p $FRONTEND_PID > /dev/null 2>&1; then
            kill $FRONTEND_PID
            log_info "Frontend stopped"
        fi
        rm /tmp/zeremonos-frontend.pid
    fi
}

# Run BDD tests
run_bdd_tests() {
    log_info "Running BDD tests..."
    cd "$BACKEND_DIR"
    
    ./mvnw test -Dtest=CucumberTest
    
    local TEST_RESULT=$?
    
    if [ $TEST_RESULT -eq 0 ]; then
        log_info "BDD tests passed!"
        log_info "Reports available at: $BACKEND_DIR/target/cucumber-reports/cucumber.html"
    else
        log_error "BDD tests failed!"
    fi
    
    return $TEST_RESULT
}

# Trap to ensure cleanup on exit
trap stop_services EXIT

# Main execution
main() {
    log_info "=== ZeroMonos BDD Test Runner ==="
    
    # Check if services are already running
    if check_backend; then
        log_warn "Backend is already running"
    else
        start_backend || exit 1
    fi
    
    if check_frontend; then
        log_warn "Frontend is already running"
    else
        start_frontend || exit 1
    fi
    
    # Give services a moment to stabilize
    sleep 5
    
    # Run tests
    run_bdd_tests
    TEST_RESULT=$?
    
    # Stop services
    stop_services
    
    exit $TEST_RESULT
}

# Parse command line arguments
case "${1:-}" in
    start)
        start_backend
        start_frontend
        log_info "Services started. Press Ctrl+C to stop."
        wait
        ;;
    stop)
        stop_services
        ;;
    test)
        main
        ;;
    *)
        echo "Usage: $0 {start|stop|test}"
        echo "  start - Start backend and frontend services"
        echo "  stop  - Stop all services"
        echo "  test  - Run BDD tests (starts services automatically)"
        exit 1
        ;;
esac

