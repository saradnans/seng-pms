package seng;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Bookings {
    private int id;
    private int userId;
    private String doctor;
    private String treatment;
    private String date;
    private String time;

    public Bookings(int id, int userId, String doctor, String treatment, String date, String time) {
        this.id = id;
        this.userId = userId;
        this.doctor = doctor;
        this.treatment = treatment;
        this.date = date;
        this.time = time;
    }

    public static List<Bookings> fetchForUser(Connection conn, int userId) throws SQLException {
        String query = "SELECT * FROM treatments WHERE user_id = ?";
        List<Bookings> bookings = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bookings.add(new Bookings(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("next_doctor"),
                        rs.getString("treatment_name"),
                        rs.getDate("next_date") != null ? rs.getDate("next_date").toString() : "N/A",
                        rs.getTime("next_time") != null ? rs.getTime("next_time").toString() : "N/A"
                    ));
                }
            }
        }

        return bookings;
    }

    public static void createBooking(Connection conn, int userId, String doctor, String treatment, String date, String time) throws SQLException {
        String query = "INSERT INTO treatments (user_id, treatment_name, next_doctor, next_date, next_time, progress_percent) VALUES (?,?,?,?,?,0)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setString(2, treatment);
            ps.setString(3, doctor);
            ps.setDate(4, Date.valueOf(date));
            ps.setTime(5, Time.valueOf(time));
            ps.executeUpdate();
        }
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getDoctor() { return doctor; }
    public String getTreatment() { return treatment; }
    public String getDate() { return date; }
    public String getTime() { return time; }

    public void printInfo() {
        System.out.printf("User %d | %s | %s | %s | %s%n", userId, doctor, treatment, date, time);
    }
}
