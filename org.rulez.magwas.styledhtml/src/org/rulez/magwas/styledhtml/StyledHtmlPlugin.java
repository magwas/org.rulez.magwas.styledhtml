package org.rulez.magwas.styledhtml;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The activator class controls the plug-in life cycle
 */
public class StyledHtmlPlugin extends AbstractUIPlugin {

    /**
     * ID of the plug-in
     */
    public static final String PLUGIN_ID = "org.rulez.magwas.styledhtml";

    /**
     * The shared instance
     */
    public static StyledHtmlPlugin INSTANCE;

    /**
     * The constructor.
     */
    public StyledHtmlPlugin() {
    	 System.setProperty("javax.xml.transform.TransformerFactory",
    				 "net.sf.saxon.TransformerFactoryImpl");
        INSTANCE = this;
    }

}
