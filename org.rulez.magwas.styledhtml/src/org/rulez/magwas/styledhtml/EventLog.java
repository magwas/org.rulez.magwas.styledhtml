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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.widgets.Display;
import org.rulez.magwas.archimate.editor.browser.BrowserEditorInput;
import org.rulez.magwas.archimate.editor.browser.IBrowserEditor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


import uk.ac.bolton.archimate.editor.model.IEditorModelManager;
import uk.ac.bolton.archimate.editor.ui.services.EditorManager;
import uk.ac.bolton.archimate.editor.ui.services.UIRequestManager;
import uk.ac.bolton.archimate.editor.views.tree.TreeSelectionRequest;

import uk.ac.bolton.archimate.model.IArchimateModel;

import uk.ac.bolton.archimate.model.util.ArchimateModelUtils;

public class EventLog {

	private IBrowserEditor editor;
	private final Browser browser;
	private Document messages;
	private Node msg;
	
	
	public EventLog(String title) {
    	System.out.println("EventLog");
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
   					 //System.out.println("trying to open location "+loc);
   					 if(loc.startsWith("archimate://")){
   						 String[] ids = loc.split("//")[1].split("/");
   						 //System.out.println("modelid="+ids[0]);
   						 //System.out.println("elementid="+ids[1]);
   						 focusElement(ids[0],ids[1]);
   					 }
   					 event.doit = false;
   				 }
   				 public void changed(LocationEvent event) {
   				 }
   				 
   			 });
   	    		}   
   		 });    	
  	}
	
	private void focusElement(String modelid, String elemid) {
        for(IArchimateModel model : IEditorModelManager.INSTANCE.getModels()) {
        	String thismodelid = model.getId();
        	System.out.println("this="+thismodelid);
        	if(thismodelid.equals(modelid)) {
                EObject theElementToSelect =  ArchimateModelUtils.getObjectByID(model, elemid);
                UIRequestManager.INSTANCE.fireRequest(new TreeSelectionRequest(this, new StructuredSelection(theElementToSelect), true));
                return;
         	}
        }
	}

	   private void issue(String qualifier,IArchimateModel model, Element node, String text, String detail) {
		    Node tr = messages.createElement("tr");
	    	msg.appendChild(tr);
	    	Node qtd = messages.createElement("td");
	    	qtd.setTextContent(qualifier);
	    	tr.appendChild(qtd);
	    	Node ttd = messages.createElement("td");
	    	ttd.setTextContent(text);
	    	tr.appendChild(ttd);
	    	Node ltd = messages.createElement("td");
	    	if((null != node) && (null != model)) {
		    	Node location = messages.createElement("a");
	    		((Element)location).setAttribute("href","archimate://"+model.getId()+"/"+node.getAttribute("id"));
	    		location.setTextContent(" at "+node.getAttribute("name"));
		    	ltd.appendChild(location);
	    	} else {
	    		((Element)ltd).setAttribute("colspan", "2");
	    	}
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
	   
	    public void issueWarning(IArchimateModel model,Element node, String text, String detail) {
	    	issue("WARNING",model,node,text,detail);
	    }    
	    public void issueError(IArchimateModel model,Element node, String text, String detail) {
	    	issue("ERROR",model,node,text,detail);
	    }
	    
	    public void printStackTrace(Exception e) {
	    	StringWriter sw = new StringWriter();
	    	PrintWriter pw = new PrintWriter(sw);
	    	e.printStackTrace(pw);    	
	    	e.printStackTrace();
	    	String msg = e.getMessage();
	    	String trace = sw.toString();
	    	issueError(null,null,msg,trace);
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
