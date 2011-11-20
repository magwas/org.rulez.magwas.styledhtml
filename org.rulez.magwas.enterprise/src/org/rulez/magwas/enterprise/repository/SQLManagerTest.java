package org.rulez.magwas.enterprise.repository;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rulez.magwas.enterprise.ModelDiff;

import uk.ac.bolton.archimate.editor.model.IEditorModelManager;
import uk.ac.bolton.archimate.model.IArchimateFactory;
import uk.ac.bolton.archimate.model.IArchimateModel;
import uk.ac.bolton.archimate.model.IArchimateModelElement;
import uk.ac.bolton.archimate.model.INameable;
import uk.ac.bolton.archimate.model.util.ArchimateModelUtils;
import uk.ac.bolton.archimate.model.util.ArchimateResource;
import uk.ac.bolton.archimate.model.util.ArchimateResourceFactory;

public class SQLManagerTest extends SQLManager {

	public SQLManagerTest() throws ConfigurationException {
		super();
		Repository repo = new Repository("test");
		__init__(repo);
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
		String objid = "b90ab107";
		EObject node = model;//ArchimateModelUtils.getObjectByID(model,objid);

		int v = addVersion("SqlManagertest", empty, "this is test","default acl");
		saveObject(node);
		close();
		con.commit();

		Repository repo = new Repository("test");
		
		EObject obj = repo.checkout("SqlManagertest");
		File target = new File("/tmp/testout.archimate");
      	ArchimateResource resource = (ArchimateResource) ArchimateResourceFactory.createResource(target);
    	resource.getContents().add(obj);
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

		ModelDiff d = new ModelDiff();
		assertTrue(d.diff(node, obj));
		forget();
	}

}
