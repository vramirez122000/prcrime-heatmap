drop table IF EXISTS incidencia_staging;

create table incidencia_staging (
Fecha text,
Hora text,
Delito text,
Delitos_code text,
POINT_X text,
POINT_Y text,
Location text,
"Area Policiaca" text
);
