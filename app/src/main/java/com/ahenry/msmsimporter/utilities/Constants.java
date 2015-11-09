package com.ahenry.msmsimporter.utilities;

import android.util.Patterns;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

/**
 * Created by axel on 09/06/15.
 */
public class Constants {

    public static final String appName = "(M|S)MSImporter";
    public static final int FILE_CODE = 0;
    public static final int FOLDER_CODE=1;
    public static final int REQUEST_CHANGE_DEFAULT_SMS_PACKAGE = 3;
    public static final Pattern aPattern = android.util.Patterns.WEB_URL;
    public static final Pattern phonePattern = Patterns.PHONE;
    public static final Pattern mailPattern = Patterns.EMAIL_ADDRESS;
    public static final String importDir = "import";
    public static final String exportDir = "export";
    public static final String mmsBaseUri = "content://mms/";
    public static final String smsBaseUri = "content://sms/";
    public static final String smsDeleteUri = smsBaseUri + "conversations/-1";
    public static final String exportFilename = "export_msmsimporter_%1$s%2$s";
    public static final String tarExtension = ".tar";
    public static final String gzExtension = ".tar.gz";
    public static final SimpleDateFormat dFormat = new SimpleDateFormat("dd.MM.yyyy_HH.mm.ss");
    public static final int CHARSET_UTF8 = 106;
}
