package com.briantroy.AlertServer;


import org.apache.log4j.*;

/*
* The ConfigItems class handles the command line options.
*
* @author Brian Roy brian@briantroy.com
 */

public class ConfigItems {

    /*
    * Two arrays to handle the config items/keys.
     */
	public String[] strKey = new String[200];
	public String[] strVal = new String[200];
	private int intNumItems = 0;

    /* Log4J logger */
    static Logger myLog = Logger.getLogger("com.briantroy.alertserver.main");


    /*
    * The method getConfigItem returns the value of a given
    * command line config parameter.
    *
    * @param String strThisKey The command line parameter to get value of.
    *
    * @return String The value of the command line config item or -1 if not found.
     */
	public String getConfigItem(String strThisKey) {
		
		
		int i = 0;
		
		//System.out.println("Looking for: " + strThisKey + "\n");
		
		for(i = 0;i <= intNumItems;++i){
			//System.out.println("Index: " + i + " Key Is: " + strKey[i] + "\n");
			if (strThisKey.equals(strKey[i])){
				//System.out.println("Found It - Returning the Value: " + strVal[i] + "\n");
				return strVal[i];
			}
		}
		return "-1";
		
	}
	
	/**
	 * Digests the commandline arguments into two like indexed 
	 * public arrays. Also sets private fields in this object.
	 * @param cmdArgs String[] The array of command line elements.
	 * @return Boolean true on success, false on fail
	 */
	public boolean digestCmdLine(String[] cmdArgs) {
		
		
		// Config Message
		final String CONFMESSAGE = "The following command lines arguments are required:\n" +
				"IM Adresse (TO): -imTo <jabber_address>\n" +
				"IM Message: -imMsg <Message>\n" +
				"IM Server: -imSvr <ip or fqdn of the Jabber Server>\n" +
				"IM User: -imUser <jabber_username>\n" +
				"IM Password: -imPasswd <jabber_password>\n" +
				"Listener Port: -listenPort <port number>\n" +
				"Optional arguments:\n" +
				"IM Domain: -imDomain <The Domain the Jabber Server serves> default is <empty>\n" +
				"IM Use SSH: -imSSH <yes or no> default is no\n" +
				"IM Server Port: -imSvrPort <port number> default is 5222 or 5223 for SSH\n" +
				"Debugging output to console: -d\n" +
				"Supported Values: yes,no <default no>";
		
		
		
		// Local Variable declarations		
		int i = 0;
		int j = 0;
		
		boolean blnDebugFound = false;
		boolean blnIMUser = false;
		boolean blnIMTo = false;
		boolean blnIMMsg = false;
		boolean blnIMPasswd = false;
		boolean blnListenPort = false;
		boolean blnIMSvr = false;
		boolean blnIMResource = false;
		boolean blnIMDomain = false;
		boolean blnIMSSH = false;
		boolean blnIMPort = false;
		boolean blnGoodConfig = true;
		String strSSHVal = ""; 
		
		
		if (i == cmdArgs.length){
            myLog.info("No command line configuration supplied for AlertServer. Aborting...");
			System.out.println(CONFMESSAGE);
			blnGoodConfig = false;
			return blnGoodConfig;
		}
		for (i=0;i<cmdArgs.length;i=i+2) {
			// System.out.println("Index: " + i + " Conf Item: " + cmdArgs[i] + " Value: " + cmdArgs[i+1] + "\n");
			if (cmdArgs[i].equals(AlertServer.DEBUG))blnDebugFound = true;
			if (cmdArgs[i].equals(AlertServer.IMTO)) blnIMTo = true;
			if (cmdArgs[i].equals(AlertServer.IMUSER)) blnIMUser = true;
			if (cmdArgs[i].equals(AlertServer.IMMSG)) blnIMMsg = true;
			if (cmdArgs[i].equals(AlertServer.IMPASSWD)) blnIMPasswd = true;
			if (cmdArgs[i].equals(AlertServer.IMDOMAIN)) blnIMDomain = true;
			if (cmdArgs[i].equals(AlertServer.LISTENPORT)) blnListenPort = true;
			if (cmdArgs[i].equals(AlertServer.IMSVR)) blnIMSvr = true;
			if (cmdArgs[i].equals(AlertServer.IMSSH)) {
				blnIMSSH = true;
				strSSHVal = cmdArgs[i+1];
			}
			if (cmdArgs[i].equals(AlertServer.IMSVRPORT)) blnIMPort = true;
                        if (cmdArgs[i].equals(AlertServer.IMRESOURCE)) blnIMResource = true;
			
			strKey[j] = cmdArgs[i];
			strVal[j] = cmdArgs[i+1];
			++j;
		}
						
		
		if(!blnDebugFound){
			strKey[j] = AlertServer.DEBUG;
			strVal[j] = AlertServer.DEBUGDEFAULT;
		}
		if(!blnIMSSH){
			++j;
			strKey[j] = AlertServer.IMSSH;
			strVal[j] = AlertServer.SSHDEFAULT;
			strSSHVal = AlertServer.SSHDEFAULT;
		}
		if(!blnIMPort){
			++j;
			strKey[j] = AlertServer.IMSVRPORT;
			if(strSSHVal.equals(AlertServer.NO)) {
				strVal[j] = AlertServer.PORTDEFAULT;
			} else {
				strVal[j] = AlertServer.PORTSSHDEFAULT;
			}
		}
		if(!blnIMDomain){
			++j;
			strKey[j] = AlertServer.IMDOMAIN;
			strVal[j] = AlertServer.DOMAINDEFAULT;
		}
        if(!blnIMResource){
			++j;
			strKey[j] = AlertServer.IMRESOURCE;
			strVal[j] = AlertServer.RESOURCEDEFAULT;
		}
		
		
		intNumItems = j;
		
		// check to see if we have all the required fields.
		
		if(!(blnIMUser & blnIMPasswd & blnListenPort & blnIMSvr)) blnGoodConfig = false;
		
		return blnGoodConfig;
	}
	


}
