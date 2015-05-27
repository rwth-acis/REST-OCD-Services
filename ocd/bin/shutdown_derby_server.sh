#!/bin/bash

# this script starts a derby server configured for this service
# please execute it from the applications /ocd/bin directory

cd ../derby
java -cp "../../lib/*" org.apache.derby.drda.NetworkServerControl shutdown -h 127.0.0.1 -p 1527 -user admin -password adminPw