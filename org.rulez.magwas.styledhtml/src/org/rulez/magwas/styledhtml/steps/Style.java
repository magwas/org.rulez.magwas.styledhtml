package org.rulez.magwas.styledhtml.steps;

import java.io.File;

import org.w3c.dom.Element;

/**
 * The "style" step: id does nothing, just executes steps under it.
 */
public class Style extends Step {

	Style(StepFactory sf) {
		super(sf);
	}

	/* (non-Javadoc)
	 * @see org.rulez.magwas.styledhtml.steps.Step#doit(org.w3c.dom.Element, java.io.File)
	 */
	@Override
	public boolean doit(Element arg0, File current) {
		return doSubSteps(arg0, current);
	}

}
