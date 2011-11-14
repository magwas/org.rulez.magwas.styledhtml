package org.rulez.magwas.enterprise.repository;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rulez.magwas.enterprise.ModelDiff;

import uk.ac.bolton.archimate.editor.model.IEditorModelManager;
import uk.ac.bolton.archimate.model.IArchimateModel;

public class SQLManagerTest extends SQLManager {

	public SQLManagerTest() throws ConfigurationException {
		super("test");
	}
	static private IArchimateModel model;
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
	public static void setUpBeforeClass() throws Exception {
		File file = new File("/tmp/test.archimate");
		System.out.println("file="+file);
		assertNotNull(file);
		model = getModel(file);
		assertNotNull(model);
		
	}

	@Test
	public void test() throws SQLException {
		List<String> empty = new ArrayList<String>();
		int v = addVersion("SqlManagertest", empty, "this is test","default acl");
		saveObject(model,v);
		con.commit();
		EObject obj = loadObject(model.getId(),v);
		ModelDiff d = new ModelDiff();
		assertTrue(d.diff(model, obj));
		forget();
	}

}
