@echo off 
¡¡¡¡

choice /t 1 /d y /n > nul  
xcopy /s/e/y  .\upgrade\gateway.jar .\  
choice /t 1 /d y /n > nul
start javaw -jar gateway.jar 
