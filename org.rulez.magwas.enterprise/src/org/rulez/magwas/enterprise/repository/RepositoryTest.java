package org.rulez.magwas.enterprise.repository;

import static org.junit.Assert.*;



import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.bolton.archimate.model.IArchimateFactory;
import uk.ac.bolton.archimate.model.IArchimateModel;
import uk.ac.bolton.archimate.model.IProperty;

import java.sql.SQLException;
import java.util.List;

public class RepositoryTest extends Repository {

	public RepositoryTest() throws SQLException, ClassNotFoundException, ConfigurationException {
		super("test");
	}

	@Test
	public void testconnect() {
		
		assertNotNull(con);
	}

	IArchimateModel model;
	
	//@BeforeClass
	public static void buildconfig() {
		//This is enough to run once in a lifetime if you do not clear the junit workspace/its configuration.
		//uncomment the BeforeClass, and set your password if needed
		ConnContentProvider cp = new ConnContentProvider();
		ConnPref pref = new ConnPref(cp);
		pref.setName("test");
		pref.setAskpass(false);
		pref.setKeystore("/home/RES/magosanyi1a313/.postgresql/archi_owner.jks");
		pref.setUrl("jdbc:postgresql://localhost:5433/archi?sslfactory=org.rulez.magwas.enterprise.repository.CertAuthFactory");
		pref.setUsername("mag");
		pref.setPassword("insert password here");
		cp.add(pref);
		pref = cp.getPref("test");
		assertNotNull(pref);		
		cp = new ConnContentProvider();
		pref = cp.getPref("test");
		assertNotNull(pref);		
	}
	@Before
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
		
	}
	
	@Test
	public void testgetModelversion() {
		String a = getModelVersion(model);
		System.out.println("model version ="+a);
		assertNotNull(a);
	}
	@Test
	public void testgetBaseVersions() {
		List<String> a = getBaseVersions(model);
		System.out.println("base versions ="+a);
		assertNotNull(a);
	}

	@Test
	public void testaddVersion() throws SQLException {
		addVersion("Target v 0.2.41", "this is test");
		addVersion("System foobar v 0.3.1", "this is test");
	}

}