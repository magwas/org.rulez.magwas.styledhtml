package org.rulez.magwas.enterprise.repository;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

class ConnPrefDialog extends Dialog {
	ConnPref cp;
	String oldname;
	
	protected ConnPrefDialog(Shell parentShell,ConnPref cp) {
		super(parentShell);
		this.cp = cp;
		oldname = cp.getName();
	}

	public int open() {
		Shell parent = getParentShell();
		final Shell shell = new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
		shell.setText("Connection Editor");
	    shell.setLayout(new GridLayout(2, true));

	    Label nameLabel = new Label(shell, SWT.NULL);
	    nameLabel.setText("Name:");
	    final Text nameText = new Text(shell, SWT.SINGLE | SWT.BORDER);
	    nameText.setText(cp.getNameAsString());

	    Label urlLabel = new Label(shell, SWT.NULL);
	    urlLabel.setText("Url:");
	    final Text urlText = new Text(shell, SWT.SINGLE | SWT.BORDER);
	    urlText.setText(cp.getUrl());

	    Label usernameLabel = new Label(shell, SWT.NULL);
	    usernameLabel.setText("Username:");
	    final Text usernameText = new Text(shell, SWT.SINGLE | SWT.BORDER);
	    usernameText.setText(cp.getUsername());

	    Label roleLabel = new Label(shell, SWT.NULL);
	    roleLabel.setText("Role:");
	    final Text roleText = new Text(shell, SWT.SINGLE | SWT.BORDER);
	    roleText.setText(cp.getRole());

	    Label askpassLabel = new Label(shell, SWT.NULL);
	    askpassLabel.setText("Ask for password?");
	    final Button askpassButton = new Button(shell, SWT.CHECK);
	    askpassButton.setSelection(cp.getAskpass());
	    
	    Label passwordLabel = new Label(shell, SWT.NULL);
	    passwordLabel.setText("password:");
	    final Text passwordText = new Text(shell, SWT.SINGLE | SWT.BORDER);
	    passwordText.setText(cp.getPassword());

	    Label postgressslLabel = new Label(shell, SWT.NULL);
	    postgressslLabel.setText("Use SSL with Postgresql?");
	    final Button postgressslButton = new Button(shell, SWT.CHECK);
	    postgressslButton.setSelection(cp.getPostgresssl());

	    Label keystoreLabel = new Label(shell, SWT.NULL);
	    keystoreLabel.setText("keystore:");
	    final Text keystoreText = new Text(shell, SWT.SINGLE | SWT.BORDER);
	    keystoreText.setText(cp.getKeystore());

	    final Button buttonOK = new Button(shell, SWT.PUSH);
	    buttonOK.setText("Ok");
	    buttonOK.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
	    Button buttonCancel = new Button(shell, SWT.PUSH);
	    buttonCancel.setText("Cancel");

        buttonOK.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	cp.setAskpass(askpassButton.getSelection());
            	cp.setPostgresssl(postgressslButton.getSelection());
            	cp.setName(nameText.getText());
            	cp.setUrl(urlText.getText());
            	cp.setUsername(usernameText.getText());
            	cp.setRole(roleText.getText());
            	cp.setPassword(passwordText.getText());
            	cp.setKeystore(keystoreText.getText());
            	if (!cp.checknames(oldname)) {
            		MessageDialog.openError(shell, "Use another name", "The name of the connections should be unique, not empty, and without ':'");
            		return;
            	}
            	cp.edit(oldname);
            	shell.dispose();
            }
        });
        buttonCancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	shell.dispose();
            }
        });
	    shell.pack();
	    shell.open();
	    
	    System.out.println("open exited");
		return 0;
		
	}
	
}