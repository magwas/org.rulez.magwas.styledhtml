package org.rulez.magwas.enterprise.repository;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.swt.widgets.Shell;
import org.postgresql.PGStatement;
import org.postgresql.jdbc2.AbstractJdbc2Connection;

import uk.ac.bolton.archimate.model.FolderType;
import uk.ac.bolton.archimate.model.IArchimateFactory;
import uk.ac.bolton.archimate.model.IArchimatePackage;
import uk.ac.bolton.archimate.model.IBounds;
import uk.ac.bolton.archimate.model.IDiagramModelBendpoint;
import uk.ac.bolton.archimate.model.IIdentifier;
import uk.ac.bolton.archimate.model.IProperty;

public class SQLManager implements PersistenceManager {

	protected Connection con;
	private String conname;
	private Repository repo;
	private int modelversion;
	private PreparedStatement psInsertObjectAttribute;
	private PreparedStatement psAddVersion;
	private PreparedStatement psGetVersionByName;
	private PreparedStatement psGetAclByName;
	private PreparedStatement psAddBaseVersion;
	//private PreparedStatement psSelectObjectAttributes;
	//private PreparedStatement psSelectChildren;
	private PreparedStatement psSelectRoot;
	private PreparedStatement psSelectObject;
	private PreparedStatement psInsertObject;

	protected SQLManager() {
		super();
	}
	protected void __init__(Repository repo) throws ConfigurationException {
		ConnContentProvider cp = new ConnContentProvider();
		this.repo=repo;
		conname=repo.getConname();
		ConnPref pref = cp.getPref(conname);
		if(null == pref) {
			throw new ConfigurationException("no such connection: "+conname);
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

		try {
			Class.forName("org.postgresql.Driver"); 	//FIXME have preference for it, and move it to module initialisation		
			con = DriverManager.getConnection(pref.getUrl(), pref.getUsername(), password);
			((AbstractJdbc2Connection)con).autoCommit=false;
			PreparedStatement psSetRole = con.prepareStatement("set role "+pref.getRole());
			//psSetRole.setString(1,pref.getRole());
			//System.out.println("executing " + psSetRole);
			psSetRole.execute();
			psSetRole.close();
			psGetVersionByName = con.prepareStatement("select id from version_view where name = ?");
			psGetAclByName = con.prepareStatement("select id from acl where name = ?");
			psSelectRoot = con.prepareStatement("select parent,type from object_view where version=? and id=parent");
			psSelectObject = con.prepareStatement("select parent,type from object_view where version=? and id=?");
			psAddVersion = con.prepareStatement("insert into version_view (name,description, acl) values (?,?,?)");
			psAddBaseVersion = con.prepareStatement("insert into version_hierarchy_view (parent, child) values (?,?)");
			psInsertObject = con.prepareStatement("insert into object_view " +
					"(version, id, parent, type)" +
					" values (?,?,?,?)");
			psInsertObjectAttribute = con.prepareStatement("insert into object_attribute_view " +
					"(version, parent, name, value)" +
					" values (?,?,?,?)");
			((PGStatement)psInsertObject).setPrepareThreshold(1);
			((PGStatement)psInsertObjectAttribute).setPrepareThreshold(1);



		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new ConfigurationException("cannot find jdbc driver "+conname);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ConfigurationException("error with database: "+conname);
		}

	
	}
	
	public SQLManager(Repository repo) throws ConfigurationException {
		super();
		__init__(repo);
		}
	
	public void close() {
		//System.out.println("closing "+con);
		try {
			psInsertObject.executeBatch();
			psInsertObjectAttribute.executeBatch();
			con.commit();
			con.close();
		} catch (SQLException e) {
			System.out.println(e.getNextException());
			e.printStackTrace();
		}
	}
	
	public void forget() {
		
		try {
			con.rollback();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	

	
	public Integer addVersion(String name, List<String> basevers, String description, String aclname) {
		int acl;
		try {
			acl = getAclByName(aclname);
			psAddVersion.setString(1,name);
			psAddVersion.setString(2,description);
			psAddVersion.setInt(3,acl);
			psAddVersion.execute();
			psGetVersionByName.setString(1, name);
			ResultSet rs = psGetVersionByName.executeQuery();
			boolean havenext = rs.next();
			assert(havenext != false);
			//System.out.println("have next "+havenext);
			int r = rs.getInt("id");
			//System.out.println("result="+r);
			rs.close();
			addBaseVersions(r,basevers);
			modelversion = r;
			return r;

		} catch (SQLException e) {
			e.printStackTrace();
			throw new Error("error with database");
		}
	}
	
	
	private int getAclByName(String name) throws SQLException {
		psGetAclByName.setString(1, name);
		ResultSet rs = psGetAclByName.executeQuery();
		boolean havenext = rs.next();
		assert(havenext != false);
		int r = rs.getInt("id");
		rs.close();
		return r;
	}
	
	private void addBaseVersions(int currversion,List<String> baseversions) throws SQLException {
		for (String bv: baseversions) {
			int parent = getVersionByName(bv);
			psAddBaseVersion.setInt(1, parent);
			psAddBaseVersion.setInt(2, currversion);
			psAddBaseVersion.execute();
		}
	}
	
	private int getVersionByName(String name) throws SQLException {
		psGetVersionByName.setString(1, name);
		//System.out.println("executing"+psGetVersionByame+";");
		ResultSet rs = psGetVersionByName.executeQuery();
		boolean havenext = rs.next();
		assert(havenext != false);
		int r = rs.getInt("id");
		rs.close();
		return r;
	}

	private boolean isobject(String attname) { //FIXME should be a db column instead
		return attname.equals("archimateModel") ||
				attname.equals(".folders")||
				attname.equals(".elements")||
				attname.equals(".children")||
				attname.equals("source") ||
				attname.equals("archimateElement") ||
				attname.equals("target")||
				attname.equals(".sourceConnections")||
				attname.equals(".targetConnections")||
				attname.equals("relationship");
	}
	private String serialize(String attname,String objtype, Object value) {
		System.out.println("serializing ("+attname+","+objtype+","+value+")");
		if (isobject(attname)) {
			saveObject((EObject) value);
			return ((IIdentifier) value).getId();
		}
		if (attname.equals(".properties")) {
			return ((IProperty)value).getKey()+"="+((IProperty)value).getValue();
		}
		if (attname.equals("bounds")) {
			return ((IBounds)value).getX()+","+
					((IBounds)value).getY()+","+
					((IBounds)value).getWidth()+","+
					((IBounds)value).getHeight();
		}
		if (attname.equals(".bendpoints")) {
			return ((IDiagramModelBendpoint)value).getStartX()+","+
					((IDiagramModelBendpoint)value).getStartY()+","+
					((IDiagramModelBendpoint)value).getEndX()+","+
					((IDiagramModelBendpoint)value).getEndY()+","+
					((IDiagramModelBendpoint)value).getWeight();
		}
		
		if (value instanceof IIdentifier) {
			System.out.println(((IIdentifier) value).getId());
			throw new Error("unhandled IIdentifier:"+objtype+","+attname);
		}
		return value.toString();
	}

	
	
	private Object deserialize(String attname,EObject obj, String value) {
		String objclass = obj.eClass().getName();
		System.out.println("deserializing ("+attname+","+objclass+","+value+")");
		
		if (attname.equals("accessType")) {
			return new Integer(value);
		}
		if (attname.equals("lineWidth")){
			return new Integer(value);
		}
		if (isobject(attname)) {
			return loadObject(obj,value);
		}
		
		if (attname.equals("viewpoint")||//FIXME type of value should be a field in the db
				attname.equals("connectionRouterType")||
				attname.equals("textPosition")||
				attname.equals("textAlignment")||
				attname.equals("interfaceType")||
				attname.equals("viewpoint")||
				(objclass.equals("DiagramModelArchimateObject")&&attname.equals("type"))
			) {
			return new Integer(value);
		}
		if (attname.equals(".properties")) { //FIXME should be stored as name/value pairs
			IProperty prop = IArchimateFactory.eINSTANCE.createProperty();
			String[] fields = value.split("=");
			System.out.println("value="+value+",fields[0]="+fields[0]+",len="+fields.length);
			prop.setKey(fields[0]);
			if(fields.length>1) {
				String v = fields[1]; // v = "=".join(fields[1:])
				if(fields.length>2) {
					for(int i=2;i<fields.length;i++){
						v = v +"="+fields[i];
					}
				}
				prop.setValue(v);
			}
			return prop;
		}
		if (attname.equals("bounds")) {
			String[] fields = value.split(",");
			IBounds bounds = IArchimateFactory.eINSTANCE.createBounds(
					new Integer(fields[0]),
					new Integer(fields[1]),
					new Integer(fields[2]),
					new Integer(fields[3]));
			//System.out.println("bounds="+bounds);
			return bounds;
		}
		if (attname.equals(".bendpoints")) {
			String[] fields = value.split(",");
			IDiagramModelBendpoint bp = IArchimateFactory.eINSTANCE.createDiagramModelBendpoint();
			bp.setStartX(new Integer(fields[0]));
			bp.setStartY(new Integer(fields[1]));
			bp.setEndX(new Integer(fields[2]));
			bp.setEndY(new Integer(fields[3]));
			bp.setWeight(new Float(fields[4]));
			return bp;
		}

		String objtype = obj.eClass().getName();
		if (objtype.equals("ArchimateModel") && attname.equals("file")) {
			return new File(value);
		}
		if (objtype.equals("Folder") && attname.equals("type")) {
			return FolderType.get(value);
		}
		return value;
	}
	

	private EObject loadObjectByParentTypeID(EObject parentobj, String parent, String type, String id) {
		if(repo.isPersisted(id)) {
			//System.out.println("cache hit: "+ id);
			return repo.getPersisted(id);
		}
		//System.out.println("no cache hit: "+ id);
		try {
			EClass eclass = (EClass) IArchimatePackage.eINSTANCE.getEClassifier(type);
			EObject instance = EcoreUtil.create((EClass) eclass);
			EStructuralFeature idfeature = eclass.getEStructuralFeature("id");
			instance.eSet(idfeature, id);
			repo.registerPersisted((IIdentifier) instance);
			PreparedStatement psSelectObjectAttributes = con.prepareStatement("select name,value from object_attribute_view where version=? and parent=?");

			psSelectObjectAttributes.setInt(1, modelversion);
			psSelectObjectAttributes.setString(2, id);
			ResultSet rsa = psSelectObjectAttributes.executeQuery();
			while (rsa.next()) {
				String name = rsa.getString("name");
				String value = rsa.getString("value");
				Object val = deserialize(name,instance,value);
				if(name.startsWith(".")) {
					name = name.substring(1);
					System.out.println("name="+name);
					EStructuralFeature feature = eclass.getEStructuralFeature(name);
					System.out.println("feature="+feature+",value="+val);
					((Collection<Object>)instance.eGet(feature)).add(val);
				} else if(name.startsWith(":")) {
					name = name.substring(1);
					HashMap<String,String> featmap= new HashMap<String,String>();//FIXME make these static
					featmap.put("Property","properties");
					featmap.put("Bounds","bounds");
					featmap.put("DiagramModelBendpoint", "bendpoints");
					
					EStructuralFeature feat = instance.eClass().getEStructuralFeature(featmap.get(name));
					System.out.println("feature="+feat+"(name="+featmap.get(name)+"|"+name+") class = "+instance.eClass()+" instance="+instance);
					System.out.println("name="+name+",value="+value);
					if(feat.isMany()) {
						Collection<EObject> coll = ((Collection<EObject>)(instance.eGet(feat)));
						//System.out.println("feature="+feat+", collection="+coll);
						coll.add((EObject) val);
					} else {
						instance.eSet(feat, val);
					}
					
				} else {
					System.out.println("setting ("+name+","+value+") as '"+val+"'("+val.getClass()+")");
					EStructuralFeature feature = eclass.getEStructuralFeature(name);
					System.out.println("feature class="+feature.getName()+",instance="+instance);
					instance.eSet(feature, val);
				}
			}
			PreparedStatement psSelectChildren = con.prepareStatement("select id,type from object_view where version=? and parent=?");
			psSelectChildren.setInt(1, modelversion);
			psSelectChildren.setString(2, id);
			ResultSet rsc = psSelectChildren.executeQuery();
			
			while(rsc.next()) {
				loadObjectByParentTypeID(instance,id,rsc.getString("type"),rsc.getString("id"));
			}
			rsc.close();
/*		System.out.println("einstance = "+instance+",parent="+parentobj);
			if(instance instanceof IFolder) {
				((IFolder)parentobj).getFolders().add((IFolder) instance);
			}else if (instance instanceof IDiagramModelComponent) {
				System.out.println("instance="+instance+",parentobj="+parentobj);
				((IDiagramModelContainer)parentobj).getChildren().add((IDiagramModelObject) instance);
			} else if (instance != null ){
				((IFolder)parentobj).getElements().add(instance);
			}
*/
			return instance;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new Error("SQL problem");
		}
	}
	public EObject load(String versionname) {
		try {
			modelversion = getVersionByName(versionname);
			psSelectRoot.setInt(1, modelversion);
			System.out.println("executing "+ psSelectRoot+";");
			ResultSet rs = psSelectRoot.executeQuery();
			boolean n = rs.next();
			assert(n);
			String parent = rs.getString("parent");
			String type = rs.getString("type");
			return loadObjectByParentTypeID(null,parent, type, parent);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new Error("cannot find in repository: "+versionname);
		}
	}
	
	public EObject loadObject(String id, String versionname) {
		if(repo.isPersisted(id)) {
			return repo.getPersisted(id);
		}
		try {
			modelversion = getVersionByName(versionname);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new Error("no such version: "+versionname);
		}
		return loadObject((EObject)null,id);
	}
	
	private EObject loadObject(EObject parentobj,String id) {
		if(repo.isPersisted(id)) {
			return repo.getPersisted(id);
		}
		try {
			psSelectObject.setInt(1, modelversion);
			psSelectObject.setString(2, id);
			//System.out.println("executing "+ psSelectObject+";");
			ResultSet rs = psSelectObject.executeQuery();
			boolean n = rs.next();
			assert(n);
			String parent = rs.getString("parent");
			String type = rs.getString("type");
			EObject obj = loadObjectByParentTypeID(parentobj,parent, type, id);
			rs.close();
			return obj;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new Error("SQL problem");
		}
	}
	

	
	private void insertObjectAttribute(String id,EStructuralFeature attr,String objtype,Object val) throws SQLException {
		String attname = attr.getName();
		if(attr.isMany()) {//FIXME should have a column in the db instead
			attname="."+attname;
		}
		String v = serialize(attname, objtype, val);
		psInsertObjectAttribute.setInt(1, modelversion);
		psInsertObjectAttribute.setString(2, id);
		psInsertObjectAttribute.setString(3,attname);
		psInsertObjectAttribute.setString(4,v);
		psInsertObjectAttribute.addBatch();
		System.out.println("executing"+psInsertObjectAttribute);
	}

	@Override
	public void saveObject(EObject node) {
		repo.checkModel(node);
		if(repo.isPersisted(((IIdentifier) node).getId())) {
			return;
		}
		repo.registerPersisted((IIdentifier) node);
		//This saves the object and its attributes
		String id = ((IIdentifier) node).getId();
		//System.out.println("saving "+id);
		try {
			String parentid;
			if(null == node.eContainer()) {
				parentid = id;
			} else {
				EObject parent = node.eContainer();
				saveObject(parent);
				parentid = ((IIdentifier) parent).getId();
			}
			String objtype = node.eClass().getName();
			psInsertObject.setInt(1, modelversion);
			psInsertObject.setString(2, id);
			psInsertObject.setString(3, parentid );
			psInsertObject.setString(4, objtype);
			//System.out.println(node.eAdapters());
			System.out.println("executing "+psInsertObject+";");
			psInsertObject.addBatch();

			EList<EStructuralFeature> attrs;// = node.eClass().getEAllAttributes();
			attrs = node.eClass().getEAllStructuralFeatures();
			for (EStructuralFeature attr : attrs) {
				String attname = attr.getName();
				if(attr.isChangeable()&(!(
						attr.isTransient()||
						attname.equals("id")||
						attr.isDerived())))
				{
					Object val = node.eGet(attr);
					System.out.println("adding attribute "+attname+"="+val+"(many="+attr.isMany()+")");
					if(attr.isMany()) {
						//System.out.println("Collection:"+attr.getName()+","+objtype);
						Iterator<?> i = ((Collection<?>) val).iterator();
						while(i.hasNext()) {
							Object v = i.next();
							insertObjectAttribute(id,attr,objtype,v);	
						}
					} else if(null != val) {
						insertObjectAttribute(id,attr,objtype,val);
					}
				}
			}
/*			//System.out.println("cross references = "+node.eCrossReferences());
			EList<EObject> xrefs = node.eCrossReferences();
			for(EObject refd :xrefs) {
				saveObject(refd);
			}
			/*
			for (EObject kid : node.eContents() ) {
				if(kid instanceof IIdentifier) {
					saveObject(kid);
				} else {
					String classname = kid.eClass().getName();
					psInsertObjectAttribute.setInt(1, modelversion);
					psInsertObjectAttribute.setString(2, id);
					psInsertObjectAttribute.setString(3,":"+classname);
					psInsertObjectAttribute.setString(4,serialize(classname,objtype,(Object)kid));
					System.out.println("executing "+psInsertObjectAttribute+";");
					psInsertObjectAttribute.addBatch();
				}
			}
			*/
		} catch (SQLException e) {
			e.printStackTrace();
			throw new Error("SQL problem");
		}
	}
}
