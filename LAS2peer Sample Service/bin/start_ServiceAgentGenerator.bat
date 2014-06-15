cd %~dp0
cd ..
set BASE=%CD%
set CLASSPATH="%BASE%/lib/*;"

java -cp %CLASSPATH% i5.las2peer.tools.ServiceAgentGenerator i5.las2peer.servicePackage.ServiceClass SampleServicePass
pause
