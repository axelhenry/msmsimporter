package com.ahenry.msmsimporter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.ahenry.msmsimporter.filepicker.TarPickerActivity;
import com.ahenry.msmsimporter.ui_fragments.UI_ProgressBar_Fragment;
import com.ahenry.msmsimporter.utilities.Constants;
import com.ahenry.msmsimporter.utilities.Utilities;
import com.codesgood.views.JustifiedTextView;
import com.crashlytics.android.Crashlytics;
import com.nononsenseapps.filepicker.FilePickerActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

/*import org.acra.*;
import org.acra.annotation.*;

import org.acra.sender.HttpSender;*/

/*@ReportsCrashes(
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON,
        formUri = "http://acra-f2bc16.smileupps.com/acra-myapp-b4efb7/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "reporter",
        formUriBasicAuthPassword = "reporter"
)*/
//public class MainActivity extends Activity implements JustifiedTextView.TextLinkClickListener {
public class MainActivity extends AppCompatActivity implements JustifiedTextView.TextLinkClickListener {

    public static final Logger LOG = LoggerFactory.getLogger(MainActivity.class);
    private File myDir;
    private String defaultSmsPackage;
    private UI_ProgressBar_Fragment mmsFrag = null;
    private UI_ProgressBar_Fragment smsFrag = null;
    //TextView tv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        //ACRA.init(this.getApplication());


        //tv.setText("TestCrash");
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.activity_main);

        changeSMSPackage();

        /*String myPackageName = getPackageName();
        defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this);
        //Log.d(TAG, "packageName: " + myPackageName);
        //LOG.debug(TAG, "packageName: " + myPackageName);
        LOG.debug("packageName : {}", myPackageName);
        //Log.d(TAG, "default SMS package: " + defaultSmsPackage);
        //LOG.debug(TAG, "default SMS package: " + defaultSmsPackage);
        LOG.debug("default SMS package : {}", defaultSmsPackage);
        if(myPackageName.compareTo(defaultSmsPackage)!=0) {
            Utilities.requestDefaultSmsPackageChange(this);
        }*/

        createFolders();

        /*myDir = getDir("appDir", MODE_PRIVATE);
        File exportDir = new File(myDir, Constants.exportDir);
        exportDir.mkdir();
        File importDir = new File(myDir, Constants.importDir);
        importDir.mkdir();*/
        /*mmsFrag = (UI_ProgressBar_Fragment) getSupportFragmentManager().findFragmentById(R.id.mmsFragment);
        //mmsFrag = (UI_ProgressBar_Fragment) getFragmentManager().findFragmentById(R.id.mmsFragment);
        if(mmsFrag == null){
            LOG.debug("mainActivity :: mmsFrag is null, why, fucking why.");
            mmsFrag = new UI_ProgressBar_Fragment();
            Bundle b = new Bundle();
            b.putString("name", getResources().getString(R.string.mmses));
            mmsFrag.setArguments(b);
            getSupportFragmentManager().beginTransaction().add(R.id.mmsFragment,mmsFrag).commit();
        }
        //mmsFrag.initFragment(getResources().getString(R.string.mmses));
        smsFrag = (UI_ProgressBar_Fragment) getSupportFragmentManager().findFragmentById(R.id.smsFragment);
        //smsFrag = (UI_ProgressBar_Fragment) getFragmentManager().findFragmentById(R.id.smsFragment);
        if(smsFrag == null){
            LOG.debug("mainActivity :: smsFrag is null, why, fucking why.");
            smsFrag = new UI_ProgressBar_Fragment();
            Bundle b = new Bundle();
            b.putString("name", getResources().getString(R.string.smses));
            smsFrag.setArguments(b);
            getSupportFragmentManager().beginTransaction().add(R.id.smsFragment,smsFrag).commit();
        }*/

        //smsFrag.initFragment(getResources().getString(R.string.smses));
        //UI_Presentation_Fragment pf = (UI_Presentation_Fragment) getSupportFragmentManager().findFragmentById(R.id.presentationFragment);
        //UI_Presentation_Fragment pf = (UI_Presentation_Fragment) getFragmentManager().findFragmentById(R.id.presentationFragment);
        //pf.initFragment(this, R.color.link_text_material_light);
        //pf.initFragment(this, R.color.links);

        mmsFrag = (UI_ProgressBar_Fragment) getSupportFragmentManager().findFragmentById(R.id.mmsFragment);
        //mmsFrag = (UI_ProgressBar_Fragment) getFragmentManager().findFragmentById(R.id.mmsFragment);
        if(mmsFrag == null){
            LOG.debug("mainActivity :: mmsFrag is null, why, fucking why.");
            mmsFrag = new UI_ProgressBar_Fragment();
            LOG.debug("onCreate :: creatingFragment.");
            Bundle b = new Bundle();
            LOG.debug("onCreate :: creating Bundle.");
            b.putString("name", getResources().getString(R.string.mmses));
            mmsFrag.setArguments(b);
            LOG.debug("onCreate :: setArguments().");
            getSupportFragmentManager().beginTransaction().add(R.id.mmsFragment,mmsFrag).commit();
            LOG.debug("onCreate :: replaceFragment.");
        }
        //mmsFrag.initFragment(getResources().getString(R.string.mmses));
        smsFrag = (UI_ProgressBar_Fragment) getSupportFragmentManager().findFragmentById(R.id.smsFragment);
        //smsFrag = (UI_ProgressBar_Fragment) getFragmentManager().findFragmentById(R.id.smsFragment);
        if(smsFrag == null){
            LOG.debug("mainActivity :: smsFrag is null, why, fucking why.");
            smsFrag = new UI_ProgressBar_Fragment();
            Bundle b = new Bundle();
            b.putString("name", getResources().getString(R.string.smses));
            smsFrag.setArguments(b);
            getSupportFragmentManager().beginTransaction().add(R.id.smsFragment,smsFrag).commit();
        }

        //String.format(getResources().getString(R.string.importingElements), getResources().getString(R.string.corProcessedSMSes));
        Button mButton = (Button) findViewById(R.id.importButton);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFragments();
                Intent i = new Intent(getApplicationContext(), TarPickerActivity.class);

                i.putExtra(TarPickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(TarPickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                i.putExtra(TarPickerActivity.EXTRA_MODE, TarPickerActivity.MODE_FILE);

                i.putExtra(TarPickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

                startActivityForResult(i, Constants.FILE_CODE);
            }
        });
        Button sButton = (Button) findViewById(R.id.exportButton);
        sButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){
                resetFragments();
                Intent i = new Intent(getApplicationContext(), FilePickerActivity.class);

                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

                startActivityForResult(i, Constants.FOLDER_CODE);
            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(item.getItemId()){
            case R.id.menu_item_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
        }
/*        switch(id){
            case R.id.menu_item_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
        }*/
        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        }*/

        return super.onOptionsItemSelected(item);
    }

    public void onTextLinkClick(View textView, String clickedString) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(clickedString));
        startActivity(i);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected void changeSMSPackage(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String myPackageName = getPackageName();
            defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this);
            //Log.d(TAG, "packageName: " + myPackageName);
            //LOG.debug(TAG, "packageName: " + myPackageName);
            LOG.debug("packageName : {}", myPackageName);
            //Log.d(TAG, "default SMS package: " + defaultSmsPackage);
            //LOG.debug(TAG, "default SMS package: " + defaultSmsPackage);
            LOG.debug("default SMS package : {}", defaultSmsPackage);
            if(myPackageName.compareTo(defaultSmsPackage)!=0) {
                Utilities.requestDefaultSmsPackageChange(this);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri uri;
        if (requestCode == Constants.FILE_CODE && resultCode == Activity.RESULT_OK) {
            uri = getUri(data);
            if(uri != null){
                Utilities.processImport(this, uri);
            }
        }
        if (requestCode == Constants.FOLDER_CODE && resultCode == Activity.RESULT_OK) {
            uri = getUri(data);
            if(uri != null){
                Utilities.processExport(this, uri);
            }
        }
    }

    public File getAppDir(){
        return myDir;
    }

    @Override
    protected void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Utilities.restoreDefaultSmsProvider(this, defaultSmsPackage);
        }
        super.onDestroy();
    }

    private void resetFragments(){
        smsFrag.setTask(getResources().getString(R.string.processedElements), getResources().getString(R.string.smses));
        smsFrag.setNumber(getResources().getString(R.string.smses), 0);
        mmsFrag.setTask(getResources().getString(R.string.processedElements), getResources().getString(R.string.mmses));
        mmsFrag.setNumber(getResources().getString(R.string.mmses), 0);
    }

    private void createFolders(){
        myDir = getDir("appDir", MODE_PRIVATE);
        File exportDir = new File(myDir, Constants.exportDir);
        exportDir.mkdir();
        File importDir = new File(myDir, Constants.importDir);
        importDir.mkdir();
    }

    private Uri getUri(Intent data){
        Uri uri = null;
        if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
        // For JellyBean and above
            //Log.d(TAG, "getUri :: innerElse ");
            //LOG.debug(TAG, "getUri :: innerElse ");
            LOG.debug("getUri :: innerElse");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ClipData clip = data.getClipData();

                if (clip != null) {
                    //Log.d(TAG, "getUri :: Version SDK >= JELLY_BEAN :: clip !=null");
                    //LOG.debug(TAG, "getUri :: Version SDK >= JELLY_BEAN :: clip !=null");
                    LOG.debug("getUri :: Version SDK >= JELLY_BEAN :: clip !=null");
                    for (int i = 0; i < clip.getItemCount(); i++) {
                        uri = clip.getItemAt(i).getUri();
                        // Do something with the URI
                    }
                }
                //Log.d(TAG, "getUri :: Version SDK >= JELLY_BEAN, uri : "+uri);
                //LOG.debug(TAG, "getUri :: Version SDK >= JELLY_BEAN, uri : "+uri);
                LOG.debug("getUri :: Version SDK >= JELLY_BEAN, uri : {}", uri);
                // For Ice Cream Sandwich
            } else {
                ArrayList<String> paths = data.getStringArrayListExtra
                        (FilePickerActivity.EXTRA_PATHS);

                if (paths != null) {
                    for (String path: paths) {
                        uri = Uri.parse(path);
                        // Do something with the URI
                    }
                }
                //Log.d(TAG,"getUri :: Version SDK < JELLY_BEAN, uri : "+uri);
                //LOG.debug(TAG,"getUri :: Version SDK < JELLY_BEAN, uri : "+uri);
                LOG.debug("getUri :: Version SDK < JELLY_BEAN, uri : {}", uri);
            }

        } else {
            uri = data.getData();
            // Do something with the URI
            //Log.d(TAG, "getUri :: outer else, uri : "+uri);
            //LOG.debug(TAG, "getUri :: outer else, uri : "+uri);
            LOG.debug("getUri :: outer else, uri : {}", uri);
        }
        return uri;
    }


}
