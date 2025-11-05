#!/bin/bash

# ZeroMonos - Helper Script
# Manages backend and frontend servers

case "$1" in
  start)
    echo "🚀 Starting ZeroMonos servers..."
    
    # Start backend
    echo "📦 Starting backend..."
    cd backend
    ./mvnw spring-boot:run > /tmp/zeremonos-backend.log 2>&1 &
    BACKEND_PID=$!
    echo $BACKEND_PID > /tmp/zeremonos-backend.pid
    echo "   Backend PID: $BACKEND_PID"
    cd ..
    
    # Wait for backend to start
    echo "   Waiting for backend to start..."
    sleep 10
    
    # Start frontend
    echo "🎨 Starting frontend..."
    cd frontend
    npm run dev > /tmp/zeremonos-frontend.log 2>&1 &
    FRONTEND_PID=$!
    echo $FRONTEND_PID > /tmp/zeremonos-frontend.pid
    echo "   Frontend PID: $FRONTEND_PID"
    cd ..
    
    echo ""
    echo "✅ Servers started successfully!"
    echo "   Backend:  http://localhost:8080"
    echo "   Frontend: http://localhost:5173"
    echo "   H2 Console: http://localhost:8080/h2-console"
    echo ""
    echo "To stop servers, run: ./run.sh stop"
    ;;
    
  stop)
    echo "🛑 Stopping ZeroMonos servers..."
    
    if [ -f /tmp/zeremonos-backend.pid ]; then
      BACKEND_PID=$(cat /tmp/zeremonos-backend.pid)
      echo "   Stopping backend (PID: $BACKEND_PID)..."
      kill $BACKEND_PID 2>/dev/null
      rm /tmp/zeremonos-backend.pid
    fi
    
    if [ -f /tmp/zeremonos-frontend.pid ]; then
      FRONTEND_PID=$(cat /tmp/zeremonos-frontend.pid)
      echo "   Stopping frontend (PID: $FRONTEND_PID)..."
      kill $FRONTEND_PID 2>/dev/null
      rm /tmp/zeremonos-frontend.pid
    fi
    
    echo "✅ Servers stopped"
    ;;
    
  restart)
    $0 stop
    sleep 2
    $0 start
    ;;
    
  status)
    echo "📊 ZeroMonos Server Status"
    echo ""
    
    if [ -f /tmp/zeremonos-backend.pid ]; then
      BACKEND_PID=$(cat /tmp/zeremonos-backend.pid)
      if ps -p $BACKEND_PID > /dev/null; then
        echo "✅ Backend: Running (PID: $BACKEND_PID)"
        echo "   URL: http://localhost:8080"
      else
        echo "❌ Backend: Not running (stale PID file)"
      fi
    else
      echo "❌ Backend: Not running"
    fi
    
    if [ -f /tmp/zeremonos-frontend.pid ]; then
      FRONTEND_PID=$(cat /tmp/zeremonos-frontend.pid)
      if ps -p $FRONTEND_PID > /dev/null; then
        echo "✅ Frontend: Running (PID: $FRONTEND_PID)"
        echo "   URL: http://localhost:5173"
      else
        echo "❌ Frontend: Not running (stale PID file)"
      fi
    else
      echo "❌ Frontend: Not running"
    fi
    ;;
    
  logs)
    case "$2" in
      backend)
        tail -f /tmp/zeremonos-backend.log
        ;;
      frontend)
        tail -f /tmp/zeremonos-frontend.log
        ;;
      *)
        echo "Usage: ./run.sh logs [backend|frontend]"
        ;;
    esac
    ;;
    
  test)
    echo "🧪 Running backend tests..."
    cd backend
    ./mvnw test
    ;;
    
  *)
    echo "ZeroMonos - Waste Collection System"
    echo ""
    echo "Usage: ./run.sh [command]"
    echo ""
    echo "Commands:"
    echo "  start     - Start backend and frontend servers"
    echo "  stop      - Stop all servers"
    echo "  restart   - Restart all servers"
    echo "  status    - Show server status"
    echo "  logs      - Show logs (backend|frontend)"
    echo "  test      - Run backend tests"
    echo ""
    ;;
esac

