package crime.heatmap

class IncidentCriteria {

    List<IncidentType> incidentTypes = new ArrayList<>(8)
    Float bboxXmin
    Float bboxYmin
    Float bboxXmax
    Float bboxYmax
    String timeMin
    String timeMax
    Date dateMin
    Date dateMax
    List<Integer> daysOfWeek = new ArrayList<>(7)
    Integer limit

}