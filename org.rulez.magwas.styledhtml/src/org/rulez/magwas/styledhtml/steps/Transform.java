package org.rulez.magwas.styledhtml.steps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.python.util.PythonInterpreter;
import org.w3c.dom.Element;

public class Transform extends Step {

	public Transform(StepFactory sf) {
		super(sf);
	}

	@Override
	public void doit(Element arg0, File current) {
		String language=arg0.getAttribute("language");
		String keep=arg0.getAttribute("keep");
		File tfile = getFileFor(arg0,"target",null,factory.targetdir, current);
		if(null == tfile) return;
		if("false".equals(keep)) {
			factory.dontkeep.add(tfile);
		}
		File sfile = getFileFor(arg0,"script",null,factory.styledir, current);
		if(null == sfile) return;
		if("".equals(language)||"xslt".equals(language)) {
    		Transformer tf = mkTransformer(sfile);
    		if(null == tf) {
    			return;
    		}
    		doTransformation(current,tf,tfile);
		} else if ("python".equals(language)) {
			callPython(sfile,current,tfile);
		} else {
			factory.log.issueError("Invalid language for <transform>", language);
		}
		doSubSteps(arg0, tfile);

	}

    private void callPython(File script, File in, File out) {
    	PythonInterpreter interp =  new PythonInterpreter();

    	File pylib = new File(script.getParentFile().getParentFile(),"pylib");
    	interp.exec("import sys");
    	interp.exec("sys.argv=['"+script.getAbsolutePath()+"','"+in.getAbsolutePath()+"','"+out.getAbsolutePath()+"']");
    	interp.exec("sys.path=['"+pylib.getAbsolutePath()+"']");
    	interp.execfile(script.getAbsolutePath());
    }
    public Transformer mkTransformer(File style) {
    	TransformerFactory tFactory = 
    	                  javax.xml.transform.TransformerFactory.newInstance();

    		try {
				return tFactory.newTransformer
				            (new javax.xml.transform.stream.StreamSource(style));
			} catch (TransformerConfigurationException e) {
    			factory.log.issueError("cannot compile "+ style.getAbsolutePath(),e.getMessageAndLocation());
    			e.printStackTrace();
    			return null;
			}		
    }
    private void doTransformation(File source, Transformer tf, File output){
    	try {
    		StreamSource ss = new StreamSource(source);
    		FileOutputStream os = new FileOutputStream(output);
    		StreamResult sr = new StreamResult( os );
    		System.out.println("ss="+ss);
    		System.out.println("sr="+sr);
    		System.out.println("tf="+tf);    		
			tf.transform(ss, sr);
		} catch (FileNotFoundException e) {
			factory.log.issueError("file not found for <transform>", source.getAbsolutePath());
		} catch (TransformerException e) {
			factory.log.issueError("xslt problem <transform>", e.getMessageAndLocation());
		}
    }
    
}
