package org.rulez.magwas.styledhtml;

import org.w3c.dom.Element;

interface NodeMassager {// could have been easier to just cut&paste...
    void function(Element node, Element property, String propname,
            String ancestor);
}