package org.rulez.magwas.styledhtmltests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.rulez.magwas.styledhtml.Util;

public class UtilTest {

	@Test
	public void readAndWriteFile() throws IOException {
		String testString="\nÁrvíztűrő\r\n\rTükörfúrógép\r\n";
		File f = File.createTempFile("test", "txt");
		f.deleteOnExit();
		Util.writeStringToFile(testString,f);
		String result = Util.readFileAsString(f);
		assertEquals(testString,result);
		
	}

}
