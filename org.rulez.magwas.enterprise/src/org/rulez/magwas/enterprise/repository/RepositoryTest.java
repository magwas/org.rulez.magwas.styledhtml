package org.rulez.magwas.enterprise.repository;

import static org.junit.Assert.*;



import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.jdbc2.AbstractJdbc2Connection;

import uk.ac.bolton.archimate.editor.model.IEditorModelManager;
import uk.ac.bolton.archimate.model.IArchimateFactory;
import uk.ac.bolton.archimate.model.IArchimateModel;
import uk.ac.bolton.archimate.model.IProperty;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class RepositoryTest extends Repository {

	@After
	public void rollback() {
		close();
	}
	public RepositoryTest() throws SQLException, ClassNotFoundException, ConfigurationException {
		super("test");
	}

	@Test
	public void testconnect() {
		
		assertNotNull(con);
	}

	IArchimateModel model;
	
	//BeforeClass
	public static void buildconfig() {
		//This is enough to run once in a lifetime if you do not clear the junit workspace/its configuration.
		//uncomment the BeforeClass, and set your password if needed
		ConnContentProvider cp = new ConnContentProvider();
		ConnPref pref = new ConnPref(cp);
		pref.setRole("archi_submitter");
		pref.setName("test");
		pref.setAskpass(false);
		pref.setKeystore("/home/RES/magosanyi1a313/.postgresql/archi_owner.jks");
		pref.setUrl("jdbc:postgresql://localhost:5433/archi?sslfactory=org.rulez.magwas.enterprise.repository.CertAuthFactory");
		pref.setUsername("mag");
		pref.setPassword("changeit");
		cp.add(pref);
		pref = cp.getPref("test");
		assertNotNull(pref);		
		cp = new ConnContentProvider();
		pref = cp.getPref("test");
		assertNotNull(pref);		
	}
	@Before
	public void loadmodel() {
		File file = new File("/build/mag/org.rulez.magwas.styledhtml/org.rulez.magwas.styledhtml/doc/styledhtml.archimate");
		System.out.println("file="+file);
		assertNotNull(file);
		model = IEditorModelManager.INSTANCE.openModel(file);
		assertNotNull(model);
	}
	//@Before
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

	//@Test //moved to testCheckin
	public void testaddVersion() throws SQLException {
		List<String> a = getBaseVersions(model);
		int v1 = addVersion(a.get(0), "this is test","default acl");
		int v2 = addVersion(a.get(1), "this is test", "default acl");
		assertNotSame(0, v1);
		assertNotSame(0, v2);
		assertNotSame(v1, v2);
	}
	
	@Test
	public void testCheckin(){
		try {
			testaddVersion();
			checkin(model);
			con.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
	}

}