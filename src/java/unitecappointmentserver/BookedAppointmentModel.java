/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unitecappointmentserver;

/**
 *
 * @author zezo
 */
public class BookedAppointmentModel {
    
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String date;
    private final String start;
    private final String end;
    private final String status;
    
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
