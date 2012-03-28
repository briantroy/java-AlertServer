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

import java.util.logging.*;
import java.io.*;

import org.json.*;

/* Google Voice */
import com.techventus.server.voice.Voice;

/**
 *
 * @author brian.roy
 */
public class QueueSendGVSMS extends Thread{


    private static Logger logger;
    private static Boolean isDone = false;
    private static ConfigFileReader cfrCfg;

    static {
        try {
          boolean append = true;
          FileHandler fh = new FileHandler("/usr/local/AlertServer/QueueSendGVSMS.log", append);
          // FileHandler fh = new FileHandler(cfrCfg.getConfigItem("logfile"), append);
          //fh.setFormatter(new XMLFormatter());
          fh.setFormatter(new SimpleFormatter());
          logger = Logger.getLogger("QueueSendGVSMS");
          logger.addHandler(fh);
        }
        catch (IOException e) {
          e.printStackTrace();
        }
    }

    public QueueSendGVSMS(ConfigFileReader cfg) {
        cfrCfg = cfg;
        logger.info("Starting Google Voice Queue Worker...");
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
                    cfrCfg.getConfigItem("bs_queue_gvsms") //tube to use
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

                    if(thisMsg.has("smsTo") && thisMsg.has("imMsg")) {
                        // Good message
                        
                        /* Test sending a Goggle Voice SMS
                         *
                         */
                        try {
                            Voice gvThis = new Voice(cfrCfg.getConfigItem("google_user"), cfrCfg.getConfigItem("google_password"));
                            gvThis.sendSMS(thisMsg.getString("smsTo"), thisMsg.getString("imMsg"));
                        } catch (IOException e) {
                            logger.severe(e.getMessage());
                        } catch (JSONException jsE) {
                            logger.severe(jsE.getMessage());
                        }



                        return true;

                       
                    } else {
                        logger.severe("Asked to send an invalid message...");
                        return false;
                    }
	}
}


