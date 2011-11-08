#!/usr/bin/python
import sys
from xml.dom.minidom import parse
import getpass
import psycopg2


"""
element documentation folder property purpose
child bound
 sourceConnection content bendpoint
"""
 
DDL = """
-- drop view property_view; drop view object_view; drop view repository;
-- drop table object cascade; drop table property;drop table version cascade; drop table version_hierarchy; drop table object_role cascade;
-- drop role archi_owner; drop role archi_submitter; drop role archi_viewer; drop role archi_addperm;

create table version (
    id serial primary key,
    name varchar(30),
    description text,
    createtime timestamp default now()
);


create table version_hierarchy (
    parent int references version(id),
    child int references version(id),
    distance int default 1
);
create index i_version_hierarchy_parent on version_hierarchy(parent);
create index i_version_hierarchy_child on version_hierarchy(child);
create or replace function addtohier() returns trigger as 
$$
BEGIN
 insert into version_hierarchy select new.id, new.id, 0;
 return NEW;
END
$$ language 'plpgsql';
create trigger "add_to_hier" after insert on version for each row execute procedure addtohier();


CREATE TABLE object (
    version int references version(id),
    id varchar(30) not null,
    parent varchar(30) not null
--        references object(id)
    ,
    name text,
    documentation text,
    type varchar(42),
    source varchar(30) 
--      references object(id)
    ,
    target varchar(30) 
--      references object(id)
    ,
    element varchar(30) 
--      references object(id)
    ,
    font varchar(42),
    fontcolor varchar(8),
    textalignment varchar(1),
    fillcolor varchar(8),
    primary key (version,id)
);
comment on table object is 'elements (including ArchimateElements and relations),childs and sourceConnections';

create index i_object_name on object(name);
create index i_object_type on object(type);
create index i_object_source on object(source);
create index i_object_target on object(target);
create index i_object_element on object(target);
create index i_object_type_element on object(type,element);
create index i_object_type_source on object(type,source);
create index i_object_type_target on object(type,target);


CREATE TABLE property (
    version int references version(id),
    parent varchar(30),
    type varchar(10),
    key text,
    value text,
    x1 int,
    x2 int,
    y1 int,
    y2 int,
    foreign key (version,parent) references object(version,id) on delete cascade
);
comment on table property is 'the properties of an object, including bounds and bendpoints';

create index i_property_parent_version on property(parent,version);
create index i_property_parent on property(parent);
create index i_property_key on property(key);
create index i_property_key_version on property(key,version);
create index i_property_value on property(value);
create index i_property_value_version on property(value,version);
create index i_property_key_parent on property(key,parent);
create index i_property_key_parent_version on property(key,parent,version);
create index i_property_key_value on property(key,value);
create index i_property_key_type on property(key,type);
create view repository as select * from object where type in (
    'archimate:BusinessEvent',
    'archimate:DataObject',
    'archimate:UsedByRelationship',
    'archimate:CompositionRelationship',
    'archimate:Folder',
    'archimate:Product',
    'archimate:BusinessProcess',
    'archimate:BusinessInterface',
    'archimate:BusinessCollaboration',
    'archimate:AssignmentRelationship',
    'archimate:RealisationRelationship',
    'archimate:ApplicationFunction',
    'archimate:ApplicationInterface',
    'archimate:DiagramObject',
    'archimate:ApplicationComponent',
    'archimate:BusinessActivity',
    'archimate:AccessRelationship',
    'archimate:BusinessObject',
    'archimate:Network',
    'archimate:OrJunction',
    'archimate:SpecialisationRelationship',
    'archimate:CommunicationPath',
    'archimate:AssociationRelationship',
    'archimate:TriggeringRelationship',
    'archimate:BusinessActor'
   ); 

create view object_view as select * from object;
create view property_view as select * from property;
"""
# this one is old and cumbersome
ACCDDLOLD="""
-- here starts the access control part. The whole stuff should work without this.
create table object_role (
    version varchar(30),
    id varchar(30),
    rolename name,
    foreign key (version,id) references object(version,id) on delete cascade
);

create or replace view repository as select o.* from object o, object_role r where type in (
    'archimate:BusinessEvent',
    'archimate:DataObject',
    'archimate:UsedByRelationship',
    'archimate:CompositionRelationship',
    'archimate:Folder',
    'archimate:Product',
    'archimate:BusinessProcess',
    'archimate:BusinessInterface',
    'archimate:BusinessCollaboration',
    'archimate:AssignmentRelationship',
    'archimate:RealisationRelationship',
    'archimate:ApplicationFunction',
    'archimate:ApplicationInterface',
    'archimate:DiagramObject',
    'archimate:ApplicationComponent',
    'archimate:BusinessActivity',
    'archimate:AccessRelationship',
    'archimate:BusinessObject',
    'archimate:Network',
    'archimate:OrJunction',
    'archimate:SpecialisationRelationship',
    'archimate:CommunicationPath',
    'archimate:AssociationRelationship',
    'archimate:TriggeringRelationship',
    'archimate:BusinessActor'
   ) and o.version = r.version and o.id = r.id and r.rolename=current_user;

create index i_object_role_version_id on object_role(version,id);
create index i_object_role_version_rolename on object_role(version,rolename);


create or replace view object_view as select o.* from object o, object_role r where o.version = r.version and o.id = r.id and r.rolename=current_user;
create or replace view property_view as select o.* from property o, object_role r where o.version = r.version and o.parent = r.id and r.rolename=current_user;

create role archi_owner;
create role archi_submitter;
create role archi_viewer;
create role archi_addperm;

alter table object owner to archi_owner;
alter table property owner to archi_owner;
alter table repository owner to archi_owner;
alter table version owner to archi_owner;
alter table version_hierarchy owner to archi_owner;
alter table object_role owner to archi_owner;

alter view repository owner to archi_owner;
alter view object_view owner to archi_owner;
alter view property_view owner to archi_owner;


grant insert on object to archi_submitter;
grant insert on version to archi_submitter;
grant insert on version_hierarchy to archi_submitter;
grant insert on property to archi_submitter;
grant select,insert on object_role to archi_addperm;

grant select on version to archi_viewer,archi_submitter;
grant select on version_hierarchy to archi_viewer,archi_submitter;
grant select on repository to archi_viewer,archi_submitter;
grant select on object_view to archi_viewer,archi_submitter;
grant select on property_view to archi_viewer,archi_submitter;

-- CREATE LANGUAGE plpythonu;

-- inserting into object_roles is permitted if either:
--  - the user is member of both the archi_submitter and the granted role
--  - the user is member of the archi_addperm role

create or replace function insert_objrole(
    ver character varying(30),
    theid character varying(30),
    rname name)
returns name as $$
if SD.has_key('groups'):
    groups = SD['groups']
else:
    groups = dict()
    sql="select r1.rolname as role ,r2.rolname as member from pg_auth_members a, pg_authid r1, pg_authid r2 where a.roleid=r1.oid and a.member = r2.oid"
    rv = plpy.execute(sql)
    #plpy.notice("%s"%(rv[0],))
    for x  in rv:
        member=x['member']
        role=x['role']
        #plpy.notice("%s,%s"%(role,member))
        if not groups.has_key(member):
            groups[member]=[role]
        else:
            groups[member].append(role)
    SD['groups'] = groups
    #plpy.notice("%s"%(groups,))
    SD['plan'] = plpy.prepare("select session_user")
    SD['insertplan'] = plpy.prepare("insert into object_role (version,id,rolename) values ($1,$2,$3)",["character varying(30)","character varying(30)","name"])
#plpy.notice("%s"%(groups,))
rv = plpy.execute(SD['plan'])
me = rv[0]['session_user']
#plpy.notice("%s"%(groups,))
if ('archi_addperm' in groups[me]) or (('archi_submitter' in groups[me]) and (rname in groups[me])):
    plpy.execute(SD['insertplan'],[ver,theid,rname])
    return me
else:
    plpy.error("you should be a member of either a) both the archi_submitter and the granted role b) the archi_addperm role. we have %s and %s"%(me,rname))
$$ language 'plpythonu' security definer;

create or replace function initial_role()
returns trigger as $$
if not SD.has_key("insertplan"):
    plan=plpy.prepare("select insert_objrole($1,$2,$3)",["character varying(30)","character varying(30)","name"])
    SD['insertplan']=plan
    plan=plpy.prepare("select current_user")
    SD["meplan"]=plan
rv = plpy.execute(SD['meplan'])
me = rv[0]['current_user']
new=TD["new"]
plpy.execute(SD['insertplan'],[new["version"],new["id"],me])
$$ language 'plpythonu';

create trigger add_role after insert 
    on object for each row execute procedure initial_role();
"""


ACCDDL="""
CREATE TABLE acl
(
  id serial NOT NULL,
  "name" character varying(20),
  CONSTRAINT acl_pkey PRIMARY KEY (id)
);
CREATE TABLE aclentry
(
  id integer,
  rolename name,
  "access" character varying(6),
  foreign key (id) references acl(id) on delete cascade
);

alter table object add column acl int not null references acl(id);

CREATE INDEX aclentry_rolename
  ON aclentry  (rolename);

create or replace view object_v as select distinct v.child as version, o.id, o.parent, o.name, o.documentation, o.type, o.source, o.target, o.element, o.font, o.fontcolor, o.textalignment, o.fillcolor, o.acl from object o, version_hierarchy v where o.version = v.parent and v.child not in ( select ooc.child from object oo, version_hierarchy pc, version_hierarchy ooc where o.id=oo.id and pc.parent = o.version and pc.child=oo.version and ooc.parent=oo.version and o.version != oo.version) and type is not null;

--FROM object o, aclentry e, version_hierarchy v WHERE o.acl = e.id AND e.rolename = "current_user"() and e.access = 'select' and o.version = v.parent and not exists (select max(version) from object oo, version_hierarchy vh where o.id=oo.id and vh.child = oo.version and vh.parent = v.child );

create index version_hierarchy_parent_child on version_hierarchy(parent,child);
--- FIXME the above does not seem to optimize the query in below
create or replace function transitive_closure() returns trigger as 
$$
DECLARE
BEGIN
-- raise notice '% % %',new.parent, new.child,new.distance;
-- if new.distance > 3
-- then
--    return new;
-- end if;
 insert into version_hierarchy select p.parent, new.child, p.distance+new.distance from version_hierarchy p where p.child = new.parent and not exists (select 1 from version_hierarchy q where p.parent=q.parent and new.child = q.child);
return new;
END
$$ language 'plpgsql';

create trigger "insert_version_t" after insert on version_hierarchy for each row execute procedure transitive_closure();

--- this rule
--- inserts only if current_user is allowed to do it according to the parent acl
--- does not insert exact duplicates
--- copies the acl from the parent
--- may be exploitable by inserting more rows in one run where one row is ok by acl, but the others are not
create or replace rule dummy_insert as ON INSERT TO object_v DO INSTEAD NOTHING;
create or replace rule
 insert_rule AS
    ON INSERT TO object_v
   WHERE 
        (
        (( SELECT distinct object.acl
           FROM object
          WHERE object.id = new.parent) IN ( SELECT aclentry.id 
           FROM aclentry
          WHERE aclentry.rolename = "current_user"() AND aclentry.access::text = 'insert'::text)) 
    and true not in 
        (select o.documentation is not distinct from new.documentation and 
            o.name is not distinct from new.name and 
            o.type is not distinct from new.type and 
            o.source is not distinct from new.source and 
            o.target is not distinct from new.target and 
            o.element is not distinct from new.element and 
            o.font is not distinct from new.font and 
            o.fontcolor is not distinct from new.fontcolor and 
            o.textalignment is not distinct from new.textalignment and 
            o.fillcolor is not distinct from new.fillcolor
        from object o, version_hierarchy vh 
        where vh.parent != vh.child and new.version = vh.child and o.version = vh.parent and o.id = new.id and o.parent = new.parent))
  DO ALSO
    INSERT INTO object select new.version, new.id, new.parent, new.name, new.documentation, new.type, new.source, new.target, new.element, new.font, new.fontcolor, new.textalignment, new.fillcolor, p.acl
    from object p, version_hierarchy vh where
    p.id = new.parent and vh.parent = p.version and vh.child = new.version and p.version not in (select po.parent from version_hierarchy po, version_hierarchy o_n, object oo where oo.id=new.id and po.parent=p.version and po.child = oo.version and o_n.parent=oo.version and o_n.child = new.version );

alter table object rename to object_data;
alter view object_v rename to object;
create role archi_owner;
create role archi_submitter;
create role archi_viewer;
create role archi_addperm;

alter table object_data owner to archi_owner;
alter view object owner to archi_owner;
alter table property owner to archi_owner;
alter table repository owner to archi_owner;
alter table version owner to archi_owner;
alter table version_hierarchy owner to archi_owner;
alter table acl owner to archi_owner;
alter table aclentry owner to archi_owner;

alter view repository owner to archi_owner;


grant insert on object to archi_submitter;
grant insert on version to archi_submitter;
grant insert on version_hierarchy to archi_submitter;
grant insert on property to archi_submitter;

grant select on version to archi_viewer,archi_submitter;
grant select on version_hierarchy to archi_viewer,archi_submitter;
grant select on repository to archi_viewer,archi_submitter;
grant select on object to archi_viewer,archi_submitter;
grant select on property_view to archi_viewer,archi_submitter;

insert into acl (id, name) values (0, 'default acl');
insert into aclentry (id, rolename, access) values (0, 'archi_submitter', 'insert');
insert into aclentry (id, rolename, access) values (0, 'archi_viewer', 'select');
insert into aclentry (id, rolename, access) values (0, 'root', 'insert');
insert into aclentry (id, rolename, access) values (0, 'root', 'select');
insert into version (id, name,description) values (0,'grandpa','The father of all versions');
insert into object_data (version, id, parent, name, type, acl) values (0,0,0,'root node','root',0);
-- test:
insert into version (name) values ('test');
insert into version_hierarchy values (0,1);
insert into object (documentation,type,id,parent,version,name) VALUES ('doc', E'model',E'7037984b',0,1,E'Archi StyledHtml module');
insert into version (name) values ('deletetest');
insert into version_hierarchy values (1,2);
insert into object (documentation,type,id,parent,version,name) VALUES (null, null,'7037984b',0,2,null);
                                                                                                                                        
explain insert into object (documentation,type,id,parent,version,name) VALUES ('doc', E'model',E'7037984b',0,E'13',E'Archi StyledHtml module');

"""

if ((len(sys.argv) == 2) and (sys.argv[1]=='DDL')):
    print DDL
    sys.exit(0)
if ((len(sys.argv) == 2) and (sys.argv[1]=='ACCDDL')):
    print DDL
    print ACCDDL
    sys.exit(0)

if len(sys.argv) < 5:
    print "usage: sqlexport.py (<version name> <parent version name> <service> <role> |DDL) "
    sys.exit(-1)

version = sys.argv[1]
parentversion = sys.argv[2]
service = sys.argv[3]
role = sys.argv[4]
dom = parse(sys.stdin)
#passw = getpass.getpass("db password:")
print "service=%s"%service
con = psycopg2.connect("service=%s"%service)
cur=con.cursor()

cur.execute("set role %s",(role,))
cur.execute("insert into version (name) values (%s)",[version])
cur.execute("select id from version where name = %s",[parentversion])
parentversionid = cur.fetchone()[0]
cur.execute("select id from version where name = %s",[version])
versionid = cur.fetchone()[0]
cur.execute("insert into version_hierarchy values (%s,%s)",[parentversionid,versionid])

for n in dom.childNodes[0].childNodes:
    #print n.nodeName
    fields={}
    for ob in n.childNodes:
        #print ob.toxml()
        cn=ob.childNodes
        if len(cn):
            fields[ob.nodeName]=cn[0].nodeValue
            #print "field:",ob.nodeName,ob.childNodes[0].nodeValue
    fields['version'] = versionid
    keys=fields.keys()
    sql="insert into %s (%s) VALUES (%s)"%(n.nodeName,",".join(keys),",".join(["%s"]*len(keys)))
    print sql,fields.values()
    cur.execute(sql,fields.values())

cur.close()
con.commit()
con.close()
