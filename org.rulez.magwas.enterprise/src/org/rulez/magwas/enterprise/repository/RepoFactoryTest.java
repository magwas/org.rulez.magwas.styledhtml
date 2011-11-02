package org.rulez.magwas.enterprise.repository;

import static org.junit.Assert.*;

import java.io.File;

import javax.net.ssl.SSLContext;

import org.eclipse.swt.widgets.Shell;
import org.hibernate.Session;
import org.junit.Test;

public class RepoFactoryTest extends RepoFactory {


	@Test
	public void test() {
		String password = new PasswordDialog(new Shell()).ask();
		System.out.println("keystore path="+System.getProperty("javax.net.ssl.trustStore"));
		File keystorepath = new File(System.getProperty("user.home"),".keystore");
		System.setProperty("javax.net.ssl.trustStore",keystorepath.getAbsolutePath());
		System.setProperty("javax.net.ssl.trustStorePassword", password);
		System.out.println("keystore path="+System.getProperty("javax.net.ssl.trustStore"));
		System.out.println("keystore pwd="+System.getProperty("javax.net.ssl.trustStorePassword"));
		System.setProperty(CertAuthFactory.CONFIG_KEYSTORE_PATH,keystorepath.getAbsolutePath());
		System.setProperty(CertAuthFactory.CONFIG_TRUSTSTORE_PATH,keystorepath.getAbsolutePath());
		System.setProperty(CertAuthFactory.CONFIG_KEYSTORE_PWD,password);
		System.setProperty(CertAuthFactory.CONFIG_TRUSTSTORE_PWD,password);
		
		
		
        Session session = getSessionFactory().getCurrentSession();
        System.out.println("session="+session);
        session.beginTransaction();
        System.out.println("transaction began");

        RepositoryVersion theVersion = new RepositoryVersion();
        theVersion.setId("2");
        theVersion.setDescription("test");
        session.save(theVersion);

        session.getTransaction().commit();
		fail("Not yet implemented");
	}

}
