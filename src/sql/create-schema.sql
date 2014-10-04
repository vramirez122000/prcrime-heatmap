create table incidencia (
  gid serial primary key,
  fecha_incidente date,
  delito int4,
  hora_incidente time,
  needs_recoding boolean not null default false,
  latitude varchar(30),
  longitude varchar(30),
  tstamp timestamp
);

AddGeometryColumn('incidencia', 'location', 4326, 'Point');