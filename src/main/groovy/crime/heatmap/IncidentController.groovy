package crime.heatmap

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

import javax.servlet.http.HttpServletResponse

/**
 * Created with IntelliJ IDEA.
 * User: victor
 * Date: 2/23/14
 * Time: 11:32 AM
 * To change this template use File | Settings | File Templates.
 */
@Controller
class IncidentController {

    @Autowired
    private IncidentDaoJdbc incidentDao;

    @RequestMapping('/incidentes.json')
    void getIncidents(@RequestBody IncidentCriteria criteria,
                      HttpServletResponse response) {
        response.setContentType("application/json")
        String json = incidentDao.getIncidentsAsGeoJson(criteria)
        response.getWriter().write(json)
    }

    @RequestMapping('/tipologia.json')
    @ResponseBody
    def getIncidentTypes() {
        return [
                incidentTypes: IncidentType.values().collect { t ->
                    ["name": t.name(), "code": t.code]
                }
        ]
    }
}
