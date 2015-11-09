package com.ahenry.msmsimporter.database;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.ahenry.msmsimporter.MainActivity;
import com.ahenry.msmsimporter.R;
import com.ahenry.msmsimporter.models.SMS;
import com.ahenry.msmsimporter.ui_fragments.UI_ProgressBar_Fragment;
import com.ahenry.msmsimporter.utilities.Constants;
import com.ahenry.msmsimporter.utilities.Utilities;
import com.daimajia.numberprogressbar.NumberProgressBar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Created by axel on 10/06/15.
 */
public class DbSMSImporter extends AsyncTask<Void, Integer, Integer> {

    public static final Logger LOG = LoggerFactory.getLogger(DbSMSImporter.class);
    private static final int MESSAGE_TYPE_ALL = 0;
    private static final int MESSAGE_TYPE_INBOX = 1;
    private static final int MESSAGE_TYPE_SENT = 2;
    private static final int MESSAGE_TYPE_DRAFT = 3;
    private static final int MESSAGE_TYPE_OUTBOX = 4;
    private static final int MESSAGE_TYPE_FAILED = 5;
    private static final int MESSAGE_TYPE_QUEUED = 6;
    private HashMap<String, Long> thread_idMap = new HashMap<String, Long>();

    private ContentValues smsValues = null;
    private Uri myUriToDelete = null;
    private Uri myUri = null;
    private MainActivity myAct;
    private Context myContext;
    private UI_ProgressBar_Fragment myFragment = null;
    private NumberProgressBar myProgressBar = null;
    private SMS[] mySMSArray = null;



    public DbSMSImporter(MainActivity aAct, UI_ProgressBar_Fragment aFrag, SMS[] aSMSArray){
        LOG.debug("DbSMSImporter created.");
        myAct = aAct;
        myContext = myAct.getApplicationContext();
        //myUri = Uri.parse("content://sms/");
        myUri = Uri.parse(Constants.smsBaseUri);
        //myUriToDelete = Uri.parse("content://sms/conversations/-1");
        myUriToDelete = Uri.parse(Constants.smsDeleteUri);
        myFragment = aFrag;
        myProgressBar = (NumberProgressBar) myFragment.getView().findViewById(R.id.progressBar);
        mySMSArray = aSMSArray;
    }

    private Uri insertSMS(SMS aSMS){
        String to = aSMS.getTo();
        if(Utilities.isPhoneNumberValid(to)) {
            Long thread_id = thread_idMap.get(to);

            if (thread_id == null) {
                thread_id = Utilities.getOrCreateThreadId(myContext, to);
                thread_idMap.put(to, thread_id);
                LOG.debug("insertSMS :: new thread_id {} in thread_idMap", thread_id);
            }

            //long thread_id = Utilities.getOrCreateThreadId(TAG, myContext, to);
            smsValues = new ContentValues();
            smsValues.put("thread_id", thread_id);
            smsValues.put("address", to);
            /* timestamp value must be on 13 digits if you want to have a coherent date, cause android take milliseconds for sms*/
            smsValues.put("date", aSMS.getTimestamp()*1000);
            //smsValues.put("date_sent", aSMS.getTimestamp()*1000);
            smsValues.put("read", 1);
            smsValues.put("type", aSMS.isSent() ? MESSAGE_TYPE_SENT : MESSAGE_TYPE_INBOX);
            smsValues.put("body", aSMS.getBody());
            smsValues.put("seen",1);
            //Uri uri = myContext.getContentResolver().insert(myUri, smsValues);
            /** This is very important line to solve the problem */
            //myContext.getContentResolver().delete(myUriToDelete, null, null);
            return myContext.getContentResolver().insert(myUri, smsValues);
        }else{
            LOG.debug("insertSMS :: phone number {} is not valid.", to);
            return null;
        }
    }

    @Override
    protected void onPreExecute() {

        LOG.debug("onPreExecute :: DbSMSImporter is live.");
        myProgressBar.setMax(mySMSArray.length);
        myFragment.setTask(myContext.getResources().getString(R.string.importingElements), myContext.getResources().getString(R.string.smses));

    }

    @Override
    protected void onPostExecute(Integer integer) {

        //super.onPostExecute(integer);

        myFragment.setTask(myContext.getResources().getString(R.string.importedElements), myContext.getResources().getString(R.string.smses));
        myFragment.setNumber(myContext.getResources().getString(R.string.smses),integer);
        String msg = String.format(myContext.getResources().getString(R.string.importCompleted), integer, myContext.getResources().getString(R.string.smses), myContext.getResources().getString(R.string.importedElements),mySMSArray.length - integer);
        Toast.makeText(myContext, msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onProgressUpdate(Integer... values) {

        myProgressBar.incrementProgressBy(1);

    }

    @Override
    protected Integer doInBackground(Void... params) {

        int insertedSMSes = 0;
        Uri uri;
        for(SMS sms : mySMSArray){
        //for(int i=0; i < mySMSArray.length; i++){
            //sms = mySMSArray[i];
            if(sms.isValid()) {
                uri = insertSMS(sms);
                if (uri != null) {
                    insertedSMSes += 1;
                }
            }
            publishProgress(1);
        }
        return insertedSMSes;
    }
}
