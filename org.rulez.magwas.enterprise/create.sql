
-- create role archi_owner nologin; -- owner of the repository
-- create role archi_user nologin; -- anyone who can use the repository. This granted on sql level only to views with access control functionality
-- create role archi_submitter nologin;
-- create role archi_viewer nologin;
-- alter group archi_user add user archi_viewer, archi_submitter; -- new roles should be added to the archi_user group to grant access to the views
set role archi_owner;

drop table acl cascade; drop table aclentry cascade;
drop table version cascade; drop table version_hierarchy cascade;
drop table object_attribute; drop table property_attribute;
drop table object cascade; drop table property cascade;
drop function addtohier() cascade;
drop function transitive_closure() cascade;


CREATE TABLE acl
(
  id serial NOT NULL,
  "name" character varying(20),
  CONSTRAINT acl_pkey PRIMARY KEY (id)
);
comment on table acl is 'The built-in access control pertains to versions';


CREATE TABLE aclentry
(
  id integer,
  rolename name,
  "access" character varying(8),
  foreign key (id) references acl(id) on delete cascade
);
comment on table aclentry is 'access can be checkin and checkout';
-- checkout means that the given role can submit a version with that acl
-- more elaborate access control for visibility can be implemented in the viewer logic, perhaps based on properties in the model and this same aclentry table
--  e.g 
--    - a property acl=name can be granted if current_user is in select ae.rolename from aclentry ae, acl a where ae.id=a.id and a.name=name and ae.access='grant' 
--    - an object can be viewed if the closest acl in its anchestry contains an acl containing 'read' access with the name of the viewing role

CREATE INDEX aclentry_rolename ON aclentry  (rolename);

create table version (
    id serial primary key,
    name varchar(30) unique,
    description text,
    createtime timestamp default now(),
    acl int not null references acl(id)
);
comment on table version is 'the version registry. no access for mortals';

create table version_hierarchy (
    parent int references version(id),
    child int references version(id),
    distance int default 1
);
comment on table version_hierarchy is 'hierarchy of versions. acyclic graph. no access for mortals';
create index i_version_hierarchy_parent on version_hierarchy(parent);
create index i_version_hierarchy_child on version_hierarchy(child);
create or replace function addtohier() returns trigger as 
 $$
 BEGIN
  insert into version_hierarchy select new.id, new.id, 0;
  return NEW;
 END
 $$ language 'plpgsql' security definer;
 create trigger "add_to_hier" after insert on version for each row execute procedure addtohier();


CREATE TABLE object (
    version int references version(id),
    id varchar(30) not null,
    parent varchar(30) not null,
    type varchar(42)
);
create unique index i_object_pkey on object(version,id);
create index i_object_type on object(type);

comment on table object is 'elements having an id, no access for mortals';

create table object_attribute (
    version int,
    parent varchar(30),
    name varchar(30),
    value text,
    foreign key (version,parent) references object(version,id) on delete cascade
);

CREATE TABLE property (
    id int,
    version int references version(id),
    parent varchar(30),
    type varchar(30),
    foreign key (version,parent) references object(version,id) on delete cascade
) with oids;
comment on table property is 'the properties of an object, including bounds and bendpoints, no access for mortals';

create unique index i_property_pkey on property(id,version);
create index i_property_parent_version on property(parent,version);
create index i_property_parent on property(parent);

create table property_attribute (
    version int,
    parent int,
    name varchar(30),
    value text,
    foreign key (version,parent) references property(version,id) on delete cascade
);
--- views for checkin and checkout

create view version_view as select distinct o.* from version o , aclentry ae where o.acl=ae.id and ae.rolename = current_user and ( ae.access = 'checkin' or ae.access='checkout');
comment on view version_view is 'the view for insert and select versions';
create or replace rule dummy_version_insert as ON INSERT TO version_view DO INSTEAD NOTHING;
create or replace rule
 insert_version_rule AS
    ON INSERT TO version_view
   WHERE 
        new.acl in (select ae.id from aclentry ae where ae.rolename = current_user and ae.access='checkin')
  DO ALSO
    INSERT INTO version (id, name, description, createtime, acl)  SELECT coalesce(new.id,nextval('version_id_seq')) , new.name, new.description, new.createtime, new.acl;

create view version_hierarchy_view as select distinct o.* from version_hierarchy o, aclentry ae, version v where v.acl=ae.id and o.child = v.id and ae.rolename = current_user and ae.access = 'checkout';
comment on view version_hierarchy_view is 'the view to maintain version_hierarchy';
create or replace rule dummy_version_hierarchy_insert as ON INSERT TO version_hierarchy_view DO INSTEAD NOTHING;
create or replace rule
 insert_version_hierarchy_rule AS
    ON INSERT TO version_hierarchy_view
   WHERE 
        new.parent in (select v.id from version v, aclentry ae where ae.rolename = current_user and ae.access='checkin')
  DO ALSO
    INSERT INTO version_hierarchy (parent, child, distance)  SELECT new.parent, new.child, coalesce(new.distance,1);

create view object_view as select distinct o.* from object o, aclentry ae, version v where v.acl=ae.id and o.version = v.id and ae.rolename = current_user and ae.access = 'checkout';
comment on view object_view is 'the view for insert and select objects';
create or replace rule dummy_object_insert as ON INSERT TO object_view DO INSTEAD NOTHING;
create or replace rule
 insert_object_rule AS
    ON INSERT TO object_view
   WHERE 
        new.version in (select v.id from version v, aclentry ae where ae.rolename = current_user and ae.access='checkin')
  DO ALSO
    INSERT INTO object select new.*;

create view object_attribute_view as select distinct o.* from object_attribute o, aclentry ae, version v where v.acl=ae.id and o.version = v.id and ae.rolename = current_user and ae.access = 'checkout';
comment on view object_attribute_view is 'the view for insert and select properties';
create or replace rule dummy_object_attribute_insert as ON INSERT TO object_attribute_view DO INSTEAD NOTHING;
create or replace rule
 insert_object_attribute_rule AS
    ON INSERT TO object_attribute_view
   WHERE 
        new.version in (select v.id from version v, aclentry ae where ae.rolename = current_user and ae.access='checkin')
  DO ALSO
    INSERT INTO object_attribute select new.*;

create view property_view as select distinct o.* from property o, aclentry ae, version v where v.acl=ae.id and o.version = v.id and ae.rolename = current_user and ae.access = 'checkout';
comment on view property_view is 'the view for insert and select properties';
create or replace rule dummy_property_insert as ON INSERT TO property_view DO INSTEAD NOTHING;
create or replace rule
 insert_property_rule AS
    ON INSERT TO property_view
   WHERE 
        new.version in (select v.id from version v, aclentry ae where ae.rolename = current_user and ae.access='checkin')
  DO ALSO
    INSERT INTO property select new.*;

create view property_attribute_view as select distinct o.* from property_attribute o, aclentry ae, version v where v.acl=ae.id and o.version = v.id and ae.rolename = current_user and ae.access = 'checkout';
comment on view property_attribute_view is 'the view for insert and select properties';
create or replace rule dummy_property_attribute_insert as ON INSERT TO property_attribute_view DO INSTEAD NOTHING;
create or replace rule
 insert_property_attribute_rule AS
    ON INSERT TO property_attribute_view
   WHERE 
        new.version in (select v.id from version v, aclentry ae where ae.rolename = current_user and ae.access='checkin')
  DO ALSO
    INSERT INTO property_attribute select new.*;

--- transitive closure on version
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
$$ language 'plpgsql' security definer;

create trigger "insert_version_closure" after insert on version_hierarchy for each row execute procedure transitive_closure();

--- fixme ownerships
alter table acl owner to archi_owner;
alter table aclentry owner to archi_owner;
alter table version owner to archi_owner;
alter view version_view owner to archi_owner;
alter table version_hierarchy owner to archi_owner;
alter view version_hierarchy_view owner to archi_owner;
alter table object owner to archi_owner;
alter view object_view owner to archi_owner;
alter table object_attribute owner to archi_owner;
alter view object_attribute_view owner to archi_owner;
alter table property owner to archi_owner;
alter view property_view owner to archi_owner;
alter table property_attribute owner to archi_owner;
alter view property_attribute_view owner to archi_owner;


grant select on aclentry to archi_user;
grant select on acl to archi_user;
grant update on version_id_seq to archi_user;
grant select,insert on version_view to archi_user;
grant select,insert on version_hierarchy_view to archi_user;
grant select,insert on object_view to archi_user;
grant select,insert on object_attribute_view to archi_user;
grant select,insert on property_view to archi_user;
grant select,insert on property_attribute_view to archi_user;

insert into acl (id, name) values (0, 'default acl');
insert into aclentry (id, rolename, access) values (0, 'archi_submitter', 'checkin');
insert into aclentry (id, rolename, access) values (0, 'archi_viewer', 'checkout');
-- insert into version (id, name,description,acl) values (0,'grandpa','The father of all versions',0);
--insert into object (version, id, parent, name, type) values (0,0,0,'root node','root');


