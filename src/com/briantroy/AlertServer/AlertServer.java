package com.briantroy.AlertServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.Date;
import java.lang.*;
import org.apache.log4j.*;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.SSLXMPPConnection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;



public class AlertServer {
	
	
	final static String IMTO = "-imTo";
	
	final static String IMMSG = "-imMsg";
	final static String IMSVR = "-imSvr";
	final static String IMDOMAIN = "-imDomain";
	final static String IMUSER = "-imUser";
	final static String IMPASSWD = "-imPasswd";
    final static String IMRESOURCE = "-imResource";
	final static String IMSVRPORT = "-imSvrPort";
	final static String IMSSH = "-imSSH";
	final static String DEBUG = "-d";
	final static String LISTENPORT = "-listenPort";
    final static String CONFIGFILE = "-configFile";

	

	final static String DEBUGDEFAULT = "no";
	final static String NO = "no";
	final static String YES = "yes";
	final static String PORTDEFAULT = "5222";
	final static String PORTSSHDEFAULT = "5223";
	final static String DOMAINDEFAULT = "";
	final static String SSHDEFAULT = "no";
    final static String RESOURCEDEFAULT = "alert_worker";

    private static Logger logger;

    static Logger myLog = Logger.getLogger("com.briantroy.alertserver.main");
	
	private static boolean runWorker(XMPPConnection imSvrConn, int intThisPort) {
		
            final int PORT = 2020;

            boolean blnOn = true;
            String j = ".";
            int m = 0;


            ServerSocket sockServer = null;
            Socket newSock = null;

            myLog.info("Starting Client Connection Server... ");

            if(intThisPort == 0){//use default
                    intThisPort = PORT;
            }

            try {
                    sockServer = new ServerSocket(intThisPort);

                    while(blnOn){
                            sockServer.setSoTimeout(3000); //timeout after 3000 ms to allow thread to die if blnOn
                            //gets set to false.
                            try {
                                    Socket imSvrThis = sockServer.accept();
                                    // ok... got a connection...
                                    ClientMsg(imSvrThis, imSvrConn);

                            } catch (SocketTimeoutException steThis){
                                    //no biggie... continue
                                    // just used to re-check blnOn to see if this thread should terminate.
                                    /*
                                    Presence presence = new Presence(Presence.Type.AVAILABLE);
                                    presence.setStatus("Working" + j);
                                    // Send the packet (assume we have a XMPPConnection instance called "con").
                                    imSvrConn.sendPacket(presence);
                                    */

                                    if(imSvrConn.isConnected()) {
                                        
                                    } else {
                                        myLog.info("Lost Connection to Jabber Server...");
                                        return false;
                                    }
                                    j += ".";
                                    if(j.equals("...........")) {
                                        // This is every 10 segments of 30 seconds (5 mintues).
                                        if(m == 9) {
                                            // Print status
                                            myLog.info("Still connected to Jabber Server...");
                                            m = 0;
                                        } else {
                                           // myLog.info("This pass m = " + m);
                                           ++m;
                                        }
                                        j = ".";
                                        
                                    }

                            }


                    }

            } catch (SocketException seThis){

                    if(seThis.getMessage().equals("Address already in use: JVM_Bind")){
                            //need to die...
                            myLog.error("ERROR: Fatal Error in starting client listener.\n" +
                                            "Port: " + intThisPort + " already in use.\nExiting...");
                            //stop the asterisk call data thread.



                    }
            } catch (IOException e) {
                    myLog.warn("IOException in runWorker: " + e.getMessage());
                    e.printStackTrace(System.err);

            }


            return false;
	}
	
	private static boolean ClientMsg(Socket mySock, XMPPConnection myConn) {
		
		final int MAXMESSAGESIZE = 4;
		
		final String CLIENTMAXMESSAGESIZEERROR = "Error 404 - exceeded maximum message size (4 lines)\n";
		final String CLIENTMALFORMEDMSGERROR = "Error 405 - Message does not comply to specificaiton\n" +
			"Message Format:\n" + "-imTo:\n<XMPP Address>\n-imMsg\n<Message Text>";
		final String CLIENTMSGSENT = "200 - Message Sent - BYE";
		
		String strMessage[] = new String [MAXMESSAGESIZE];
                    int i;
                    boolean blnTO = false;
                    boolean blnMSG = false;
                    String strTO = "";
                    String strMSG = "";

                    try {
                            //set thread as active
                            OutputStreamWriter oswiClient = new OutputStreamWriter(mySock.getOutputStream());
                            InputStream insClient;
                                            Reader iswiClient = new InputStreamReader(insClient
                                            = mySock.getInputStream());
                            BufferedReader breadClient = new BufferedReader(iswiClient);
                            BufferedWriter bwriteClient = new BufferedWriter(oswiClient);

                            //this will wait on a newline
                            String strReturn = breadClient.readLine();
                            i = 0;
                            while (strReturn != null) {
                                strReturn = strReturn.trim();
                                if(strReturn.length() == 0){
                                        //end of message

                                        // handle the message here
                                        myLog.warn(strMessage[0]);
                                        myLog.warn(strMessage[1]);
                                        myLog.warn(strMessage[2]);
                                        myLog.warn(strMessage[3]);
                                        if(strMessage[0].equals(IMTO)) {
                                                blnTO = true;
                                                strTO = strMessage[1];
                                        }
                                        if(strMessage[0].equals(IMMSG)) {
                                                blnMSG = true;
                                                strMSG = strMessage[1];
                                        }
                                        if(strMessage[2].equals(IMTO)) {
                                                blnTO = true;
                                                strTO = strMessage[3];
                                        }
                                        if(strMessage[2].equals(IMMSG)) {
                                                blnMSG = true;
                                                strMSG = "'" + strMessage[3] + "'";
                                        }

                                        if(blnTO && blnMSG) {
                                                Chat newChat = myConn.createChat(strTO);
                                                        try {
                                                                newChat.sendMessage(strMSG);
                                                                bwriteClient.write(CLIENTMSGSENT);
                                                                bwriteClient.flush();
                                                                mySock.close();
                                                        } catch (XMPPException e) {
                                                                // TODO Auto-generated catch block
                                                                e.printStackTrace();
                                                        }
                                        } else {
                                                bwriteClient.write(CLIENTMALFORMEDMSGERROR);
                                        bwriteClient.flush();
                                        }

                                        for(i=0;i<MAXMESSAGESIZE;i++) strMessage[i] = null;
                                        i = 0;
                                } else {
                                        if(i<MAXMESSAGESIZE){
                                                strMessage[i] = strReturn;
                                                i++;
                                        } else {
                                                bwriteClient.write(CLIENTMAXMESSAGESIZEERROR);
                                                bwriteClient.flush();
                                        }
                                }
                                if(mySock.isConnected()) {
                                        try {
                                                strReturn = breadClient.readLine();
                                        } catch (SocketException exGone){
                                                //Client Went away... no biggie.
                                                strReturn = null;
                                        }
                                        //System.out.println("Connected...");
                                }else{
                                        //System.out.println("Closed...");
                                        strReturn = null;
                                }

                        }
                } catch (SocketException exSock){
                        // Call Socket Exception Handler
                        exSock.printStackTrace();
                        // TODO perform any local cleanup.

                } catch (IOException e) {
                        // TODO Clean Up Cch Block
                        e.printStackTrace();
                }

		
		return false;
	}

    public static XMPPConnection getXMPPConnection(ConfigItems cfg) {
            int iPort = 5222;
            if(cfg.getConfigItem(IMSSH).equals(NO)) {
//				 Create a connection to the jabber.org server.
                    iPort = 5222;
                    try {

                            myLog.info("Attempting Jabber Connnection to:\n" + "Server: " +
                                            cfg.getConfigItem(IMSVR) + "\nOn Port: " + iPort +
                                            "\nFor Domain: " + cfg.getConfigItem(IMDOMAIN));
                            XMPPConnection conn1 = new XMPPConnection(cfg.getConfigItem(IMSVR), iPort, cfg.getConfigItem(IMDOMAIN));
                            conn1.login(cfg.getConfigItem(IMUSER),cfg.getConfigItem(IMPASSWD), cfg.getConfigItem(IMRESOURCE));

                            return conn1;

                    } catch (XMPPException e) {
                            // TODO Auto-generated catch block
                            myLog.info("XMPPException Connecting to the server: " + e.getMessage());
                            return null;
                    }
            } else {
                    iPort = 5223;
                    try {
//					 Create an SSL connection to jabber.org.

                            myLog.info("Attempting Jabber Connnection to:\n" + "Server: " +
                                            cfg.getConfigItem(IMSVR) + "\nOn Port: " + iPort +
                                            "\nFor Domain: " + cfg.getConfigItem(IMDOMAIN) + "\n");
                            XMPPConnection conn1 = new SSLXMPPConnection(cfg.getConfigItem(IMSVR), iPort, cfg.getConfigItem(IMDOMAIN));
                            conn1.login(cfg.getConfigItem(IMUSER),cfg.getConfigItem(IMPASSWD), cfg.getConfigItem(IMRESOURCE));
                            return conn1;

                    } catch (XMPPException e) {
                            // TODO Auto-generated catch block
                            myLog.error("XMPPException Connecting to the server: " + e.getMessage());
                            e.printStackTrace();
                            return null;
                    }
            }

            
        }
	
	public static void main(String[] args){

		
		ConfigItems cfgMyConfig = new ConfigItems();
                ConfigFileReader cfgFileConf = new ConfigFileReader();
                XMPPConnection conn1;
                int j = 1;
                int intSlMil = 1000;
                long finalMil = 0;
                char cVal = 'L';
                String sSleep = "";

		
		if(cfgMyConfig.digestCmdLine(args)) {

                    if(cfgFileConf.fetchConfig(cfgMyConfig)) {

                        /* Now we will just call another function to start the XMPP server
                         * connection. If it isn't null we start the worker.
                         *
                         * 11-16-2010
                         *
                         */
                        // This loop handles re-connect on XMPP Disconnect (runWorker will return false).

                        while(true) {
                            myLog.info("\n\n****************Starting AlertServer Threads now.***************");
                            finalMil = j*intSlMil;
                            try {
                                Thread.sleep(finalMil);

                                conn1 = getXMPPConnection(cfgMyConfig);

                                QueueSendGVSMS gvQueue = new QueueSendGVSMS(cfgFileConf);
                                gvQueue.setName("QueueGVSMSWorker");
                                QueueSendXMPP thQueue = new QueueSendXMPP(conn1, cfgFileConf);
                                thQueue.setName("QueueXMPPWorker");



                                if(conn1 != null || conn1.isConnected()) {
                                    
                                    // Good Connection
                                    myLog.info("Now Starting GVSMS");
                                    gvQueue.start();
                                    myLog.info("GVSMS should be running...");
                                    thQueue.start();
                                    myLog.info("Starting the IP Socket XMPP Sender.");
                                    runWorker(conn1,Integer.parseInt(cfgMyConfig.getConfigItem(LISTENPORT)));
                                    j = 1;
                                } else {
                                    myLog.info("Stopping our queue worker threads...");
                                    if(gvQueue.isAlive()) gvQueue.isDone();
                                    if(thQueue.isAlive()) thQueue.isDone();
                                    myLog.info("Sleeping for a minute...\n\n");
                                    Thread.sleep(60000); // Wait 60 seconds for the threads to exit.
                                    ++j;
                                    if(j > 60) j = 60;
                                }
                                
                            } catch (InterruptedException e) {
                                myLog.error("Caught Interruped Exception in main: " + e.getMessage());

                            }



                        }
                    } else {
                        // Config file wasn't found.
                        myLog.error("The specified configuration file was not found... aborting...");
                    }
                    
			
			
		}
		
		
	}
	
}
	
	