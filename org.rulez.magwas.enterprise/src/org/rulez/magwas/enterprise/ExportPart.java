package org.rulez.magwas.enterprise;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EContentsEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.rulez.magwas.styledhtml.IPreferenceConstants;
import org.rulez.magwas.styledhtml.Widgets;

import uk.ac.bolton.archimate.model.IArchimateFactory;
import uk.ac.bolton.archimate.model.IArchimateModel;
import uk.ac.bolton.archimate.model.IArchimateModelElement;
import uk.ac.bolton.archimate.model.IDiagramModel;
import uk.ac.bolton.archimate.model.IFolder;
import uk.ac.bolton.archimate.model.IFolderContainer;
import uk.ac.bolton.archimate.model.IIdentifier;
import uk.ac.bolton.archimate.model.INameable;
import uk.ac.bolton.archimate.model.impl.ArchimateModel;
import uk.ac.bolton.archimate.model.util.ArchimateModelUtils;
import uk.ac.bolton.archimate.model.util.ArchimateResource;
import uk.ac.bolton.archimate.model.util.ArchimateResourceFactory;


public class ExportPart implements IEditorActionDelegate, IViewActionDelegate {



	private IViewPart view;
	EObject selectedObj;

	
	private EObject copyEobj(EObject that, IArchimateModel model, boolean withchildren) {
		//System.out.println("copyEObj("+((INameable)that).getName()+")");
		EObject newObject = ArchimateModelUtils.getObjectByID(model, ((IIdentifier)that).getId());
		if(null != newObject) {
			return newObject;
		}
        newObject = EcoreUtil.copy(that);
        if(!withchildren) {
    		if(newObject instanceof IFolderContainer) {
    			((IFolderContainer) newObject).getFolders().clear();
    		}
    		if(newObject instanceof IFolder) {
    			((IFolder) newObject).getElements().clear();
    		}
        }
		System.out.println("created "+((INameable)newObject).getName()+":"+newObject.eClass().getName()+" pclass:"+that.eClass().getName());
        return newObject;
	}
	
	private EObject addwithparents(EObject newob, EObject oldob, IArchimateModel model){
		if (null == newob ) {
			// we are above the originally inserted element. The new object is a copy of oldob
			newob = copyEobj(oldob, model, false);
		}
		//System.out.println("addwithparents("+((INameable)newob).getName()+","+((INameable)oldob).getName()+")");
		IFolder oldcontainer = (IFolder) oldob.eContainer();

		if (oldcontainer.eContainer() instanceof ArchimateModel) {
			// we are below the top level folder level
			// replace the corresponding top level folder of model with a copy of the container of oldob
			// and return it
			// FIXME: make sure we cannot export the top level folder or above
			//System.out.println("oldcontainer=" + ((INameable)oldcontainer).getName());
			EList<IFolder> fl = model.getFolders();
			for (IFolder folder : fl) {
				if ((folder.getType().equals(oldcontainer.getType())) &&
						(folder.getName().equals(oldcontainer.getName()))	 ){
					//IFolder newfolder = (IFolder) replaceObjWithCopy(folder, oldcontainer);
					if(newob instanceof IFolder) {
						folder.getFolders().add((IFolder) newob);
					}else {
						folder.getElements().add(newob);
					}
					return newob;
				}
			}
			System.out.println("this should not happen: missed top-level folder");
			return null;
		}
		
		IFolder newcontainer = (IFolder) addwithparents(null, oldcontainer, model);
		System.out.println("newcontainer=" + ((INameable)newcontainer).getName());
		System.out.println("newobj="+((INameable)newob).getName()+")");
		
		if(newob instanceof IFolder) {
			((IFolder)newcontainer).getFolders().add((IFolder) newob);
		}else {
			((IFolder)newcontainer).getElements().add(newob);
		}
		return newob;
	}

	private void copyDependencies(EObject newob, IArchimateModel model) {
		//should walk the children of selectedObj, create the copy of all of the dependencies mentioned,
		//and bind the copy on newobj instead of a reference to the old one
		// then copy dependencies for the copy as well
		//System.out.println("copyDependecies("+newob+")");
		for (EContentsEList.FeatureIterator<EObject> featureIterator = 
		        (EContentsEList.FeatureIterator<EObject>)newob.eCrossReferences().iterator();
		       featureIterator.hasNext(); )
		{
		    EObject refd = featureIterator.next();
		    EReference eReference = (EReference)featureIterator.feature();
			//System.out.println(" refd="+((INameable)refd).getName());
			//System.out.println(" ereference="+eReference);
			EObject modelobj = ArchimateModelUtils.getObjectByID(model, ((IIdentifier)refd).getId());
			if (null == modelobj ) {
				modelobj = copyEobj(refd,model, false);
				addwithparents(modelobj,refd,model);
				copyDependencies(modelobj,model);
			}
			if(eReference.isChangeable()) {
				//System.out.println(" Modelobj="+ modelobj);
				//System.out.println(" Ereference="+eReference);
				Object c = newob.eGet(eReference);
				if (c instanceof Collection) {
					@SuppressWarnings("unchecked")
					Collection<EObject> coll = (Collection<EObject>) c;
					//System.out.println(" Refd="+ refd);
					//System.out.println(" collection="+coll);
					coll.remove(refd);
					coll.add(modelobj);
					//System.out.println(" Collection="+coll);
				} else {
					newob.eSet(eReference, modelobj);
				}
			} else {
				//System.out.println("didn't set");
			}
		}
		EList<EObject> contents = newob.eContents();
		for (EObject  obj : contents) {
			copyDependencies(obj, model);
		}
	}
	

	public void run(IAction action) {
		Shell shell = view.getSite().getShell();
		
		File target = Widgets.askSaveFile(IPreferenceConstants.LAST_RICH_PATH, new String[] { "*.archimate" } );
    	if(null == target) {
    		return;
    	}
      	ArchimateResource resource = (ArchimateResource) ArchimateResourceFactory.createResource(target);
        IArchimateModel model = IArchimateFactory.eINSTANCE.createArchimateModel();
        model.setDefaults();
        EObject newob = copyEobj(selectedObj,model, true);
        model.setName(((INameable)selectedObj).getName()+" from "+ ((IArchimateModelElement)selectedObj).getArchimateModel().getName());
       
        addwithparents(newob,selectedObj,model);
        copyDependencies(newob,model);
    	resource.getContents().add(model);
    	OutputStream os;
		try {
			os = new FileOutputStream(target);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
    	try {
			resource.save(os, resource.getDefaultSaveOptions());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		//System.out.println("savethis "+ selectedObj);
	}

	
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		IStructuredSelection selected = (IStructuredSelection) selection;
		selectedObj = (EObject) selected.getFirstElement();
		if(((selectedObj instanceof IFolder) ||
				(selectedObj instanceof IDiagramModel))&&
				(!(selectedObj.eContainer() instanceof IArchimateModel))
		 ){
			action.setEnabled(true);
		} else {
			action.setEnabled(false);
		}
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {

	}


	@Override
	public void init(IViewPart viewpart) {
		view = viewpart;
		System.out.println("initialising with viewpart "+ view);
	}

}
