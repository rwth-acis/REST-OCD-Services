:: this script starts a derby server configured for this service
:: pls execute it from by double-clicking on it in the folder

%~d0
cd %~p0
cd ../derby
java -cp "../../lib/*" org.apache.derby.tools.ij
pause