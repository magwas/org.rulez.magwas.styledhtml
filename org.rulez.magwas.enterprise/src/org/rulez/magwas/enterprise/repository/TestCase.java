package org.rulez.magwas.enterprise.repository;


import static org.junit.Assert.assertNotNull;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.bolton.archimate.editor.model.IEditorModelManager;
import uk.ac.bolton.archimate.model.IArchimateFactory;
import uk.ac.bolton.archimate.model.IArchimateModel;
import uk.ac.bolton.archimate.model.IArchimatePackage;
import uk.ac.bolton.archimate.model.IProperty;
import uk.ac.bolton.archimate.model.impl.ArchimatePackage;

import org.eclipse.emf.cdo.eresource.CDOResource;
import org.eclipse.emf.cdo.net4j.CDONet4jUtil;
import org.eclipse.emf.cdo.net4j.CDOSessionConfiguration;
import org.eclipse.emf.cdo.session.CDOSession;
import org.eclipse.emf.cdo.transaction.CDOTransaction;

import org.eclipse.net4j.FactoriesProtocolProvider;
import org.eclipse.net4j.Net4jUtil;
import org.eclipse.net4j.buffer.IBufferProvider;
import org.eclipse.net4j.protocol.IProtocolProvider;
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;
import org.eclipse.net4j.util.om.OMPlatform;
import org.eclipse.net4j.util.om.log.PrintLogHandler;
import org.eclipse.net4j.util.om.trace.PrintTraceHandler;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class TestCase {
	static IArchimateModel model;
	
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
	public void testcdo()
	{
		buildmodel();
	    // Enable logging and tracing
	    OMPlatform.INSTANCE.setDebugging(false);
	    OMPlatform.INSTANCE.addLogHandler(PrintLogHandler.CONSOLE);
	    OMPlatform.INSTANCE.addTraceHandler(PrintTraceHandler.CONSOLE);

	    // Prepare receiveExecutor
	    final ThreadGroup threadGroup = new ThreadGroup("net4j"); //$NON-NLS-1$
	    ExecutorService receiveExecutor = Executors.newCachedThreadPool(new ThreadFactory()
	    {
	      public Thread newThread(Runnable r)
	      {
	        Thread thread = new Thread(threadGroup, r);
	        thread.setDaemon(true);
	        return thread;
	      }
	    });

	    // Prepare bufferProvider
	    IBufferProvider bufferProvider = Net4jUtil.createBufferPool();
	    LifecycleUtil.activate(bufferProvider);

	    IProtocolProvider protocolProvider = new FactoriesProtocolProvider(
	        new org.eclipse.emf.internal.cdo.net4j.protocol.CDOClientProtocolFactory());

	    // Prepare selector
	    org.eclipse.net4j.internal.tcp.TCPSelector selector = new org.eclipse.net4j.internal.tcp.TCPSelector();
	    selector.activate();

	    // Prepare connector
	    org.eclipse.net4j.internal.tcp.TCPClientConnector connector = new org.eclipse.net4j.internal.tcp.TCPClientConnector();
	    connector.getConfig().setBufferProvider(bufferProvider);
	    connector.getConfig().setReceiveExecutor(receiveExecutor);
	    connector.getConfig().setProtocolProvider(protocolProvider);
	    connector.getConfig().setNegotiator(null);
	    connector.setSelector(selector);
	    connector.setHost("localhost"); //$NON-NLS-1$
	    connector.setPort(2036);
	    connector.activate();

	    // Create configuration
	    CDOSessionConfiguration configuration = CDONet4jUtil.createSessionConfiguration();
	    configuration.setConnector(connector);
	    configuration.setRepositoryName("repo1"); //$NON-NLS-1$

	    // Open session
	    CDOSession session = configuration.openSession();
	    session.getPackageRegistry().putEPackage(ArchimatePackage.eINSTANCE);

	    // Open transaction
	    CDOTransaction transaction = session.openTransaction();

	    // Get or create resource
	    CDOResource resource = transaction.getOrCreateResource("/path/to/my/resource"); //$NON-NLS-1$

	    // Work with the resource and commit the transaction
	    IArchimateModel object = model;
	    System.out.println("adding "+object);
	    boolean u = resource.getContents().add(object);
	    System.out.println("returned "+u+"state="+resource.cdoState());
	    transaction.commit();
	    System.out.println("state="+resource.cdoState());
	    EList<Diagnostic> ws = resource.getErrors();
	    for(Diagnostic w: ws) {
	    	System.out.println("WARNING: "+w);
	    }
	    EList<EObject> c = resource.getContents();
	    for(EObject o :c) {
	    	System.out.println("have "+o);
	    }

	    // Cleanup
	    session.close();
	    connector.deactivate();
	 }
	//@Test
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




