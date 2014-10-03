package crime.heatmap

import groovyx.net.http.RESTClient
import net.sf.json.JSONArray
import net.sf.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

import java.time.format.DateTimeFormatter

/**
 * Created by victor on 10/2/14.
 */
@Component
class UpdateTasks {

    @Autowired
    private IncidentDaoJdbc incidentDao

    @Autowired
    private SocrataService socrataService

    @Scheduled(fixedDelay = 30_000L)
    void update() {
        def maxDate = incidentDao.getMaxDate()

        socrataService.getIncidentsLaterThanDate(maxDate) { List json ->
            json.each { Map it ->
                Incident incident = new Incident(
                        incidentType: IncidentType.forCode(Integer.parseInt(it.delito)),
                        dateTime: DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(it.fecha),
                        lat: (float) Double.parseDouble(it.location.latitude),
                        lng: (float) Double.parseDouble(it.location.longitude),
                )
                incidentDao.insert(incident)
            }
        }
    }
}
