package crime.heatmap

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

import java.time.LocalDateTime
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

    @Scheduled(fixedDelay = 3_600_000L)
    void update() {
        LocalDateTime maxDate = incidentDao.getMaxDate()
        socrataService.getIncidentsLaterThanDate(maxDate) { Incident incident ->
            incidentDao.insert(incident)
        }
    }
}
