package org.rulez.magwas.enterprise.repository;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Random;

import junit.framework.Assert;


import org.eclipse.swt.widgets.Shell;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.contrib.assumes.Assumes;

public class RepoFactoryTest extends RepoFactory {


	static String repoversion;
	
	
	@BeforeClass
	static public void testConnect() {
		System.out.println("testConnect");
		String password = new PasswordDialog(new Shell()).ask();
		File keystorepath = new File(System.getProperty("user.home"),".postgresql/archi_owner.jks");
		System.setProperty(CertAuthFactory.CONFIG_KEYSTORE_PATH,keystorepath.getAbsolutePath());
		System.setProperty(CertAuthFactory.CONFIG_TRUSTSTORE_PATH,keystorepath.getAbsolutePath());
		System.setProperty(CertAuthFactory.CONFIG_KEYSTORE_PWD,password);
		System.setProperty(CertAuthFactory.CONFIG_TRUSTSTORE_PWD,password);
		Random rand = new Random();
		repoversion = "test_"+rand.nextInt();
	}
	@Test
	@Assumes("testConnect")
	public void testInsertversion() {

		System.out.println("testInsertVersion");
		Session session = getSessionFactory().getCurrentSession();
        session.beginTransaction();

        RepositoryVersion theVersion = new RepositoryVersion();
        theVersion.setId(repoversion);
        theVersion.setDescription("test");
        session.save(theVersion);
        session.getTransaction().commit();

        org.hibernate.classic.Session newsession = getSessionFactory().openSession();

        RepositoryVersion lVersion = new RepositoryVersion();
        newsession.beginTransaction();
        newsession.load(lVersion, repoversion);
        Assert.assertEquals(lVersion.getId(), theVersion.getId());
        Assert.assertEquals(lVersion.getDescription(), theVersion.getDescription());
        Assert.assertEquals(lVersion.getCreatetime().getTime(),theVersion.getCreatetime().getTime());
        newsession.close();
	}
	
	@Test
	@Assumes("testInsertVersion")
	public void testInsertObject() {
		System.out.println("testInsertObject");
		
		Session session = getSessionFactory().getCurrentSession();
		SessionFactory sf = getSessionFactory();
		session.beginTransaction();

		RepositoryObject o = new RepositoryObject();
		o.setDocumentation("doksi");
		o.setVersion(repoversion);
		o.setId("42");
		o.setName("Test object");
		o.setType("Test Object Type");
        session.save(o);
        session.getTransaction().commit();

	}
	
	@Assumes("testInsertVersion")
	public void testDeleteVersion() {
		System.out.println("testDeleteVersion");
	
		Session session = getSessionFactory().getCurrentSession();
        session.beginTransaction();
        Object lVersion = session.load(RepositoryVersion.class, repoversion);
        session.delete(lVersion);
        session.getTransaction().commit();
	}

}
