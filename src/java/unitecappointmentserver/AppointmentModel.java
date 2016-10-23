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
public class AppointmentModel {
    
    private final String date;
    private final String start;
    private final String end;
    
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
