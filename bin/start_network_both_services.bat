:: this script starts both the viewer and the ocd service
:: requires the ocd service jar in the service directory

%~d0
cd %~p0
cd ..
set BASE=%CD%
set CLASSPATH="%BASE%/lib/*;%BASE%/service/*;"

java -cp %CLASSPATH% i5.las2peer.tools.L2pNodeLauncher windows_shell -w -p 9081 uploadStartupDirectory('etc/startup') startService('i5.las2peer.services.ocd.ServiceClass@1.0','ocdPass') startService('i5.las2peer.services.ocdViewer.ServiceClass@1.0','ocdViewerPass') startWebConnector interactive
pause