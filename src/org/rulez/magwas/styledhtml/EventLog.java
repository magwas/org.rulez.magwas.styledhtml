package org.rulez.magwas.styledhtml;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import uk.ac.bolton.archimate.editor.browser.BrowserEditorInput;
import uk.ac.bolton.archimate.editor.browser.IBrowserEditor;
import uk.ac.bolton.archimate.editor.ui.services.EditorManager;

public class EventLog {

	private IBrowserEditor editor;
	private final Browser browser;
	private Document messages;
	private Node msg;
	
	
	public EventLog(String title) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("no document builder");
		}
		messages = db.newDocument();
		Node table = messages.createElement("table");
		messages.appendChild(table);
		msg = table;
		BrowserEditorInput br = new BrowserEditorInput(null,title);
    	editor = (IBrowserEditor)EditorManager.openEditor(br, IBrowserEditor.ID);
    	browser = editor.getBrowser();

   	 Display.getDefault().asyncExec(new Runnable() { // On a thread for when browser has been created
   		 @Override
   		 public void run() {
   			 browser.addProgressListener(new ProgressListener() {
   	    			@Override
   	    			public void completed(ProgressEvent event) {
   	    				System.out.println(editor.getBrowser().getUrl());
   	    				}
   	    			@Override
   	    			public void changed(ProgressEvent event) {
   	    				
   	    			}
   	    			});
   			 browser.addLocationListener(new LocationListener() {
   				 public void changing(LocationEvent event) {
   					 String loc = event.location;//FIXME highlight this entity in tree and properties view
   					 System.out.println("trying to open location "+loc);
   					 event.doit = false;
   				 }
   				 public void changed(LocationEvent event) {
   				 }
   				 
   			 });
   	    		}   
   		 });    	
  	}
	   private void issue(String qualifier,Element node, String text, String detail) {
	 
	    	Node location = messages.createElement("a");
	    	if(null != node) {
	    		((Element)location).setAttribute("href","archimate://"+node.getAttribute("id"));
	    		location.setTextContent(" at "+node.getAttribute("name"));
	    	} else {
	    		location.setTextContent("");
	    	}
	    	Node tr = messages.createElement("tr");
	    	msg.appendChild(tr);
	    	Node qtd = messages.createElement("td");
	    	qtd.setTextContent(qualifier);
	    	tr.appendChild(qtd);
	    	Node ttd = messages.createElement("td");
	    	ttd.setTextContent(text);
	    	tr.appendChild(ttd);
	    	Node ltd = messages.createElement("td");
	    	ltd.appendChild(location);
	    	tr.appendChild(ltd);
	    	Node dtd = messages.createElement("td");
	    	dtd.setTextContent(detail);
	    	tr.appendChild(dtd);
	    	//bs.refresh();
	    }

	   public void show() {
	    	String repr = xmlToString(messages);
	    	browser.setText(repr);
	   }
	   
	    public void issueWarning(Element node, String text, String detail) {
	    	issue("WARNING",node,text,"");
	    }    
	    public void issueError(Element node, String text, String detail) {
	    	issue("ERROR",node,text,detail);
	    }
	    
	    public void printStackTrace(Exception e) {
	    	StringWriter sw = new StringWriter();
	    	PrintWriter pw = new PrintWriter(sw);
	    	e.printStackTrace(pw);
	    	issue("ERROR",null,e.getMessage(),sw.toString());
	    	show();
	    }
        
	    private String xmlToString(Document doc) {
	    	TransformerFactory factory = TransformerFactory.newInstance();
	    	Transformer transformer;
			try {
				transformer = factory.newTransformer();
		    	StringWriter writer = new StringWriter();
		    	Result result = new StreamResult(writer);
		    	Source source = new DOMSource(doc);
		    	transformer.transform(source, result);
		    	writer.close();
		    	String xml = writer.toString();
		    	return xml;
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("cannot convert dom to string");
			}
	    }

}
