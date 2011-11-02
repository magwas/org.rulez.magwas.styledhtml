package org.rulez.magwas.enterprise.repository;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

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
            return new AnnotationConfiguration().configure().buildSessionFactory();
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

}
