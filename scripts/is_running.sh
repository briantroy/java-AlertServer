# This script can be used in cron to monitor and automatically restart the
# AlertServer. This is usually not needed... but for the paranoid (like me)
# your AlertServer will restart with no human intervention.

#!/bin/sh
SERVICE='AlertServer'

# The directory where you've installed AlertServer.jar
BASEPATH=/usr/local/AlertServer_v1.2

if ps ax | grep -v grep | grep $SERVICE > /dev/null
then
    echo "`date +"%F %r"` - $SERVICE service running, everything is fine." >> $BASEPATH/monitor_log
else
    echo "`date +"%F %r"` - $SERVICE is not running. Restarting now..." >> $BASEPATH/monitor_log
    $BASEPATH/alertServer.sh
fi