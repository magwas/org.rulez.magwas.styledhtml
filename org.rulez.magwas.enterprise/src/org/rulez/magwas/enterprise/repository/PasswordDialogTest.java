package org.rulez.magwas.enterprise.repository;

import static org.junit.Assert.*;

import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

public class PasswordDialogTest {

	//@Test
	public void test() {
		System.out.println("result='"+new PasswordDialog(new Shell()).ask()+"'");
/*		Shell shell = new Shell();
		PasswordDialog pd = new PasswordDialog(shell);
		pd.ask()+"'");
		*/
	}

}
