package org.rulez.magwas.enterprise;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;

public class ModelDiff {

	class DiffItem {
		
	}
	List<DiffItem> diffs= new ArrayList<DiffItem>();
	public boolean diff(EObject node1, EObject node2) {
		System.out.println("diff");
		if(node1.equals(node2)) {
			System.out.println("equals");
			return true;
		}
		if(!node1.eClass().equals(node1.eClass())) {
			System.out.println("class diff");
			return false;
		}
		EList<EAttribute> atts = node1.eClass().getEAllAttributes();
		for (EAttribute att : atts) {
			System.out.println("checking"+att);
			if(!node1.eGet(att).equals(node2.eGet(att))) {
				return false;
			}
		}
		System.out.println("all ok");
		return true;
	}
}
