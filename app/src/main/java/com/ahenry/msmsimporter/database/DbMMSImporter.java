package com.ahenry.msmsimporter.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.ahenry.msmsimporter.MainActivity;
import com.ahenry.msmsimporter.R;
import com.ahenry.msmsimporter.models.MMS;
import com.ahenry.msmsimporter.ui_fragments.UI_ProgressBar_Fragment;
import com.ahenry.msmsimporter.utilities.Constants;
import com.ahenry.msmsimporter.utilities.Utilities;
import com.daimajia.numberprogressbar.NumberProgressBar;

import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by axel on 10/06/15.
 */
public class DbMMSImporter extends AsyncTask<Void, Integer, Integer> {

    private static final String TAG = Constants.appName + " :: " + DbMMSImporter.class.getSimpleName();
    public static final Logger LOG = LoggerFactory.getLogger(DbSMSImporter.class);

    private static final int MESSAGE_TYPE_ALL = 0;
    private static final int MESSAGE_TYPE_INBOX = 1;
    private static final int MESSAGE_TYPE_SENT = 2;
    private static final int MESSAGE_TYPE_DRAFT = 3;
    private static final int MESSAGE_TYPE_OUTBOX = 4;
    private static final int MESSAGE_TYPE_FAILED = 5;
    private static final int MESSAGE_TYPE_QUEUED = 6;

    private MainActivity myAct = null;
    private Context myContext = null;
    private ContentValues mmsValues = null;
    private Uri myUri = null;
    private NumberProgressBar myProgressBar = null;
    private MMS[] myMMSArray = null;
    private UI_ProgressBar_Fragment myFragment = null;

    public DbMMSImporter(MainActivity aAct, UI_ProgressBar_Fragment aFrag, MMS[] aMMSArray ){
        //Log.d(TAG, "DbMMSImporter created");
        LOG.debug("DbMMSImporter created.");
        //myUri = Uri.parse("content://mms");
        myUri = Uri.parse(Constants.mmsBaseUri);
        myAct = aAct;
        myContext = myAct.getApplicationContext();
        myFragment = aFrag;
        myProgressBar = (NumberProgressBar) myFragment.getView().findViewById(R.id.progressBar);
        myMMSArray = aMMSArray;
    }

    public static void update(Context context, Uri uri, int status)
    {
        ContentValues updateValues = new ContentValues();
        updateValues.put("msg_box", status);
        context.getContentResolver().update(uri, updateValues, null, null);
    }

    public static void update(Context context, Uri uri, int status, String message_id)
    {
        ContentValues updateValues = new ContentValues();
        updateValues.put("msg_box", status);
        updateValues.put("m_id", message_id);
        context.getContentResolver().update(uri, updateValues, null, null);
    }

    public static void setUnread(Context context, Uri uri)
    {
        ContentValues updateValues = new ContentValues();
        updateValues.put("read", 0);
        context.getContentResolver().update(uri, updateValues, null, null);
    }

    public Uri insertMMS(MMS aMMS){

        String[] to = aMMS.getTo();
        long timestamp = aMMS.getTimestamp();

        Set<String> recipients = new HashSet<String>();
        recipients.addAll(Utilities.getValidatedSenders(Arrays.asList(to)));
        long thread_id = Utilities.getOrCreateThreadId(myContext, recipients);
        LOG.debug("insertMMS :: [DATABASE] Thread ID is {}.", thread_id);
/*
        ContentValues dummyValues = new ContentValues();
        dummyValues.put("thread_id",thread_id);
        dummyValues.put("body", "Dummy SMS body.");

        Uri dummySms = myContext.getContentResolver().insert(Uri.parse("content://sms/sent"), dummyValues);*/
        //Log.d(TAG, "[DATABASE] Thread ID is " + thread_id);
        /*working statement*/
        //String mySubject = new String(aMMS.getSubject().getBytes(), ISO_8859_1);
        //String mySubject = new String(aMMS.getSubject().getBytes(), UTF_8);
        //String mySubject = aMMS.getSubject();
        //String mySubject = new String(aMMS.getSubject().getBytes(UTF_8), UTF_8);
        String mySubject = Utilities.internalToUTF8(aMMS.getSubject());
        LOG.debug("insertMMS :: subject : {}", mySubject);
        mmsValues = new ContentValues();
        mmsValues.put("thread_id", thread_id);
        mmsValues.put("date", timestamp);
        //mmsValues.put("date_sent", timestamp);
        //mmsValues.put("msg_box", aMMS.isSent() ? MESSAGE_TYPE_SENT: MESSAGE_TYPE_INBOX);
        //tests for api10
        mmsValues.put("msg_box", aMMS.isSent() ? MESSAGE_TYPE_SENT : MESSAGE_TYPE_INBOX);
        mmsValues.put("read", 1);
        mmsValues.put("sub", mySubject);
        //mmsValues.put("sub_cs", 106);
        //TODO IF NOT SET, PREVIEW WORKS, CONVERSATION VIEW IS NOT DISPLAYING SPECIAL CHARACTERS
        //TODO IF SET, NONE'S WORKING CORRECTLY
        mmsValues.put("sub_cs", Constants.CHARSET_UTF8);
        mmsValues.put("ct_t", "application/vnd.wap.multipart.related");
        mmsValues.put("exp", 604800);
        mmsValues.put("m_cls", "personal");
        mmsValues.put("m_type", 128); // 132 (RETRIEVE CONF) 130 (NOTIF IND) 128 (SEND REQ)
        mmsValues.put("v", 18);
        mmsValues.put("pri", 129);
        mmsValues.put("rr", 129);
        //mmsValues.put("tr_id", "T"+ Long.toHexString(timestamp));
        mmsValues.put("tr_id", aMMS.getTransactionid());
        mmsValues.put("d_rpt", 129);
        mmsValues.put("resp_st", 128);
        mmsValues.put("seen",1);

        // Check if column exists (Fix for Xperia)
        //try(Cursor c = myContext.getContentResolver().query(Uri.parse("content://mms"), null, null, null, null)) {
        Cursor c = null;
        try{
            c = myContext.getContentResolver().query(Uri.parse(Constants.mmsBaseUri), null, null, null, null);
            if (c.getColumnIndex("sequence_time") != -1) {
                mmsValues.put("sequence_time", timestamp);
            }
        }catch(NullPointerException npe){
            LOG.error("insertMMS :: cursor NullPointerException.\n\t",npe);
        }finally{
            //Utilities.close(c);
            if(c!=null){
                c.close();
            }
        }

        /*JAVA 1.7 version
        try(Cursor c = myContext.getContentResolver().query(Uri.parse(Constants.mmsBaseUri), null, null, null, null)) {
            //Cursor c = myContext.getContentResolver().query(Uri.parse("content://mms"), null, null, null, null);
            if (c.getColumnIndex("sequence_time") != -1) {
                mmsValues.put("sequence_time", timestamp);
            }
        }*/
        //c.close();

        // Insert message
        Uri res = myContext.getContentResolver().insert(myUri, mmsValues);
        String messageId = res.getLastPathSegment().trim();
        LOG.debug("insertMMS :: [DATABASE] Message saved as {}", res);
        //Log.d(TAG, "[DATABASE] Message saved as " + res);

        if(aMMS.pathToAttachment != null){
            createPart(messageId, aMMS);
        }else{
            LOG.debug("insertMMS :: no parts found.");
            //Log.d(TAG, "no parts found");
        }

        // Create addresses
        for (String address : recipients)
        {
            createAddress(messageId, address);
        }

        //myContext.getContentResolver().delete(dummySms, null, null);

        return res;

        /*}catch(UnsupportedEncodingException uee){
            Log.d(TAG, "can't convert subject to ISO-8859-1, " + uee.getMessage());
        }*/
        //return null;

    }


    private Uri createPart(String id, MMS aMMS){
        ContentValues mmsPartValue = new ContentValues();
        mmsPartValue.put("mid", id);
        mmsPartValue.put("ct", aMMS.getMimeType());
        mmsPartValue.put("name", aMMS.getAttachmentName());
        mmsPartValue.put("cl", aMMS.getAttachmentName());
        mmsPartValue.put("cid", "<"+ aMMS.getAttachmentName()+ ">");
        //mmsPartValue.put("chset", Constants.CHARSET_UTF8);
        //Uri partUri = Uri.parse("content://mms/" + id + "/part");
        Uri partUri = Uri.parse(Constants.mmsBaseUri + id + "/part");
        Uri res = myContext.getContentResolver().insert(partUri, mmsPartValue);
        LOG.debug("createPart :: [DATABASE] Part uri is {}", res.toString());
        //Log.d(TAG, "[DATABASE] Part uri is " + res.toString());

        // Add data to part
        OutputStream os = null;
        try{
            os = myContext.getContentResolver().openOutputStream(res);
            File attachmentFile = new File(new File(myAct.getAppDir(), Constants.importDir) , aMMS.getPathToAttachment());
            LOG.debug("createPart :: file path : {}", attachmentFile.getPath());

            FileInputStream fis = null;
            try{
                fis = new FileInputStream(attachmentFile);
                byte[] img = IOUtils.toByteArray(fis);
                os.write(img);
            }catch(IOException ioe){
                LOG.error("createPart :: fileinputstream IOException ioe.\n\t",ioe);
            }finally{
                //Utilities.close(fis);
                if(fis != null){
                    try{
                        fis.close();
                    } catch (IOException ioe) {
                        LOG.error("close :: IOException.\n\t", ioe);
                    }
                }
            }
            return res;
        }catch(FileNotFoundException fne){
            LOG.error("createPart :: outputstream FileNotFoundException.\n\t",fne);
        }finally {
            //Utilities.close(os);
            if(os != null){
                try{
                    os.close();
                } catch (IOException ioe) {
                    LOG.error("close :: IOException.\n\t", ioe);
                }
            }
        }

        /*JAVA 1.7 VERSION
        try(OutputStream os = myContext.getContentResolver().openOutputStream(res)) {
            //OutputStream os = myContext.getContentResolver().openOutputStream(res);
            File attachmentFile = new File(new File(myAct.getAppDir(), Constants.importDir) , aMMS.getPathToAttachment());
            LOG.debug("createPart :: file path : {}", attachmentFile.getPath());
            //Log.d(TAG, "file path : "+folder.getPath());
            try(FileInputStream fis = new FileInputStream(attachmentFile)) {
                //FileInputStream fis = new FileInputStream(attachmentFile);
                byte[] img = IOUtils.toByteArray(fis);
                os.write(img);
            }
            *//*os.close();
            fis.close();*//*

            return res;
        }catch(FileNotFoundException fnfe){
            LOG.error("createPart :: FileNotFoundException : attachement file not found.\n\t", fnfe);
            //Log.d(TAG, "attachment file not found, "+fnfe.getMessage());
        }catch(IOException ioe){
            LOG.error("createPart :: IOException.\n\t", ioe);
            //Log.d(TAG, "exception, "+ioe.getMessage());
        }*/
        return null;
    }

    private Uri createAddress(String id, String addr){
        ContentValues addrValues = new ContentValues();
        addrValues.put("address", addr);
        //addrValues.put("charset", "106");
        addrValues.put("charset", Constants.CHARSET_UTF8);
        addrValues.put("type", 151); // TO

        //Uri addrUri = Uri.parse("content://mms/"+ id +"/addr");
        Uri addrUri = Uri.parse(Constants.mmsBaseUri+ id +"/addr");
        Uri res = myContext.getContentResolver().insert(addrUri, addrValues);
        LOG.debug("createAddress :: [DATABASE] Addr uri is {}", res.toString());
        //Log.d(TAG, "[DATABASE] Addr uri is " + res.toString());

        return res;

    }

    @Override
    protected void onPreExecute() {

        LOG.debug("onPreExecute :: DbMMSImporter is live.");
        myFragment.setTask(myContext.getResources().getString(R.string.importingElements), myContext.getResources().getString(R.string.mmses));
        myProgressBar.setMax(myMMSArray.length);

    }

    @Override
    protected void onPostExecute(Integer integer) {

        //super.onPostExecute(integer);
        myFragment.setTask(myContext.getResources().getString(R.string.importedElements), myContext.getResources().getString(R.string.mmses));
        myFragment.setNumber(myContext.getResources().getString(R.string.mmses), integer);
        String msg = String.format(myContext.getResources().getString(R.string.importCompleted), integer, myContext.getResources().getString(R.string.mmses), myContext.getResources().getString(R.string.importedElements),myMMSArray.length - integer);
        Toast.makeText(myContext, msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onProgressUpdate(Integer... values) {

        myProgressBar.incrementProgressBy(1);

    }

    @Override
    protected Integer doInBackground(Void... params) {

        return processMMS();

    }

    private int processMMS(){
        int insertedMMSes = 0;
        Uri uri;
        for(MMS mms : myMMSArray){
            if(mms.isValid()) {
                uri = insertMMS(mms);
                if (uri != null) {
                    insertedMMSes += 1;
                }
            }
            publishProgress(1);
        }
        return insertedMMSes;
    }

    public Uri insertMMS(MMS aMMS, int charset) {

        String[] to = aMMS.getTo();
        long timestamp = aMMS.getTimestamp();

        Set<String> recipients = new HashSet<String>();
        recipients.addAll(Utilities.getValidatedSenders(Arrays.asList(to)));
        long thread_id = Utilities.getOrCreateThreadId(myContext, recipients);
        LOG.debug("insertMMS :: [DATABASE] Thread ID is {}.", thread_id);
        //Log.d(TAG, "[DATABASE] Thread ID is " + thread_id);
        /*working statement*/
        //String mySubject = new String(aMMS.getSubject().getBytes(), ISO_8859_1);

        String mySubject = Utilities.internalToUTF8(aMMS.getSubject());

        //String mySubject = aMMS.getSubject();
        LOG.debug("insertMMS :: subject : {}", mySubject);
        mmsValues = new ContentValues();
        mmsValues.put("thread_id", thread_id);
        mmsValues.put("date", timestamp);
        //mmsValues.put("date_sent", timestamp);
        mmsValues.put("msg_box", aMMS.isSent() ? MESSAGE_TYPE_SENT : MESSAGE_TYPE_INBOX);
        mmsValues.put("read", 1);
        mmsValues.put("sub", mySubject);
        //mmsValues.put("sub_cs", charset);
        mmsValues.put("sub_cs", Constants.CHARSET_UTF8);
        mmsValues.put("ct_t", "application/vnd.wap.multipart.related");
        mmsValues.put("exp", 604800);
        mmsValues.put("m_cls", "personal");
        mmsValues.put("m_type", 128); // 132 (RETRIEVE CONF) 130 (NOTIF IND) 128 (SEND REQ)
        mmsValues.put("v", 18);
        mmsValues.put("pri", 129);
        mmsValues.put("rr", 129);
        mmsValues.put("tr_id", "T" + Long.toHexString(timestamp));
        mmsValues.put("d_rpt", 129);
        mmsValues.put("resp_st", 128);
        mmsValues.put("seen", 1);


        Cursor c = null;
        try{
            c = myContext.getContentResolver().query(Uri.parse(Constants.mmsBaseUri), null, null, null, null);
            if (c.getColumnIndex("sequence_time") != -1) {
                mmsValues.put("sequence_time", timestamp);
            }
        }catch(NullPointerException npe){
            LOG.error("insertMMS :: cursor NullPointerException.\n\t",npe);
        }finally{
            //Utilities.close(c);
            if(c!=null){
                c.close();
            }
        }

        // Check if column exists (Fix for Xperia)
        //try(Cursor c = myContext.getContentResolver().query(Uri.parse("content://mms"), null, null, null, null)) {
        /*JAVA 1.7 VERSION
        try (Cursor c = myContext.getContentResolver().query(Uri.parse(Constants.mmsBaseUri), null, null, null, null)) {
            //Cursor c = myContext.getContentResolver().query(Uri.parse("content://mms"), null, null, null, null);
            if (c.getColumnIndex("sequence_time") != -1) {
                mmsValues.put("sequence_time", timestamp);
            }
        }*/
        //c.close();

        // Insert message
        Uri res = myContext.getContentResolver().insert(myUri, mmsValues);
        String messageId = res.getLastPathSegment().trim();
        LOG.debug("insertMMS :: [DATABASE] Message saved as {}", res);
        return res;
    }

}
