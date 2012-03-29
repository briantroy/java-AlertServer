# This script starts the AlertServer.

#!/bin/bash

# The directory where you've installed AlertServer.jar
BASEPATH=/usr/local/AlertServer_v1.2

XMPPSERVER=talk.google.com
XMPPDOMAIN=gmail.com
USESSH=no
XMPPUSER=my.gmail.name
XMPPPWD=my.gmail.password
XMPPRESOURCE=this.resource
# Do not include path in this... should be in BASEPATH
CONFIGFILENAME=myconfigfile.conf

sleep 5
cd $BASEPATH

# Replace the below with the full path to java on your system.
/opt/jre1.6.0_24/bin/java -jar $BASEPATH/AlertServer.jar -imSvr talk.google.com -imDomain justsignal.com -imSSH yes -listenPort 3030 -imUser alert -imPasswd Is@b3l10 -imResource prod_pbx -configFile $BASEPATH/xmppworker.conf >> /dev/null &
echo $! > $BASEPATH/alertServer.pid