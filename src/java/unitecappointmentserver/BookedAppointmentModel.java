package unitecappointmentserver;

/**
 * Data model for a booked appointment
 *
 * @author Marzouq Almarzooq (1380949)
 * @author Nawaf Altuwayjiri (1377387)
 */
public class BookedAppointmentModel {
    
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String date;
    private final String start;
    private final String end;
    private final String status;
    
    /**
     * Constructor
     *
     * @param username      booking username
     * @param firstName     booking user first name
     * @param date          appointment date
     * @param lastName      booking user last name
     * @param start         start time
     * @param end           end time
     * @param status        status of booking
     */
    public BookedAppointmentModel(String username, String firstName, String lastName, String date, String start, String end, String status) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.date = date;
        this.start = start;
        this.end = end;
        this.status = status;
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

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }
    
}
