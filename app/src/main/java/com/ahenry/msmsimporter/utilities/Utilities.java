package com.ahenry.msmsimporter.utilities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.text.TextUtils;

import com.ahenry.msmsimporter.MainActivity;
import com.ahenry.msmsimporter.database.ExportTask;
import com.ahenry.msmsimporter.json.MMSParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarInputStream;
import org.kamranzafar.jtar.TarOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
/**
 * Created by axel on 09/06/15.
 */
public class Utilities {

    private static final Uri contentUri = Uri.parse("content://mms-sms/threadID");
    private static final Logger LOG = LoggerFactory.getLogger(Utilities.class);

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void requestDefaultSmsPackageChange(Activity myActivity) {
        final Intent changeIntent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                .putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, myActivity.getPackageName());

        myActivity.startActivityForResult(changeIntent, Constants.REQUEST_CHANGE_DEFAULT_SMS_PACKAGE);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void restoreDefaultSmsProvider(Context myContext, String smsPackage) {
        LOG.debug("restoreDefaultSmsProvider :: restoring SMS provider {}.", smsPackage);
        //Log.d(TAG, "restoring SMS provider " + smsPackage);
        if (!TextUtils.isEmpty(smsPackage)) {
            final Intent changeDefaultIntent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                    .putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, smsPackage);

            myContext.startActivity(changeDefaultIntent);
        }
    }


    static void unTarGzFile(String aDest, String aTarGz){
        TarInputStream tis = null;
        try{
            tis = new TarInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(new File(aTarGz)))));
            unTarToStream(aDest, tis);
        }catch(FileNotFoundException fne){
            LOG.error("unTarGzFile :: FileNotFoundException.\n\t", fne);
        }catch(IOException ioe){
            LOG.error("unTarGzFile :: IOException.\n\t",ioe);
        }finally {
            //close(tis);
            if(tis!=null){
                try{
                    tis.close();
                } catch (IOException ioe) {
                    LOG.error("close :: IOException.\n\t", ioe);
                }
            }
        }

        /*JAVA 1.7 VERSION
        try(TarInputStream tis = new TarInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(new File(aTarGz)))))){
            unTarToStream(aDest, tis);
        }catch(IOException ioe){
            LOG.error("unTarGzFile :: IOException.\n\t",ioe);
        }*/
    }

    static void unTarToStream(String aDest, TarInputStream tis) throws IOException{
        TarEntry entry;
        BufferedOutputStream dest;
        while ((entry = tis.getNextEntry()) != null) {

            if (entry.isDirectory()) {
                new File(aDest + "/" + entry.getName()).mkdirs();
                continue;
            } else {
                int di = entry.getName().lastIndexOf('/');
                if (di != -1) {
                    new File(aDest + "/" + entry.getName().substring(0, di)).mkdirs();
                }
            }

            dest = null;
            try{
                dest = new BufferedOutputStream(new FileOutputStream(aDest + "/" + entry.getName()));
                IOUtils.copy(tis, dest);
                dest.flush();
            }catch(FileNotFoundException fne){
                LOG.error("untarToStream :: FileNotFoundException.\n\t", fne);
            }catch (IOException ioe){
                LOG.error("untarToStream :: IOException.\n\t", ioe);
            }finally{
                //close(dest);
                if(dest != null){
                    try{
                        dest.close();
                    } catch (IOException ioe) {
                        LOG.error("close :: IOException.\n\t", ioe);
                    }
                }
            }

            /*JAVA 1.7 VERSION
            try(BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(aDest + "/" + entry.getName()))){
                IOUtils.copy(tis, dest);
                dest.flush();
            }*/
        }
    }

    static void unTarFile(String aDest, String aTar) {
        TarInputStream tis = null;
        try{
            tis = new TarInputStream(new BufferedInputStream(new FileInputStream(new File(aTar))));
            unTarToStream(aDest, tis);
        }catch(FileNotFoundException fne){
            LOG.error("unTarFile :: FileNotFoundException.\n\t", fne);
        }catch(IOException ioe){
            LOG.error("unTarFile :: IOException.\n\t", ioe);
        }finally{
            //close(tis);
            if(tis!=null){
                try{
                    tis.close();
                } catch (IOException ioe) {
                    LOG.error("close :: IOException.\n\t", ioe);
                }
            }
        }

        /*JAVA 1.7 VERSION
        try(TarInputStream tis = new TarInputStream(new BufferedInputStream(new FileInputStream(new File(aTar))))) {
            unTarToStream(aDest, tis);
        }catch(IOException ioe){
            LOG.error("unTarFile :: IOException.\n\t", ioe);
            //Log.d(TAG, ioe.getMessage());
        }*/
    }

    public static void untar(String aDest, String aTar){
        if(isTarGz(new File(aTar),".tar.gz")){
            unTarGzFile(aDest, aTar);
        }else{
            unTarFile(aDest, aTar);
        }
    }

    /**
     * This is a single-recipient version of
     * getOrCreateThreadId.  It's convenient for use with SMS
     * messages.
     */
    public static long getOrCreateThreadId(Context context, String recipient)
    {
        Set<String> recipients = new HashSet<String>();
        recipients.add(recipient);
        return getOrCreateThreadId(context, recipients);
    }

    /**
     * Given the recipients list and subject of an unsaved message,
     * return its thread ID.  If the message starts a new thread,
     * allocate a new thread ID.  Otherwise, use the appropriate
     * existing thread ID.
     *
     * Find the thread ID of the same set of recipients (in
     * any order, without any additions). If one
     * is found, return it.  Otherwise, return a unique thread ID.
     */
    public static long getOrCreateThreadId(Context context, Set<String> recipients)
    {
        //Uri.Builder uriBuilder = Uri.parse("content://mms-sms/threadID").buildUpon();
        Uri.Builder uriBuilder = contentUri.buildUpon();

        for (String recipient : recipients)
        {
            uriBuilder.appendQueryParameter("recipient", recipient);
        }

        Uri uri = uriBuilder.build();
        Cursor cursor = context.getContentResolver().query(uri, new String[] {"_id"}, null, null, null);
        if (cursor != null)
        {
            try
            {
                if (cursor.moveToFirst())
                {
                    return cursor.getLong(0);
                }
                else
                {
                    LOG.debug("getOrCreateThreadId :: getOrCreateThreadId returned no rows!");
                    //Log.d(TAG, "getOrCreateThreadId returned no rows!");
                }
            }
            finally
            {
                cursor.close();
            }
        }
        return -1;
    }

    public static boolean deleteAllFilesFromFolder(String aFolderPath){
        try {
            FileUtils.cleanDirectory(new File(aFolderPath));
            LOG.debug("deleteAllFilesFromFolder :: directory cleaned successfully.");
            //Log.d(TAG, "directory cleaned successfully");
            return true;
        }catch(IOException ioe){
            LOG.error("deleteAllFilesFromFolder :: directory not cleaned.\n\t", ioe);
            //Log.d(TAG, "directory not cleaned");
        }
        return false;

    }

    public static void processImport(MainActivity aAct, Uri aUri){
        File myDir = new File(aAct.getAppDir(),Constants.importDir);
        Utilities.deleteAllFilesFromFolder(myDir.getPath());
        Utilities.untar(myDir.getPath(), aUri.getPath());
        //Log.d(TAG, "untaring done");
        LOG.debug("processImport :: untaring done.");
        File f = new File(myDir,"mms.json");
        if(f.exists()){
            MMSParser aMMSParser = new MMSParser(f.getPath(), aAct);
            aMMSParser.execute();
        }else{
            LOG.debug("processImport :: mms.json could not be found");
        }

        /*f = new File(myDir, "sms.json");
        if(f.exists()) {
            SMSParser aSMSParser = new SMSParser(new File(myDir, "sms.json").getPath(), aAct);
            aSMSParser.execute();
        }else{
            LOG.debug("processImport :: sms.json could not be found.");
        }*/
    }

    public static void processExport(MainActivity aAct, Uri aUri){
        //Log.d(TAG, "uri " + aUri);
        LOG.debug("processExport :: uri ; {}", aUri);
        ExportTask export = new ExportTask(aAct, aUri);
        export.execute();
    }

    public static void writeJson(String aFilePath, LinkedList<?> aList){
        OutputStream file = null;
        try{
            file = new FileOutputStream(new File(aFilePath));

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(aList);
            //Log.d(TAG, "corProcessedSMSes exported to json : \n" + json);
            file.write(json.getBytes(Charsets.UTF_8));

        }catch(FileNotFoundException fne){
            LOG.error("writeJson :: FileNotFoundException.\n\t", fne);
        }catch(IOException ioe){
            LOG.error("writeJson :: IOException.\n\t", ioe);
        }finally{
            //close(file);
            if(file != null){
                try{
                    file.close();
                } catch (IOException ioe) {
                    LOG.error("close :: IOException.\n\t", ioe);
                }
            }
        }

        /*JAVA 1.7 VERSION
        try(OutputStream file = new FileOutputStream(new File(aFilePath))){
        //try(BufferedWriter file = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(aFilePath)), StandardCharsets.UTF_8)) ) {
        //try(FileOutputStream file = new FileOutputStream(new File(aFilePath))) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(aList);
            //Log.d(TAG, "corProcessedSMSes exported to json : \n" + json);
            file.write(json.getBytes(StandardCharsets.UTF_8));
            //file.write(json);

        }catch(FileNotFoundException fne){
            LOG.error("writeJson :: FileNotFoundException.\n\t", fne);
        }catch(IOException ioe){
            LOG.error("writeJson :: IOException.\n\t", ioe);
        }*/
    }

    public static byte[] getMMSPart(Context context, String _id){
        Uri partUri = Uri.parse(Constants.mmsBaseUri+"part/"+_id);
        //TODO process null return
        byte[] b = null;
        InputStream is = null;
        try{
            is = context.getContentResolver().openInputStream(partUri);
            b = IOUtils.toByteArray(is);
        }catch(FileNotFoundException fne){
            LOG.error("getMMSPart :: FileNotFoundException.\n\t", fne);
        }catch(IOException ioe){
            LOG.error("getMMSPart :: IOException.\n\t", ioe);
        }finally{
            //close(is);
            if(is != null){
                try{
                    is.close();
                } catch (IOException ioe) {
                    LOG.error("close :: IOException.\n\t", ioe);
                }
            }
        }
        return b;

/*        JAVA 1.7 VERSION
        try(InputStream is = context.getContentResolver().openInputStream(partUri)){
            b = IOUtils.toByteArray(is);
        }catch(IOException ioe){
            LOG.error("getMMSPart :: IOException.\n\t", ioe);
        }
        return b;*/
    }

    public static String tarGz(Uri aUri, String parent, String path, boolean debugFiles){
        File f = new File(aUri.getPath(), String.format(Constants.exportFilename, Constants.dFormat.format(new java.util.Date()), Constants.gzExtension));
        TarOutputStream out = null;

        try{
            out = new TarOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(f))));
            if(debugFiles){
                File df = new File(path).getParentFile();
                LOG.debug("tarGz :: dir parent :  {}", df.getPath());
                includeDebugFiles(parent, df, out);
            }
            tarFolder(parent, path, out);
            return f.getName();
        }catch(FileNotFoundException fne){
            LOG.error("tarGz :: FileNotFoundException.\n\t", fne);
        }catch(IOException ioe){
            LOG.error("tarGz :: IOException.\n\t", ioe);
        }finally{
            //close(out);
            if(out != null){
                try{
                    out.close();
                } catch (IOException ioe) {
                    LOG.error("close :: IOException.\n\t", ioe);
                }
            }
        }

        /*JAVA 1.7 VERSION
        try(TarOutputStream out = new TarOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(f))))){
            if(debugFiles){
                File df = new File(path).getParentFile();
                LOG.debug("tarGz :: dir parent :  {}", df.getPath());
                includeDebugFiles(parent, df, out);
            }
            tarFolder(parent, path, out);
            return f.getName();
        }catch(IOException ioe){
            LOG.error("tarGz :: IOException.\n\t", ioe);
        }*/
        return null;
    }

    public static String tar(Uri aUri, String parent, String path, boolean debugFiles){
        File f = new File(aUri.getPath(), String.format(Constants.exportFilename, Constants.dFormat.format(new java.util.Date()), Constants.tarExtension));
        TarOutputStream out = null;

        try{
            out = new TarOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
            if(debugFiles){
                File df = new File(path).getParentFile();
                LOG.debug("tar :: dir parent :  {}", df.getPath());
                includeDebugFiles(parent, df, out);
            }
            tarFolder(parent, path, out);
            return f.getName();
        }catch(FileNotFoundException fne){
            LOG.error("tar :: FileNotFoundException.\n\t", fne);
        }catch(IOException ioe){
            LOG.error("tar :: IOException.\n\t", ioe);
        }finally {
            //close(out);
            if(out != null){
                try{
                    out.close();
                } catch (IOException ioe) {
                    LOG.error("close :: IOException.\n\t", ioe);
                }
            }
        }

/*        try(TarOutputStream out = new TarOutputStream(new BufferedOutputStream(new FileOutputStream(f)))){
            if(debugFiles){
                File df = new File(path).getParentFile();
                LOG.debug("tarGz :: dir parent :  {}", df.getPath());
                includeDebugFiles(parent, df, out);
            }
            tarFolder(parent, path, out);
            return f.getName();
        }catch(IOException ioe){
            LOG.error("tar :: IOException.\n\t", ioe);
        }*/
        return null;
    }

    static void tarFolder(String parent, String path, TarOutputStream out) throws IOException {
        File f = new File(path);
        String files[] = f.list();
        //BufferedInputStream origin = null;
        BufferedInputStream origin;
        // is file
        if (files == null) {
            files = new String[1];
            files[0] = f.getName();
        }

        if(f.getName().compareTo("export")==0){
            //Log.d(TAG, "head folder, won't be included");
            LOG.debug("tarFolder :: head folder, won't be included.");
            parent = "";
        }else {
            parent = ((parent == null) ? (f.isFile()) ? "" : f.getName() + "/" : parent + f.getName() + "/");
        }

        for(String filename : files){
        //for (int i = 0; i < files.length; i++) {
            //System.out.println("Adding: " + files[i]);
            File fe = f;

            if (f.isDirectory()) {
                //fe = new File(f, files[i]);
                fe = new File(f, filename);
            }

            if (fe.isDirectory()) {
                String[] fl = fe.list();
                if (fl != null && fl.length != 0) {
                    tarFolder(parent, fe.getPath(), out);
                } else {
                    //TarEntry entry = new TarEntry(fe, parent + files[i] + "/");
                    TarEntry entry = new TarEntry(fe, parent + filename + "/");
                    out.putNextEntry(entry);
                }
                continue;
            }

/*            FileInputStream fi = new FileInputStream(fe);
            origin = new BufferedInputStream(fi);*/
            //TarEntry entry = new TarEntry(fe, parent + files[i]);
            TarEntry entry = new TarEntry(fe, parent + filename);
            origin = null;
            try{
                origin = new BufferedInputStream(new FileInputStream(fe));
                out.putNextEntry(entry);
                IOUtils.copy(origin, out);
                out.flush();
            }catch(FileNotFoundException fne){
                LOG.error("tarFolder :: FileNotFoundException.\n\t", fne);
            }catch(IOException ioe){
                LOG.error("tarFolder :: IOException.\n\t", ioe);
            }finally {
                //close(origin);
                if(origin != null){
                    try{
                        origin.close();
                    } catch (IOException ioe) {
                        LOG.error("close :: IOException.\n\t", ioe);
                    }
                }
            }

            /*JAVA 1.7 VERSION
            try(BufferedInputStream origin = new BufferedInputStream(new FileInputStream(fe))){
                out.putNextEntry(entry);
                IOUtils.copy(origin, out);
                out.flush();
            }*/

            //origin.close();
        }
    }

    static void includeDebugFiles(String parent, File dir, TarOutputStream out){
        File workingDir = new File(dir, "files");
        try {
            tarFolder(parent, workingDir.getPath(), out);
        }catch(IOException ioe){
            LOG.error("includeDebugFiles :: IOException.\n\t",ioe);
        }
    }

    public static boolean isMMSAddressValid(String aAddr){
        return isPhoneNumberValid(aAddr) || Constants.mailPattern.matcher(aAddr).matches();
    }

    public static boolean isPhoneNumberValid(String aAddr){
        return Constants.phonePattern.matcher(aAddr).matches();
    }

    public static List<String> getValidatedSenders(List<String> aList){
        LinkedList<String> l = new LinkedList<String>();
        for(String s : aList){
            if(isMMSAddressValid(s)){
                l.add(s);
            }
        }
        LOG.debug("getValidatedSenders :: {} validated senders out of {}.", l.size(), aList.size());
        return l;
    }

    public static boolean isTarGz(File f, String s){
        String path = f.getPath();
        boolean b = path.endsWith(s);
        LOG.debug("isTarGz :: path : {}\npath.endsWith({}) : {}", path, s, b);
        return b;
        //return false;
    }

    /**
     * Used ONLY to convert mms subject before inserting in mmssms.db.
     * Avoid using java.nio.charset to have a better android support, ie android 2.3.x
     * @param s The string to convert
     * @return The string converted to UTF8
     */
    public static String internalToUTF8(String s){
        //return new String(s.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        //byte[] b = StringUtils.getBytesUtf8(s);
        //String tmp =  StringUtils.newStringIso8859_1(b);
        //String tmp = new String(b);
        //LOG.debug("internalToUTF8 :: new String ; {}",tmp);
        //return tmp;
        return new String(s.getBytes(Charsets.UTF_8), Charsets.ISO_8859_1);

    }

    /**
     * Used ONLY to convert mms subject after retrieving from mmssms.db.
     * Avoid using java.nio.charset to have a better android support, ie android 2.3.x
     * @param s The string to convert.
     * @return The string converted from utf8 to internal
     */
    public static String utf8ToInternal(String s){
        return new String (s.getBytes(Charsets.ISO_8859_1), Charsets.UTF_8);
    }

    public static void close(Closeable c){
        if(c!=null) {
            try {
                c.close();
            } catch (IOException ioe) {
                LOG.error("close :: IOException.\n\t", ioe);
            }
        }
    }

    public static int getElementsNumber(Context aContext, String location){
        Cursor c = null;
        int count = 0;
        try {
            c = aContext.getContentResolver().query(Uri.parse(location), null, null, null, null);
            count = c.getCount();
        }catch(NullPointerException npe){
            LOG.error("getElementsNumber :: NullPointerException", npe);
        }finally {
            //close(c);
            if(c != null){
                c.close();
            }
        }

        return count;
    }

}
