package org.rulez.magwas.styledhtml;

import java.io.File;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import uk.ac.bolton.archimate.editor.model.IEditorModelManager;
import uk.ac.bolton.archimate.model.IArchimateModel;

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

    void runcmd() {
  		 EventLog log = new EventLog("Styled export");
  		 log.issueInfo("starting", "Styledhtml");
         String[] args = Platform.getCommandLineArgs();
      	 IArchimateModel model = null;
       	 File targetdir = null;
       	 int i = 0;
       	 while (i < args.length)
       	 {
      		 log.issueInfo("next arg:", args[i]);
           	 if (args[i].equals("-load"))
           	 {
               	 i++;
               	 String modelpath = args[i];
          		 log.issueInfo("loading model", modelpath);
               	 File file = new File(modelpath);
               	 model = getModel(file);
               	 if (null == model) {
               		 model = IEditorModelManager.INSTANCE.openModel(file);
               	 }
		   	 }
           	 if (args[i].equals("-targetdir"))
           	 {
               	 i++;
               	 String tpath = args[i];
          		 log.issueInfo("setting targetpath", tpath);
               	 targetdir = new File(tpath);
           	 }
           	 if (args[i].equals("-runstyle"))
           	 {
               	 i++;
               	 String stylepath = args[i];
          		 log.issueInfo("running style", stylepath);
               	 if(model != null){
               		 if(targetdir == null) {
                       	 StyledHtml.export(model, stylepath, log);
               		 } else {
                       	 StyledHtml.export(model, stylepath, log, targetdir);            			 
               		 }
               	 } else {
               		 log.issueInfo("cannot run style "+stylepath, "no model");
               	 }
           	 }
           	 if (args[i].equals("-exit"))
           	 {
          		 log.issueInfo("exiting", "nicely");
           		 PlatformUI.getWorkbench().close();
           	 }
           	 i++;
       	 }

    }    

    IArchimateModel getModel(File file) {
       	for(IArchimateModel m : IEditorModelManager.INSTANCE.getModels()) {
            if(file.equals(m.getFile())) {
                return m;
            }
        }
       	return null;
    }

    
    //@override
    protected void refreshPluginActions() {
        // If the workbench is not started yet, or is no longer running, do nothing.
        if (!PlatformUI.isWorkbenchRunning()) {
			return;
		}

        // startup() is not guaranteed to be called in the UI thread,
        // but refreshPluginActions must run in the UI thread, 
        // so use asyncExec.  See bug 6623 for more details.
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                runcmd();
            }
        });
    }
}
