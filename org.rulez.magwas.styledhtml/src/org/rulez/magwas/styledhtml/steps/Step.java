package org.rulez.magwas.styledhtml.steps;



import java.io.File;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public abstract class Step {
	StepFactory factory;
	Step(StepFactory sf) {
		factory = sf;
	}
	public final void doIt(Element e, File current) {
		String name = e.getNodeName();
		Step s = factory.get(name);
		s.doit(e, current);
	}
	public void doSubSteps(Element arg0, File current) {
		NodeList l = arg0.getChildNodes();
		for(int i=0; i<l.getLength();i++) {
			Node n = l.item(i);
			if(n.getNodeType() == Node.ELEMENT_NODE) {
				doIt((Element) n, current);
			}
		}
	}

	public File getFileFor(Element e, String attname, String defval, File defbase, File current) {
		String attval=e.getAttribute(attname);
		File retfile;
		if("".equals(attval)) {
			if (null != defval) {
				retfile=new File(defbase.getAbsolutePath()+"/"+defval);
			} else {
				factory.log.issueError("No attribute " +attname, "at "+e.getNodeName());
				return null;
			}
		} else {
			String path;
			if(attval.contains("$target")) {
				path=attval.replace("$target",factory.targetdir.getAbsolutePath());
			} else if (attval.contains("$source")) {
				path=attval.replace("$source",factory.styledir.getAbsolutePath());
			} else if (attval.contains("$current")) {
				path=attval.replace("$current",current.getAbsolutePath());
			} else {
				path=defbase.getAbsolutePath()+"/"+attval;
			}
			retfile=new File(path);
		}
		return retfile;
	}
	
	abstract public void doit(Element arg0, File current);

}
