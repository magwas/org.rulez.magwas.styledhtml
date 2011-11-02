package org.rulez.magwas.enterprise;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class EnterprisePlugin extends AbstractUIPlugin {

    /**
     * ID of the plug-in
     */
    public static final String PLUGIN_ID = "org.rulez.magwas.enterprise";

    /**
     * The shared instance
     */
    public static EnterprisePlugin INSTANCE;

    /**
     * The constructor.
     */
    public EnterprisePlugin() {
        INSTANCE = this;
    }

}
