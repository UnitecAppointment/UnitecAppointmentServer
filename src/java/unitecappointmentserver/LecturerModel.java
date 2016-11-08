package unitecappointmentserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Data model for a lecturer
 *
 * @author Marzouq Almarzooq (1380949)
 * @author Nawaf Altuwayjiri (1377387)
 */
public class LecturerModel {

    private final String username;
    private final String title;
    private final String firstName;
    private final String lastName;
    private final String department;
    private final List<String> subjects;

    /**
     * Constructor
     *
     * @param username lecturer username
     * @param title lecturer title
     * @param firstName lecturer first name
     * @param lastName lecturer last name
     * @param department lecturer department
     */
    public LecturerModel(String username, String title, String firstName,
            String lastName, String department) {
        this.username = username;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
        subjects = new ArrayList<>();
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @return the department
     */
    public String getDepartment() {
        return department;
    }

    /**
     * @return the list of lecturer subjects
     */
    public List<String> getSubjects() {
        return subjects;
    }

    /**
     * @param subject   subject to add to the list of subjects
     */
    public void addSubject(String subject) {
        subjects.add(subject);
    }
}
