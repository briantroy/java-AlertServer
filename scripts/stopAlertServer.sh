# This script stops the AlertServer

#!/bin/bash

# The directory where you've installed AlertServer.jar
BASEPATH=/usr/local/AlertServer_v1.2

kill `cat $BASEPATH/alertServer.pid`
rm /usr/local/AlertServer_v1.2/alertServer.pid
sleep 5