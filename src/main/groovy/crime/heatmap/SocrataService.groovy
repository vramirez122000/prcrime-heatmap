package crime.heatmap

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import groovyx.net.http.URIBuilder
import net.sf.json.JSONArray
import org.springframework.stereotype.Component

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

/**
 * Created by victor on 10/2/14.
 */
@Component
class SocrataService {

    private HTTPBuilder socrata = new HTTPBuilder('https://data.pr.gov')

    SocrataService() {
        socrata.headers['X-App-Token'] = 'W1m5bImQlZ0rtFhO9rDHb6tGQ'
    }

    def getIncidentsLaterThanDate(TemporalAccessor maxDate, Closure successHandler) {
        socrata.request(Method.GET, ContentType.TEXT) { req ->
            uri.path = '/resource/incidencia-crime-map.json'
            uri.query= [
                    $where: "fecha > '${DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(maxDate)}'",
                    $order: "fecha ASC"
            ]
            println "${uri.path}?\$where=fecha > '${DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(maxDate)}'&\$order=fecha ASC"

            headers.Accept = 'application/json'
            response.success = { resp, reader ->
                successHandler(JSONArray.fromObject(reader.text))
            }
        }
    }

    public static void main(String[] args) {
        new SocrataService().getIncidentsLaterThanDate(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse('2014-01-01T00:00:00'))
    }
}
