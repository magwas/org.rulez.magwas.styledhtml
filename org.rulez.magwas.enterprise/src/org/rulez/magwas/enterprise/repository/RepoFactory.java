package org.rulez.magwas.enterprise.repository;

import java.util.Enumeration;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

public class RepoFactory {

    private static final SessionFactory sessionFactory = buildSessionFactory();

    public RepoFactory() {    	
    }
    public RepoFactory(String password) {
    	System.setProperty("javax.net.ssl.trustStorePassword", password);		
	}

    private static SessionFactory buildSessionFactory() {
    	
        try {
            // Create the SessionFactory from hibernate.cfg.xml
        	AnnotationConfiguration cfg = new AnnotationConfiguration();
            SessionFactory factory = cfg.configure().buildSessionFactory();
            //SchemaExport se = new SchemaExport(cfg);
            //se.create(false, true);
            
            return factory;
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    static public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

}
