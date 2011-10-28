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
-- drop table object cascade; drop table property; drop table bendpoint; drop table bounds;
CREATE TABLE object (
    id varchar(30) primary key,
    parent varchar(30) 
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
);
comment on table object is 'elements (including ArchimateElements and relations),childs and sourceConnections';

create unique index iu_object_id on object(id);
create index i_object_name on object(name);
create index i_object_type on object(type);
create index i_object_source on object(source);
create index i_object_target on object(target);
create index i_object_element on object(target);
create index i_object_type_element on object(type,element);
create index i_object_type_source on object(type,source);
create index i_object_type_target on object(type,target);

CREATE TABLE property (
    parent varchar(30) 
--      references object(id)
    ,
    key text,
    value text
);
comment on table property is 'the property of an object';

create index i_property_parent on property(parent);
create index i_property_key on property(key);
create index i_property_value on property(value);
create index i_property_key_parent on property(key,parent);
create index i_property_key_value on property(key,value);

CREATE TABLE bounds (
    parent varchar(30) 
--      references object(id)
    ,
    x int,
    y int,
    width int,
    height int
);
comment on table bounds is 'bounds for diagram objects';

create unique index i_bounds_parent on bounds(parent);

CREATE TABLE bendpoint (
    parent varchar(30) 
--      references object(id)
    ,
    startx int,
    starty int,
    endx int,
    endy int
);
comment on table bendpoint is 'bendpoints for sourceconnections';

create index i_bendpoint_parent on bendpoint(parent);

"""

if ((len(sys.argv) == 2) and (sys.argv[1]=='DDL')):
    print DDL
    sys.exit(0)

dom = parse(sys.stdin)
passw = getpass.getpass("db password:")
con = psycopg2.connect("host=localhost port=5433 dbname=archi user=mag password=%s"%passw)
cur=con.cursor()

for n in dom.childNodes[0].childNodes:
    print n.nodeName
    fields={}
    for ob in n.childNodes:
        print ob.toxml()
        cn=ob.childNodes
        if len(cn):
            fields[ob.nodeName]=cn[0].nodeValue
            print "field:",ob.nodeName,ob.childNodes[0].nodeValue
    keys=fields.keys()
    sql="insert into %s (%s) VALUES (%s)"%(n.nodeName,",".join(keys),",".join(["%s"]*len(keys)))
    print sql,fields.values()
    cur.execute(sql,fields.values())

cur.close()
con.commit()
con.close()
