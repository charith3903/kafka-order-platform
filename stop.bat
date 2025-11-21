@echo off
echo ========================================
echo   Stopping All Services
echo   Student: EG/2020/3903
echo ========================================
echo.

echo Stopping Docker services...
docker-compose down

echo.
echo All services stopped successfully!
echo.
pause
