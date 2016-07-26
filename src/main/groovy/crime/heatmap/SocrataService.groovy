package crime.heatmap

import com.socrata.api.HttpLowLevel
import com.socrata.api.Soda2Consumer
import com.socrata.model.soql.CompositeExpression
import com.socrata.model.soql.CompositeOperations
import com.socrata.model.soql.ConditionalExpression
import com.socrata.model.soql.OrderByClause
import com.socrata.model.soql.SoqlQuery
import com.socrata.model.soql.SortOrder
import com.sun.jersey.api.client.ClientResponse
import groovy.json.JsonSlurper
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

import java.time.*
import java.time.format.DateTimeFormatter
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by victor on 10/3/14.
 */
@Component
class SocrataService {

    private static final Logger log = LoggerFactory.getLogger(SocrataService.class)
    private static final Pattern MISSING_ZERO_PATTERN = ~/^\d:/

    private Soda2Consumer consumer = Soda2Consumer.newConsumer("https://data.pr.gov")

    //'https://data.pr.gov/resource/pzaz-tkx9.json';

    int getIncidentsLaterThanDate(LocalDateTime maxDate, Closure successHandler) {

        //To get a raw String of the results
        String defaultMaxDate = '1980-01-01 00:00:00'
        ClientResponse response = consumer.query('pzaz-tkx9.json',
                HttpLowLevel.JSON_TYPE,
                new SoqlQuery(
                        null,
                        new CompositeExpression(CompositeOperations.AND, Arrays.asList(
                                new ConditionalExpression("fecha < '${DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())}'"),
                                new ConditionalExpression("fecha > '${maxDate ? DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(maxDate) : defaultMaxDate}'") )),
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

            try {

                log.info(it.toString())

                LocalDate date = LocalDateTime.parse(it.fecha, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate()

                String horaStr = '00:00:00'
                if (StringUtils.isNotBlank(it.hora)) {
                    if (it.hora ==~ MISSING_ZERO_PATTERN) {
                        horaStr = '0' + it.hora
                    }
                }
                LocalTime time = LocalTime.parse(horaStr, DateTimeFormatter.ISO_LOCAL_TIME)
                Instant instant = LocalDateTime.of(date, time).toInstant(ZoneOffset.ofHours(-4))

                IncidentType incidentType = IncidentType.forCodeString(it.delito)
                Incident incident = new Incident()
                incident.incidentType = incidentType
                incident.tstamp = instant
                incident.lat = it.point_x ?: it.location?.latitude
                incident.lng = it.point_y ?: it.location?.longitude
                incident.needsRecoding = Boolean.valueOf(it.location?.needs_recoding ?: "false")
                incident.date = it.fecha
                incident.time = it.hora
                if (!incident.lat || !incident.lng) {
                    log.warn('no coordinates! {}', it)
                    return
                }
                successHandler(incident)
            } catch (Exception e) {
                log.warn("Data conversion exception", e)
            }
        }
        return json.size()

    }
}
