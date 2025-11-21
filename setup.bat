@echo off
echo ========================================
echo   Kafka Order System - Complete Setup
echo   Student: EG/2020/3903
echo ========================================
echo.

echo [1/4] Starting Kafka infrastructure...
docker-compose up -d

echo.
echo [2/4] Waiting for services to initialize (30 seconds)...
timeout /t 30 /nobreak

echo.
echo [3/4] Building backend...
cd backend
call mvn clean install -DskipTests
cd ..

echo.
echo [4/4] Installing frontend dependencies...
cd frontend
call npm install
cd ..

echo.
echo ========================================
echo   Setup Complete!
echo ========================================
echo.
echo Next steps:
echo   1. Start Backend:  cd backend  and  mvn spring-boot:run
echo   2. Start Frontend: cd frontend and  npm start
echo   3. Open browser:   http://localhost:3000
echo.
pause
