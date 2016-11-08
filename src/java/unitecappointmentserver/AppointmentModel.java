package unitecappointmentserver;

/**
 * Data model for an available appointment
 *
 * @author Marzouq Almarzooq (1380949)
 * @author Nawaf Altuwayjiri (1377387)
 */
public class AppointmentModel {
    
    private final String date;
    private final String start;
    private final String end;
    
    /**
     * Constructor
     *
     * @param date      appointment date
     * @param start     start time
     * @param end       end time
     */
    public AppointmentModel(String date, String start, String end) {
        this.date = date;
        this.start = start;
        this.end = end;
    }

    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * @return the start
     */
    public String getStart() {
        return start;
    }

    /**
     * @return the end
     */
    public String getEnd() {
        return end;
    }
    
}
