package org.rulez.magwas.styledhtml;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class PropertyAdder implements NodeMassager {
    
    private XPath       xpath;
    private VarResolver vars;
    private EventLog    log;
    
    PropertyAdder(XPath xpath, VarResolver vars, EventLog log) {
        this.xpath = xpath;
        this.vars = vars;
        this.log = log;
    }
    
    @Override
    public void function(Element node, Element property, String propname,
            String ancestor) {
        if (null != ancestor) {
            copyAncestorProperty(node, propname, ancestor);
        }
        NodeList pl = property.getElementsByTagName("default");
        int i = 0;
        Element defitem;
        do {
            defitem = defaultItem(pl, i);
            // System.out.println(" defitem=" + defitem);
            if (null != defitem) {
                // System.out.println("trying it out");
                tryDefaultForProperty(node, propname, defitem);
            }
            i++;
        } while (null != defitem);
    }
    
    private boolean tryDefaultForProperty(Element node, String propname,
            Element defitem) {
        int definedproperties = node.getElementsByTagName(propname).getLength();
        if ((definedproperties > 0)
                && (!"true".equals(defitem.getAttribute("always")))) {
            // System.out.println("already have this");
            return true;
        }
        String path = defitem.getAttribute("select");
        boolean multi = false;
        if ("true".equals(defitem.getAttribute("multi"))) {
            multi = true;
        }
        if (!defitem.getAttribute("indirect").isEmpty()) {
            return false;
        }
        vars.put("id", node.getAttribute("parentid"));
        
        // System.out.println("propname="+propname);
        // System.out.println("id="+vars.get("id"));
        // System.out.println("select="+path+", multi="+multi);
        try {
            if (multi) {
                NodeList result = (NodeList) xpath.evaluate(path, node,
                        XPathConstants.NODESET);
                int l = result.getLength();
                if (l == 0) {
                    // System.out.println("empty nodelist");
                    return false;
                }
                for (int i = 0; i < l; i++) {
                    // System.out.println("inserting "+propname+" using path "+path);
                    Element n = (Element) result.item(i);
                    String val = n.getAttribute("name");
                    if (null == val) {
                        // System.out.println("empty nodevalue");
                        return false;
                    }
                    // System.out.println(" value="+val);
                    Element prop = node.getOwnerDocument().createElement(
                            propname);
                    node.appendChild(prop);
                    prop.setTextContent(val);
                    String id;
                    id = n.getAttribute("id");
                    if (null != id) {
                        prop.setAttribute("originid", id);
                    }
                }
                return true;
            } else {
                String result = (String) xpath.evaluate(path, node,
                        XPathConstants.STRING);
                // System.out.println("result="+result);
                if ("".equals(result)) {
                    return false;
                }
                Element prop = node.getOwnerDocument().createElement(propname);
                node.appendChild(prop);
                prop.setTextContent(result);
                return true;
            }
        } catch (XPathExpressionException e) {
            // issue warning, with error message compiled from policy
            log.printStackTrace(e);
        }
        return false;
    }
    
    private void copyAncestorProperty(Element node, String propname,
            String ancestor) {
        /*
         * try to copy ../[ancestor]/[propname] here
         */
        // System.out.println("  copying "+propname+" from "+ancestor);
        String path = "../" + ancestor + "/" + propname;
        NodeList defaults;
        try {
            defaults = (NodeList) xpath.evaluate(path, node,
                    XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            log.printStackTrace(e);
            throw new RuntimeException("bad path" + path);
        }
        int k = defaults.getLength();
        // System.out.println("  path='"+path+"' items:"+k);
        for (int j = 0; j < k; j++) {
            String v = ((Element) defaults.item(j)).getTextContent();
            // System.out.println("  inserting to "+node.getNodeName()+"."+node.getAttribute("parentid")+"("+propname+") from "+ancestor+":"+v);
            Element e = node.getOwnerDocument().createElement(propname);
            e.setTextContent(v);
            node.appendChild(e);
        }
        // return;
    }
    
    private Element defaultItem(NodeList dl, int n) {
        int l = dl.getLength();
        for (int i = 0; i < l; i++) {
            Element d = (Element) dl.item(i);
            if (Integer.parseInt(d.getAttribute("order")) == n) {
                return d;
            }
        }
        return null;
    }
    
}