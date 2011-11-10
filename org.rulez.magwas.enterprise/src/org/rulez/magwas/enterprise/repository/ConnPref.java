package org.rulez.magwas.enterprise.repository;

import org.eclipse.jface.preference.IPreferenceStore;
import org.rulez.magwas.enterprise.EnterprisePlugin;

class ConnPref {
	String name;
	String keystore;
	String password;
	String url;
	String username;
	Boolean askpass;
	Boolean postgresssl;
	static IPreferenceStore ps;
	ConnContentProvider provider;
	
	ConnPref (ConnContentProvider cp) {
		ps = EnterprisePlugin.INSTANCE.getPreferenceStore();
		name = null;
		url = "";
		username ="";
		keystore = "";
		password = "";
		askpass = false;
		postgresssl = false;
		provider = cp;
	}
	
	ConnPref (ConnContentProvider cp, String n) {
		name = n;
		ps = EnterprisePlugin.INSTANCE.getPreferenceStore();
		url = ps.getString(RepositoryPreferencesPage.CONNECTIONS_PREF+"."+name+".url");
		username = ps.getString(RepositoryPreferencesPage.CONNECTIONS_PREF+"."+name+".username");
		keystore = ps.getString(RepositoryPreferencesPage.CONNECTIONS_PREF+"."+name+".keystore");
		password = ps.getString(RepositoryPreferencesPage.CONNECTIONS_PREF+"."+name+".password");
		askpass = ps.getBoolean(RepositoryPreferencesPage.CONNECTIONS_PREF+"."+name+".askpass");
		postgresssl = ps.getBoolean(RepositoryPreferencesPage.CONNECTIONS_PREF+"."+name+".postgresssl");
		provider = cp;
	}
	
	public void unsave() {
		ps.setToDefault(RepositoryPreferencesPage.CONNECTIONS_PREF+"."+name+".url");
		ps.setToDefault(RepositoryPreferencesPage.CONNECTIONS_PREF+"."+name+".username");
		ps.setToDefault(RepositoryPreferencesPage.CONNECTIONS_PREF+"."+name+".askpass");    		
		ps.setToDefault(RepositoryPreferencesPage.CONNECTIONS_PREF+"."+name+".postgresssl");    		
		ps.setToDefault(RepositoryPreferencesPage.CONNECTIONS_PREF+"."+name+".keystore");
		ps.setToDefault(RepositoryPreferencesPage.CONNECTIONS_PREF+"."+name+".password");
	}
	public void save() {
		System.out.println("saving "+ this );

		ps.setValue(RepositoryPreferencesPage.CONNECTIONS_PREF+"."+name+".url",url);
		ps.setValue(RepositoryPreferencesPage.CONNECTIONS_PREF+"."+name+".username",username);
		ps.setValue(RepositoryPreferencesPage.CONNECTIONS_PREF+"."+name+".askpass",askpass);    		
		ps.setValue(RepositoryPreferencesPage.CONNECTIONS_PREF+"."+name+".postgresssl",postgresssl);    		
		ps.setValue(RepositoryPreferencesPage.CONNECTIONS_PREF+"."+name+".keystore",keystore);
		ps.setValue(RepositoryPreferencesPage.CONNECTIONS_PREF+"."+name+".password",password);
	}
	
	public Boolean checknames(String origname) {
		return provider.checknames(origname, name);
	}
	
	public void edit(String origname) {
		provider.edit(this,origname);
	}

	public String getName() {
		return name;
	}
	public String getNameAsString() {
		if(null == name) {
			return "";
		}
		return name;
	}
	public void setName(String name) {
		System.out.println("setName("+name+")");
		this.name = name;
	}
	
	public String getUrl() {
		if(null == url) {
			return "jdbc::";
		}
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUsername() {
		if(null == username) {
			return "";
		}
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Boolean getAskpass() {
		return askpass;
	}
	public void setAskpass(Boolean askpass) {
		this.askpass = askpass;
	}

	public Boolean getPostgresssl() {
		return postgresssl;
	}

	public void setPostgresssl(Boolean postgresssl) {
		this.postgresssl = postgresssl;
	}

	public String getKeystore() {
		return keystore;
	}

	public void setKeystore(String keystore) {
		this.keystore = keystore;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}