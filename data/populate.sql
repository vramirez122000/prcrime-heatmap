drop table if exists incidencia;

create table incidencia (
  gid serial primary key,
  fecha_incidente date,
  delito int4,
  hora_incidente time,
  needs_recoding boolean not null default false,
  latitude text,
  longitude text,
  tstamp timestamp,
  location geometry(Point,4326)
);

insert into incidencia(fecha_incidente, delito, hora_incidente, latitude, longitude, tstamp, location)  (select
  cast(fecha as date) fecha_incidente,
  cast(delito as int4) delito,
  cast(hora as time) hora_incidente,
  point_x latitude,
  point_y longitude,
  cast((fecha || ' ' || hora) as TIMESTAMP) as tstamp,
  st_pointfromtext('POINT(' || point_y ||' ' || point_x || ')', 4326) as location
from incidencia_staging
where hora not in ('60:10'));

delete from incidencia where fecha_incidente > current_timestamp;

create index idx_incidencia_location on incidencia using gist(location);

create index idx_incidencia_fecha on incidencia(fecha_incidente);

drop table incidencia_staging;

VACUUM analyze;
