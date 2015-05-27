#!/bin/bash

# this script starts a derby server configured for this service
# please execute it from the applications /ocd/bin directory

cd ../derby
java -cp "../../lib/*" org.apache.derby.drda.NetworkServerControl start -h 127.0.0.1 -p 1527