:: this script shuts down the derby server configured for this service
:: pls execute it from by double-clicking on it in the folder

%~d0
cd %~p0
java -cp "../../lib/*" org.apache.derby.drda.NetworkServerControl shutdown -h 127.0.0.1 -p 1527 -user admin -password adminPw