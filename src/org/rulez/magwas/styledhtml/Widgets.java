package org.rulez.magwas.styledhtml;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import uk.ac.bolton.archimate.editor.preferences.Preferences;

public class Widgets {

	/**
     * Ask user for file name to save to
     */
    static public File askSaveFile(String prefname, String[] extensions) {
    	Shell myshell = Display.getCurrent().getActiveShell();
        FileDialog dialog = new FileDialog(myshell, SWT.SAVE);
        dialog.setText("Export Model");
    	String lastpath = Preferences.STORE.getString(prefname);
    	dialog.setFileName(lastpath);
    	if(null != extensions) {
            dialog.setFilterExtensions(extensions);
    	}
        String path = dialog.open();
        if(path == null) {
            return null;
        }
        Preferences.STORE.setValue(prefname, path);        
        File file = new File(path);
        
        // Make sure the file does not already exist
        if(file.exists()) {
            boolean result = MessageDialog.openQuestion(myshell, "Export Model",
                    "'" + file +
                    "' already exists. Are you sure you want to overwrite it?");
            if(!result) {
                return null;
            }
        }
        
        return file;
    }
    
    public static void tellProblem(String title,String message) {
    	Shell myshell = Display.getCurrent().getActiveShell();
    	MessageDialog.openInformation(myshell, title,message);
    }

}
