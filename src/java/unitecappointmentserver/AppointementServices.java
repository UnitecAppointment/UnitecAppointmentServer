/**
 * SOAP Web Services Class
 *
 * @author      Marzouq Almarzooq (1380949)
 * @author      Nawaf Altuwayjiri (1377387)
 */
package unitecappointmentserver;

import java.io.IOException;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.json.JsonArrayBuilder;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

/**
 * Web Service definition and implementation class
 *
 * @author Marzouq Almarzooq (1380949)
 * @author Nawaf Altuwayjiri (1377387)
 */
@WebService(serviceName = "AppointementServices")                       //web services name
public class AppointementServices {

    @Resource(lookup = "mail/AppointmentMailSession")                   //mail session lookup from server
    private Session mailSession;                                        //mail session object
    private final Connection connection;                                //db connection
    //SQL prepared statements
    private final PreparedStatement getStudent;                         //get students 
    private final PreparedStatement getLecturer;                        //get lecturer 
    private final PreparedStatement getLecturerAppointmentsByDate;      //get lecturer appointments by date 
    private final PreparedStatement addLecturerAppointment;             //get lecturer appointments 
    private final PreparedStatement getAssignedLecturers;               //get student assigned lecturers  
    private final PreparedStatement getAvailableAppointments;           //get available appointments
    private final PreparedStatement getAppointment;                     //get specific appointment
    private final PreparedStatement bookAppointment;                    //book specific appointment
    private final PreparedStatement getStudentBookedAppointment;        //get booked appointments for a student
    private final PreparedStatement getLecturerBookedAppointment;       //get booked appointments for a lecturer
    private final PreparedStatement studentCancelAppointment;           //cancel a booked appointment for a student
    private final PreparedStatement lecturerCancelAppointment;          //cancel a booked appointment for a lecturer
    //DB table names and table attributes
    private final String dbStudentTable;
    private final String dbLecturerTable;
    private final String dbAppointmentTable;
    private final String dbStudentLecturerAssignmentTable;
    private final String dbUsernameAtt;
    private final String dbTitleAtt;
    private final String dbFirstNameAtt;
    private final String dbLastNameAtt;
    private final String dbDepartmentAtt;
    private final String dbPasswordAtt;
    private final String dbSaltAtt;
    private final String dbDateAtt;
    private final String dbStartAtt;
    private final String dbEndAtt;
    private final String dbLecturerUserNameAtt;
    private final String dbStudentUserNameAtt;
    private final String dbIsActiveAtt;
    private final String dbSubjectAtt;
    private final String dbEmailAtt;

    /**
     * Web services constructor, connects to db, and prepares all SQL statements
     *
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     * @throws java.sql.SQLException
     */
    public AppointementServices() throws IOException, ClassNotFoundException, SQLException {
        //load db properties file 
        Properties properties = new Properties();
        properties.loadFromXML(getClass().getResourceAsStream("DBConfig.xml"));
        //db driver and name
        String dbDriver = properties.get("dbDriver").toString();
        String dbUrl = properties.get("dbUrl").toString();
        //obtain sb table names and table attributes from loaded db properties file
        dbStudentTable = properties.get("dbStudentTable").toString();
        dbLecturerTable = properties.get("dbLecturerTable").toString();
        dbAppointmentTable = properties.get("dbAppointmentTable").toString();
        dbStudentLecturerAssignmentTable = properties.get("dbStudentLecturerAssignmentTable").toString();
        dbUsernameAtt = properties.get("dbUsernameAtt").toString();
        dbTitleAtt = properties.get("dbTitleAtt").toString();
        dbFirstNameAtt = properties.get("dbFirstNameAtt").toString();
        dbLastNameAtt = properties.get("dbLastNameAtt").toString();
        dbDepartmentAtt = properties.get("dbDepartmentAtt").toString();
        dbPasswordAtt = properties.get("dbPasswordAtt").toString();
        dbSaltAtt = properties.get("dbSaltAtt").toString();
        dbDateAtt = properties.get("dbDateAtt").toString();
        dbStartAtt = properties.get("dbStartAtt").toString();
        dbEndAtt = properties.get("dbEndAtt").toString();
        dbLecturerUserNameAtt = properties.get("dbLecturerUserNameAtt").toString();
        dbStudentUserNameAtt = properties.get("dbStudentUserNameAtt").toString();
        dbIsActiveAtt = properties.get("dbIsActiveAtt").toString();
        dbSubjectAtt = properties.getProperty("dbSubjectAtt");
        dbEmailAtt = properties.getProperty("dbEmailAtt");
        //connect to db using driver, location, and db admin user details
        String dbUserName = properties.get("dbusername").toString();
        String dbPassword = properties.get("dbpassword").toString();
        Class.forName(dbDriver);
        connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
        //sql prepared statement definition
        getStudent = connection.prepareStatement("SELECT * FROM " + dbStudentTable + " WHERE " + dbUsernameAtt + " = ?");
        getLecturer = connection.prepareStatement("SELECT * FROM " + dbLecturerTable + " WHERE " + dbUsernameAtt + " = ?");
        getLecturerAppointmentsByDate = connection.prepareStatement("SELECT * FROM "
                + dbAppointmentTable + " WHERE " + dbLecturerUserNameAtt + " = ? AND " + dbDateAtt + " = ?");
        addLecturerAppointment = connection.prepareStatement("INSERT INTO " + dbAppointmentTable + " ("
                + dbDateAtt + ", " + dbStartAtt + ", " + dbEndAtt + ", " + dbLecturerUserNameAtt + ", "
                + dbIsActiveAtt + ") VALUES (?, ?, ?, ?, ?)");
        getAssignedLecturers = connection.prepareStatement("SELECT * FROM "
                + dbStudentLecturerAssignmentTable + " WHERE " + dbStudentUserNameAtt + " = ?");
        getAvailableAppointments = connection.prepareStatement("SELECT * FROM "
                + dbAppointmentTable + " WHERE " + dbLecturerUserNameAtt + " = ? "
                + "AND " + dbIsActiveAtt + " = 'available'");
        getAppointment = connection.prepareStatement("SELECT * FROM " + dbAppointmentTable + " WHERE "
                + dbDateAtt + " = ? AND " + dbStartAtt + " = ? AND " + dbEndAtt + " = ? AND "
                + dbLecturerUserNameAtt + " = ?");
        bookAppointment = connection.prepareStatement("UPDATE " + dbAppointmentTable
                + " SET " + dbIsActiveAtt + " = 'booked', "
                + dbStudentUserNameAtt + " = ? WHERE " + dbLecturerUserNameAtt + " = ? AND "
                + dbDateAtt + " = ? AND " + dbStartAtt + " = ? AND " + dbEndAtt + " = ?");
        getStudentBookedAppointment = connection.prepareStatement("SELECT * FROM "
                + dbAppointmentTable + " WHERE " + dbStudentUserNameAtt + " = ? "
                + "AND (" + dbIsActiveAtt + " = 'booked' OR " + dbIsActiveAtt + " = 'cancelled')");
        getLecturerBookedAppointment = connection.prepareStatement("SELECT * FROM "
                + dbAppointmentTable + " WHERE " + dbLecturerUserNameAtt + " = ? "
                + "AND " + dbIsActiveAtt + " = 'booked'");
        studentCancelAppointment = connection.prepareStatement("UPDATE " + dbAppointmentTable
                + " SET " + dbIsActiveAtt + " = 'available' WHERE " + dbLecturerUserNameAtt + " = ? AND "
                + dbDateAtt + " = ? AND " + dbStartAtt + " = ? AND " + dbEndAtt + " = ?");
        lecturerCancelAppointment = connection.prepareStatement("UPDATE " + dbAppointmentTable
                + " SET " + dbIsActiveAtt + " = 'cancelled' WHERE " + dbStudentUserNameAtt + " = ? AND "
                + dbDateAtt + " = ? AND " + dbStartAtt + " = ? AND " + dbEndAtt + " = ?");
    }

    /**
     * Web service operation for logging in user
     *
     * @param userLoginDetails
     * @return login result
     */
    @WebMethod(operationName = "login")
    public String login(@WebParam(name = "userLoginDetails") String userLoginDetails) {
        //obtain username and password from json
        JsonObject loginDetails = Json.createReader(new StringReader(userLoginDetails)).readObject();
        JsonObject result;
        String username = loginDetails.getString("username");
        String password = loginDetails.getString("password");
        boolean isStudentFound = false;
        boolean isLecturerFound = false;
        ResultSet resultSet = null;
        //lookup user record using username
        try {
            getStudent.setString(1, username);
            resultSet = getStudent.executeQuery();
            isStudentFound = resultSet.first();
            if (!isStudentFound) {
                getLecturer.setString(1, username);
                resultSet = getLecturer.executeQuery();
                isLecturerFound = resultSet.first();
            }
        } catch (SQLException ex) {
            Logger.getLogger(AppointementServices.class.getName()).log(Level.SEVERE, null, ex);
        }

        if ((isStudentFound || isLecturerFound) && resultSet != null) {
            //if user name found
            try {
                //obtain user details
                String firstName = resultSet.getString(dbFirstNameAtt);
                String lastName = resultSet.getString(dbLastNameAtt);
                String userPassword = resultSet.getString(dbPasswordAtt);
                String userSalt = resultSet.getString(dbSaltAtt);
                
                //obtain salt for user and decode from string to bytes
                Base64.Decoder decoder = Base64.getDecoder();
                byte[] salt = decoder.decode(userSalt);

                //hash supplied password and salt
                KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
                SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                byte[] hash = secretKeyFactory.generateSecret(keySpec).getEncoded();

                //encode hash back to string
                Base64.Encoder encoder = Base64.getEncoder();
                String hashedPassword = encoder.encodeToString(hash);

                //compare string hash with password hash from db
                if (hashedPassword.compareTo(userPassword) == 0) {
                    //password correct
                    if (isStudentFound) {
                        //student user succesfyul login
                        result = Json.createObjectBuilder()
                                .add("result", "true")
                                .add("user", "student")
                                .add("firstName", firstName)
                                .add("lastName", lastName).build();
                    } else {
                        //lecturer user successful login
                        String title = resultSet.getString(dbTitleAtt);
                        String department = resultSet.getString(dbDepartmentAtt);
                        result = Json.createObjectBuilder()
                                .add("result", "true")
                                .add("user", "lecturer")
                                .add("title", title)
                                .add("firstName", firstName)
                                .add("lastName", lastName)
                                .add("department", department).build();
                    }
                } else {
                    //password incorrect
                    result = Json.createObjectBuilder()
                            .add("result", "false").build();
                }
            } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
                //server error
                Logger.getLogger(AppointementServices.class.getName()).log(Level.SEVERE, null, ex);
                result = Json.createObjectBuilder()
                        .add("result", "error").build();
            }
        } else {
            //no user found
            result = Json.createObjectBuilder()
                    .add("result", "false").build();
        }
        return result.toString();
    }

    /**
     * Web service operation for creating appointment
     *
     * @param appointmentDetails
     * @return make appointment result
     */
    @WebMethod(operationName = "createAppointment")
    public String createAppointment(@WebParam(name = "appointmentDetails") String appointmentDetails) {
        //obtain create appointment detials
        JsonObject makeAppointmentDetails = Json.createReader(new StringReader(appointmentDetails)).readObject();
        String username = makeAppointmentDetails.getString("username");
        String day = makeAppointmentDetails.getString("day");
        String month = makeAppointmentDetails.getString("month");
        String year = makeAppointmentDetails.getString("year");
        String startHour = makeAppointmentDetails.getString("startHour");
        String startMinute = makeAppointmentDetails.getString("startMinute");
        String endHour = makeAppointmentDetails.getString("endHour");
        String endMinute = makeAppointmentDetails.getString("endMinute");
        String isDaily = makeAppointmentDetails.getString("isDaily");
        String isWeekly = makeAppointmentDetails.getString("isWeekly");
        String recurrence = makeAppointmentDetails.getString("recurrence");

        //number of recurrences if creating appointment has multiple recurrences 
        int repeat;
        if (recurrence.isEmpty()) {
            repeat = 1;
        } else {
            repeat = Integer.parseInt(recurrence);
        }

        //date time formating to store in db
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        //repeat for number of ocurrences
        for (int i = 0; i < repeat; i++) {
            try {
                //obtain date objects for caluclations from string date and time given format
                Date appointmentDate = dateFormat.parse(year + "-" + month + "-" + day);
                Date appointmentStart = timeFormat.parse(startHour + ":" + startMinute);
                Date appointmentEnd = timeFormat.parse(endHour + ":" + endMinute);

                //determine type of reocuurence weekly or dialy
                Calendar calendar = Calendar.getInstance();
                if (isDaily.compareTo("true") == 0) {
                    calendar.setTime(appointmentDate);
                    calendar.add(Calendar.DATE, i);
                    appointmentDate = calendar.getTime();
                } else if (isWeekly.compareTo("true") == 0) {
                    calendar.setTime(appointmentDate);
                    calendar.add(Calendar.DATE, i * 7);
                    appointmentDate = calendar.getTime();
                }

                // get lecturer current appointments
                getLecturerAppointmentsByDate.setString(1, username);
                getLecturerAppointmentsByDate.setString(2, dateFormat.format(appointmentDate));
                ResultSet resultSet = getLecturerAppointmentsByDate.executeQuery();
                boolean isFound = resultSet.first();

                if (isFound) {
                    //if lecturer has existing appointments then check for clash
                    do {
                        Date startTime = timeFormat.parse(resultSet.getString(dbStartAtt));
                        Date endTime = timeFormat.parse(resultSet.getString(dbEndAtt));
                        if (startTime.before(appointmentEnd) && appointmentStart.before(endTime)) {
                            //clash exist cannot make appointment
                            return Json.createObjectBuilder().add("result", "false").build().toString();
                        }
                    } while (resultSet.next());
                }

                //no clash intialise prepare statement with new appointment details
                addLecturerAppointment.setString(1, dateFormat.format(appointmentDate));
                addLecturerAppointment.setString(2, timeFormat.format(appointmentStart));
                addLecturerAppointment.setString(3, timeFormat.format(appointmentEnd));
                addLecturerAppointment.setString(4, username);
                addLecturerAppointment.setString(5, "available");
                //create appointment
                addLecturerAppointment.executeUpdate();

            } catch (ParseException | SQLException ex) {
                //server error
                Logger.getLogger(AppointementServices.class.getName()).log(Level.SEVERE, null, ex);
                return Json.createObjectBuilder().add("result", "error").build().toString();
            }
        }
        //appoitment created return conformation result
        return Json.createObjectBuilder().add("result", "true").build().toString();
    }

    /**
     * Web service operation for obtaining lecturers for a given student
     *
     * @param userDetails
     * @return list of assigned lecturers to student
     */
    @WebMethod(operationName = "makeAppointment")
    public String makeAppointment(@WebParam(name = "userDetails") String userDetails) {
        try {
            //obtain student details
            JsonObject makeAppointmentDetails = Json.createReader(new StringReader(userDetails)).readObject();
            String username = makeAppointmentDetails.getString("username");
            //get lecturers for student from db
            getAssignedLecturers.setString(1, username);
            ResultSet resultSet = getAssignedLecturers.executeQuery();
            if (resultSet.first()) {
                //student has assigned lecturers
                Map<String, LecturerModel> lecturers = new HashMap<>();
                //obtain details for each assigned lecturer
                do {
                    String lecturerUsername = resultSet.getString(dbLecturerUserNameAtt);
                    LecturerModel lecturer;
                    if (!lecturers.containsKey(lecturerUsername)) {
                        getLecturer.setString(1, lecturerUsername);
                        ResultSet resultSet1 = getLecturer.executeQuery();
                        resultSet1.first();
                        lecturer = new LecturerModel(
                                lecturerUsername, resultSet1.getString(dbTitleAtt),
                                resultSet1.getString(dbFirstNameAtt),
                                resultSet1.getString(dbLastNameAtt),
                                resultSet1.getString(dbDepartmentAtt));
                        lecturers.put(lecturerUsername, lecturer);
                    }

                    lecturer = lecturers.get(lecturerUsername);

                    lecturer.addSubject(resultSet.getString(dbSubjectAtt));
                } while (resultSet.next());
                //build json with all lecturer details
                JsonArrayBuilder jsonLecturersArrayBuilder = Json.createArrayBuilder();
                for (String lecturer : lecturers.keySet()) {
                    JsonArrayBuilder jsonSubjectsArrayBuilder = Json.createArrayBuilder();

                    for (String subject : lecturers.get(lecturer).getSubjects()) {
                        jsonSubjectsArrayBuilder.add(Json.createObjectBuilder().
                                add("subject", subject));
                    }

                    jsonLecturersArrayBuilder.add(Json.createObjectBuilder().
                            add("username", lecturers.get(lecturer).getUsername()).
                            add("title", lecturers.get(lecturer).getTitle()).
                            add("firstName", lecturers.get(lecturer).getFirstName()).
                            add("lastName", lecturers.get(lecturer).getLastName()).
                            add("department", lecturers.get(lecturer).getDepartment()).
                            add("subjects", jsonSubjectsArrayBuilder.build()));
                }
                //return json string with assigned lecturers
                return Json.createObjectBuilder().
                        add("result", "true").
                        add("lecturers", jsonLecturersArrayBuilder.build()).build().toString();
            } else {
                //student has no assigned lectures
                return Json.createObjectBuilder().
                        add("result", "false").build().toString();
            }
        } catch (SQLException ex) {
            //server error
            Logger.getLogger(AppointementServices.class.getName()).log(Level.SEVERE, null, ex);
            return Json.createObjectBuilder().add("result", "error").build().toString();
        }
    }

    /**
     * Web service operation to obtain available appointments for a specific lecturer 
     *
     * @param userDetails
     * @return list of available appointments for a specific lecturer
     */
    @WebMethod(operationName = "getAvailableAppointment")
    public String getAvailableAppointment(@WebParam(name = "userDetails") String userDetails) {
        try {
            //obtain lecturer details
            JsonObject makeAppointmentDetails = Json.createReader(new StringReader(userDetails)).readObject();
            String username = makeAppointmentDetails.getString("username");
            //look up db for avaialable appointment for lecturer
            getAvailableAppointments.setString(1, username);
            ResultSet resultSet = getAvailableAppointments.executeQuery();
            if (resultSet.first()) {
                //lecturer has available appointments
                List<AppointmentModel> appointments = new ArrayList<>();
                //obtain available appointments
                do {
                    appointments.add(new AppointmentModel(
                            resultSet.getString(dbDateAtt),
                            resultSet.getString(dbStartAtt),
                            resultSet.getString(dbEndAtt)));
                } while (resultSet.next());
                //build json result containing available appointment details
                JsonArrayBuilder jsonAppointmentArrayBuilder
                        = Json.createArrayBuilder();
                for (AppointmentModel appointment : appointments) {
                    jsonAppointmentArrayBuilder.add(Json.createObjectBuilder().
                            add("date", appointment.getDate()).
                            add("start", appointment.getStart()).
                            add("end", appointment.getEnd()).build());
                }
                //return json string with avaialble appointments
                return Json.createObjectBuilder().add("result", "true").
                        add("appointments", jsonAppointmentArrayBuilder.build()
                        ).build().toString();

            } else {
                //no availbale appointments for given lecturer
                return Json.createObjectBuilder().
                        add("result", "false").build().toString();
            }
        } catch (SQLException ex) {
            //server error
            Logger.getLogger(AppointementServices.class.getName()).log(Level.SEVERE, null, ex);
            return Json.createObjectBuilder().add("result", "error").build().toString();
        }
    }

    /**
     * Web service operation to book an appointment
     *
     * @param appointmentDetails
     * @return book appointment result
     */
    @WebMethod(operationName = "bookAppointment")
    public String bookAppointment(@WebParam(name = "appointmentDetails") String appointmentDetails) {
        try {
            //obtain appointment details for booking
            JsonObject makeAppointmentDetails = Json.createReader(new StringReader(appointmentDetails)).readObject();
            String studentUsername = makeAppointmentDetails.getString("studentUsername");
            String lecturerUserName = makeAppointmentDetails.getString("lecturerUsername");
            String date = makeAppointmentDetails.getString("date");
            String start = makeAppointmentDetails.getString("start");
            String end = makeAppointmentDetails.getString("end");

            //look up available appointemnt
            getAppointment.setString(1, date);
            getAppointment.setString(2, start);
            getAppointment.setString(3, end);
            getAppointment.setString(4, lecturerUserName);
            ResultSet resultSet = getAppointment.executeQuery();
            if (resultSet.first()) {
                String isActive = resultSet.getString(dbIsActiveAtt);
                //check appointment is available
                if (isActive.compareTo("available") == 0) {
                    //appointment is available then proceed with booking by SQL update statement
                    bookAppointment.setString(1, studentUsername);
                    bookAppointment.setString(2, lecturerUserName);
                    bookAppointment.setString(3, date);
                    bookAppointment.setString(4, start);
                    bookAppointment.setString(5, end);
                    //get lecturer and student details for booked appointment
                    int result = bookAppointment.executeUpdate();
                    getLecturer.setString(1, lecturerUserName);
                    ResultSet resultSet1 = getLecturer.executeQuery();
                    getStudent.setString(1, studentUsername);
                    ResultSet resultSet2 = getStudent.executeQuery();
                    if (result != 0 && resultSet1.first() && resultSet2.first()) {
                        //send booked appointment, student and lecturer details to lecturer email
                        String lecturerEmail = resultSet1.getString(dbEmailAtt);
                        String studentFirstName = resultSet2.getString(dbFirstNameAtt);
                        String studentLastName = resultSet2.getString(dbLastNameAtt);
                        MimeMessage message = new MimeMessage(mailSession);
                        try {
                            //create an email appointment with booking details
                            InternetAddress[] recipientAddress = {new InternetAddress(lecturerEmail)};
                            InternetAddress senderAddress = new InternetAddress(mailSession.getProperty("mail.from"));
                            message.addHeaderLine("method=REQUEST");
                            message.addHeaderLine("charset=UTF-8");
                            message.addHeaderLine("component=VEVENT");
                            message.setFrom(senderAddress);
                            message.setRecipients(Message.RecipientType.TO, recipientAddress);
                            message.setSubject("Appointment Request From " + studentFirstName + " "
                                    + studentLastName);
                            Calendar calendar = Calendar.getInstance();
                            message.setSentDate(calendar.getTime());
                            StringBuilder stringBuilder = new StringBuilder();
                            StringBuilder buffer = stringBuilder.append("BEGIN:VCALENDAR\n"
                                    + "PRODID:-//Microsoft Corporation//Outlook 9.0 MIMEDIR//EN\n"
                                    + "VERSION:2.0\n"
                                    + "METHOD:REQUEST\n"
                                    + "BEGIN:VEVENT\n"
                                    + "ATTENDEE;ROLE=REQ-PARTICIPANT;RSVP=FALSE:MAILTO:" + recipientAddress[0].toString() + "\n"
                                    + "ORGANIZER:MAILTO:" + senderAddress.toString() + "\n"
                                    + "DTSTART:" + date.replace("-", "") + "T" + start.replace(":", "") + "00Z\n"
                                    + "DTEND:" + date.replace("-", "") + "T" + end.replace(":", "") + "00Z\n"
                                    + "LOCATION:Lecturer office\n"
                                    + "TRANSP:OPAQUE\n"
                                    + "SEQUENCE:0\n"
                                    + "UID:040000008200E00074C5B7101A82E00800000000002FF466CE3AC5010000000000000000100\n"
                                    + " 000004377FE5C37984842BF9440448399EB02\n"
                                    + "DTSTAMP:" + calendar.get(Calendar.YEAR) + (calendar.get(Calendar.MONTH) + 1) + calendar.get(Calendar.DAY_OF_MONTH) + "T"
                                    + calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) + calendar.get(Calendar.SECOND) + "Z\n"
                                    + "CATEGORIES:Meeting\n"
                                    + "DESCRIPTION:Student meeting.\n\n"
                                    + "SUMMARY:Student meeting request\n"
                                    + "PRIORITY:5\n"
                                    + "CLASS:PUBLIC\n"
                                    + "BEGIN:VALARM\n"
                                    + "TRIGGER:PT1440M\n"
                                    + "ACTION:DISPLAY\n"
                                    + "DESCRIPTION:Reminder\n"
                                    + "END:VALARM\n"
                                    + "END:VEVENT\n"
                                    + "END:VCALENDAR");

                            // Create the message part
                            BodyPart messageBodyPart = new MimeBodyPart();

                            // Fill the message
                            messageBodyPart.setHeader("Content-Class", "urn:content-  classes:calendarmessage");
                            messageBodyPart.setHeader("Content-ID", "calendar_message");
                            messageBodyPart.setDataHandler(new DataHandler(
                                    new ByteArrayDataSource(buffer.toString(), "text/calendar")));// very important

                            // Create a Multipart
                            Multipart multipart = new MimeMultipart();

                            // Add part one
                            multipart.addBodyPart(messageBodyPart);

                            // Put parts in message
                            message.setContent(multipart);
                            Transport.send(message);
                        } catch (MessagingException | IOException ex) {
                            Logger.getLogger(AppointementServices.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        //return confirmation result that appointment is booked
                        return Json.createObjectBuilder().add("result", "true").build().toString();
                    } else {
                        return Json.createObjectBuilder().
                                add("result", "false").build().toString();
                    }
                } else {
                    //appointment cannot be booked result since it is not available
                    return Json.createObjectBuilder().add("result", "booked").build().toString();
                }
            } else {
                //server error
                return Json.createObjectBuilder().add("result", "error").build().toString();
            }

        } catch (SQLException ex) {
            //server error
            Logger.getLogger(AppointementServices.class.getName()).log(Level.SEVERE, null, ex);
            return Json.createObjectBuilder().add("result", "error").build().toString();
        }
    }

    /**
     * Web service operation to obtain booked appointments
     *
     * @param userDetails
     * @return list of booked appointments for a user
     */
    @WebMethod(operationName = "viewAppointment")
    public String viewAppointment(@WebParam(name = "userDetails") String userDetails) {
        try {
            //obtain user detail
            JsonObject viewAppointmentDetails = Json.createReader(new StringReader(userDetails)).readObject();
            String username = viewAppointmentDetails.getString("username");
            String type = viewAppointmentDetails.getString("type");
            ResultSet resultSet;
            //determine if user is student or lecturer
            if (type.compareToIgnoreCase("student") == 0) {
                //get booked appointments for student from db
                getStudentBookedAppointment.setString(1, username);
                resultSet = getStudentBookedAppointment.executeQuery();
            } else {
                //get booked appointments for lecturer from db
                getLecturerBookedAppointment.setString(1, username);
                resultSet = getLecturerBookedAppointment.executeQuery();
            }
            if (resultSet.first()) {
                //booked appointments exist
                List<BookedAppointmentModel> bookedAppointments = new ArrayList<>();
                if (type.compareToIgnoreCase("student") == 0) {
                    //create data model of booked appointments for student
                    do {
                        String lecturerUsername = resultSet.getString(dbLecturerUserNameAtt);
                        getLecturer.setString(1, lecturerUsername);
                        ResultSet resultSet1 = getLecturer.executeQuery();
                        resultSet1.first();
                        BookedAppointmentModel bookedAppointment = new BookedAppointmentModel(
                                lecturerUsername, resultSet1.getString(dbFirstNameAtt),
                                resultSet1.getString(dbLastNameAtt), resultSet.getString(dbDateAtt), resultSet.getString(dbStartAtt),
                                resultSet.getString(dbEndAtt), resultSet.getString(dbIsActiveAtt));
                        bookedAppointments.add(bookedAppointment);
                    } while (resultSet.next());
                } else {
                    //create data model of booked appointments for student
                    do {
                        String studentUsername = resultSet.getString(dbStudentUserNameAtt);
                        getStudent.setString(1, studentUsername);
                        ResultSet resultSet1 = getStudent.executeQuery();
                        resultSet1.first();
                        BookedAppointmentModel bookedAppointment = new BookedAppointmentModel(
                                studentUsername, resultSet1.getString(dbFirstNameAtt),
                                resultSet1.getString(dbLastNameAtt), resultSet.getString(dbDateAtt), resultSet.getString(dbStartAtt),
                                resultSet.getString(dbEndAtt), resultSet.getString(dbIsActiveAtt));
                        bookedAppointments.add(bookedAppointment);
                    } while (resultSet.next());
                }

                //build result json string from booked appointment data model
                JsonArrayBuilder jsonBookedAppointmentsArrayBuilder = Json.createArrayBuilder();
                for (BookedAppointmentModel appointment : bookedAppointments) {

                    jsonBookedAppointmentsArrayBuilder.add(Json.createObjectBuilder().
                            add("username", appointment.getUsername()).
                            add("firstName", appointment.getFirstName()).
                            add("lastName", appointment.getLastName()).
                            add("date", appointment.getDate()).
                            add("start", appointment.getStart()).
                            add("end", appointment.getEnd()).
                            add("status", appointment.getStatus()).build());
                }
                //successful result returns json with booked appointments
                return Json.createObjectBuilder().
                        add("result", "true").
                        add("appointments", jsonBookedAppointmentsArrayBuilder.build()).build().toString();
            } else {
                //no booked appointments
                return Json.createObjectBuilder().
                        add("result", "false").build().toString();
            }
        } catch (SQLException ex) {
            //server error
            Logger.getLogger(AppointementServices.class.getName()).log(Level.SEVERE, null, ex);
            return Json.createObjectBuilder().add("result", "error").build().toString();
        }
    }

    /**
     * Web service operation to cancel appointment
     *
     * @param appointmentDetails
     * @return
     */
    @WebMethod(operationName = "cancelAppointment")
    public String cancelAppointment(@WebParam(name = "appointmentDetails") String appointmentDetails) {
        try {
            //obtain appointement details from json string
            JsonObject cancelAppointmentDetails = Json.createReader(new StringReader(appointmentDetails)).readObject();
            String username = cancelAppointmentDetails.getString("username");
            String date = cancelAppointmentDetails.getString("date");
            String start = cancelAppointmentDetails.getString("start");
            String end = cancelAppointmentDetails.getString("end");
            String firstName = cancelAppointmentDetails.getString("firstName");
            String lastName = cancelAppointmentDetails.getString("lastName");
            String type = cancelAppointmentDetails.getString("type");
            int result;
            ResultSet resultSet;
            //check if student or lecturer is cancelling
            if (type.compareTo("student") == 0) {
                //update db with cancel, making appointment avaialble for other students
                studentCancelAppointment.setString(1, username);
                studentCancelAppointment.setString(2, date);
                studentCancelAppointment.setString(3, start);
                studentCancelAppointment.setString(4, end);
                result = studentCancelAppointment.executeUpdate();
                getLecturer.setString(1, username);
                resultSet = getLecturer.executeQuery();
            } else {
                //update db with cancel, appointment no longer available since lecturer has cancelled
                lecturerCancelAppointment.setString(1, username);
                lecturerCancelAppointment.setString(2, date);
                lecturerCancelAppointment.setString(3, start);
                lecturerCancelAppointment.setString(4, end);
                result = lecturerCancelAppointment.executeUpdate();
                getStudent.setString(1, username);
                resultSet = getStudent.executeQuery();
            }
            if (result != 0 && resultSet != null && resultSet.first()) {

                MimeMessage message = new MimeMessage(mailSession);
                try {
                    //send cancel appointment, student and lecturer details to lecturer email
                    String lecturerEmail = resultSet.getString(dbEmailAtt);
                    InternetAddress[] recipientAddress = {new InternetAddress(lecturerEmail)};
                    InternetAddress senderAddress = new InternetAddress(mailSession.getProperty("mail.from"));
                    message.addHeaderLine("method=CANCEL");
                    message.addHeaderLine("charset=UTF-8");
                    message.addHeaderLine("component=VEVENT");
                    message.setFrom(senderAddress);
                    message.setRecipients(Message.RecipientType.TO, recipientAddress);
                    message.setSubject("Appointment Cancel From " + firstName + " "
                            + lastName);
                    Calendar calendar = Calendar.getInstance();
                    message.setSentDate(calendar.getTime());

                    StringBuilder stringBuilder = new StringBuilder();
                    StringBuilder buffer = stringBuilder.append("BEGIN:VCALENDAR\n"
                            + "PRODID:-//Microsoft Corporation//Outlook 9.0 MIMEDIR//EN\n"
                            + "VERSION:2.0\n"
                            + "METHOD:CANCEL\n"
                            + "BEGIN:VEVENT\n"
                            + "ATTENDEE;ROLE=REQ-PARTICIPANT;RSVP=FALSE:MAILTO:" + recipientAddress[0].toString() + "\n"
                            + "ORGANIZER:MAILTO:" + senderAddress.toString() + "\n"
                            + "DTSTART:" + date.replace("-", "") + "T" + start.replace(":", "") + "00Z\n"
                            + "DTEND:" + date.replace("-", "") + "T" + end.replace(":", "") + "00Z\n"
                            + "LOCATION:Lecturer office\n"
                            + "TRANSP:OPAQUE\n"
                            + "SEQUENCE:0\n"
                            + "UID:040000008200E00074C5B7101A82E00800000000002FF466CE3AC5010000000000000000100\n"
                            + " 000004377FE5C37984842BF9440448399EB02\n"
                            + "DTSTAMP:" + calendar.get(Calendar.YEAR) + (calendar.get(Calendar.MONTH) + 1) + calendar.get(Calendar.DAY_OF_MONTH) + "T"
                            + calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) + calendar.get(Calendar.SECOND) + "Z\n"
                            + "CATEGORIES:Meeting\n"
                            + "DESCRIPTION:Student meeting.\n\n"
                            + "SUMMARY:Student meeting request\n"
                            + "PRIORITY:5\n"
                            + "CLASS:PUBLIC\n"
                            + "BEGIN:VALARM\n"
                            + "TRIGGER:PT1440M\n"
                            + "ACTION:DISPLAY\n"
                            + "DESCRIPTION:Reminder\n"
                            + "END:VALARM\n"
                            + "END:VEVENT\n"
                            + "END:VCALENDAR");

                    // Create the message part
                    BodyPart messageBodyPart = new MimeBodyPart();

                    // Fill the message
                    messageBodyPart.setHeader("Content-Class", "urn:content-  classes:calendarmessage");
                    messageBodyPart.setHeader("Content-ID", "calendar_message");
                    messageBodyPart.setDataHandler(new DataHandler(
                            new ByteArrayDataSource(buffer.toString(), "text/calendar")));// very important

                    // Create a Multipart
                    Multipart multipart = new MimeMultipart();

                    // Add part one
                    multipart.addBodyPart(messageBodyPart);

                    // Put parts in message
                    message.setContent(multipart);
                    Transport.send(message);
                } catch (MessagingException | IOException ex) {
                    Logger.getLogger(AppointementServices.class.getName()).log(Level.SEVERE, null, ex);
                }
                //successful result
                return Json.createObjectBuilder().
                        add("result", "true").build().toString();
            } else {
                //server error
                return Json.createObjectBuilder().
                        add("result", "error").build().toString();
            }
        } catch (SQLException ex) {
            //server error
            Logger.getLogger(AppointementServices.class.getName()).log(Level.SEVERE, null, ex);
            return Json.createObjectBuilder().add("result", "error").build().toString();
        }
    }
}
