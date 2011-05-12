/*******************************************************************************
 * Copyright (c) 2010 Bolton University, UK.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 *******************************************************************************/
package org.rulez.magwas.styledhtml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import org.python.util.PythonInterpreter;

import uk.ac.bolton.archimate.editor.diagram.util.DiagramUtils;
import uk.ac.bolton.archimate.editor.model.IModelExporter;
import uk.ac.bolton.archimate.model.IArchimateModel;
import uk.ac.bolton.archimate.model.IDiagramModel;

import org.rulez.magwas.styledhtml.Widgets;
import org.rulez.magwas.styledhtml.IPreferenceConstants;

/**
 * Styled HTML Exporter of Archimate model
 * <p>
 * Input is the style directory, containing:<ul>
 * 		<li>a file called style.xslt: the xml stylesheet to be applied to the "rich model file"</li>
 * 		<li>any other files needed for the presentation of resulting html</li></ul>
 * 
 * <p>
 * In the future (If I still think it is a good idea), the style directory may contain a file containing the name of scripts to be run on archirich.xml and index.html.
 * <p>
 * Output is the report directory, containing:<ul>
 * 		<li>copy of contents of the style directory</li>
 * 		<li>archirich.xml : it is the model file, but each document tags are enriched with HTMLReportExporter's paqrseCharsAndLinks.</li>
 * 		<li>diagrams in png: all diagrams are saved to ID.png, where ID is the id of the diagram</li>
 * 		<li>index.html: the result of applying style.xslt to archirich.xml</li>
 * 
 * @author Árpád Magosányi
 */
public class StyledHtml implements IModelExporter {
	
	public class NoConfigException extends RuntimeException {
		private static final long serialVersionUID = -1109045666264335290L;
		public NoConfigException() {
            super("Bad stylesheet or you should set the stylesheet location in edit/preferences first.");
        }
		public NoConfigException(String s) {
            super(s);
        }
        @Override
        public String toString() {
        	return getMessage();
        }
    }
	public class BadPreprocessorException extends NoConfigException {
		private static final long serialVersionUID = -1109045666264335290L;
		public BadPreprocessorException() {
            super("preprocessor.xslt have some problems");
        }
    }
	public class BadPostprocessorException extends NoConfigException {
		private static final long serialVersionUID = -1109045666264335290L;
		public BadPostprocessorException() {
            super("postprocessor.xslt have some problems");
        }
    }


    public StyledHtml() {
    	
    }

    @Override
    public void export(IArchimateModel model){
        try {
        	String path = StyledHtmlPlugin.INSTANCE.getPreferenceStore().getString(IPreferenceConstants.STYLE_PATH);
        	File stylesheet = new File(path);
        	Transformer transformer = mkTransformer(stylesheet);
        	if((!stylesheet.exists())|(transformer == null)) {
        		throw new NoConfigException();
        	}
           	File dir = new File(stylesheet.getParent());
           	File preprocessor = new File(dir,"preprocess.xslt");
           	File pypreprocessor = new File(dir,"preprocess.py");
           	File policyfile = new File(dir,"policy.xml");
           	File postprocessor = new File(dir,"postrocess.xslt");
           	File pypostprocessor = new File(dir,"postprocess.py");
           	Transformer pretf = null;           	
           	Transformer posttf = null;           	
           	if((!pypreprocessor.exists()) && preprocessor.exists()) {
        		pretf = mkTransformer(preprocessor);
        		if(pretf == null) {
        			throw new BadPreprocessorException();
        		}
        	}
           	if((!pypostprocessor.exists()) && postprocessor.exists()) {
        		posttf = mkTransformer(postprocessor);
        		if(posttf == null) {
        			throw new BadPreprocessorException();
        		}
        	}
          	Boolean ask = StyledHtmlPlugin.INSTANCE.getPreferenceStore().getBoolean(IPreferenceConstants.OUT_ASK);
        	String opath = StyledHtmlPlugin.INSTANCE.getPreferenceStore().getString(IPreferenceConstants.OUT_PATH);
        	File targetdir;
        	if((!ask) || (opath == null)) {
        		String lastpath = StyledHtmlPlugin.INSTANCE.getPreferenceStore().getString(IPreferenceConstants.LAST_STYLED_PATH);
        		if (null == lastpath) {
        			StyledHtmlPlugin.INSTANCE.getPreferenceStore().setValue(IPreferenceConstants.LAST_STYLED_PATH, opath);
        		}
        		targetdir = Widgets.askSaveFile(IPreferenceConstants.LAST_STYLED_PATH,null);
        	} else {
        		targetdir = new File(opath);
        	}
        	if(targetdir == null) {
        		return;
        	}
        	createOutputDir(dir, targetdir);
        
        	File file = new File(targetdir,"archirich.xml");
        	RichExport.export(model,file,policyfile);
        	//save pictures
        	saveDiagrams(model,targetdir);
        	// we get it in xml
    		File ofile = new File(targetdir,"model.xml");
            if(pypreprocessor.exists()){
               	callPython(pypreprocessor,file,ofile);
               	file = ofile;
            } else if(pretf != null) {
        		doTransformation(file,pretf,ofile);
        		file = ofile;
        	}
            File output = new File(targetdir,"index.html");
            File stage;
            if(pypostprocessor.exists()||(posttf != null)) {
                stage = new File(targetdir,"stage.html");
            } else {
                stage = output;
            }
            doTransformation(file, transformer, stage);
            if(pypostprocessor.exists()){
               	callPython(pypostprocessor,stage,output);
            } else if(posttf != null) {
        		doTransformation(stage,posttf,output);
        	}
        } catch(Exception e) {
        	Widgets.tellProblem("Problem Exporting Model", e.toString());
        	e.printStackTrace();
        }
    }
    
private void callPython(File script, File in, File out) {
    	PythonInterpreter interp =  new PythonInterpreter();

    	File pylib = new File(script.getParentFile().getParentFile(),"pylib");
    	interp.exec("import sys");
    	interp.exec("sys.argv=['"+script.getAbsolutePath()+"','"+in.getAbsolutePath()+"','"+out.getAbsolutePath()+"']");
    	interp.exec("sys.path=['"+pylib.getAbsolutePath()+"']");
    	interp.execfile(script.getAbsolutePath());
    }
    
    public static Transformer mkTransformer(File style) {
     	// do we use the example code as is? :)
    	// 1. Instantiate a TransformerFactory.
    	TransformerFactory tFactory = 
    	                  javax.xml.transform.TransformerFactory.newInstance();

    	// 2. Use the TransformerFactory to process the stylesheet Source and
    	//    	    generate a Transformer.
    		try {
				return tFactory.newTransformer
				            (new javax.xml.transform.stream.StreamSource(style));
			} catch (TransformerConfigurationException e) {
				return null;
			}		
    }
    private void doTransformation(File source, Transformer tf, File output) throws Exception {
   
    	// 3. Use the Transformer to transform an XML Source and send the
    	//    	    output to a Result object.
    	tf.transform
    	    (new javax.xml.transform.stream.StreamSource(source), 
    	     new javax.xml.transform.stream.StreamResult( new
    	                                  java.io.FileOutputStream(output)));
    }
    
    private void createOutputDir(File dir, File targetdir) throws IOException {
    	targetdir.mkdir();
        File[] filelist = dir.listFiles();
        for( File f: filelist ) {
        	if( f.isDirectory()) {
        		File td = new File(targetdir,f.getName());
        		createOutputDir(f,td);
        	} else {
        		File outputFile = new File(targetdir,f.getName());
        		copyFile(f,outputFile);
        	}
        }
    }
    private static void copyFile(File f1, File f2) throws IOException{

        InputStream in = new FileInputStream(f1);
        
        //For Append the file.
//        OutputStream out = new FileOutputStream(f2,true);

        //For Overwrite the file.
        OutputStream out = new FileOutputStream(f2);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0){
          out.write(buf, 0, len);
        }
        in.close();
        out.close();

      }

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
