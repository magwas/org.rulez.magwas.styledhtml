package org.rulez.magwas.enterprise;


import java.io.File;
import java.io.IOException;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import uk.ac.bolton.archimate.editor.model.IEditorModelManager;
import uk.ac.bolton.archimate.model.IArchimateModel;
import uk.ac.bolton.archimate.model.IArchimateModelElement;
import uk.ac.bolton.archimate.model.IFolder;
import uk.ac.bolton.archimate.model.util.ArchimateResourceFactory;
import org.rulez.magwas.enterprise.MoveAround;
import org.rulez.magwas.styledhtml.EventLog;

public class ImportPart implements IEditorActionDelegate, IViewActionDelegate {
	

	private IViewPart view;
	EObject selectedObj;
	EventLog log;

	@Override
	public void run(IAction action) {
		log = new EventLog("Import Part");

		if ( ! (selectedObj instanceof IArchimateModelElement)) {
			return;
		}
		IArchimateModel currmodel = ((IArchimateModelElement) selectedObj).getArchimateModel();
		File file = askOpenFile();
		if(file == null) {
            return;
		}
		Resource resource = ArchimateResourceFactory.createResource(file);
        try {
			resource.load(null);
		} catch (IOException e) {
			log.printStackTrace(e);
			e.printStackTrace();
		}
        //IArchimateModel model = (IArchimateModel)resource.getContents().get(0);
        IArchimateModel model = IEditorModelManager.INSTANCE.openModel(file);
        model.setId(currmodel.getId()+"_");
        copyModel(model,currmodel);
        try {
			IEditorModelManager.INSTANCE.closeModel(model);
		} catch (IOException e) {
			log.printStackTrace(e);
			e.printStackTrace();
		}
    }

	

	private void copyModel(IArchimateModel sourcemodel, IArchimateModel targetmodel) {
		/* walk the tree of the sourcemodel, and move each element to targetmodel*/
		EList<IFolder> list = sourcemodel.getFolders();
		for (IFolder i : list) {
			MoveAround ma = new MoveAround(log,targetmodel);
			ma.moveElement(i, targetmodel);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		IStructuredSelection selected = (IStructuredSelection) selection;
		selectedObj = (EObject) selected.getFirstElement();
		if(selectedObj instanceof IArchimateModel){
			action.setEnabled(true);
		} else {
			action.setEnabled(false);
		}
		
	}

	@Override
	public void init(IViewPart viewpart) {
		view = viewpart;
		
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		
		
	}
	private File askOpenFile() {
        FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
        dialog.setFilterExtensions(new String[] { "*.archimate", "*.*" } );
        String path = dialog.open();
        return path != null ? new File(path) : null;
    }
}
