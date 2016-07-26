package crime.heatmap

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.sql.Sql
import org.geojson.Feature
import org.geojson.FeatureCollection
import org.geojson.GeometryCollection
import org.geojson.Point
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import javax.sql.DataSource
import java.sql.Time
import java.sql.Timestamp
import java.time.LocalDateTime

@Repository
class IncidentDaoJdbc {

    private Sql sql;
    private def objectMapper = new ObjectMapper()

    @Autowired
    void setDataSource(DataSource dataSource) {
        this.sql = new Sql(dataSource);
    }

    FeatureCollection getIncidentsAsGeoJson(IncidentCriteria criteria) {
        CriteriaQueryBuilder queryBuilder = new CriteriaQueryBuilder('select fecha_incidente, hora_incidente, delito, st_asgeojson(location) json from incidencia ')
        if (criteria.bboxXmax != null && criteria.bboxXmin != null && criteria.bboxYmax != null && criteria.bboxYmin != null) {
            queryBuilder.whereClause('incidencia.location && st_makeenvelope(?, ?, ?, ?)',
                    criteria.bboxXmin,
                    criteria.bboxYmin,
                    criteria.bboxXmax,
                    criteria.bboxYmax,
            )
        }

        if (criteria.incidentTypes != null) {
            def incidentTypeCodes = criteria.incidentTypes.collect { it ->
                it.code
            }
            queryBuilder.whereIn('delito', incidentTypeCodes.toArray(new Integer[incidentTypeCodes.size()]))
        }

        if(criteria.dateMin) {
            queryBuilder.whereGreaterOrEquals('fecha_incidente', new java.sql.Date(criteria.dateMin.time))
        }

        if(criteria.dateMax) {
            queryBuilder.whereLessOrEquals('fecha_incidente', new java.sql.Date(criteria.dateMax.time))
        }

        if (criteria.daysOfWeek != null) {
            queryBuilder.whereClause("extract(dow from fecha_incidente) in ( ${(['?'] * criteria.daysOfWeek.size()).join(',')} )",
                    criteria.daysOfWeek.toArray(new Integer[criteria.daysOfWeek.size()]))
        }

        if (criteria.timeMin || criteria.timeMax) {
            if (criteria.timeMin) {
                criteria.timeMin += ':00'
            }
            if (criteria.timeMax) {
                criteria.timeMax += ':59'
            }

            queryBuilder.whereBetween(
                    'hora_incidente',
                    Time.valueOf(criteria.timeMin ?: '00:00:00'),
                    Time.valueOf(criteria.timeMax ?: '23:59:59')
            )
        }

        queryBuilder.orderBy('fecha_incidente', true)
        queryBuilder.limit(criteria.limit ?: 2000)

        def featureCollection = new FeatureCollection()
        sql.eachRow(queryBuilder.sql(), queryBuilder.values(), { row ->
            def f = new Feature()
            f.geometry = objectMapper.readValue((String) row.json, Point.class)
            f.setProperty('fecha', ((java.sql.Date)row.fecha_incidente)?.toLocalDate()?.toString())
            f.setProperty('delito', IncidentType.forCode(row.delito)?.name() )
            f.setProperty('hora', ((Time)row.hora_incidente)?.toLocalTime()?.toString() )
            featureCollection.add(f)
        })
        return featureCollection
    }

    LocalDateTime getMaxDate() {
        def row = sql.firstRow("SELECT max(tstamp) AS max_date FROM incidencia where tstamp < current_timestamp")
        return row.max_date ? ((Timestamp) row.max_date).toLocalDateTime() : null
    }

    void insert(Incident incident) {
        def params = incident.properties
        params.incidenTypeCode = incident.incidentType.code
        params.tstamp = new Timestamp(incident.tstamp.toEpochMilli())
        params.latFloat = Double.parseDouble(incident.lat)
        params.lngFloat = Double.parseDouble(incident.lng)
        sql.executeInsert('''INSERT INTO incidencia
            (fecha_incidente, hora_incidente, delito, location, needs_recoding, latitude, longitude, tstamp)
            values (:date :: date, :time :: time, :incidenTypeCode,
            st_snaptogrid(ST_SetSRID(ST_Point(:lngFloat, :latFloat), 4326), 0.000001),
            :needsRecoding, :lat, :lng, :tstamp)
        ''', params)
    }

}