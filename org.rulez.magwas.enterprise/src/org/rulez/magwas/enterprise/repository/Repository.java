package org.rulez.magwas.enterprise.repository;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.widgets.Shell;
import org.postgresql.jdbc2.AbstractJdbc2Connection;

import uk.ac.bolton.archimate.model.IAccessRelationship;
import uk.ac.bolton.archimate.model.IArchimateDiagramModel;
import uk.ac.bolton.archimate.model.IArchimateElement;
import uk.ac.bolton.archimate.model.IArchimateModel;
import uk.ac.bolton.archimate.model.IBounds;
import uk.ac.bolton.archimate.model.IDiagramModel;
import uk.ac.bolton.archimate.model.IDiagramModelArchimateConnection;
import uk.ac.bolton.archimate.model.IDiagramModelArchimateObject;
import uk.ac.bolton.archimate.model.IDiagramModelBendpoint;
import uk.ac.bolton.archimate.model.IDiagramModelConnection;
import uk.ac.bolton.archimate.model.IDiagramModelContainer;
import uk.ac.bolton.archimate.model.IDiagramModelObject;
import uk.ac.bolton.archimate.model.IDiagramModelReference;
import uk.ac.bolton.archimate.model.IDocumentable;
import uk.ac.bolton.archimate.model.IFolder;
import uk.ac.bolton.archimate.model.IFolderContainer;
import uk.ac.bolton.archimate.model.IFontAttribute;
import uk.ac.bolton.archimate.model.IIdentifier;
import uk.ac.bolton.archimate.model.IInterfaceElement;
import uk.ac.bolton.archimate.model.INameable;
import uk.ac.bolton.archimate.model.IProperties;
import uk.ac.bolton.archimate.model.IProperty;
import uk.ac.bolton.archimate.model.IRelationship;
import uk.ac.bolton.archimate.model.ITextContent;

public class Repository {

	Connection con;
	private List<String> basevers;
	private int versionid;
	
	
	public Repository(String reponame) throws SQLException, ClassNotFoundException, ConfigurationException {
		ConnContentProvider cp = new ConnContentProvider();
		ConnPref pref = cp.getPref(reponame);
		if(null == pref) {
			throw new ConfigurationException("no such connection: "+reponame);
		}
		String password;
		if(pref.getAskpass()) {
			password = new PasswordDialog(new Shell()).ask();
		} else {
			password = pref.getPassword();
		}

		System.setProperty(CertAuthFactory.CONFIG_KEYSTORE_PATH, pref.getKeystore());
		System.setProperty(CertAuthFactory.CONFIG_KEYSTORE_PWD,password);
		System.setProperty(CertAuthFactory.CONFIG_TRUSTSTORE_PATH, pref.getKeystore());
		System.setProperty(CertAuthFactory.CONFIG_TRUSTSTORE_PWD,password);

		Class.forName("org.postgresql.Driver");	//FIXME have preference for it, and move it to module initialisation
		con = DriverManager.getConnection(pref.getUrl(), pref.getUsername(), password);
		((AbstractJdbc2Connection)con).autoCommit=false;
		PreparedStatement psSetRole = con.prepareStatement("set role "+pref.getRole());
		//psSetRole.setString(1,pref.getRole());
		System.out.println("executing " + psSetRole);
		psSetRole.execute();
		psSetRole.close();
	}
	
	public void close() {
		System.out.println("closing "+con);
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * rollback and close
	 */
	public void forget() {
		
		try {
			con.rollback();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	protected String getModelProperty(IArchimateModel model, String propname) {
		EList<IProperty> props = model.getProperties();
		String value = null;
		for(IProperty prop : props) {
			if(prop.getKey() == propname) {
				value = prop.getValue();
			}
		}
		return value;
	}
	
	protected String getModelVersion(IArchimateModel model) {
		return getModelProperty(model, "modelVersion");
	}

	protected List<String> getBaseVersions(IArchimateModel model) {
		EList<IProperty> props = model.getProperties();
		List<String> vers = new ArrayList<String>();
		for(IProperty prop : props) {
			if(prop.getKey() == "baseVersion") {
				vers.add(prop.getValue());
			}
		}
		return vers;
	}
	
	protected int addVersion(String name, String description, String aclname) throws SQLException {
		int acl = getAclByName(aclname);
		PreparedStatement psAddVersion = con.prepareStatement("insert into version_view (name,description, acl) values (?,?,?)");
		psAddVersion.setString(1,name);
		psAddVersion.setString(2,description);
		psAddVersion.setInt(3,acl);
		psAddVersion.execute();
		PreparedStatement psGetVersionByName = con.prepareStatement("select id from version_view where name = ?");
		psGetVersionByName.setString(1, name);
		ResultSet rs = psGetVersionByName.executeQuery();
		boolean havenext = rs.next();
		assert(havenext != false);
		System.out.println("have next "+havenext);
		int r = rs.getInt("id");
		System.out.println("result="+r);
		rs.close();
		return r;
	}
	
	private int getAclByName(String name) throws SQLException {
		PreparedStatement psGetAclByName = con.prepareStatement("select id from acl where name = ?");
		psGetAclByName.setString(1, name);
		ResultSet rs = psGetAclByName.executeQuery();
		boolean havenext = rs.next();
		assert(havenext != false);
		int r = rs.getInt("id");
		rs.close();
		return r;
	}
	
	private void addBaseVersions(int currversion,List<String> baseversions) throws SQLException {
		PreparedStatement psAddBaseVersion = con.prepareStatement("insert into version_hierarchy_view (parent, child) values (?,?)");
		for (String bv: baseversions) {
			int parent = getVersionByName(bv);
			psAddBaseVersion.setInt(1, parent);
			psAddBaseVersion.setInt(2, currversion);
			psAddBaseVersion.execute();
		}
		psAddBaseVersion.close();
	}
	
	private int getVersionByName(String name) throws SQLException {
		PreparedStatement psGetAclByName = con.prepareStatement("select id from version_view where name = ?");
		psGetAclByName.setString(1, name);
		ResultSet rs = psGetAclByName.executeQuery();
		boolean havenext = rs.next();
		assert(havenext != false);
		int r = rs.getInt("id");
		rs.close();
		return r;
	}

	private void setInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
		if(null == value) {
			stmt.setNull(index, Types.INTEGER);
		} else {
			stmt.setInt(index, value);
		}
	}

	/*
	 * trace the usage of overloaded variables. This is a debug tool;
	 */
	List<String> ovrvars;
	private void qinit() {
		ovrvars = new ArrayList<String>();
	}
	private void q(String name) {
		if (ovrvars.contains(name)) {
			throw new Error("multiple override for "+ name+":"+ovrvars);
		}
	}
	private void addNode(EObject node, String parent) {
		qinit();
		String id=null;
		String name=null;
		String documentation=null;
		String type=null;
		Integer subtype=null; //FIXME was textalignment
		String source=null,target=null, element=null; //these are references to other objects
		String font=null, fontcolor=null,fillcolor=null; //graphical properties
		Integer x1=null, x2=null, y1=null, y2=null;
		String key=null, value=null;
		q("type");
		type = node.eClass().getName();
		System.out.println("type=" +type);
		if(node instanceof IIdentifier){
			q("id");
			id = ((IIdentifier) node).getId();
		}
		if (null == parent) {
			assert(null != id);
			q("parent");
			parent = id;
		}
		if(node instanceof INameable) {
			q("name");
			name = ((INameable) node).getName();
		}
		
		if(node instanceof IDocumentable) {
			q("documentation");
			documentation = ((IDocumentable) node).getDocumentation();
		}
		if(node instanceof ITextContent) {
			q("documentation");
			documentation = ((ITextContent) node).getContent();
		}
		
		if(node instanceof IArchimateModel) {
			q("documentation");
			q("modelversion");
			documentation = ((IArchimateModel) node).getPurpose();
			String modelversion = ((IArchimateModel) node).getVersion();
			if(! modelversion.equals("2.0.0")) {
				//FIXME appropriate warning here
			}
		}
		if(node instanceof IProperty) {
			q("key");
			q("value");
			key = ((IProperty) node).getKey();
			value = ((IProperty) node).getValue();
		}
		if(node instanceof IBounds) {
			q("x1");
			q("x2");
			q("y1");
			q("y2");
			// FIXME move to properties
			x1 = ((IBounds) node).getX();
			y1 = ((IBounds) node).getY();
			x2 = ((IBounds) node).getWidth();
			y2 = ((IBounds) node).getHeight();
		}
		if(node instanceof IArchimateDiagramModel) {
			q("subtype");
			subtype = ((IArchimateDiagramModel) node).getViewpoint();
		}
		if(node instanceof IInterfaceElement) {
			q("subtype");
			subtype  = ((IInterfaceElement) node).getInterfaceType();
		}
		if(node instanceof IDiagramModel) {
			q("subtype");
			subtype = ((IDiagramModel) node).getConnectionRouterType();
		}
		if(node instanceof IDiagramModelArchimateConnection) {
			q("element");
			element = ((IDiagramModelArchimateConnection) node).getRelationship().getId();
		}
		if(node instanceof IDiagramModelArchimateObject) {
			q("subtype");
			q("element");
			element = ((IDiagramModelArchimateObject) node).getArchimateElement().getId();
			subtype = ((IDiagramModelArchimateObject) node).getType();
		}
		if(node instanceof IDiagramModelBendpoint) {
			q("x1");
			q("x2");
			q("y1");
			q("y2");
			//FIXME figure it out float weight = ((IDiagramModelBendpoint) node).getWeight();
			x1 = ((IDiagramModelBendpoint) node).getStartX();
			y1 = ((IDiagramModelBendpoint) node).getStartY();
			x2 = ((IDiagramModelBendpoint) node).getEndX();
			y2 = ((IDiagramModelBendpoint) node).getEndY();			
		}
		if(node instanceof IFontAttribute) {
			q("font");
			q("fontcolor");
			q("subtype");
			font = ((IFontAttribute) node).getFont();
			fontcolor = ((IFontAttribute) node).getFontColor();
			subtype = ((IFontAttribute) node).getTextAlignment();// FIXME this is textalignment, wrong in sql already			
		}
		
		if(node instanceof IDiagramModelObject) {
			//FIXME IBounds bounds = ((IDiagramModelObject) node).getBounds();
			EList<IDiagramModelConnection> scs = ((IDiagramModelObject) node).getSourceConnections();
			for (IDiagramModelConnection sc: scs) {
				//FIXME addNode(sc,id);
			}
			EList<IDiagramModelConnection> tcs = ((IDiagramModelObject) node).getTargetConnections();
			for (IDiagramModelConnection tc : tcs) {
				//FIXME addNode(tc,id);		
			}
			q("fillcolor");
			fillcolor = ((IDiagramModelObject) node).getFillColor();
		}

		if(node instanceof IDiagramModelReference) {
			q("element");
			element = ((IDiagramModelReference) node).getReferencedModel().getId();
		}
		
		if(node instanceof IRelationship) {
			q("source");
			q("target");
			source = ((IRelationship) node).getSource().getId();
			target = ((IRelationship) node).getTarget().getId();
			//FIXME: assure that the referenced elements are stored in the db before we store this one.
		}
		if(node instanceof IAccessRelationship) {
			q("subtype");
			subtype = ((IAccessRelationship) node).getAccessType();
		}

			/*
		 *          here comes the multiple-value stuff
		 */
		if(node instanceof IDiagramModelConnection) {
			q("source");
			q("target");
			q("documentation");
			q("type");
			documentation = ((IDiagramModelConnection) node).getText();
			//FIXME int textpos = ((IDiagramModelConnection) node).getTextPosition();
			source = ((IDiagramModelConnection) node).getSource().getId();
			target = ((IDiagramModelConnection) node).getTarget().getId();
			EList<IDiagramModelBendpoint> bendpoints = ((IDiagramModelConnection) node).getBendpoints();
			for (IDiagramModelBendpoint bp: bendpoints) {
				// FIXME addNode(bp,id);
			}
			// FIXME int linewith = ((IDiagramModelConnection) node).getLineWidth();
			// FIXME String linecolor = ((IDiagramModelConnection) node).getLineColor()
			type = ((IDiagramModelConnection) node).getType();
		}
		
		if(node instanceof IProperties) {
			EList<IProperty> pl = ((IProperties) node).getProperties();
			for (IProperty prop : pl) {
				// FIXME addNode(prop,id);
			}
		}
		if(node instanceof IDiagramModelContainer) {
			EList<IDiagramModelObject> children = ((IDiagramModelContainer) node).getChildren();
			for (IDiagramModelObject child : children) {
				// FIXME addNode(child,id);
			}
		}
	
		if(node instanceof IFolderContainer) {
			EList<IFolder> fl = ((IFolderContainer) node).getFolders();
			for (IFolder folder : fl) {
				// FIXME we are not recursing yet addNode(folder,id);
			}
		}
		if(node instanceof IFolder) {
			q("id");
			EList<IFolder> fl = ((IFolder) node).getFolders();
			for (IFolder folder : fl) {
				// FIXME we are not recursing yet addNode(folder,id);
			}
			System.out.println("foldertype name="+ ((IFolder) node).getType().getName());
			System.out.println("foldertype literal="+ ((IFolder) node).getType().getLiteral());
			id = ((IFolder) node).getType().getName(); //This is intentional, to make merging models easier. FIXME make sure not getLiteral is the right method
		}
		if (null != id) {
			//object
			try {
				PreparedStatement psInserObject = con.prepareStatement(
						"insert into object_view " +
						"(version, id, parent, name, documentation, type, source, target, element, font, fontcolor, subtype, fillcolor)" +
						"values (?,?,?,?,?,?,?,?,?,?,?,?,?)");
				psInserObject.setInt(1, versionid);
				psInserObject.setString(2, id);
				psInserObject.setString(3, parent);
				psInserObject.setString(4, name);
				psInserObject.setString(5, documentation);
				psInserObject.setString(6, type);
				psInserObject.setString(7, source);
				psInserObject.setString(8, target);
				psInserObject.setString(9, element);
				psInserObject.setString(10, font);
				psInserObject.setString(11, fontcolor);
				setInt(psInserObject,12,subtype);
				psInserObject.setString(13, fillcolor);
				psInserObject.execute();
				psInserObject.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		} else {
			//property
			try {
				PreparedStatement psInserObject = con.prepareStatement(
						"insert into property_view " +
						"(version, parent, type, key, value, x1, y1, x2, y2)" +
						"values (?,?,?,?,?,?,?,?,?,?,?,?,?)");
				psInserObject.setInt(1, versionid);
				psInserObject.setString(2, parent);
				psInserObject.setString(3, type);
				psInserObject.setString(4, key);
				psInserObject.setString(5, value);
				setInt(psInserObject,6,x1);
				setInt(psInserObject,7,y1);
				setInt(psInserObject,8,x2);
				setInt(psInserObject,9,y2);
				psInserObject.execute();
				psInserObject.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}
	
	public void checkin(IArchimateModel model) throws SQLException {
		String version = getModelVersion(model);
		String name = model.getName();
		String aclname = getModelProperty(model,"modelAcl");
		basevers = getBaseVersions(model);
		versionid = addVersion(version, name,aclname);
		addBaseVersions(versionid,basevers);
		addNode(model,null);
	}

}
