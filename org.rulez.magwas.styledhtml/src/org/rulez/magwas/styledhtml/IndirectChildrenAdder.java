package org.rulez.magwas.styledhtml;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class IndirectChildrenAdder implements NodeMassager {
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
                String[] parts = indirectatt.split("/");
                for (int j = 0; j < parts.length; j++) {
                }
            }
        }
    }
}