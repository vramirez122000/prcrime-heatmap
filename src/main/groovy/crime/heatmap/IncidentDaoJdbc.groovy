package crime.heatmap

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.sql.Sql
import org.geojson.GeometryCollection
import org.geojson.Point
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import javax.sql.DataSource
import java.sql.Time
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Repository
class IncidentDaoJdbc {

    private Sql sql;
    private def objectMapper = new ObjectMapper()

    @Autowired
    void setDataSource(DataSource dataSource) {
        this.sql = new Sql(dataSource);
    }

    GeometryCollection getIncidentsAsGeoJson(IncidentCriteria criteria) {
        CriteriaQueryBuilder queryBuilder = new CriteriaQueryBuilder('select st_asgeojson(location) json from incidencia ')
        if(criteria.bboxXmax != null && criteria.bboxXmin != null && criteria.bboxYmax != null && criteria.bboxYmin != null) {
            queryBuilder.whereClause('incidencia.location && st_makeenvelope(?, ?, ?, ?)',
                    criteria.bboxXmin,
                    criteria.bboxYmin,
                    criteria.bboxXmax,
                    criteria.bboxYmax,
            )
        }

        if(criteria.incidentTypes != null) {
            def incidentTypeCodes = criteria.incidentTypes.collect { it ->
                it.code
            }
            queryBuilder.whereIn('delito', incidentTypeCodes.toArray(new Integer[incidentTypeCodes.size()]))
        }

        if(criteria.daysOfWeek != null) {
            queryBuilder.whereClause("extract(dow from fecha_incidente) in ( ${(['?']*criteria.daysOfWeek.size()).join(',')} )",
                    criteria.daysOfWeek.toArray(new Integer[criteria.daysOfWeek.size()]))
        }

        if(criteria.timeMin || criteria.timeMax) {
            if(criteria.timeMin) {
                criteria.timeMin += ':00'
            }
            if(criteria.timeMax) {
                criteria.timeMax += ':59'
            }

            queryBuilder.whereBetween(
                    'hora_incidente',
                    Time.valueOf(criteria.timeMin?:'00:00:00'),
                    Time.valueOf(criteria.timeMax?:'23:59:59')
            )
        }
        queryBuilder.limit(2000)

        def geomCollection = new GeometryCollection()
        sql.eachRow(queryBuilder.sql(), queryBuilder.values(), {row ->
            geomCollection.add(objectMapper.readValue((String) row.json, Point.class))
        })
        return geomCollection
    }

    LocalDateTime getMaxDate() {
        def row = sql.firstRow("select max((fecha_incidente || ' ' || hora_incidente) :: timestamp) as max_date from incidencia")
        return ((java.sql.Timestamp)row.max_date).toLocalDateTime()
    }

    void insert(Incident incident) {
        println incident
        //sql.executeInsert()
    }

}