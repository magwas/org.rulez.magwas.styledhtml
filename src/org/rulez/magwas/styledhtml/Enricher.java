package org.rulez.magwas.styledhtml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.bolton.archimate.editor.utils.HTMLUtils;

public class Enricher{

	private Document policy = null;
	private Document xml = null;
	
	private Enricher(Document infile, File policyfile) {
		xml=infile;
		if ((null != policyfile) && policyfile.exists()) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			try {
				db = dbf.newDocumentBuilder();
				policy = db.parse(policyfile);
			} catch (Exception e) {
				Widgets.tellProblem("Problem loading policy file", e.toString());
	        	e.printStackTrace();
			}
		}
	}
	
	public static void enrichXML(Document infile, File policyfile) {
		Enricher er = new Enricher(infile,policyfile);
		er.enrichDocs();
		er.enrichXML(infile);
	}
	private void enrichDocs() {
    	//enrich the documentation-like parts in the xml
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

	}
    private void enrichXML(Node n) {
    	NodeList nl = n.getChildNodes();
    	int ll=nl.getLength();
    	for(int i=0;i<ll;i++) {
    		Node m = nl.item(i);
           	if (Node.ELEMENT_NODE == m.getNodeType()) {
           		Element e = (Element)m;
           	    enrichXML(e);
           	    System.out.println("node="+e+e.getAttribute("name"));
        		enrichElement((Element) e);    		
           	}
    	}
    }
    
    private void enrichElement(Element m){
    	// copies element node to an identical node which have nodename of the xsi:type attribute
    	String typename = m.getAttribute("xsi:type");
    	if("" != typename ) {
    		xml.renameNode(m, namespaceForType(typename), typename);
    		m.removeAttribute("xsi:type");
    	}
    	
    	List<Element> props = getChildElementsByTagName(m,"property");
    	System.out.println("property children="+ props);
    	int l = props.size();
    	for(int i=0;i<l;i++) {
    		Element p = (Element) props.get(i);
    		if ( m != p.getParentNode()) {
    			continue;
    		}
    		String key = p.getAttribute("key");
    		String value = p.getAttribute("value");
    		System.out.println("property("+key+")="+value);
    		if(key.equals("objectClass")) {
    			System.out.println("creating "+value);
    			Element e = getOrCreateElement(m,value);
    			e.setAttribute("parentid", m.getAttribute("id"));
    			
    		}
    		if(key.contains(":")) {
    			String[] k = key.split(":",2);
    			createSubElement(m,k[0],k[1], value);
    		}
    	}
    }

    private static List<Element> getChildElementsByTagName(Element e, String name) {
    	NodeList nl = e.getChildNodes();
    	int l = nl.getLength();
    	List<Element> out = new ArrayList<Element>();
    	for(int i=0;i<l;i++) {
    		Node c = nl.item(i);
           	if (Node.ELEMENT_NODE == c.getNodeType()) {
           		System.out.println("childnode="+c);
           		if(c.getNodeName().equals(name)) {
           			out.add((Element) c);
           		}
           	}    		
    	}
    	return out;
    }
    private void createSubElement(Element m, String el, String propname, String value) {
    	Element obj = getOrCreateElement(m,el);
    	obj.setAttribute("parentid", m.getAttribute("id"));
    	Element prop = getOrCreateElement(obj,propname);
    	prop.setTextContent(value);
    }
    private Element getOrCreateElement(Element m, String value) {
    	List<Element> nl = getChildElementsByTagName(m,value);
    	System.out.println("found "+nl.size()+" "+value+" children");
    	if(0 == nl.size()) {
        	System.out.println("creating it");
    		Element e = xml.createElement(value);
    		m.appendChild(e);
    		return e;
    	}
    	if(1 == nl.size()) {
        	System.out.println("getting it");
    		return nl.get(0);
        }
    	Widgets.tellProblem("property problem", "objectClass name '"+value+"' is reserved");
    	return null;
    }
    
    private static String namespaceForType(String tname) {
    	//FIXME use the policy to figure this out
    	String xmlns = tname.split(":")[0];
    	if(xmlns.equals("archimate")) {
    		return "http://www.bolton.ac.uk/archimate";
    	}
    	return "http://namespaces.local/"+xmlns;
    }
    private static void parseCharsAndLinks(Element n) {
        // Escape chars
    	Document d = n.getOwnerDocument();
    	String s = n.getTextContent();
    	n.setTextContent("");
        
        String[] ss = s.split("(\r\n|\r|\n)");
        
        for(String sss : ss) {
        	parseLinks(sss,n);
        	n.appendChild(d.createElement("br"));
        }
        
    }
    
    private static void parseLinks(String s, Node parent) {
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
}

