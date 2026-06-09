@echo off
echo Starting MkSafeNet Backend...
cd backend
mvn clean install
mvn spring-boot:run
pause
