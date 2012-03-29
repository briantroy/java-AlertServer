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


import java.io.*;


import org.apache.commons.logging.*;
import org.apache.commons.logging.impl.SimpleLog;
import org.apache.log4j.*;
import org.json.*;

/* Google Voice */
import com.techventus.server.voice.Voice;

/**
 * This class implements the beanstalkd worker thread for the alert server.
 *
 * @author Brian Roy brian@briantroy.com
 */
public class QueueSendGVSMS extends Thread{

    /* log4j logger */
    static org.apache.log4j.Logger myLog = org.apache.log4j.Logger.getLogger("com.briantroy.alertserver.main");
    /* May or may not be needed for the trendrr beanstalk library */
    protected static Log log = LogFactory.getLog("gvsms_beanstalk_log");


    private static Boolean isDone = false;
    private static ConfigFileReader cfrCfg;

    /*
    * Constructor...
    *
    * @param ConfigFileReader cfg The config object for the Alert Server
    *
     */
    public QueueSendGVSMS(ConfigFileReader cfg) {
        cfrCfg = cfg;
        myLog.info("Starting Google Voice Queue Worker...");
    }

    /*
    * Public method to allow the parent thread to gracefully stop this thread.
    *
     */
    public void isDone() {
        isDone = true;
    }

    /*
    * Run the thread.
     */
    @Override
    public void run() {

        while(!isDone) {
            try {

                pooledQueue();

            } catch (BeanstalkException bsE) {
                if(bsE.getMessage() != "TIMED OUT") myLog.error(bsE.getMessage());
            }
        }

    }

    /*
    * Method pooledQueue establishes the Beanstalkd client and listens for jobs
    * and handles them.
    *
    *
     */
    private static void pooledQueue()  throws BeanstalkException {
            BeanstalkPool pool = new BeanstalkPool(cfrCfg.getConfigItem("beanstalk_host"),
                Integer.parseInt(cfrCfg.getConfigItem("beanstalk_port")),
                Integer.parseInt(cfrCfg.getConfigItem("beanstalk_pool_size")), //poolsize
                cfrCfg.getConfigItem("bs_queue_gvsms") //tube to use
            );

            BeanstalkClient client = pool.getClient();

            BeanstalkJob job = client.reserve(Integer.parseInt(cfrCfg.getConfigItem("beanstalk_reserve_timeout")));
            myLog.info("Got job: " + job);
            /*
             * Have to call the method here... need the client connection
             * to delete the job.
             */
            String jobBody = new String(job.getData());
            myLog.info("JSON From Queue: " + jobBody);
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
                myLog.error(e.getMessage());
                client.close();
            }


    }

    /*
    * Method ClientMsg handles the individual message and sends it out.
    *
    * @param JSONObject thisMsg The JSON object representing the message.
    * @return Boolean true on success, false if invalid message.
     */
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
                        
                        /* Sending a Goggle Voice SMS
                         *
                         */
                        try {
                            Voice gvThis = new Voice(cfrCfg.getConfigItem("google_user"), cfrCfg.getConfigItem("google_password"));
                            gvThis.sendSMS(thisMsg.getString("smsTo"), thisMsg.getString("imMsg"));
                        } catch (IOException e) {
                            myLog.error(e.getMessage());
                        } catch (JSONException jsE) {
                            myLog.error(jsE.getMessage());
                        }
                        return true;

                       
                    } else {
                        myLog.error("Asked to send an invalid message...");
                        return false;
                    }
	}
}


