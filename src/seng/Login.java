package seng;

import java.sql.*;
import seng.Attendance;
import java.util.List;
import java.util.Scanner;

public class Login {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/pms?useSSL=false&serverTimezone=UTC";
        String username = "root";
        String password = "cardigan"; 

        try (Connection conn = DriverManager.getConnection(url, username, password);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to database!\n");

            System.out.print("Enter email: ");
            String inputEmail = scanner.nextLine();

            System.out.print("Enter password: ");
            String inputPassword = scanner.nextLine();

            String query = "SELECT * FROM users WHERE email = ? AND password = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, inputEmail);
            ps.setString(2, inputPassword);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("Login successful! Welcome, " + rs.getString("name"));
                int userId = rs.getInt("id");

                Treatment treatment = Treatment.fetchForUser(conn, userId);
                System.out.println("\nTreatment Info:");
                if (treatment != null) {
                    System.out.println("Progress: " + treatment.getProgressPercent() + "%");
                    if (treatment.getNextDoctor() != null && treatment.getNextDate() != null) {
                        System.out.println("Next Appointment: " + treatment.getNextDoctor() +
                                           " on " + treatment.getNextDate());
                    } else {
                        System.out.println("No upcoming appointments.");
                    }
                } else {
                    System.out.println("No treatment info available.");
                }

                
                List<Attendance> history = Attendance.fetchForUser(conn, userId);
                System.out.println("\nAttendance / Visit History:");
                System.out.println("Date | Doctor | Treatment | Status");
                for (Attendance record : history) {
                    System.out.printf("%s | %s | %s | %s%n",
                        record.getVisitDate(),
                        record.getDoctorName(),
                        record.getTreatmentName(),
                        record.getStatus()
                    );
                }


            } else {
                System.out.println("Login failed. Invalid email or password.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}