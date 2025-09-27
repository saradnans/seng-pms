package seng;

import java.sql.*;

public class Treatment {
    private int userId;
    private int progressPercent;
    private String nextDoctor;
    private String nextDate;
    private String nextTime;

    public Treatment(int userId, int progressPercent, String nextDoctor, String nextDate, String nextTime) {
        this.userId = userId;
        this.progressPercent = progressPercent;
        this.nextDoctor = nextDoctor != null ? nextDoctor : "N/A";
        this.nextDate = nextDate != null ? nextDate : "N/A";
        this.nextTime = nextTime != null ? nextTime : "N/A";
    }

 
    public static Treatment fetchForUser(Connection conn, int userId) throws SQLException {
        String query = "SELECT * FROM treatments WHERE user_id = ? AND next_date IS NOT NULL " +
                       "ORDER BY next_date ASC, next_time ASC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String date = rs.getDate("next_date") != null ? rs.getDate("next_date").toString() : "N/A";
                    String time = rs.getTime("next_time") != null ? rs.getTime("next_time").toString() : "N/A";
                    String doctor = rs.getString("next_doctor") != null ? rs.getString("next_doctor") : "N/A";

                    return new Treatment(
                        rs.getInt("user_id"),
                        rs.getInt("progress_percent"),
                        doctor,
                        date,
                        time
                    );
                }
            }
        }
        return null;
    }


    public int getProgressPercent() { return progressPercent; }
    public String getNextDoctor() { return nextDoctor; }
    public String getNextDate() { return nextDate; }
    public String getNextTime() { return nextTime; }
}
