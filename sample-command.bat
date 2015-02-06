@echo off

IF "%1" == "TEST_SERVER1" (
	echo Houston we got a problem
	EXIT /B 2
)

echo Everything went well "%1"