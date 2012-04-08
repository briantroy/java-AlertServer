#!/bin/sh
SERVICE='AlertServer.jar'
MAILTO='you@yourdomain.com'

NUMFOUND=`ps ax |grep -v grep | grep $SERVICE | wc -l`

echo "NOTICE: $(date -R) - Found $NUMFOUND running $SERVICE" >> /usr/local/AlertServer_v1.2/monitor_log

if [ "$NUMFOUND" = "1" ]
then
    echo "NOTICE: $(date -R) - $SERVICE service running, everything is fine" >> /usr/local/AlertServer_v1.2/monitor_log
    exit
fi

if [ "$NUMFOUND" -gt "1" ]
then
    echo "NOTICE: $(date -R) - More than one $SERVICE is running... killing all and restarting." >> /usr/local/AlertServer_v1.2/monitor_log
    # echo "$(date -R) - $SERVICE is running multiple times... restarting!" | mail -s "$SERVICE down" $MAILTO
    /usr/local/AlertServer_v1.2/stopAlertServer.sh >> /dev/null
    killall java
    /usr/local/AlertServer_v1.2/alertServer.sh >> /dev/null
    exit
fi

if [ "$NUMFOUND" = "0" ]
then
    echo "ERROR: $(date -R) - $SERVICE is not running and will be restarted..." >> /usr/local/AlertServer_v1.2/monitor_log
    # echo "$(date -R) - $SERVICE is not running! It will now be restarted..." | mail -s "$SERVICE down" $MAILTO
    /usr/local/AlertServer_v1.2/alertServer.sh >> /dev/null
fi