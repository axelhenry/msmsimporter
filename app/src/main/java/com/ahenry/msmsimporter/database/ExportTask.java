package com.ahenry.msmsimporter.database;

//import android.app.FragmentManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.ahenry.msmsimporter.MainActivity;
import com.ahenry.msmsimporter.R;
import com.ahenry.msmsimporter.SettingsActivity;
import com.ahenry.msmsimporter.models.MMS;
import com.ahenry.msmsimporter.models.SMS;
import com.ahenry.msmsimporter.ui_fragments.UI_ProgressBar_Fragment;
import com.ahenry.msmsimporter.utilities.Constants;
import com.ahenry.msmsimporter.utilities.Utilities;
import com.daimajia.numberprogressbar.NumberProgressBar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by axel on 01/10/15.
 */
class ProgressUpdate{
    public final NumberProgressBar npb;
    public final int nb;

    public ProgressUpdate(NumberProgressBar a, int b){
        npb = a;
        nb = b;
    }
}

class ProcessingResults {
    public final int corProcessedSMSes;
    public final int corProcessedMMSes;

    public ProcessingResults(int smsesNumber, int mmsesNumber){
        corProcessedSMSes = smsesNumber;
        corProcessedMMSes = mmsesNumber;
    }
}

public class ExportTask extends AsyncTask<Void, NumberProgressBar, ProcessingResults> {

    private static final Logger LOG = LoggerFactory.getLogger(ExportTask.class);
    private static final int MESSAGE_TYPE_SENT = 2;

    private File myDir = null;
    private Uri myUri = null;
    private MainActivity myAct = null;
    private Context myContext = null;
    private LinkedList<SMS> mySMSes = null;
    private LinkedList<MMS> myMMSes = null;
    private NumberProgressBar myMMSBar= null;
    private NumberProgressBar mySMSBar = null;
    private int smsesNumber = 0;
    private int mmsesNumber = 0;
    private UI_ProgressBar_Fragment mmsBar = null;
    private UI_ProgressBar_Fragment smsBar = null;

    public ExportTask(MainActivity aAct, Uri aUri){
        myAct = aAct;
        myContext = myAct.getApplicationContext();
        myDir = new File(aAct.getAppDir(), Constants.exportDir);
        myUri = aUri;
        mySMSes = new LinkedList<SMS>();
        myMMSes = new LinkedList<MMS>();

    }

    @Override
    protected void onPreExecute(){

        Utilities.deleteAllFilesFromFolder(myDir.getPath());
        FragmentManager fm = myAct.getSupportFragmentManager();
        //FragmentManager fm = myAct.getFragmentManager();
        smsesNumber = Utilities.getElementsNumber(myContext, Constants.smsBaseUri);
        smsBar = (UI_ProgressBar_Fragment) fm.findFragmentById(R.id.smsFragment);
        //Log.d(TAG, "corProcessedMMSes number : "+corProcessedSMSes);
        smsBar.setNumber(myAct.getResources().getString(R.string.smses), smsesNumber);
        //Log.d(TAG, "fragment ");
        mySMSBar = (NumberProgressBar) smsBar.getView().findViewById(R.id.progressBar);
        mySMSBar.setMax(smsesNumber);

        mmsesNumber = Utilities.getElementsNumber(myContext, Constants.mmsBaseUri);
        mmsBar = (UI_ProgressBar_Fragment) fm.findFragmentById(R.id.mmsFragment);
        //Log.d(TAG, "corProcessedMMSes number : "+corProcessedMMSes);
        mmsBar.setNumber(myAct.getResources().getString(R.string.mmses), mmsesNumber);
        //Log.d(TAG, "fragment ");
        myMMSBar = (NumberProgressBar) mmsBar.getView().findViewById(R.id.progressBar);
        myMMSBar.setMax(mmsesNumber);

        /*JAVA 1.7 VERSION
        try (Cursor cur = myContext.getContentResolver().query(Uri.parse(Constants.smsBaseUri), null, null, null, null)) {
            smsesNumber = cur.getCount();
            smsBar = (UI_ProgressBar_Fragment) fm.findFragmentById(R.id.smsFragment);
            //Log.d(TAG, "corProcessedMMSes number : "+corProcessedSMSes);
            smsBar.setNumber(myAct.getResources().getString(R.string.smses), smsesNumber);
            //Log.d(TAG, "fragment ");
            mySMSBar = (NumberProgressBar) smsBar.getView().findViewById(R.id.progressBar);
            mySMSBar.setMax(smsesNumber);
        }
        try (Cursor cur = myContext.getContentResolver().query(Uri.parse(Constants.mmsBaseUri), null, null, null, null)){
            mmsesNumber = cur.getCount();
            mmsBar = (UI_ProgressBar_Fragment) fm.findFragmentById(R.id.mmsFragment);
            //Log.d(TAG, "corProcessedMMSes number : "+corProcessedMMSes);
            mmsBar.setNumber(myAct.getResources().getString(R.string.mmses), mmsesNumber);
            //Log.d(TAG, "fragment ");
            myMMSBar = (NumberProgressBar) mmsBar.getView().findViewById(R.id.progressBar);
            myMMSBar.setMax(mmsesNumber);
        }*/
    }



    public int processSMSes(){
        int addedSMSes = 0;
        Cursor cur = null;
        try{
            cur = myContext.getContentResolver().query(Uri.parse(Constants.smsBaseUri), null, null, null, null);

            while (cur.moveToNext()) {
                String address = cur.getString(cur.getColumnIndex("address"));
                String body = cur.getString(cur.getColumnIndex("body"));
                boolean sent = cur.getInt(cur.getColumnIndex("type")) == MESSAGE_TYPE_SENT;
                long tstamp = cur.getLong(cur.getColumnIndex("date"));

                SMS sms = new SMS(tstamp, address, body, sent);
                if(sms.isValid()) {
                    mySMSes.add(sms);
                    addedSMSes+=1;
                }

                //Log.d(TAG, sms.toString());
                //publishProgress(new ProgressUpdate(mySMSBar,1));
                publishProgress(mySMSBar);
                //publishProgress(new ProgressUpdate(mySMSBar,addedSMSes+=1));
                //smsesFound+=1;
            }

        }catch(NullPointerException npe){
            LOG.error("doInBackground :: null pointer exception occured.\n\t", npe);
        }finally{
            //Utilities.close(cur);
            if(cur != null){
                cur.close();
            }
        }

/*        JAVA 1.7 VERSION
        try (Cursor cur = myContext.getContentResolver().query(Uri.parse(Constants.smsBaseUri), null, null, null, null)) {
            //Log.d(TAG, "elem number : "+cur.getCount());
            while (cur.moveToNext()) {
                String address = cur.getString(cur.getColumnIndex("address"));
                String body = cur.getString(cur.getColumnIndex("body"));
                boolean sent = cur.getInt(cur.getColumnIndex("type")) == MESSAGE_TYPE_SENT;
                long tstamp = cur.getLong(cur.getColumnIndex("date"));

                SMS sms = new SMS(tstamp, address, body, sent);
                if(sms.isValid()) {
                    mySMSes.add(sms);
                    addedSMSes+=1;
                }

                //Log.d(TAG, sms.toString());
                publishProgress(new ProgressUpdate(mySMSBar,1));
                //publishProgress(new ProgressUpdate(mySMSBar,addedSMSes+=1));
                //smsesFound+=1;
            }
        } catch (NullPointerException npe) {
            LOG.error("doInBackground :: null pointer exception occured.\n\t", npe);
            //Log.d(TAG, "doInBackground :: null pointer exception occured");
        }*/

        LOG.debug("doInBackground :: {} smses found.", addedSMSes);
        //Log.d(TAG, "doInBackground :: "+ smsesFound + " corProcessedSMSes found");
        Utilities.writeJson(new File(myDir, "sms.json").getPath(), mySMSes);

        return addedSMSes;
    }

    public int processMMSes(){
        int addedMMSes = 0;
        Cursor cur = null;
        Cursor addr;
        Cursor part;
        FileOutputStream fos;

        try{
            cur = myContext.getContentResolver().query(Uri.parse(Constants.mmsBaseUri), null, null, null, null);
            while (cur.moveToNext()) {
                String id =  cur.getString(cur.getColumnIndex("_id"));
                boolean sent = cur.getInt(cur.getColumnIndex("msg_box")) == MESSAGE_TYPE_SENT;
                String trid = cur.getString(cur.getColumnIndex("tr_id"));
                long tstamp = cur.getLong(cur.getColumnIndex("date"));
                //String subject = cur.getString(cur.getColumnIndex("sub"));
                String tmpsub = cur.getString(cur.getColumnIndex("sub"));
                String subject = tmpsub == null ? "" : Utilities.utf8ToInternal(tmpsub);
                //String subject = new String(tmpsub.getBytes(ISO_8859_1), UTF_8);
                //String subject = tmpsub;
                //Log.d(TAG, "subject : "+subject);
                //LOG.debug("processMMS :: subject : {}", Utilities.utf8ToInternal(tmpsub));
                String type = "";
                String pathToAttachment = "";
                String attachmentName = "";
                ArrayList<String> to = new ArrayList<String>();
                addr = null;
                try{
                    addr = myContext.getContentResolver().query(Uri.parse(Constants.mmsBaseUri + id + "/addr"), null, null, null, null);
                    while(addr.moveToNext()){
                        String num = addr.getString(addr.getColumnIndex("address"));
                        if(Utilities.isMMSAddressValid(num)) {
                            to.add(num);
                        }
                    }
                }catch(NullPointerException npe){
                    LOG.error("doInBackground :: null pointer exception on addr.\n\t", npe);
                }finally{
                    //Utilities.close(addr);
                    if(addr != null){
                        addr.close();
                    }
                }

                part = null;
                try{
                    part = myContext.getContentResolver().query(Uri.parse(Constants.mmsBaseUri+id+"/part"),null,null,null,null);
                    while(part.moveToNext()){
                        String _id = part.getString(part.getColumnIndex("_id"));
                        byte[] fileBytes = Utilities.getMMSPart(myContext, _id);
                        if( fileBytes != null ){
                            LOG.debug("doInBackground :: mms part found.");
                            type = part.getString(part.getColumnIndex("ct"));
                            //pathToAttachment = part.getString(part.getColumnIndex("_data"));
                            attachmentName = part.getString(part.getColumnIndex("name"));
                            //File workingFolder = new File(myDir);
                            //TODO CREATE FOLDER TREE ONLY IF PART IS NOT NULL, CHECK GETMMSPART RESULT FIRST

                            File relative = new File(trid,attachmentName);
                            //Log.d(TAG, "relative path : "+relative.getPath());
                            File absolute = new File(myDir, relative.getPath());

                            new File(myDir, trid).mkdirs();
                            /*File folder = new File(myDir, trid);
                            folder.mkdirs();*/

                            //absolute.mkdir();
                            //Log.d(TAG, "path : "+absolute.getPath());
                            //fos = null;
                            try{
                                fos = new FileOutputStream(absolute);
                                fos.write(fileBytes);
                            }catch (FileNotFoundException fne) {
                                //Log.d(TAG, "doInBackground :: FileNotFoundException :: "+fne.getMessage());
                                LOG.debug("doInBackground :: FileNotFoundException.\n\t", fne);
                            } catch (IOException ioe) {
                                //Log.d(TAG, "doInBackground :: IOException :: "+ioe.getMessage());
                                LOG.debug("doInBackground :: IOException.\n\t", ioe);
                            }
                            pathToAttachment = relative.getPath();
                        }else{
                            LOG.debug("doInBackground :: no mms part found");
                        }

                    }
                }catch(NullPointerException npe){
                    LOG.error("doInBackground :: null pointer exception on part.\n\t", npe);
                }finally{
                    //Utilities.close(part);
                    if(part != null){
                        part.close();
                    }
                }
                MMS mms = new MMS(trid, tstamp, to.toArray(new String[to.size()]), pathToAttachment, attachmentName, subject, type, sent);
                if(mms.isValid()) {
                    myMMSes.add(mms);
                    //Log.d(TAG, "cursor : \n" + cur.toString());
                    //publishProgress(new ProgressUpdate(myMMSBar, addedMMSes += 1));
                    addedMMSes+=1;
                }
                //publishProgress(new ProgressUpdate(myMMSBar, 1));
                publishProgress(myMMSBar);
                //mmsesFound+=1;
            }
        }catch(NullPointerException npe){
            LOG.error("doInBackground :: null pointer exception occured.\n\t", npe);
        }finally{
            //Utilities.close(cur);
            if(cur != null){
                cur.close();
            }
        }

        /*JAVA 1.7 VERSION
        try (Cursor cur = myContext.getContentResolver().query(Uri.parse(Constants.mmsBaseUri), null, null, null, null)) {

            while (cur.moveToNext()) {
                String id =  cur.getString(cur.getColumnIndex("_id"));
                boolean sent = cur.getInt(cur.getColumnIndex("msg_box")) == MESSAGE_TYPE_SENT;
                String trid = cur.getString(cur.getColumnIndex("tr_id"));
                long tstamp = cur.getLong(cur.getColumnIndex("date"));
                //String subject = cur.getString(cur.getColumnIndex("sub"));
                String tmpsub = cur.getString(cur.getColumnIndex("sub"));
                tmpsub = tmpsub == null ? "" : tmpsub;
                //String subject = new String(tmpsub.getBytes(ISO_8859_1), UTF_8);
                String subject = tmpsub;
                //Log.d(TAG, "subject : "+subject);
                String type = "";
                String pathToAttachment = "";
                String attachmentName = "";
                ArrayList<String> to = new ArrayList<>();
                try(Cursor addr = myContext.getContentResolver().query(Uri.parse(Constants.mmsBaseUri + id + "/addr"), null, null, null, null)){
                    while(addr.moveToNext()){
                        String num = addr.getString(addr.getColumnIndex("address"));
                        if(Utilities.isMMSAddressValid(num)) {
                            to.add(num);
                        }
                    }
                }catch(NullPointerException npe){
                    LOG.error("doInBackground :: null pointer exception on addr.\n\t", npe);
                }
                try(Cursor part = myContext.getContentResolver().query(Uri.parse(Constants.mmsBaseUri+id+"/part"),null,null,null,null)){
                    while(part.moveToNext()){
                        String _id = part.getString(part.getColumnIndex("_id"));
                        byte[] fileBytes = Utilities.getMMSPart(myContext, _id);
                        if( fileBytes != null ){
                            LOG.debug("doInBackground :: mms part found.");
                            type = part.getString(part.getColumnIndex("ct"));
                            //pathToAttachment = part.getString(part.getColumnIndex("_data"));
                            attachmentName = part.getString(part.getColumnIndex("name"));
                            //File workingFolder = new File(myDir);
                            //TODO CREATE FOLDER TREE ONLY IF PART IS NOT NULL, CHECK GETMMSPART RESULT FIRST

                            File relative = new File(trid,attachmentName);
                            //Log.d(TAG, "relative path : "+relative.getPath());
                            File absolute = new File(myDir, relative.getPath());

                            new File(myDir, trid).mkdirs();
                            *//*File folder = new File(myDir, trid);
                            folder.mkdirs();*//*

                            //absolute.mkdir();
                            //Log.d(TAG, "path : "+absolute.getPath());
                            try (FileOutputStream fos = new FileOutputStream(absolute)) {
                                fos.write(fileBytes);
                            } catch (FileNotFoundException fne) {
                                //Log.d(TAG, "doInBackground :: FileNotFoundException :: "+fne.getMessage());
                                LOG.debug("doInBackground :: FileNotFoundException.\n\t", fne);
                            } catch (IOException ioe) {
                                //Log.d(TAG, "doInBackground :: IOException :: "+ioe.getMessage());
                                LOG.debug("doInBackground :: IOException.\n\t", ioe);
                            }
                            pathToAttachment = relative.getPath();
                        }else{
                            LOG.debug("doInBackground :: no mms part found");
                        }

                    }
                }catch(NullPointerException npe){
                    LOG.error("doInBackground :: null pointer exception on part.\n\t", npe);
                }
                MMS mms = new MMS(trid, tstamp, to.toArray(new String[to.size()]), pathToAttachment, attachmentName, subject, type, sent);
                //if(mms.isValid()) {
                    myMMSes.add(mms);
                    //Log.d(TAG, "cursor : \n" + cur.toString());
                    //publishProgress(new ProgressUpdate(myMMSBar, addedMMSes += 1));
                    addedMMSes+=1;
                //}
                publishProgress(new ProgressUpdate(myMMSBar, 1));
                //mmsesFound+=1;
            }
        } catch (NullPointerException npe) {
            LOG.error("doInBackground :: null pointer exception occured.\n\t", npe);
            //Log.d(TAG,"doInBackground :: null pointer exception occured");
        }*/

        LOG.debug("doInBackground :: {} mmses found.", addedMMSes);
        //Log.d(TAG,"doInBackground :: "+ mmsesFound + " corProcessedMMSes found");
        Utilities.writeJson(new File(myDir, "mms.json").getPath(), myMMSes);

        return addedMMSes;
    }

    @Override
    protected ProcessingResults doInBackground(Void... params) {

        int smsesFound = processSMSes();

        int mmsesFound = processMMSes();


        //return new ProcessingResults(0, mmsesFound);
        return new ProcessingResults(smsesFound,mmsesFound);
    }

    @Override
    protected void onPostExecute(ProcessingResults e){
        String mmses = myContext.getResources().getString(R.string.mmses);
        String smses = myContext.getResources().getString(R.string.smses);
        String exported = myContext.getResources().getString(R.string.exportedElements);
        smsBar.setTask(exported, smses);
        smsBar.setNumber(smses, e.corProcessedSMSes);
        mmsBar.setTask(exported, mmses);
        mmsBar.setNumber(mmses, e.corProcessedMMSes);
        String fName;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(myContext);
        boolean usingTarGz = sharedPref.getBoolean(SettingsActivity.KEY_PREF_TAR_GZ, false);
        boolean includeDebug = sharedPref.getBoolean(SettingsActivity.KEY_PREF_DEBUG, false);
        LOG.debug("onPostExecute :: gonna export in .tar.gz ? {}", usingTarGz);
        LOG.debug("onPostExecute :: gonna include debug files ? {}", includeDebug);
        //TODO adjust number of smses and mmses exported
        if(usingTarGz){
            LOG.debug("onPostExecute :: export to .tar.gz");
            fName = Utilities.tarGz(myUri, null, myDir.getPath(), includeDebug);
        }else{
            LOG.debug("onPostExecute :: export to .tar");
            fName = Utilities.tar(myUri, null, myDir.getPath(), includeDebug);
        }
        String msg;
        if(fName != null) {
            msg = String.format(myContext.getResources().getString(R.string.exportCompleted), e.corProcessedSMSes, smses, e.corProcessedMMSes, mmses, exported, fName);
        }else{
            msg = myContext.getResources().getString(R.string.exceptionOccured);
        }
        Toast.makeText(myContext, msg, Toast.LENGTH_LONG).show();
        //Log.d(TAG, "taring done");
    }

    protected  void onProgressUpdate(NumberProgressBar... npb){
        npb[0].incrementProgressBy(1);
    }

/*    protected void onProgressUpdate(ProgressUpdate... p){
        NumberProgressBar npb = p[0].npb;
        npb.incrementProgressBy(1);

    }*/
}
