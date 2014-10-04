package crime.heatmap

/**
 * Created with IntelliJ IDEA.
 * User: victor
 * Date: 2/23/14
 * Time: 1:03 PM
 * To change this template use File | Settings | File Templates.
 */
enum IncidentType {

    HOMICIDE(1),
    RAPE(2),
    ROBBERY(3),
    AGGRAVATED_ASSAULT(4),
    BURGLARY(5),
    LARCENY_THEFT(6),
    MOTOR_VEHICLE_THEFT(7),
    ARSON(8);

    private int code;

    IncidentType(int code) {
        this.code = code
    }

    public int getCode() {
        return this.code;
    }

    static IncidentType forCode(int code) {
        for (IncidentType t : values()) {
            if (t.code == code) {
                return t;
            }
        }
    }

    static IncidentType forCodeString(String code) {
        forCode(Integer.parseInt(code))
    }
}

