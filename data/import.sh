#!/usr/bin/env bash


cat create_table.sql | psql -U postgres -h localhost -d crime_heatmap

printf "copy incidencia_staging from stdin with (format csv, header TRUE);\n" \
| cat - Incidencia_Crime_Map.csv \
| psql -U postgres -h localhost -d crime_heatmap


cat populate.sql | psql -U postgres -h localhost -d crime_heatmap

