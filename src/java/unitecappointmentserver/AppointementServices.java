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
 *
 * @author Group2
 */
@WebService(serviceName = "AppointementServices")
public class AppointementServices {

    @Resource(lookup = "mail/AppointmentMailSession")
    private Session mailSession;

    private final Connection connection;
    private final PreparedStatement getStudent;
    private final PreparedStatement getLecturer;
    private final PreparedStatement getLecturerAppointmentsByDate;
    private final PreparedStatement addLecturerAppointment;
    private final PreparedStatement getAssignedLecturers;
    private final PreparedStatement getAvailableAppointments;
    private final PreparedStatement getAppointment;
    private final PreparedStatement bookAppointment;
    private final PreparedStatement getStudentBookedAppointment;
    private final PreparedStatement getLecturerBookedAppointment;
    private final PreparedStatement studentCancelAppointment;
    private final PreparedStatement lecturerCancelAppointment;
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

    //identify of all the database table and attrebute
    public AppointementServices() throws IOException, ClassNotFoundException, SQLException {
        Properties properties = new Properties();
        properties.loadFromXML(getClass().getResourceAsStream("DBConfig.xml"));
        String dbDriver = properties.get("dbDriver").toString();
        String dbUrl = properties.get("dbUrl").toString();
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
        String dbUserName = properties.get("dbusername").toString();
        String dbPassword = properties.get("dbpassword").toString();
        Class.forName(dbDriver);
        //this is the condetion of login details to database
        connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
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
     * Web service operation
     *
     * @param userLoginDetails
     * @return login result
     */
    //connection between server and android logindetials
    @WebMethod(operationName = "login")
    public String login(@WebParam(name = "userLoginDetails") String userLoginDetails) {
        JsonObject loginDetails = Json.createReader(new StringReader(userLoginDetails)).readObject();
        JsonObject result;
        String username = loginDetails.getString("username");
        String password = loginDetails.getString("password");
        //this is condition statement wether its true or false 
        boolean isStudentFound = false;
        boolean isLecturerFound = false;
        ResultSet resultSet = null;
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
            try {
                String firstName = resultSet.getString(dbFirstNameAtt);
                String lastName = resultSet.getString(dbLastNameAtt);
                String userPassword = resultSet.getString(dbPasswordAtt);
                String userSalt = resultSet.getString(dbSaltAtt);
                //this is to check if the password compatible with userpassword or not
                Base64.Decoder decoder = Base64.getDecoder();
                byte[] salt = decoder.decode(userSalt);

                KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
                SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                byte[] hash = secretKeyFactory.generateSecret(keySpec).getEncoded();

                Base64.Encoder encoder = Base64.getEncoder();
                String hashedPassword = encoder.encodeToString(hash);

                if (hashedPassword.compareTo(userPassword) == 0) {
                    if (isStudentFound) {
                        result = Json.createObjectBuilder()
                                .add("result", "true")
                                .add("user", "student")
                                .add("firstName", firstName)
                                .add("lastName", lastName).build();
                    } else {
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
                    result = Json.createObjectBuilder()
                            .add("result", "false").build();
                }
            } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
                Logger.getLogger(AppointementServices.class.getName()).log(Level.SEVERE, null, ex);
                result = Json.createObjectBuilder()
                        .add("result", "error").build();
            }
        } else {
            result = Json.createObjectBuilder()
                    .add("result", "false").build();
        }
        return result.toString();
    }

    /**
     * Web service operation
     *
     * @param appointmentDetails
     * @return make appointment result
     */
    @WebMethod(operationName = "createAppointment")
    public String createAppointment(@WebParam(name = "appointmentDetails") String appointmentDetails) {

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

        int repeat;
        if (recurrence.isEmpty()) {
            repeat = 1;
        } else {
            repeat = Integer.parseInt(recurrence);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        for (int i = 0; i < repeat; i++) {
            try {
                Date appointmentDate = dateFormat.parse(year + "-" + month + "-" + day);
                Date appointmentStart = timeFormat.parse(startHour + ":" + startMinute);
                Date appointmentEnd = timeFormat.parse(endHour + ":" + endMinute);

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

                getLecturerAppointmentsByDate.setString(1, username);
                getLecturerAppointmentsByDate.setString(2, dateFormat.format(appointmentDate));
                ResultSet resultSet = getLecturerAppointmentsByDate.executeQuery();
                boolean isFound = resultSet.first();

                if (isFound) {
                    do {
                        Date startTime = timeFormat.parse(resultSet.getString(dbStartAtt));
                        Date endTime = timeFormat.parse(resultSet.getString(dbEndAtt));
                        if (startTime.before(appointmentEnd) && appointmentStart.before(endTime)) {
                            return Json.createObjectBuilder().add("result", "false").build().toString();
                        }
                    } while (resultSet.next());
                }

                addLecturerAppointment.setString(1, dateFormat.format(appointmentDate));
                addLecturerAppointment.setString(2, timeFormat.format(appointmentStart));
                addLecturerAppointment.setString(3, timeFormat.format(appointmentEnd));
                addLecturerAppointment.setString(4, username);
                addLecturerAppointment.setString(5, "available");
                addLecturerAppointment.executeUpdate();

            } catch (ParseException | SQLException ex) {
                Logger.getLogger(AppointementServices.class.getName()).log(Level.SEVERE, null, ex);
                return Json.createObjectBuilder().add("result", "error").build().toString();
            }
        }
        return Json.createObjectBuilder().add("result", "true").build().toString();
    }

    /**
     * Web service operation
     *
     * @param userDetails
     * @return
     */
    @WebMethod(operationName = "makeAppointment")
    public String makeAppointment(@WebParam(name = "userDetails") String userDetails) {
        try {
            JsonObject makeAppointmentDetails = Json.createReader(new StringReader(userDetails)).readObject();
            String username = makeAppointmentDetails.getString("username");
            getAssignedLecturers.setString(1, username);
            ResultSet resultSet = getAssignedLecturers.executeQuery();
            if (resultSet.first()) {

                Map<String, LecturerModel> lecturers = new HashMap<>();

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

                return Json.createObjectBuilder().
                        add("result", "true").
                        add("lecturers", jsonLecturersArrayBuilder.build()).build().toString();
            } else {
                return Json.createObjectBuilder().
                        add("result", "false").build().toString();
            }
        } catch (SQLException ex) {
            Logger.getLogger(AppointementServices.class.getName()).log(Level.SEVERE, null, ex);
            return Json.createObjectBuilder().add("result", "error").build().toString();
        }
    }

    /**
     * Web service operation
     *
     * @param userDetails
     * @return
     */
    @WebMethod(operationName = "getAvailableAppointment")
    public String getAvailableAppointment(@WebParam(name = "userDetails") String userDetails) {
        try {
            JsonObject makeAppointmentDetails = Json.createReader(new StringReader(userDetails)).readObject();
            String username = makeAppointmentDetails.getString("username");
            getAvailableAppointments.setString(1, username);
            ResultSet resultSet = getAvailableAppointments.executeQuery();
            if (resultSet.first()) {
                List<AppointmentModel> appointments = new ArrayList<>();
                do {
                    appointments.add(new AppointmentModel(
                            resultSet.getString(dbDateAtt),
                            resultSet.getString(dbStartAtt),
                            resultSet.getString(dbEndAtt)));
                } while (resultSet.next());
                JsonArrayBuilder jsonAppointmentArrayBuilder
                        = Json.createArrayBuilder();
                for (AppointmentModel appointment : appointments) {
                    jsonAppointmentArrayBuilder.add(Json.createObjectBuilder().
                            add("date", appointment.getDate()).
                            add("start", appointment.getStart()).
                            add("end", appointment.getEnd()).build());
                }

                return Json.createObjectBuilder().add("result", "true").
                        add("appointments", jsonAppointmentArrayBuilder.build()
                        ).build().toString();

            } else {
                return Json.createObjectBuilder().
                        add("result", "false").build().toString();
            }
        } catch (SQLException ex) {
            Logger.getLogger(AppointementServices.class.getName()).log(Level.SEVERE, null, ex);
            return Json.createObjectBuilder().add("result", "error").build().toString();
        }
    }

    /**
     * Web service operation
     *
     * @param appointmentDetails
     * @return
     */
    @WebMethod(operationName = "bookAppointment")
    public String bookAppointment(@WebParam(name = "appointmentDetails") String appointmentDetails) {
        try {
            JsonObject makeAppointmentDetails = Json.createReader(new StringReader(appointmentDetails)).readObject();
            String studentUsername = makeAppointmentDetails.getString("studentUsername");
            String lecturerUserName = makeAppointmentDetails.getString("lecturerUsername");
            String date = makeAppointmentDetails.getString("date");
            String start = makeAppointmentDetails.getString("start");
            String end = makeAppointmentDetails.getString("end");

            getAppointment.setString(1, date);
            getAppointment.setString(2, start);
            getAppointment.setString(3, end);
            getAppointment.setString(4, lecturerUserName);
            ResultSet resultSet = getAppointment.executeQuery();
            if (resultSet.first()) {
                String isActive = resultSet.getString(dbIsActiveAtt);

                if (isActive.compareTo("available") == 0) {
                    bookAppointment.setString(1, studentUsername);
                    bookAppointment.setString(2, lecturerUserName);
                    bookAppointment.setString(3, date);
                    bookAppointment.setString(4, start);
                    bookAppointment.setString(5, end);
                    int result = bookAppointment.executeUpdate();
                    getLecturer.setString(1, lecturerUserName);
                    ResultSet resultSet1 = getLecturer.executeQuery();
                    getStudent.setString(1, studentUsername);
                    ResultSet resultSet2 = getStudent.executeQuery();
                    if (result != 0 && resultSet1.first() && resultSet2.first()) {
                        String lecturerEmail = resultSet1.getString(dbEmailAtt);
                        String studentFirstName = resultSet2.getString(dbFirstNameAtt);
                        String studentLastName = resultSet2.getString(dbLastNameAtt);
                        MimeMessage message = new MimeMessage(mailSession);
                        try {
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

                        return Json.createObjectBuilder().add("result", "true").build().toString();
                    } else {
                        return Json.createObjectBuilder().
                                add("result", "false").build().toString();
                    }
                } else {
                    return Json.createObjectBuilder().add("result", "booked").build().toString();
                }
            } else {
                return Json.createObjectBuilder().add("result", "error").build().toString();
            }

        } catch (SQLException ex) {
            Logger.getLogger(AppointementServices.class.getName()).log(Level.SEVERE, null, ex);
            return Json.createObjectBuilder().add("result", "error").build().toString();
        }
    }

    /**
     * Web service operation
     *
     * @param userDetails
     * @return
     */
    @WebMethod(operationName = "viewAppointment")
    public String viewAppointment(@WebParam(name = "userDetails") String userDetails) {
        try {
            JsonObject viewAppointmentDetails = Json.createReader(new StringReader(userDetails)).readObject();
            String username = viewAppointmentDetails.getString("username");
            String type = viewAppointmentDetails.getString("type");
            ResultSet resultSet;
            if (type.compareToIgnoreCase("student") == 0) {
                getStudentBookedAppointment.setString(1, username);
                resultSet = getStudentBookedAppointment.executeQuery();
            } else {
                getLecturerBookedAppointment.setString(1, username);
                resultSet = getLecturerBookedAppointment.executeQuery();
            }
            if (resultSet.first()) {

                List<BookedAppointmentModel> bookedAppointments = new ArrayList<>();
                if (type.compareToIgnoreCase("student") == 0) {
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

                return Json.createObjectBuilder().
                        add("result", "true").
                        add("appointments", jsonBookedAppointmentsArrayBuilder.build()).build().toString();
            } else {
                return Json.createObjectBuilder().
                        add("result", "false").build().toString();
            }
        } catch (SQLException ex) {
            Logger.getLogger(AppointementServices.class.getName()).log(Level.SEVERE, null, ex);
            return Json.createObjectBuilder().add("result", "error").build().toString();
        }
    }

    /**
     * Web service operation
     *
     * @param appointmentDetails
     * @return
     */
    @WebMethod(operationName = "cancelAppointment")
    public String cancelAppointment(@WebParam(name = "appointmentDetails") String appointmentDetails) {
        try {
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
            if (type.compareTo("student") == 0) {
                studentCancelAppointment.setString(1, username);
                studentCancelAppointment.setString(2, date);
                studentCancelAppointment.setString(3, start);
                studentCancelAppointment.setString(4, end);
                result = studentCancelAppointment.executeUpdate();
                getLecturer.setString(1, username);
                resultSet = getLecturer.executeQuery();
            } else {
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

                return Json.createObjectBuilder().
                        add("result", "true").build().toString();
            } else {
                return Json.createObjectBuilder().
                        add("result", "error").build().toString();
            }
        } catch (SQLException ex) {
            Logger.getLogger(AppointementServices.class.getName()).log(Level.SEVERE, null, ex);
            return Json.createObjectBuilder().add("result", "error").build().toString();
        }
    }
}
