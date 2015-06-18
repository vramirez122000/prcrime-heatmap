prcrime-heatmap
===============

Puerto Rico Crime Heatmap

Véase [Demo](http://crimenpr.com/)

Depende de PostGIS. Vease [Esquema](src/sql/create-schema.sql)

Automáticamente carga los datos de [data.pr.gov](https://data.pr.gov/resource/incidencia-crime-map.json)
por medio de polling cada hora.

Como compilar y empacar
------------------------------

El proyecto esta escrito en Groovy y depende de Java 8. El backend depende de librer&iacute;as de Maven Central.
Se utiliza [Spring Framework](http://projects.spring.io/spring-framework/) para "dependency injection" y Servlet API 3.0 (Tomcat 7 o Jetty 8)
El GUI utiliza librer&iacute;as de javascript que se deben descargar con bower.
Para compilar, ejecute:

`sudo npm -g install bower && bower install && ./gradlew build`

![Screenshot](screenshot.png)
