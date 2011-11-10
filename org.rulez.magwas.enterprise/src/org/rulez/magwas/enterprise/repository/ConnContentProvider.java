package org.rulez.magwas.enterprise.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.rulez.magwas.enterprise.EnterprisePlugin;


class ConnContentProvider implements IStructuredContentProvider {
	
	static List<String> connList=null;
	static List<ConnPref> ConnPrefList=null;
	TableViewer tv;
	List<ConnPref> current = null;

	public ConnContentProvider() {
		super();
		IPreferenceStore ps = EnterprisePlugin.INSTANCE.getPreferenceStore();
		String connListS = ps.getString(RepositoryPreferencesPage.CONNECTIONS_PREF+".list");
		
		if(null != connListS) {
			connList = splitString(connListS);
		} else {
			connList= new ArrayList<String>();
		}
		ConnPrefList = new ArrayList<ConnPref>();
		for (String name :connList) {
			ConnPrefList.add(new ConnPref(this,name));
		}
	}

	private List<String> splitString(String s) {
		ArrayList<String> ss = new ArrayList<String>(Arrays.asList(s.split(":")));
		if(ss.contains("")) {
			ss.remove("");
		}
		return ss;
	}
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		System.out.println("input changed from "+oldInput+" to "+ newInput+ " in "+ v);
		tv=(TableViewer) v;
	}

	public Object[] getElements(Object parent) {
		return ConnPrefList.toArray();
	}

    static String join( List<String> list , String replacement  ) {
    	if (list.isEmpty()) {
    		return "";
    	}
        StringBuilder b = new StringBuilder();
        for( String item: list ) { 
            b.append( replacement ).append( item );
        }
        return b.toString().substring( replacement.length() );
    }

	public void removeSelected() {
		System.out.println("removeselected()");
		ISelection sel = tv.getSelection();
		assert(sel instanceof IStructuredSelection);
		@SuppressWarnings("unchecked")
		Iterator<ConnPref> iter = ((IStructuredSelection)sel).iterator();
		while(iter.hasNext()){
			ConnPref a = iter.next();
			System.out.println("removing"+a);
			remove(a);
			saveConnlist();
			tv.refresh();
		}
	}

	public void remove(ConnPref cp) {
		connList.remove(cp.getName());
		ConnPrefList.remove(cp);
		cp.unsave();
		saveConnlist();
		if(tv != null) {
			tv.refresh();
		}
	}
	
	public void add(ConnPref cp) {
		connList.add(cp.getName());
		ConnPrefList.add(cp);
		cp.save();
		saveConnlist();
		if(tv != null) {
			tv.refresh();
		}
	}
	public boolean checknames(String oname,String newname) {
		System.out.println("checknames("+oname+","+newname+")");
		if (newname.contains(":")) {
			System.out.println("contains :");
			return false;
		}
		if ((oname == null) || oname.equals(newname) ) {
			System.out.println("same or null, ok");
			return true;
		}
		if (newname.isEmpty()||connList.contains(newname)) {
			System.out.println("empty or in list");
			return false;
		}
		System.out.println("ok");
		return true;
	}

	/*
	 * Edit a connection.
	 */
	public void edit(ConnPref cp, String oname) {
		if(null == oname) {
			add(cp);
		} else {
			remove(cp);
			add(cp);
		}
		saveConnlist();
	}
	
	private void saveConnlist() {		
		IPreferenceStore ps = EnterprisePlugin.INSTANCE.getPreferenceStore();
		String cl = join(connList, ":");
		System.out.println("connlist = "+cl);
		ps.setValue(RepositoryPreferencesPage.CONNECTIONS_PREF+".list",cl);
	}

	public ConnPref getPref(String name) {
		int i = connList.indexOf(name);
		System.out.println(name + "is at "+i);
		if(-1 == i) {
			return null;
		}
		return(ConnPrefList.get(i));
	}
	public void dispose() {
	}

}