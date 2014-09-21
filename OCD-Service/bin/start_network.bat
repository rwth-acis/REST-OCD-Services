:: this script starts a las2peer node providing the example service of this project
:: pls execute it from the bin folder of your deployment by double-clicking on it

%~d0
cd %~p0
cd ..
java -cp "lib/*" i5.las2peer.tools.L2pNodeLauncher windows_shell -s 9011 - uploadStartupDirectory('etc/startup') startService('i5.las2peer.services.ocd.ServiceClass','ocdPass') startWebConnector interactive
pause
