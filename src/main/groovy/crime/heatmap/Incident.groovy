package crime.heatmap

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Created by victor on 10/3/14.
 */
class Incident {

    IncidentType incidentType
    Instant tstamp
    String date
    String time
    String lat
    String lng
    boolean needsRecoding
}
