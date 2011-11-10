package org.rulez.magwas.enterprise.repository;

//shamelessly stolen from http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/UsernamePasswordDialog.htm
/*
SWT/JFace in Action
GUI Design with Eclipse 3.0
Matthew Scarpino, Stephen Holder, Stanford Ng, and Laurent Mihalkovic

ISBN: 1932394273

Publisher: Manning
*/


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PasswordDialog extends Dialog {
  private static final int RESET_ID = IDialogConstants.NO_TO_ALL_ID + 1;

  //private Text usernameField;

  private Text passwordField;
  private String password = null;

  public PasswordDialog(Shell parentShell) {
    super(parentShell);
  }

  protected Control createDialogArea(Composite parent) {
    Composite comp = (Composite) super.createDialogArea(parent);

    GridLayout layout = (GridLayout) comp.getLayout();
    layout.numColumns = 2;

    //Label usernameLabel = new Label(comp, SWT.RIGHT);
    //usernameLabel.setText("Username: ");

    //usernameField = new Text(comp, SWT.SINGLE);
    //GridData data = new GridData(GridData.FILL_HORIZONTAL);
    //usernameField.setLayoutData(data);

    Label passwordLabel = new Label(comp, SWT.RIGHT);
    passwordLabel.setText("Password: ");

    passwordField = new Text(comp, SWT.SINGLE | SWT.PASSWORD);
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    passwordField.setLayoutData(data);

    return comp;
  }

  protected void createButtonsForButtonBar(Composite parent) {
    super.createButtonsForButtonBar(parent);
    createButton(parent, RESET_ID, "Reset", false);
  }

  protected void buttonPressed(int buttonId) {
    if (buttonId == RESET_ID) {
      passwordField.setText("");
    } else {
        password=passwordField.getText();
        super.buttonPressed(buttonId);
    }
  }
  
public String ask() {
	  int returnCode;
	  returnCode = open();
	  if(returnCode != OK){
		  return null;
	  }
	  return password;
	}
}
