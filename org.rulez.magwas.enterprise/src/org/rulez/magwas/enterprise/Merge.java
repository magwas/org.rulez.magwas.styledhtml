package org.rulez.magwas.enterprise;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.rulez.magwas.styledhtml.EventLog;

import uk.ac.bolton.archimate.model.IArchimateElement;

public class Merge implements IEditorActionDelegate, IViewActionDelegate {

	List<EObject> selectedObjs = new ArrayList<EObject>();
	EventLog log;
	
	@Override
	public void run(IAction action) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		IStructuredSelection selected = (IStructuredSelection) selection;
		@SuppressWarnings("rawtypes")
		Iterator itr = selected.iterator();
		selectedObjs.clear();
		while (itr.hasNext()) {
			Object ob = itr.next();
			if ( ob instanceof IArchimateElement) {
				selectedObjs.add((EObject) ob);
			}
		}
		System.out.println("enabled="+action.isEnabled());
		if(selectedObjs.size() == 2){
			action.setEnabled(true);
		} else {
			action.setEnabled(false);
		}
		System.out.println("enabled2="+action.isEnabled());

	}

	@Override
	public void init(IViewPart view) {
		System.out.println("initialising merge");
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {

		
	}

}
