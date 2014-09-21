%~d0
cd %~dp0
cd ..
set BASE=%CD%
set CLASSPATH="%BASE%/lib/*"

java -cp %CLASSPATH% i5.las2peer.tools.UserAgentGenerator user "User" user@mail.com > user.xml
pause
