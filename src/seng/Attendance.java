package seng;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Attendance {
    private int id;
    private int userId;
    private String visitDate;
    private String doctorName;
    private String treatment;
    private String status;

    public Attendance(int id, int userId, String visitDate, String doctorName, String treatmentName, String status) {
        this.id = id;
        this.userId = userId;
        this.visitDate = visitDate;
        this.doctorName = doctorName;
        this.treatment = treatmentName;
        this.status = status;
    }


    public static List<Attendance> fetchForUser(Connection conn, int userId) throws SQLException {
        String query = "SELECT * FROM attendance WHERE user_id = ? ORDER BY visit_date ASC";
        List<Attendance> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Attendance(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getDate("visit_date").toString(),
                        rs.getString("doctor_name"),
                        rs.getString("treatment"),
                        rs.getString("status")
                    ));
                }
            }
        }
        return list;
    }

    public void printInfo() {
        System.out.printf("%s | %s | %s | %s%n", visitDate, doctorName, treatment, status);
    }

    public String getVisitDate() { return visitDate; }
    public String getDoctorName() { return doctorName; }
    public String getTreatmentName() { return treatment; }
    public String getStatus() { return status; }
}
