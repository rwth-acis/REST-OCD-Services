cd %~dp0
cd ..
set BASE=%CD%
set CLASSPATH="%BASE%/lib/*"

java -cp %CLASSPATH% i5.las2peer.tools.UserAgentGenerator userAPass "User A" usera@mail.com
pause
