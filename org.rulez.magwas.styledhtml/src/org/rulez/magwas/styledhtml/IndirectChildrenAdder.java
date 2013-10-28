package org.rulez.magwas.styledhtml;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class IndirectChildrenAdder implements NodeMassager {
    private XPath    xpath;
    private EventLog log;
    
    IndirectChildrenAdder(XPath xpath, EventLog log) {
        this.xpath = xpath;
        this.log = log;
    }
    
    @Override
    public void function(Element node, Element property, String propname,
            String ancestor) {
        NodeList pl = property.getElementsByTagName("default");
        int l = pl.getLength();
        for (int i = 0; i < l; i++) {
            Element d = (Element) pl.item(i);
            String indirectatt = d.getAttribute("indirect");
            if (!indirectatt.isEmpty()) {
                // okay, we have an indirect property here
                System.out.println("indirect prop " + propname + " for "
                        + xml2String(node));
                List<String> parts = Arrays.asList(indirectatt.split("/"));
                List<Element> nl = followPath(node, parts);
                for (Element e : nl) {
                    Element prop = node.getOwnerDocument().createElement(
                            propname);
                    node.appendChild(prop);
                    String val = e.getTextContent();
                    prop.setTextContent(val);
                    String id;
                    id = e.getAttribute("originid");
                    if (null != id) {
                        prop.setAttribute("originid", id);
                    }
                }
            }
        }
    }
    
    private List<Element> followPath(Element node, List<String> parts) {
        // the first step is easy, we have them as direct children
        List<Element> thisfar = new ArrayList<Element>();
        if (parts.isEmpty()) {
            return thisfar;
        }
        String thispart = parts.get(0);
        System.out.println("this part=" + thispart + " element = " + node
                + "id=" + node.getAttribute("parentid"));
        NodeList children = node.getElementsByTagName(thispart);
        thisfar = nodeListToList(children);
        System.out.println("have " + children.getLength() + " nodes for "
                + thispart);
        List<Element> l = followPathFurther(parts.subList(1, parts.size()),
                thisfar);
        return l;
    }
    
    private List<Element> nodeListToList(NodeList list) {
        int length = list.getLength();
        List<Element> copy = new ArrayList<Element>();
        
        for (int n = 0; n < length; ++n) {
            System.out.println("#"
                    + ((Element) list.item(n)).getAttribute("parentid") + ","
                    + ((Element) list.item(n)).getAttribute("originid") + ","
                    + ((Element) list.item(n)).getAttribute("id"));
            copy.add((Element) list.item(n));
        }
        return copy;
    }
    
    private List<Element> followPathFurther(List<String> parts,
            List<Element> thisfar) {
        if (parts.isEmpty()) {
            System.out.println("eos");
            return thisfar;
        }
        String thispart = parts.get(0);
        List<Element> newones = new ArrayList<Element>();
        for (Element currchild : thisfar) {
            String originid = currchild.getAttribute("originid");
            // String expression = "//*[@parentid='" + originid + "' and " +
            // thispart + "]";
            String expression = "//*[@parentid='" + originid + "']/" + thispart;
            System.out.println("origin id=" + originid + " expression="
                    + expression);
            NodeList result;
            try {
                result = (NodeList) xpath.evaluate(expression,
                        currchild.getOwnerDocument(), XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                log.printStackTrace(e);
                throw new RuntimeException("bad path" + expression);
            }
            
            int length = result.getLength();
            
            for (int n = 0; n < length; ++n) {
                Element e = ((Element) result.item(n));
                System.out.println("e=" + xml2String(e));
                newones.add(e);
            }
        }
        System.out.println("returning " + newones.size() + " nodes for "
                + thispart);
        return newones;
    }
    
    private String xml2String(Element node) {
        Transformer transformer;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(node);
            transformer.transform(source, result);
            
            String xmlString = result.getWriter().toString();
            return xmlString;
        } catch (Exception e) {
            e.printStackTrace();
            return "<exception>";
        }
    }
}