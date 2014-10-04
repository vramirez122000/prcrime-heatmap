package crime.heatmap

import com.socrata.api.HttpLowLevel
import com.socrata.api.Soda2Consumer
import com.socrata.model.soql.ConditionalExpression
import com.socrata.model.soql.OrderByClause
import com.socrata.model.soql.SoqlQuery
import com.socrata.model.soql.SortOrder
import com.sun.jersey.api.client.ClientResponse
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

import java.time.*
import java.time.format.DateTimeFormatter

/**
 * Created by victor on 10/3/14.
 */
@Component
class SocrataService {

    private static final Logger log = LoggerFactory.getLogger(SocrataService.class)

    private Soda2Consumer consumer = Soda2Consumer.newConsumer("https://data.pr.gov")

    int getIncidentsLaterThanDate(LocalDateTime maxDate, Closure successHandler) {

        //To get a raw String of the results
        ClientResponse response = consumer.query('incidencia-crime-map',
                HttpLowLevel.JSON_TYPE,
                new SoqlQuery(
                        null,
                        maxDate ? new ConditionalExpression("fecha > '${DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(maxDate)}'") : null,
                        null,
                        null,
                        Arrays.asList(new OrderByClause(SortOrder.Ascending, 'fecha')),
                        null,
                        null,
                        null));
        String payload = response.getEntity(String.class);
        def slurper = new JsonSlurper()
        def json = slurper.parseText(payload)
        json.each {
            log.info(it.toString())

            LocalDate date = LocalDateTime.parse(it.fecha, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate()
            LocalTime time = LocalTime.parse(it.hora, DateTimeFormatter.ISO_LOCAL_TIME)
            Instant instant  = LocalDateTime.of(date, time).toInstant(ZoneOffset.ofHours(-4))

            IncidentType incidentType = IncidentType.forCodeString(it.delito)
            Incident incident = new Incident()
            incident.incidentType = incidentType
            incident.tstamp = instant
            incident.lat = it.point_x ?: it.location?.latitude
            incident.lng = it.point_y ?: it.location?.longitude
            incident.needsRecoding = Boolean.valueOf(it.location?.needs_recoding ?: "false")
            incident.date = it.fecha
            incident.time = it.hora
            if(!incident.lat || !incident.lng) {
                log.warn('no coordinates! {}', it)
                return
            }
            successHandler(incident)
        }
        return json.size()

    }
}
