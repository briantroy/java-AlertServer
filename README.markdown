# AlertServer

## Overview
AlertServer is a Java server daemon that does one simple thing - sends Instant Messages and
SMS messages on demand.
It is designed to allow any client application to simply send alerts (errors, site outages,
operations notifications, etc) via a centralized service.

## Requirements

1. Java Runtime - Tested using JRE 1.6
1. Beanstalkd - Beanstalk is a simple, fast work queue. http://kr.github.com/beanstalkd/
Tested and in production on 1.4.6

### Java Libraries:
These libraries are required and not inclued here...

1. SMACK 2.1.0 (http://www.igniterealtime.org/projects/smack/ - Tested with 2.1.0, may work with newer versions).
1. Google Voice Java (http://code.google.com/p/google-voice-java/ - Tested with 1.6)
    NOTE: you must also include the modified JSON library included in GV-Java.
1. JSON-Java (https://github.com/douglascrockford/JSON-java)
1. Log4J (http://logging.apache.org/log4j/1.2/ - Tested using 1.2.16)
1. Commons Logging (May or may not be used by the beanstalkd library - tested with version 1.1.1)
1. Trendrr-Beanstalk (https://github.com/dustismo/TrendrrBeanstalk)

## Usage
The AlertServer accepts messages to send in two ways:

1. Via a beanstalkd job queue.
    1. One for google voice text messages, one for instant messages via XMPP
1. Via a TCP/IP connection

The beanstalkd method is preferred for security reasons (the AlertServer provides little protection
from malicious connections).

### TCP/IP Method
The following sample code contains a PHP function which implements sending a message to the
AlertServer.

    <?PHP

    	define("IMTO","-imTo");
    	define("IMMSG","-imMsg");
    	define("NEWLINE","\n");

    	define("MWSERVER","localhost");
    	define("MWSERVERPORT","3030");

    	function socketSendIM($strIMto, $strIMMsg) {

    		$sock = fsockopen(MWSERVER,MWSERVERPORT);

    		if(! $sock) {
    			echo "Connection to XMPPWorker at: ".MWSERVER." on port ".MWSERVERPORT." could not be established.<br>";
    		} else {
    			$svrMsg = IMTO.NEWLINE.$strIMto.NEWLINE.IMMSG.NEWLINE.$strIMMsg.NEWLINE.NEWLINE;

    			fwrite($sock, $svrMsg);
    			while (!feof($sock)) {
    				echo fgets($sock, 128);
    			}
    			fclose($sock);
    		}
    	}


    ?>


The following code sends an instant message (XMPP) via the beanstalkd queue method.
NOTE: The pheanstalk PHP library is used - https://github.com/pda/pheanstalk

    <?PHP
            require_once("/usr/local/api-base/pheanstalk/pheanstalk_init.php");

            $bs = new Pheanstalk("localhost:11300");

            $aryMsg = array("imTo" => "myuser@gmail.com", "imMsg" => "Testing our new queue based XMPP send system: ".date('r'));

            // The tube below must match the tube set up in xmppworker.conf for the AlertServer
            $bs->useTube("prod_send_xmpp_queue")->put(json_encode($aryMsg));

    ?>

The following code sends a text message (SMS) via a google voice account using the beanstalkd queue
method.

    <?PHP
            require_once("/usr/local/api-base/pheanstalk/pheanstalk_init.php");

            $bs = new Pheanstalk("localhost:11300");

            $aryMsg = array("smsTo" => "4158675309", "imMsg" => "Testing MVx API Alert system:  ".date('r'));

            // The tube below must match the tube set up in xmppworker.conf for the AlertServer
            $bs->useTube("prod_send_gvsms_queue")->put(json_encode($aryMsg));

    ?>

## Configuraiton
The primary configuraiton is handled in the xmppworker.conf file. There is a commented sample
included.

The remaining configuration is done via the command line. The command line accepts the following
parameters:

    The following command lines arguments are required:
    IM Adresse (TO): -imTo <jabber_address>
    IM Message: -imMsg <Message>
    IM Server: -imSvr <ip or fqdn of the Jabber Server>
    IM User: -imUser <jabber_username>
    IM Password: -imPasswd <jabber_password>
    Listener Port: -listenPort <port number>
    Optional arguments:
    IM Domain: -imDomain <The Domain the Jabber Server serves> default is <empty>
    IM Use SSH: -imSSH <yes or no> default is no
    IM Server Port: -imSvrPort <port number> default is 5222 or 5223 for SSH
    Debugging output to console: -d
    Supported Values: yes,no <default no>

NOTE: This is also the output of the AlertServer if no command line parameters are passed in.

## Running the AlertServer
There are sample scripts included in the scripts folder:

1. alertServer.sh
    1. A script to start the AlertServer and create a PID file. Command line options are present with sample values
1. stopAlertServer.sh
    1. A script to stop the AlertServer cleanly.
1. is_running.sh
    1. A script for the paranoid - can be run via cron to check if your AlertServer is running and start it if not.

## Logging

The logging is quite verbose by default, as such the log4j.properties file is set to rotate the
log files every hour. Feel free to tweak to your desired values.