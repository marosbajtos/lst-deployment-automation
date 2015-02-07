@echo off


REM wait 10 seconds
PING 1.1.1.1 -n 1 -w 10000 >NUL
REM execute command logic


IF "%1" == "TEST_SERVER1" (
	echo Houston we got a problem
	EXIT /B 2
)

echo Everything went well "%1"