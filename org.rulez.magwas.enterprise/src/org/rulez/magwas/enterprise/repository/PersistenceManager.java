package org.rulez.magwas.enterprise.repository;

import java.util.List;

import org.eclipse.emf.ecore.EObject;


public interface PersistenceManager {

	public void saveObject(EObject node);
	public EObject load(String versionname);

	Integer addVersion(String version, List<String> basevers, String name,String aclname);
	public void close();
	public void forget();
}
