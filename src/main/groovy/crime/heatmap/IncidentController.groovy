package crime.heatmap

import org.geojson.GeometryCollection
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Created with IntelliJ IDEA.
 * User: victor
 * Date: 2/23/14
 * Time: 11:32 AM
 * To change this template use File | Settings | File Templates.
 */
@RestController
class IncidentController {

    @Autowired
    private IncidentDaoJdbc incidentDao;

    @RequestMapping('/incidentes.json')
    GeometryCollection getIncidents(@RequestBody IncidentCriteria criteria) {
        return incidentDao.getIncidentsAsGeoJson(criteria)
    }

    @RequestMapping('/tipologia.json')
    def getIncidentTypes() {
        return [
                incidentTypes: IncidentType.values().collect { t ->
                    ["name": t.name(), "code": t.code]
                }
        ]
    }
}
