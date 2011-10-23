package org.rulez.magwas.enterprise;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.rulez.magwas.styledhtml.EventLog;

import uk.ac.bolton.archimate.model.IArchimateModel;
import uk.ac.bolton.archimate.model.IArchimateModelElement;
import uk.ac.bolton.archimate.model.IFolder;
import uk.ac.bolton.archimate.model.IFolderContainer;
import uk.ac.bolton.archimate.model.IIdentifier;
import uk.ac.bolton.archimate.model.util.ArchimateModelUtils;

public class MoveAround {
	private EventLog log;
	private IArchimateModel targetmodel;

	public MoveAround(EventLog elog,IArchimateModel model) {
		log = elog;
		targetmodel = model;
	}
	public void moveElement(EObject e,EObject targetparent) {
		String id = ((IIdentifier) e).getId();
		IArchimateModel sourcemodel = ((IArchimateModelElement) e).getArchimateModel();
		log.issueInfo(sourcemodel,e,"source", e.toString()+sourcemodel);
		EObject targetobj = ArchimateModelUtils.getObjectByID(targetmodel,id);
		IArchimateModel tmodel = null;
		if ( null != targetobj ) {
			tmodel = ((IArchimateModelElement) targetobj).getArchimateModel();
		}
		log.issueInfo(tmodel,targetobj, "target", ""+targetobj);
		// we make the source object shallow here to be able to control insertion
		List<EObject> elements = new ArrayList<EObject>();
		List<IFolder> childfolders = new ArrayList<IFolder>();
		if ( e instanceof IFolder) {
			IFolder f = (IFolder)e;
			EList<EObject> elements0 = f.getElements();
			for (EObject i : elements0 ) {
				elements.add(i);
			}
			elements0.clear();
		}
		if ( e instanceof IFolderContainer) {
			IFolderContainer f = (IFolderContainer)e;
			EList<IFolder> childfolders0 = f.getFolders();
			for (IFolder i : childfolders0 ) {
				childfolders.add(i);
			}
			childfolders0.clear();
		}
		
		if (null == targetobj) {
			// this object does not exist in the target
			if (e instanceof IFolder) {
				((IFolderContainer) targetparent).getFolders().add((IFolder) e);
			} else {
				((IFolder) targetparent).getElements().add(e);
			} 
			targetobj=e;
		} else {
			diffElements(e,targetobj);
		}
		log.issueInfo(tmodel,targetobj,"moving depth for",""+targetobj);
		if (targetobj instanceof IFolder) {
			for (EObject i : elements) {
				log.issueInfo("moving element", ""+i);
				moveElement(i,targetobj);
			}
		}
		if (targetobj instanceof IFolderContainer) {
			for (EObject i : childfolders) {
				log.issueInfo("moving folder", ""+i);
				moveElement(i,targetobj);				
			}
		}			
	}

	private void diffElements(EObject e, EObject targetobj) {
		diffTreeposition(e,targetobj);
		diffDocumentation(e,targetobj);
		diffName(e,targetobj);
		diffProperties(e,targetobj);
		
	}
	private void diffProperties(EObject e, EObject targetobj) {
		// TODO Auto-generated method stub
	}
	private void diffName(EObject e, EObject targetobj) {
		// TODO Auto-generated method stub
		
	}
	private void diffDocumentation(EObject e, EObject targetobj) {
		// TODO Auto-generated method stub
		
	}
	private void diffTreeposition(EObject e, EObject targetobj) {
		// TODO Auto-generated method stub
		
	}
}
