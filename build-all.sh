#!/usr/bin/env bash
set -e

echo "========================================="
echo "   Building CodeJIT Full Stack Platform  "
echo "========================================="

# 1. Build and test backend
echo ""
echo "[1/2] Building Backend Microservices..."
cd backend
export JAVA_HOME=${JAVA_HOME:-/opt/homebrew/opt/openjdk}
export PATH="$JAVA_HOME/bin:$PATH"
mvn clean test package -DskipTests=false
cd ..

# 2. Build and test frontend
echo ""
echo "[2/2] Building Frontend SPA..."
cd frontend
npm test
npm run build
cd ..

echo ""
echo "========================================="
echo "   Build & Test Completed Successfully!  "
echo "========================================="

