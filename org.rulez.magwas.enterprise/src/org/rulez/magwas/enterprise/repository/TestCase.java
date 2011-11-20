package org.rulez.magwas.enterprise.repository;

import java.io.ObjectOutputStream;
import java.io.StringBufferInputStream;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.Test;

import uk.ac.bolton.archimate.model.IArchimateFactory;
import uk.ac.bolton.archimate.model.IArchimateModel;
import uk.ac.bolton.archimate.model.IArchimatePackage;
import uk.ac.bolton.archimate.model.IProperty;

public class TestCase {
	IArchimateModel model;
	
	
	public void buildmodel() {
		
		model = IArchimateFactory.eINSTANCE.createArchimateModel();
        model.setDefaults();
        model.setName("test model");
        IProperty prop = IArchimateFactory.eINSTANCE.createProperty();
        prop.setKey("modelVersion");
        prop.setValue("Target v 0.2.42");
        model.getProperties().add(prop);
        IProperty prop2 = IArchimateFactory.eINSTANCE.createProperty();
        prop2.setKey("baseVersion");
        prop2.setValue("Target v 0.2.41");
        model.getProperties().add(prop2);
        IProperty prop3 = IArchimateFactory.eINSTANCE.createProperty();
        prop3.setKey("baseVersion");
        prop3.setValue("System foobar v 0.3.1");
        model.getProperties().add(prop3);
        IProperty prop4 = IArchimateFactory.eINSTANCE.createProperty();
        prop4.setKey("modelAcl");
        prop4.setValue("default acl");
        model.getProperties().add(prop4);
		
	}
	
	@Test
	public void testsplit() {
		String value = "hello=";
		String[] v = value.split("=");
		System.out.println("v[0]="+v[0]+",len="+v.length);
	}
	//@Test
	public void testeobj() {
		buildmodel();
		System.out.println("eclass="+model.eClass());
		EList<EAttribute> attrs = model.eClass().getEAllAttributes();
		for (EAttribute attr : attrs) {
			System.out.println(attr.getName()+"="+model.eGet(attr));
		}
		String type="ArchimateModel";
		System.out.println("type ="+type);
		EClass eclass = (EClass) IArchimatePackage.eINSTANCE.getEClassifier(type);
		System.out.println("eclass="+eclass);
		EObject foo = EcoreUtil.create((EClass) eclass);
		String name = "name";
		String value = "/usr/local/bin";
		EStructuralFeature feature = eclass.getEStructuralFeature(name);

		foo.eSet(feature, value);
		System.out.println("instance="+foo);
		

	}

}
