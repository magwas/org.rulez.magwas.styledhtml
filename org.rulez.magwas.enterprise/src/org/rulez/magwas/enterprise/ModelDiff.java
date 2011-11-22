package org.rulez.magwas.enterprise;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import uk.ac.bolton.archimate.model.IIdentifier;

public class ModelDiff {

	class DiffItem {
		String id, attname, val1, val2;
		DiffItem(String id,String attname,String val1,String val2){
			this.id=id;
			this.attname=attname;
			this.val1=val1;
			this.val2=val2;
		}
	}
	public List<DiffItem> diff(EObject node1, EObject node2) {
		List<DiffItem> diffs= new ArrayList<DiffItem>();
		return diff(diffs,node1,node2);
	}
	
	public List<DiffItem> diff(List<DiffItem> diffs, EObject node1, EObject node2) {	
		System.out.println("diff"+node1+node2);
		if(node1.equals(node2)) {
			System.out.println("equals");
			return diffs;
		}
		if(!node1.eClass().equals(node2.eClass())) {
			System.out.println("class diff");
			diffs.add(new DiffItem(((IIdentifier) node1).getId(),"type",node1.eClass().getName(),node2.eClass().getName()));
		}
		EList<EStructuralFeature> attrs = node1.eClass().getEAllStructuralFeatures();
		for (EStructuralFeature att : attrs) {
			System.out.println("checking "+att.getName());
			if(att.isMany()) {
				
			} else {
				
			}
		}
		return diffs;
	}
}
