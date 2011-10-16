package org.rulez.magwas.styledhtml.steps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.dom.Element;

public class Copy extends Step {

	public Copy(StepFactory sf) {
		super(sf);
	}

	@Override
	public void doit(Element arg0, File current) {
		String keep=arg0.getAttribute("keep");
		File sfile = getFileFor(arg0,"source",null,factory.styledir,current);
		if(null == sfile) return;
		File tfile = getFileFor(arg0,"target",sfile.getName(),factory.targetdir,current); //FIXME: default should be more clewer
		if(null == tfile) return;
		if("false".equals(keep)) {
			factory.dontkeep.add(tfile);
		}
		try {
			if(sfile.isDirectory()) {
				copyDir(sfile,tfile);
			} else {
				copyFile(sfile,tfile);
			}
		} catch (IOException e) {
			factory.log.issueError("<copy> failed", "source="+sfile.getAbsolutePath()+"\ntarget="+tfile.getAbsolutePath());
			e.printStackTrace();
		}
		doSubSteps(arg0, tfile);

	}

	private void copyDir(File dir, File targetdir) throws IOException {
    	targetdir.mkdir();
        File[] filelist = dir.listFiles();
        for( File f: filelist ) {
        	if( f.isDirectory()) {
        		File td = new File(targetdir,f.getName());
        		copyDir(f,td);
        	} else {
        		File outputFile = new File(targetdir,f.getName());
        		copyFile(f,outputFile);
        	}
        }
    }

	private static void copyFile(File f1, File f2) throws IOException{

        InputStream in = new FileInputStream(f1);
        OutputStream out = new FileOutputStream(f2);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0){
          out.write(buf, 0, len);
        }
        in.close();
        out.close();

      }

}
