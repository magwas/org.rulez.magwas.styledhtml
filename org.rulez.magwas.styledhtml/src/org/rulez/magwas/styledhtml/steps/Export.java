package org.rulez.magwas.styledhtml.steps;

import java.io.File;
import java.io.IOException;

import org.eclipse.emf.ecore.resource.Resource;
import org.rulez.magwas.styledhtml.EventLog;
import org.rulez.magwas.styledhtml.RichExport;
import org.w3c.dom.Element;

import uk.ac.bolton.archimate.model.util.ArchimateResourceFactory;

/**
 * The Exports the model either to archimate or rich format (default is rich).
 */
public class Export extends Step {

	/**
	 * Instantiates a new export.
	 *
	 * @param sf the Step Factory
	 */
	public Export(StepFactory sf) {
		super(sf);
	}

	/* (non-Javadoc)
	 * @see org.rulez.magwas.styledhtml.steps.Step#doit(org.w3c.dom.Element, java.io.File)
	 */
	@Override
	public boolean doit(Element arg0, File current) {
		String style=arg0.getAttribute("style");
		String keep=arg0.getAttribute("keep");
		File policyfile = getFileFor(arg0,"policy",null,factory.styledir,current);
		File tfile = getFileFor(arg0,"target",null,factory.targetdir,current);
		factory.log.issueInfo("target="+tfile.getAbsolutePath(), EventLog.now());


		if("".equals(style)||"rich".equals(style)) {
        	RichExport.export(factory.model,tfile,policyfile,factory.log);
		} else if("archi".equals(style)){
	        Resource resource = ArchimateResourceFactory.createResource(tfile);
	        resource.getContents().add(factory.model);
	        try {
				resource.save(null);
			} catch (IOException e) {
				factory.log.issueError("cannot export model to", tfile.getAbsolutePath());
				factory.log.printStackTrace(e);
				return false;
			}
	        resource.getContents().remove(factory.model);
		} else {
			factory.log.issueError("Unknown export style", style);
			return false;
		}
		if("false".equals(keep)) {
			factory.dontkeep.add(tfile);
		}
		return doSubSteps(arg0, tfile);
	}
}
