prcrime-heatmap
===============

Puerto Rico Crime Heatmap

Depende de PostGIS. Vease [Esquema](src/sql/create-schema.sql)

Automáticamente carga los datos de [https://data.pr.gov/resource/incidencia-crime-map.json](https://data.pr.gov/resource/incidencia-crime-map.json)
por medio de polling cada hora.

El proyecto esta hecho en Groovy. Para compilar, ejecute `./gradlew build`

![Screenshot](screenshot.png)
