package com.ahenry.msmsimporter.filepicker;

import android.support.annotation.NonNull;

import com.ahenry.msmsimporter.utilities.Utilities;
import com.nononsenseapps.filepicker.FilePickerFragment;

import java.io.File;

/**
 * Created by axel on 11/06/15.
 */
public class TarPickerFragment extends FilePickerFragment {

    private static final String EXTENSION = ".tar";
    private static final String EXTENSIONGZ = ".gz";

    /**
     *
     * @param file The file to test.
     * @return The file extension. If file has no extension, it returns null.
     */
    private String getExtension(@NonNull File file) {
        String path = file.getPath();
        int i = path.lastIndexOf(".");
        if (i < 0) {
            return null;
        } else {
            return path.substring(i);
        }
    }

    @Override
    protected boolean isItemVisible(final File file) {
        return isDir(file) || Utilities.isTarGz(file, EXTENSION+EXTENSIONGZ) || EXTENSION.equalsIgnoreCase(getExtension(file));
    }
}
