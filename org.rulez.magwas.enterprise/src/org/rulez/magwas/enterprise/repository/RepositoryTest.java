package org.rulez.magwas.enterprise.repository;

import static org.junit.Assert.*;



import org.eclipse.swt.widgets.Shell;
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
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

public class RepositoryTest extends Repository {

	@After
	public void rollback() {
		pm.forget();
	}
	public RepositoryTest() throws SQLException, ClassNotFoundException, ConfigurationException {
		super("test");
	}

	@Test
	public void testconnect() {
		assertNotNull(pm);
	}

	static IArchimateModel model;
	
	//@BeforeClass
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
		pref.setPassword(new PasswordDialog(new Shell()).ask());
		cp.add(pref);
		pref = cp.getPref("test");
		assertNotNull(pref);		
		cp = new ConnContentProvider();
		pref = cp.getPref("test");
		assertNotNull(pref);		
	}
    static private IArchimateModel getModel(File file) {
        if(file != null) {
            for(IArchimateModel model : IEditorModelManager.INSTANCE.getModels()) {
                if(file.equals(model.getFile())) {
                    return model;
                }
            }
        }
		model = IEditorModelManager.INSTANCE.openModel(file);
        return model;
    }

	@BeforeClass
	public static void loadmodel() {
		File file = new File("/tmp/test.archimate");
		System.out.println("file="+file);
		assertNotNull(file);
		model = getModel(file);
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
		System.out.println("base versions:"+a);
		assertEquals(a.size(),1);
	}

	//@Test //moved to testCheckin
	public void testaddVersion() throws SQLException {
		List<String> a = getBaseVersions(model);
		List<String> empty = new ArrayList<String>();
		int v1 = pm.addVersion("dummy", empty, "dummy version for test","default acl");
		int v2 = pm.addVersion("foo", a, "this is test","default acl");
		assertNotSame(0, v1);
		assertNotSame(v1, v2);
	}
	
	@Test
	public void testCheckin(){
		try {
			testaddVersion();
			checkin(model);
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		}
	}

}