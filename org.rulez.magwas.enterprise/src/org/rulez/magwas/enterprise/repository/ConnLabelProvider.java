package org.rulez.magwas.enterprise.repository;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class ConnLabelProvider extends LabelProvider implements
		ITableLabelProvider {


	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		String result = ""; //$NON-NLS-1$
		ConnPref cp =  (ConnPref) element;
		switch (columnIndex) {
		case 0:
			result = cp.getName();
			break;
		case 1:
			result = cp.getUrl();
			break;
		case 2:
			result = cp.getUsername();
			break;
		default:
			break;
		}
		return result;
	}

}