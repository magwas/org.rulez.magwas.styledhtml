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
import org.w3c.dom.NamedNodeMap;


/**
 * The "transform" step. Executes an xslt or python script, with the given attributes
 */
public class Transform extends Step {
	
	/** The attributes given to the step */
	NamedNodeMap atts;
	
	/**
	 * Instantiates a new transform.
	 *
	 * @param sf the step factory
	 */
	public Transform(StepFactory sf) {
		super(sf);
	}

	/* (non-Javadoc)
	 * @see org.rulez.magwas.styledhtml.steps.Step#doit(org.w3c.dom.Element, java.io.File)
	 */
	@Override
	public boolean doit(Element arg0, File current) {
		factory.log.issueInfo("transforming", current.getAbsolutePath());
        String language=arg0.getAttribute("language");
		String keep=arg0.getAttribute("keep");
		atts = arg0.getAttributes();
		File tfile = getFileFor(arg0,"target",null,factory.targetdir, current);
		if(null == tfile) return false;
		if("false".equals(keep)) {
			factory.dontkeep.add(tfile);
		}
		File sfile = getFileFor(arg0,"script",null,factory.styledir, current);
		if(null == sfile) return false;
		if("".equals(language)||"xslt".equals(language)) {
    		Transformer tf = mkTransformer(sfile);
    		if(null == tf) {
    			return false;
    		}
    		if(!doTransformation(current,tf,tfile)) {
    			return false;
    		}
		} else if ("python".equals(language)) {
			if(!callPython(sfile,current,tfile)) {
				return false;
			}
		} else {
			factory.log.issueError("Invalid language for <transform>", language);
			return false;
		}
		return doSubSteps(arg0, tfile);
	}

    /**
     * Call python with the two first parameters (in sys.argv) the input and output file names,
     *  and the remaining are the attributes as key=value pairs
     *
     * @param script the script
     * @param in the input file
     * @param out the output file
     * @return true if successful
     */
    private boolean callPython(File script, File in, File out) {
    	PythonInterpreter interp =  new PythonInterpreter();

    	File pylib = new File(script.getParentFile().getParentFile(),"pylib");
    	interp.exec("import sys");
    	interp.exec("sys.argv=['"+script.getAbsolutePath()+"','"+in.getAbsolutePath()+"','"+out.getAbsolutePath()+"']");
		for(int i = 0;i<atts.getLength();i++) {
			String name = atts.item(i).getNodeName();
			String value = atts.item(i).getNodeValue();
			factory.log.issueInfo("param", name + "=" + value);
			interp.exec("sys.argv.append('"+name + "=" + value+"')");
		}
		interp.exec("sys.argv.append('targetdir" + "=" + factory.targetdir.getAbsolutePath() +"')");
    	interp.exec("sys.path=['"+pylib.getAbsolutePath()+"']");
    	interp.execfile(script.getAbsolutePath());
    	return true;
    }
    
    /**
     * Make an xslt transformer.
     *
     * @param style the style file
     * @return the transformer
     */
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
    
    /**
     * Do the transformation.
     *
     * @param source the source file
     * @param tf the transformer class
     * @param output the output file
     * @return true if successful
     */
    private boolean doTransformation(File source, Transformer tf, File output){
    	try {
    		StreamSource ss = new StreamSource(source);
    		FileOutputStream os = new FileOutputStream(output);
    		StreamResult sr = new StreamResult( os );
    		for(int i = 0;i<atts.getLength();i++) {
    			String name = atts.item(i).getNodeName();
    			String value = atts.item(i).getNodeValue();
    			factory.log.issueInfo("param", name + "=" + value);
        		tf.setParameter(name, value);    			
    		}
    		tf.setParameter("targetdir", factory.targetdir.getAbsolutePath());
    		factory.log.issueInfo("targetdir", factory.targetdir.getAbsolutePath());
			tf.transform(ss, sr);
		} catch (FileNotFoundException e) {
			factory.log.issueError("file not found for <transform>", source.getAbsolutePath());
			return false;
		} catch (TransformerException e) {
			factory.log.issueError("xslt problem <transform>", e.getMessageAndLocation());
			return false;
		}
    	return true;
    }
    
}
