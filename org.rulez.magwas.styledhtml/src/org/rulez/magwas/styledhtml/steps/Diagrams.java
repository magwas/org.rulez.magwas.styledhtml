package org.rulez.magwas.styledhtml.steps;

import java.io.File;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.w3c.dom.Element;

import uk.ac.bolton.archimate.editor.diagram.util.DiagramUtils;
import uk.ac.bolton.archimate.model.IArchimateModel;
import uk.ac.bolton.archimate.model.IDiagramModel;

/**
 * Responsible for exporting diagrams from the model.
 */
public class Diagrams extends Step {

	/**
	 * Instantiates a new Siagrams step.
	 *
	 * @param sf the Step Factory
	 */
	Diagrams(StepFactory sf) {
		super(sf);
	}
	
	/* (non-Javadoc)
	 * @see org.rulez.magwas.styledhtml.steps.Step#doit(org.w3c.dom.Element, java.io.File)
	 */
	@Override
	public boolean doit(Element arg0, File current) {
		String keep=arg0.getAttribute("keep");
		File tdir = getFileFor(arg0,"target","",factory.targetdir,current);
		if(null == tdir) return false;

		if("false".equals(keep)) {
			factory.dontkeep.add(tdir);
		}
    //FIXME: log
		System.out.println("saving diagrams to "+ tdir.getAbsolutePath());
    	saveDiagrams(factory.model,tdir);
		return doSubSteps(arg0, tdir);

	}
	
    /**
     * Save the diagrams.
     *
     * @param model the model
     * @param targetdir the target directory
     */
    private void saveDiagrams(IArchimateModel model,File targetdir) {
    	List<IDiagramModel> dias = model.getDiagramModels();
    	for (IDiagramModel dia : dias) {
    		Image image = DiagramUtils.createImage(dia);
    		String diagramID = dia.getId();
    		File file = new File(targetdir,diagramID+".png");
            ImageLoader loader = new ImageLoader();
            loader.data = new ImageData[] { image.getImageData() };
            loader.save(file.getAbsolutePath(), SWT.IMAGE_PNG);
    	}
    }

}
