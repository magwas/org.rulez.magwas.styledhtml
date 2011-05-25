package org.rulez.magwas.styledhtml.steps;

import java.io.File;

import org.w3c.dom.Element;

public class Style extends Step {

	Style(StepFactory sf) {
		super(sf);
	}

	@Override
	public void doit(Element arg0, File current) {
		doSubSteps(arg0, current);
	}

}
