package seng;

import java.sql.*;
import static spark.Spark.*;

public class Server {

    static final String url = "jdbc:mysql://localhost:3306/pms?useSSL=false&serverTimezone=UTC";
    static final String dbUser = "root";
    static final String dbPass = "cardigan";

    public static void main(String[] args) {
        port(4581);

        staticFiles.externalLocation("C:/Users/saraa/eclipse-workspace/seng/public");

        post("/login", (req, res) -> {
            String email = req.queryParams("email");
            String password = req.queryParams("password");

            try (Connection conn = DriverManager.getConnection(url, dbUser, dbPass)) {
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT * FROM users WHERE email = ? AND password = ?"
                );
                ps.setString(1, email);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    int userId = rs.getInt("id");
                    req.session(true).attribute("userId", userId);

                    res.redirect("/dashboard.html");
                    return "";
                } else {
                    return "<p>Login failed. Invalid email or password.</p>"
                            + "<p><a href='/login.html'>Back to login</a></p>";
                }
            }
        });

        get("/api/dashboard-data", (req, res) -> {
            res.type("application/json");
            Integer userId = req.session().attribute("userId");
            if (userId == null) {
                res.status(401);
                return "{\"error\":\"Not logged in\"}";
            }

            try (Connection conn = DriverManager.getConnection(url, dbUser, dbPass)) {

                Treatment treatment = null;
                String treatmentQuery = "SELECT * FROM treatments WHERE user_id = ? ORDER BY next_date ASC, next_time ASC LIMIT 1";
                try (PreparedStatement ps = conn.prepareStatement(treatmentQuery)) {
                    ps.setInt(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            treatment = new Treatment(
                                rs.getInt("user_id"),
                                0, 
                                rs.getString("next_doctor"),
                                rs.getDate("next_date") != null ? rs.getDate("next_date").toString() : "N/A",
                                rs.getTime("next_time") != null ? rs.getTime("next_time").toString() : "N/A"
                            );
                        }
                    }
                }

                int attended = 0, total = 0;
                String attendanceQuery = "SELECT status FROM attendance WHERE user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(attendanceQuery)) {
                    ps.setInt(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            total++;
                            if ("Attended".equalsIgnoreCase(rs.getString("status"))) {
                                attended++;
                            }
                        }
                    }
                }

                int progressPercent = (total > 0) ? (attended * 100 / total) : 0;

                String userName = getUserName(userId, conn);

                StringBuilder sb = new StringBuilder();
                sb.append("{");
                sb.append("\"name\":\"").append(userName).append("\",");

                sb.append("\"treatment\":{");
                if (treatment != null) {
                    sb.append("\"progress\":").append(progressPercent).append(",");
                    sb.append("\"nextDoctor\":\"").append(treatment.getNextDoctor()).append("\",");
                    sb.append("\"nextDate\":\"").append(treatment.getNextDate()).append("\",");
                    sb.append("\"nextTime\":\"").append(treatment.getNextTime()).append("\"");
                }
                sb.append("},");

                sb.append("\"history\":[");
                String historyQuery = "SELECT * FROM attendance WHERE user_id = ? ORDER BY visit_date DESC";
                try (PreparedStatement ps = conn.prepareStatement(historyQuery)) {
                    ps.setInt(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        boolean first = true;
                        while (rs.next()) {
                            if (!first) sb.append(",");
                            sb.append("{")
                              .append("\"date\":\"").append(rs.getDate("visit_date")).append("\",")
                              .append("\"doctor\":\"").append(rs.getString("doctor_name")).append("\",")
                              .append("\"treatment\":\"").append(rs.getString("treatment")).append("\",")
                              .append("\"status\":\"").append(rs.getString("status")).append("\"")
                              .append("}");
                            first = false;
                        }
                    }
                }
                sb.append("]");

                sb.append("}");
                return sb.toString();
            }
        });

        post("/book", (req, res) -> {
            Integer userId = req.session().attribute("userId");
            if (userId == null) {
                res.status(401);
                return "Not logged in";
            }

            String doctor = req.queryParams("doctor");
            String treatment = req.queryParams("treatment");
            String date = req.queryParams("date");
            String time = req.queryParams("time");

            try (Connection conn = DriverManager.getConnection(url, dbUser, dbPass)) {

                String query = "UPDATE treatments SET next_doctor = ?, next_date = ?, next_time = ?, progress_percent = 0 WHERE user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setString(1, doctor);
                    ps.setDate(2, Date.valueOf(date));
                    ps.setTime(3, Time.valueOf(time + ":00"));
                    ps.setInt(4, userId);
                    ps.executeUpdate();
                }

                String attendanceQuery = "INSERT INTO attendance (user_id, visit_date, doctor_name, treatment, status) VALUES (?, ?, ?, ?, 'Pending')";
                try (PreparedStatement ps2 = conn.prepareStatement(attendanceQuery)) {
                    ps2.setInt(1, userId);
                    ps2.setDate(2, Date.valueOf(date));
                    ps2.setString(3, doctor);
                    ps2.setString(4, treatment);
                    ps2.executeUpdate();
                }

                res.redirect("/dashboard.html");
                return "";
            }
        });
    }

    private static String getUserName(int userId, Connection conn) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT name FROM users WHERE id=?");
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getString("name");
        return "User";
    }
}




