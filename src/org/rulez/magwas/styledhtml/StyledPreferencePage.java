/*******************************************************************************
 * Copyright (c) 2010 Bolton University, UK.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 *******************************************************************************/
package org.rulez.magwas.styledhtml;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import uk.ac.bolton.archimate.editor.preferences.Preferences;

/**
 * General Preferences Page
 * 
 * @author Phillip Beauvoir
 */
public class StyledPreferencePage extends PreferencePage
implements IWorkbenchPreferencePage, IPreferenceConstants {
    public static String HELPID = "org.rulez.magwas.styledhtml.StyledEditor"; //$NON-NLS-1$
    
    private Button fStyleDirButton;
    private Label fStyleDirLabel;
    private CLabel fStyleDirPathLabel;
    private File styleDir = null;
    
    private Button fOutDirButton;
    private Label fOutDirLabel;
    private Button fOutDirCheckBox;
    private Label fOutDirCBLabel;
    private CLabel fOutDirPathLabel;
    private File outDir = null;
    
    
	public StyledPreferencePage() {
		setPreferenceStore(Preferences.STORE);
	}
	
    @Override
    protected Control createContents(Composite parent) {
    	getPreferenceStore().setDefault(OUT_ASK, true);
        // Help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, HELPID);

        Composite client = new Composite(parent, SWT.NULL);
        client.setLayout(new GridLayout());
        
        GridData gd;
        
        Group styleGroup = new Group(client, SWT.NULL);
        styleGroup.setText("Style Dir");
        styleGroup.setLayout(new GridLayout(2, false));
        styleGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fStyleDirLabel =  new Label(styleGroup, SWT.NULL);
        fStyleDirLabel.setText("Stylesheet Location:");
        fStyleDirPathLabel = new CLabel(styleGroup, SWT.BORDER);
        gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        gd.widthHint = 400;
        fStyleDirPathLabel.setLayoutData(gd);
        
        fStyleDirButton = new Button(styleGroup,SWT.PUSH);
        fStyleDirButton.setText("modify");
        fStyleDirButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	styleDir = askTemplateDir();
            	if(null != styleDir) {
            		fStyleDirPathLabel.setText(styleDir.getAbsolutePath());
            	}
            }
        });

        Group outGroup = new Group(client, SWT.NULL);
        outGroup.setText("Output Dir");
        outGroup.setLayout(new GridLayout(2, false));
        outGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fOutDirCBLabel = new Label(outGroup, SWT.NULL);
        fOutDirCBLabel.setText("Always ask for location");
        fOutDirCheckBox = new Button(outGroup,SWT.CHECK);
        fOutDirLabel = new Label(outGroup, SWT.NULL);
        fOutDirLabel.setText("Use this location:");
        fOutDirPathLabel = new CLabel(outGroup, SWT.BORDER);
        gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        gd.widthHint = 400;
        fOutDirPathLabel.setLayoutData(gd);
        
        fOutDirButton = new Button(outGroup,SWT.PUSH);
        fOutDirButton.setText("modify");
        fOutDirButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	outDir = askSaveFile();
            	if(null != outDir) {
            		fOutDirPathLabel.setText(outDir.getAbsolutePath());
            	}
            }
        });
            
        setValues();
        
        return client;
    }

    private void setValues() {
    	String path = getPreferenceStore().getString(STYLE_PATH);
    	styleDir = new File(path);
        fStyleDirPathLabel.setText(styleDir.getAbsolutePath());
        
        Boolean ask = getPreferenceStore().getBoolean(OUT_ASK);
        fOutDirCheckBox.setSelection(ask);
        path = getPreferenceStore().getString(OUT_PATH);
        outDir = new File(path);
        fOutDirPathLabel.setText(outDir.getAbsolutePath());
    }
    
    
    @Override
    public boolean performOk() {
    	Shell myshell = Display.getCurrent().getActiveShell();
    	if ((null != styleDir) && (null != checkStyleSheet(myshell,styleDir))) {
    		getPreferenceStore().setValue(STYLE_PATH, styleDir.getAbsolutePath());
    	}
        getPreferenceStore().setValue(OUT_ASK, fOutDirCheckBox.getSelection());
        getPreferenceStore().setValue(OUT_PATH, outDir.getAbsolutePath());
        return true;
    }
    
    @Override
    protected void performDefaults() {
    	String path = getPreferenceStore().getDefaultString(STYLE_PATH);
    	styleDir = new File(path);
    	fStyleDirPathLabel.setText(path);
    	path = getPreferenceStore().getDefaultString(OUT_PATH);
    	outDir = new File(path);
    	fOutDirPathLabel.setText(path);
    	fOutDirCheckBox.setSelection(getPreferenceStore().getDefaultBoolean(OUT_ASK));
        super.performDefaults();
    }

    public void init(IWorkbench workbench) {
    }

    /**
     * Ask user for template directory to use
     */
    private File askTemplateDir() {
    	//actually we look at style.xslt
    	Shell myshell = Display.getCurrent().getActiveShell();
        FileDialog dialog = new FileDialog(myshell, SWT.OPEN);
        if(null != styleDir) {
        	String opath = styleDir.getAbsolutePath();
            dialog.setFileName(opath);
        }
        dialog.setText("Template Directory");
        String path = dialog.open();
        
        if(path == null) {
            return null;
        }    
        
        File file = new File(path);
        return checkStyleSheet(myshell, file);

    }
    
    private File checkStyleSheet(Shell myshell, File file) {
    	if (null == StyledHtml.mkTransformer(file)){
    		MessageDialog.openError(myshell, "Incorrect stylesheet", "This stylesheet is unuseable.");
    		return null;
    	}
    	return file;
    	
    }
    /**
     * Ask user for file name to save to
     */
    static public File askSaveFile() {
    	Shell myshell = Display.getCurrent().getActiveShell();
        FileDialog dialog = new FileDialog(myshell, SWT.SAVE);
        dialog.setText("Export Model");
        dialog.setFilterExtensions(new String[] { "*.*" } );
        String path = dialog.open();
        if(path == null) {
            return null;
        }
                
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
    
}
