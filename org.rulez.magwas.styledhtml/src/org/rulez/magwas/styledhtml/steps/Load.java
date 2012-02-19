package org.rulez.magwas.styledhtml.steps;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.bolton.archimate.editor.model.IEditorModelManager;
import uk.ac.bolton.archimate.model.IArchimateModel;

public class Load extends Step {
	Load(StepFactory sf) {
		super(sf);
	}

	//loads a (presumably massaged) archi file, and do the substeps on it 
	@Override
	public void doit(Element arg0, File current) {
		factory.log.issueInfo("loading model from", current.getAbsolutePath());
    	IEditorModelManager.INSTANCE.openModel(current);
        for(IArchimateModel model : IEditorModelManager.INSTANCE.getModels()) {
            if(current.equals(model.getFile())) {
        		StepFactory sf = new StepFactory(factory.log,model,factory.styledir, factory.targetdir);
           		try {
           			//the following is essentially doSubSteps, but with the new factory
           	   		NodeList l = arg0.getChildNodes();
           	   		for(int i=0; i<l.getLength();i++) {
           	   			Node n = l.item(i);
           	   			if(n.getNodeType() == Node.ELEMENT_NODE) {
               	   			sf.get(n.getNodeName()).doit((Element) n,current);
           	    		}
           	    	}
           	   		sf.cleanUp();
    				IEditorModelManager.INSTANCE.closeModel(model);
    				} catch (IOException e) {
    					factory.log.issueError("closing model", e.getMessage());
    					e.printStackTrace();
    				}
                break;
            }
        }
	}

}
