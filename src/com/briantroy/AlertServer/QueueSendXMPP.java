/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.briantroy.AlertServer;

/* Import tredrr beanstalk libraries */
import com.trendrr.beanstalk.BeanstalkClient;
import com.trendrr.beanstalk.BeanstalkException;
import com.trendrr.beanstalk.BeanstalkJob;
import com.trendrr.beanstalk.BeanstalkPool;

import org.jivesoftware.smack.*;

import java.util.logging.*;
import java.io.*;

import org.json.*;

/* Google Voice */
import com.techventus.server.voice.Voice;


/**
 *
 * @author brian.roy
 */

public class QueueSendXMPP extends Thread {

    private static XMPPConnection tConn;
    private static Logger logger;
    private static Boolean isDone = false;
    private static ConfigFileReader cfrCfg;


    static {
        try {
          boolean append = true;
          FileHandler fh = new FileHandler("/usr/local/AlertServer/QueueSendXMPP.log", append);
          // FileHandler fh = new FileHandler(cfrCfg.getConfigItem("logfile"), append);
          //fh.setFormatter(new XMLFormatter());
          fh.setFormatter(new SimpleFormatter());
          logger = Logger.getLogger("QueueSendXMPP");
          logger.addHandler(fh);
        }
        catch (IOException e) {
          e.printStackTrace();
        }
    }

    public QueueSendXMPP(XMPPConnection xConn, ConfigFileReader cfg) {
        tConn = xConn;
        cfrCfg = cfg;
    }

    public void isDone() {
        isDone = true;
    }

    @Override
    public void run() {
        while(!isDone) {
            try {

                pooledQueue();

            } catch (BeanstalkException bsE) {
                logger.severe(bsE.getMessage());
            }
        }


    }

    private static void pooledQueue()  throws BeanstalkException {
            BeanstalkPool pool = new BeanstalkPool(cfrCfg.getConfigItem("beanstalk_host"), Integer.valueOf(cfrCfg.getConfigItem("beanstalk_port")),
                            30, //poolsize
                    cfrCfg.getConfigItem("bs_queue_xmpp") //tube to use
            );

            BeanstalkClient client = pool.getClient();
            
            BeanstalkJob job = client.reserve(10);
            logger.info("Got job: " + job);
            /*
             * Have to call the method here... need the client connection
             * to delete the job.
             */
            String jobBody = new String(job.getData());
            logger.info("JSON From Queue: " + jobBody);
            try {
                JSONObject iJob = new JSONObject(jobBody);
                if(ClientMsg(iJob)) {
                    client.deleteJob(job);
                    client.close();
                } else {
                    client.deleteJob(job);
                    client.close();
                }
            } catch (JSONException e) {
                logger.severe(e.getMessage());
                client.close();
            }
            
            
    }

    private static boolean ClientMsg(JSONObject thisMsg) {

		final int MAXMESSAGESIZE = 4;

		String strMessage[] = new String [MAXMESSAGESIZE];
                    int i;
                    boolean blnTO = false;
                    boolean blnMSG = false;
                    String strTO = "";
                    String strMSG = "";

                    if(thisMsg.has("imTo") && thisMsg.has("imMsg")) {
                        // Good message
                        try {
                            Chat newChat = tConn.createChat(thisMsg.getString("imTo"));
                            try {
                                    newChat.sendMessage(thisMsg.getString("imMsg"));
                                    return true;
                            } catch (XMPPException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                    logger.severe(e.getMessage());
                                    return false;
                            }
                        } catch (JSONException e) {
                            logger.severe(e.getMessage());
                            return false;
                        }
                    } else {
                        logger.severe("Asked to send an invalid message...");
                        return false;
                    }
	}

}
