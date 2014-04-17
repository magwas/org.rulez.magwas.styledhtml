package org.rulez.magwas.enterprise;

import java.util.HashMap;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import com.archimatetool.editor.views.tree.TreeModelView;
import com.archimatetool.model.IArchimateElement;
import com.archimatetool.model.IArchimatePackage;
import com.archimatetool.model.impl.ArchimateElement;


public class ChangeType implements IEditorActionDelegate, IViewActionDelegate {

	private EObject selectedObj;
	private HashMap<String,EClass> elementmap = new HashMap<String,EClass>();
	private HashMap<String,EClass> relationmap = new HashMap<String,EClass>();
	private IViewPart view;
	
	@Override
	public void run(IAction action) {
		// - get the desired type
		// - check whether the type transition is valid (object->object okay, but object -> relation is not)
		// - check object - relation compatibility
		// - eSetClass
		// if any of the checks fail, inform the user, and do not change type
		ArchimateElement e = (ArchimateElement) selectedObj;
		//e.eSetClass(IArchimatePackage.Literals.APPLICATION_COMPONENT);
		
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		IStructuredSelection selected = (IStructuredSelection) selection;
		selectedObj = (EObject) selected.getFirstElement();
		if(selectedObj instanceof IArchimateElement){
			action.setEnabled(true);
		} else {
			action.setEnabled(false);
		}
		
	}

	@Override
	public void init(IViewPart viewpart) {
		view = (TreeModelView) viewpart;

		System.out.println("initialising ChangeType with "+view);
		elementmap.put("Actor", IArchimatePackage.Literals.BUSINESS_ACTOR);
		elementmap.put("Role", IArchimatePackage.Literals.BUSINESS_ROLE);
		elementmap.put("Collaboration", IArchimatePackage.Literals.BUSINESS_COLLABORATION);
		elementmap.put("Business Interrface", IArchimatePackage.Literals.BUSINESS_INTERFACE);
		elementmap.put("Business Function", IArchimatePackage.Literals.BUSINESS_FUNCTION);
		elementmap.put("Business Process", IArchimatePackage.Literals.BUSINESS_PROCESS);
		elementmap.put("Business Activity", IArchimatePackage.Literals.BUSINESS_ACTIVITY);
		elementmap.put("Business Event", IArchimatePackage.Literals.BUSINESS_EVENT);
		elementmap.put("Business Interaction", IArchimatePackage.Literals.BUSINESS_INTERACTION);
		elementmap.put("Product", IArchimatePackage.Literals.PRODUCT);
		elementmap.put("Contract", IArchimatePackage.Literals.CONTRACT);
		elementmap.put("Business Service", IArchimatePackage.Literals.BUSINESS_SERVICE);
		elementmap.put("Value", IArchimatePackage.Literals.VALUE);
		elementmap.put("Meaning", IArchimatePackage.Literals.MEANING);
		elementmap.put("Representation", IArchimatePackage.Literals.REPRESENTATION);
		elementmap.put("Business Object", IArchimatePackage.Literals.BUSINESS_OBJECT);
		elementmap.put("Application Component", IArchimatePackage.Literals.APPLICATION_COMPONENT);
		elementmap.put("Application Collaboration", IArchimatePackage.Literals.APPLICATION_COLLABORATION);
		elementmap.put("Application Interface", IArchimatePackage.Literals.APPLICATION_INTERFACE);
		elementmap.put("Application Service", IArchimatePackage.Literals.APPLICATION_SERVICE);
		elementmap.put("Application Function", IArchimatePackage.Literals.APPLICATION_FUNCTION);
		elementmap.put("Application Interaction", IArchimatePackage.Literals.APPLICATION_INTERACTION);
		elementmap.put("Data Object", IArchimatePackage.Literals.DATA_OBJECT);
		elementmap.put("Artifact", IArchimatePackage.Literals.ARTIFACT);
		elementmap.put("Communication Path", IArchimatePackage.Literals.ARTIFACT);
		elementmap.put("Network", IArchimatePackage.Literals.ARTIFACT);
		elementmap.put("Infrastructure interface", IArchimatePackage.Literals.ARTIFACT);
		elementmap.put("Infrastructure Service", IArchimatePackage.Literals.ARTIFACT);
		elementmap.put("Node", IArchimatePackage.Literals.ARTIFACT);
		elementmap.put("System Software", IArchimatePackage.Literals.ARTIFACT);
		elementmap.put("Device", IArchimatePackage.Literals.ARTIFACT);
		elementmap.put("Artifact", IArchimatePackage.Literals.ARTIFACT);
		relationmap.put("Specialisation relation", IArchimatePackage.Literals.SPECIALISATION_RELATIONSHIP);
		relationmap.put("Composition relation", IArchimatePackage.Literals.COMPOSITION_RELATIONSHIP);
		relationmap.put("Aggregation relation", IArchimatePackage.Literals.AGGREGATION_RELATIONSHIP);
		relationmap.put("Assignment relation", IArchimatePackage.Literals.ASSIGNMENT_RELATIONSHIP);
		relationmap.put("Realisation relation", IArchimatePackage.Literals.REALISATION_RELATIONSHIP);
		relationmap.put("Triggering relation", IArchimatePackage.Literals.TRIGGERING_RELATIONSHIP);
		relationmap.put("Flow relation", IArchimatePackage.Literals.FLOW_RELATIONSHIP);
		relationmap.put("Used By relation", IArchimatePackage.Literals.USED_BY_RELATIONSHIP);
		relationmap.put("Access relation", IArchimatePackage.Literals.ACCESS_RELATIONSHIP);
		relationmap.put("Association relation", IArchimatePackage.Literals.ASSOCIATION_RELATIONSHIP);

		
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		
		
	}

}
