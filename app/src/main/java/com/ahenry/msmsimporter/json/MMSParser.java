package com.ahenry.msmsimporter.json;

//import android.app.FragmentManager;

import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;

import com.ahenry.msmsimporter.MainActivity;
import com.ahenry.msmsimporter.R;
import com.ahenry.msmsimporter.database.DbMMSImporter;
import com.ahenry.msmsimporter.models.MMS;
import com.ahenry.msmsimporter.ui_fragments.UI_ProgressBar_Fragment;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

/**
 * Created by axel on 11/06/15.
 */
public class MMSParser extends AsyncTask<Void, Integer, MMS[]>{

    public static final Logger LOG = LoggerFactory.getLogger(MMSParser.class);
    private String myFilePath = null;
    private MainActivity aAct = null;

    public MMSParser(String filePath, MainActivity act){
        aAct = act;
        myFilePath = filePath;

    }

    @Override
    protected void onPostExecute(MMS[] mmses) {
        //super.onPostExecute(corProcessedMMSes);
        if(mmses!=null) {
            //FragmentManager fm = aAct.getFragmentManager();
            FragmentManager fm = aAct.getSupportFragmentManager();
            UI_ProgressBar_Fragment f = (UI_ProgressBar_Fragment) fm.findFragmentById(R.id.mmsFragment);
            LOG.debug("onPostExecute :: mmses number {}.", mmses.length);
            f.setNumber(aAct.getResources().getString(R.string.mmses), mmses.length);
            //NumberProgressBar npb = (NumberProgressBar) f.getView().findViewById(R.id.progressBar);
            DbMMSImporter mmsImporter = new DbMMSImporter(aAct, f, mmses);
            mmsImporter.execute();
        }else{
            LOG.debug("onPostExecute :: no mmses to process");
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected MMS[] doInBackground(Void... params) {

        LinkedList<MMS> myMMSes = new LinkedList<MMS>();
        //try (InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(myFilePath)), StandardCharsets.UTF_8)){
        InputStreamReader isr = null;
        try{
            isr = new InputStreamReader(new FileInputStream(new File(myFilePath)), Charsets.UTF_8);

            JsonFactory jsf = new JsonFactory();
            ObjectMapper mapper = new ObjectMapper();
            //InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(myFilePath)), "UTF-8");
            JsonParser jsp = jsf.createParser(isr);

            jsp.nextToken();
            MMS aMMS;
            int counter = 0;
            while(jsp.nextToken() == JsonToken.START_OBJECT){
                aMMS = mapper.readValue(jsp, MMS.class);
                publishProgress(counter++);
                //if(aMMS != null){
                if(aMMS != null && aMMS.isValid()){
                    myMMSes.add(aMMS);
                }
            }

            return myMMSes.toArray(new MMS[myMMSes.size()]);

        }catch(IOException ioe){
            LOG.error("doInBackground :: IOException.\n\t",ioe);
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
        try (InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(myFilePath)), StandardCharsets.UTF_8)){
        //try(InputStream isr = new FileInputStream(new File(myFilePath))){
            JsonFactory jsf = new JsonFactory();
            ObjectMapper mapper = new ObjectMapper();
            //InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(myFilePath)), "UTF-8");
            JsonParser jsp = jsf.createParser(isr);

            jsp.nextToken();
            MMS aMMS = null;
            int counter = 0;
            while(jsp.nextToken() == JsonToken.START_OBJECT){
                aMMS = mapper.readValue(jsp, MMS.class);
                publishProgress(counter++);
                if(aMMS != null){
                //if(aMMS != null && aMMS.isValid()){
                    myMMSes.add(aMMS);
                }
            }

            MMS[] values = myMMSes.toArray(new MMS[myMMSes.size()]);

            return values;

        }catch (IOException ioe){
            LOG.error("doInBackground :: IOException.\n\t",ioe);
            //Log.d(TAG, "");
        }
        return null;*/
    }
}
