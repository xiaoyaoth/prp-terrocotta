@echo off
cd ..
%TC_HOME%\platform\bin\dso-java.bat -cp F:\Workspace\Bank\bin simulation.runtime.Client usr3 200
D:
cd D:\terracotta\terracotta-3.4.1\bin
run-dgc.bat
pause > null