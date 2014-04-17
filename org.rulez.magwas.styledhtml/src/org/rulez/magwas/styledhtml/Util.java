package org.rulez.magwas.styledhtml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Util {
    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
    
    public static String now() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
        
    }
    
    public static String xml2String(Element node) {
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
    
    public static Document createXmlDocumentFromResource(Object instance,
            String name) throws ParserConfigurationException, SAXException,
            IOException, URISyntaxException {
        String content = readFile(instance, name);
        return createXmlDocumentFromString(content);
    }
    
    public static String readFile(Object instance, String filename)
            throws UnsupportedEncodingException, IOException,
            URISyntaxException {
        java.net.URL url = instance.getClass().getResource(filename);
        if(null == url)
        	throw new IOException(String.format("no such resource: %s",filename));
        String fname = url.getFile();
		String xml = readFileAsString(new File(fname));
        return xml;
    }

    public static String readFileAsString(File file) throws IOException {
        byte[] buffer = new byte[(int) file.length()];
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            inputStream.read(buffer);
        }
        finally {
            if(inputStream != null) {
                inputStream.close();
            }
        }
        return new String(buffer);
    }

    public static void writeStringToFile(String str, File file) throws IOException {
    	byte[] buffer = new byte[str.length()];
    	buffer = str.getBytes();
    	FileOutputStream outputStream = new FileOutputStream(file);
    	outputStream.write(buffer);
    	outputStream.close();
    }
    
    public static Document createXmlDocumentFromString(String xmlString)
            throws ParserConfigurationException, SAXException, IOException {
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        factory.setNamespaceAware(true);
        factory.setCoalescing(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setIgnoringComments(true);
        builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(
                xmlString)));
        return document;
    }
    
}
