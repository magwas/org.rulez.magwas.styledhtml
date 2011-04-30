/*******************************************************************************
 * Copyright (c) 2010 Bolton University, UK.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 *******************************************************************************/
package org.rulez.magwas.styledhtml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.nio.charset.Charset;
import java.io.OutputStreamWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import uk.ac.bolton.archimate.editor.diagram.util.DiagramUtils;
import uk.ac.bolton.archimate.editor.model.IModelExporter;
import uk.ac.bolton.archimate.editor.preferences.Preferences;
import uk.ac.bolton.archimate.model.IArchimateModel;
import uk.ac.bolton.archimate.model.IDiagramModel;
import uk.ac.bolton.archimate.model.util.ArchimateResource;
import uk.ac.bolton.archimate.model.util.ArchimateResourceFactory;
import uk.ac.bolton.archimate.editor.utils.HTMLUtils;

import org.rulez.magwas.styledhtml.IPreferenceConstants;

/**
 * Styled HTML Exporter of Archimate model
 * <p>
 * Input is the style directory, containing:<ul>
 * 		<li>a file called style.xslt: the xml stylesheet to be applied to the "rich model file"</li>
 * 		<li>any other files needed for the presentation of resulting html</li></ul>
 * 
 * <p>
 * In the future (If I still think it is a good idea), the style directory may contain a file containing the name of scripts to be run on model.archirich and index.html.
 * <p>
 * Output is the report directory, containing:<ul>
 * 		<li>copy of contents of the style directory</li>
 * 		<li>model.archirich : it is the model file, but each document tags are enriched with HTMLReportExporter's paqrseCharsAndLinks.</li>
 * 		<li>diagrams in png: all diagrams are saved to ID.png, where ID is the id of the diagram</li>
 * 		<li>index.html: the result of applying syle.xslt to model.archirich</li>
 * 
 * @author Árpád Magosányi
 */
public class StyledHtml implements IModelExporter {
	
	private Transformer transformer;
	
	public class NoConfig extends RuntimeException {
		private static final long serialVersionUID = -1109045666264335290L;
		public NoConfig() {
            super("You should set the stylesheet location in edit/preferences first.");
        }
        @Override
        public String toString() {
        	return getMessage();
        }
    }

    public StyledHtml() {
    	
    }

    @Override
    public void export(IArchimateModel model){
        try {
        	String path = Preferences.STORE.getString(IPreferenceConstants.STYLE_PATH);
        	File stylesheet = new File(path);
        	transformer = mkTransformer(stylesheet);
        	if((!stylesheet.exists())|(transformer == null)) {
        		throw new NoConfig();
        	}
        	Boolean ask = Preferences.STORE.getBoolean(IPreferenceConstants.OUT_ASK);
        	String opath = Preferences.STORE.getString(IPreferenceConstants.OUT_PATH);
        	File targetdir;
        	System.out.println("ask="+ask+" opath="+opath);
        	if((!ask) || (opath == null)) {
        		targetdir = StyledPreferencePage.askSaveFile();
        	} else {
        		targetdir = new File(opath);
        	}
        	if(targetdir == null) {
        		return;
        	}
        	File dir = new File(stylesheet.getParent());
        	createOutputDir(dir, targetdir);
        
        	File file = new File(targetdir,"model.archirich");
        
        	ArchimateResource resource = (ArchimateResource) ArchimateResourceFactory.createResource(file);
        	resource.getContents().add(model);
        	//save pictures
        	saveDiagrams(model,targetdir);
        	// we get it in xml
        	Document xml = resource.save(null,resource.getDefaultSaveOptions(),null);
        	resource.getContents().remove(model);
        	//work around
        	//enrich the xml
        	NodeList nl = xml.getElementsByTagName("documentation");
        	for(int i=0;i<nl.getLength();i++) {
        		Element n = (Element) nl.item(i);
        		parseCharsAndLinks(n);
        	}
        	NodeList pl = xml.getElementsByTagName("purpose");
        	for(int j=0;j<pl.getLength();j++) {
        		Element k = (Element) pl.item(j);
        		parseCharsAndLinks(k);
        	}
        	//save the xml
        	DOMConfiguration docConfig = xml.getDomConfig();
        	docConfig.setParameter("well-formed", true);
        	xml.normalizeDocument();
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl = (DOMImplementationLS)registry.getDOMImplementation("LS");
            LSSerializer writer = impl.createLSSerializer();
            String str = writer.writeToString(xml);
            String enc = xml.getXmlEncoding();
            if (enc == null) {
            	enc="utf-16";
            }
            Charset cs = Charset.forName(enc);
            OutputStream os = new FileOutputStream(file);
            OutputStreamWriter fw = new OutputStreamWriter(os,cs);
            fw.write(str);
            fw.close();
            File output = new File(targetdir,"index.html");
            doTransformation(file, output);
        } catch(Exception e) {
        	StyledPreferencePage.tellProblem("Problem Exporting Model", e.toString());
        	e.printStackTrace();
        }
    }
    
    private void parseCharsAndLinks(Element n) {
        // Escape chars
    	Document d = n.getOwnerDocument();
    	String s = n.getTextContent();
    	n.setTextContent("");
    	s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        s = s.replaceAll("\"", "&quot;");
        
        String[] ss = s.split("(\r\n|\r|\n)");
        
        for(String sss : ss) {
        	parseLinks(sss,n);
        	n.appendChild(d.createElement("br"));
        }
        
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
    private void doTransformation(File source, File output) throws Exception {
   
    	// 3. Use the Transformer to transform an XML Source and send the
    	//    	    output to a Result object.
    	transformer.transform
    	    (new javax.xml.transform.stream.StreamSource(source), 
    	     new javax.xml.transform.stream.StreamResult( new
    	                                  java.io.FileOutputStream(output)));
    }
    
    private void parseLinks(String s, Node parent) {
       	Matcher matcher = HTMLUtils.HTML_LINK_PATTERN.matcher(s);
    	Document d = parent.getOwnerDocument();
    	
        int lastend=0;
        while(matcher.find(lastend)) {
            String group = matcher.group();
            String text = s.substring(lastend,matcher.start());

            Node txt = d.createTextNode(text);
            lastend=matcher.end();
            parent.appendChild(txt);
            Element a = d.createElement("a");
            a.setAttribute("href", group);
            Node sub = d.createTextNode(group);
            a.appendChild(sub);
            parent.appendChild(a);
        }
        if(lastend < s.length()){
        	Node txt = d.createTextNode(s.substring(lastend));
        	parent.appendChild(txt);
        }
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
        		FileReader in = new FileReader(f);
        		FileWriter out = new FileWriter(outputFile);
        		int c;
        		
        		while ((c = in.read()) != -1)
        			out.write(c);
        		
        		in.close();
        		out.close();
        	}
        }
    }


    private void saveDiagrams(IArchimateModel model,File targetdir) {
    	List<IDiagramModel> dias = model.getDiagramModels();
    	for (IDiagramModel dia : dias) {
    		Image image = DiagramUtils.createImage(dia);
    		String diagramID = dia.getId();
    		File file = new File(targetdir,diagramID+".png");
            try {
                ImageLoader loader = new ImageLoader();
                loader.data = new ImageData[] { image.getImageData() };
                loader.save(file.getAbsolutePath(), SWT.IMAGE_PNG);
            }
            finally {
                image.dispose();
            }
    	}
    }
}
