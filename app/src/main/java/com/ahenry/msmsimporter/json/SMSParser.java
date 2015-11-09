package com.ahenry.msmsimporter.json;

//import android.app.FragmentManager;

import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;

import com.ahenry.msmsimporter.MainActivity;
import com.ahenry.msmsimporter.R;
import com.ahenry.msmsimporter.database.DbSMSImporter;
import com.ahenry.msmsimporter.models.SMS;
import com.ahenry.msmsimporter.ui_fragments.UI_ProgressBar_Fragment;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

/**
 * Created by axel on 11/06/15.
 */
public class SMSParser extends AsyncTask<Void, Integer, SMS[]> {

    public static final Logger LOG = LoggerFactory.getLogger(SMSParser.class);
    private String myFilePath;
    private MainActivity aAct;

    public SMSParser(String filePath, MainActivity act){

        aAct = act;
        myFilePath = filePath;
    }

    @Override
    protected SMS[] doInBackground(Void... params) {

        LinkedList<SMS> mySMSes = new LinkedList<SMS>();

        InputStream isr = null;

        try{
            isr = new FileInputStream(new File(myFilePath));

            JsonFactory jsf = new JsonFactory();
            ObjectMapper mapper = new ObjectMapper();
            //InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(myFilePath)), "UTF-8");
            JsonParser jsp = jsf.createParser(isr);

            jsp.nextToken();
            SMS aSMS;
            int counter = 0;

            while(jsp.nextToken() == JsonToken.START_OBJECT){
                aSMS = mapper.readValue(jsp, SMS.class);
                publishProgress(counter++);
                if(aSMS != null && aSMS.isValid()){
                    mySMSes.add(aSMS);
                }
            }

            return mySMSes.toArray(new SMS[mySMSes.size()]);

        }catch(IOException ioe){
            LOG.error("doInBackground :: IOException.\n\t", ioe);
        }finally{
            //Utilities.close(isr);
            if(isr != null){
                try{
                    isr.close();
                } catch (IOException ioe) {
                    LOG.error("close :: IOException.\n\t", ioe);
                }
            }
        }
        return null;

/*        JAVA 1.7 VERSION
        try(InputStream isr = new FileInputStream(new File(myFilePath))){
        //try(InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(myFilePath)), StandardCharsets.UTF_8)){
            JsonFactory jsf = new JsonFactory();
            ObjectMapper mapper = new ObjectMapper();
            //InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(myFilePath)), "UTF-8");
            JsonParser jsp = jsf.createParser(isr);

            jsp.nextToken();
            SMS aSMS = null;
            int counter = 0;

            while(jsp.nextToken() == JsonToken.START_OBJECT){
                aSMS = mapper.readValue(jsp, SMS.class);
                publishProgress(counter++);
                if(aSMS != null && aSMS.isValid()){
                    mySMSes.add(aSMS);
                }
            }

            SMS[] values = mySMSes.toArray(new SMS[mySMSes.size()]);

            return values;


        }catch(IOException ioe){
            //Log.d(TAG, "");
            LOG.error("doInBackground :: IOException.\n\t",ioe);
        }
        return null;*/
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(SMS[] smses) {

        //super.onPostExecute(corProcessedSMSes);
        if(smses!=null) {


            //FragmentManager fm = aAct.getFragmentManager();
            FragmentManager fm = aAct.getSupportFragmentManager();
            //Log.d(TAG, "fragment manager");
            UI_ProgressBar_Fragment f = (UI_ProgressBar_Fragment) fm.findFragmentById(R.id.smsFragment);
            LOG.debug("onPostExecute :: smses number : {}", smses.length);
            //Log.d(TAG, "corProcessedSMSes number : "+corProcessedSMSes.length);
            f.setNumber(aAct.getResources().getString(R.string.smses), smses.length);
            //Log.d(TAG, "fragment ");
            //NumberProgressBar npb = (NumberProgressBar) f.getView().findViewById(R.id.progressBar);
            //Log.d(TAG, "number progress bar");

            DbSMSImporter aSMSImporter = new DbSMSImporter(aAct, f, smses);
            aSMSImporter.execute();
        }else{
            LOG.debug("onPostExecute :: no smses to process.");
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
}
