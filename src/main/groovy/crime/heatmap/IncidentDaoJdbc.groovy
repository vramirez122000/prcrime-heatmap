package crime.heatmap

import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import javax.sql.DataSource

@Repository
class IncidentDaoJdbc {

    private Sql sql;

    @Autowired
    void setDataSource(DataSource dataSource) {
        this.sql = new Sql(dataSource);
    }

    String getIncidentsAsGeoJson(IncidentCriteria criteria) {
        boolean first = true;
        StringBuilder jsonBuilder = new StringBuilder('{"type":"GeometryCollection","geometries":[')
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

        //queryBuilder.whereBetween('hora_incidente', criteria.timeMin?:'00:00:00', criteria.timeMax?:'11:59:59')
        queryBuilder.limit(1000)

        sql.eachRow(queryBuilder.sql(), queryBuilder.values(), {row ->
            if(first) {
                first = false
            } else {
                jsonBuilder.append(',')
            }
            jsonBuilder.append(row.json)
        })
        jsonBuilder.append(']}')
        return jsonBuilder.toString();
    }

}