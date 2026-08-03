import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UpdatePasswords {
    public static void main(String[] args) throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("password123");
        System.out.println("Generated Hash: " + hash);
        
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/restaurant_erp?allowPublicKeyRetrieval=true&useSSL=false", "root", "2007@25thangam");
        PreparedStatement ps = con.prepareStatement("UPDATE users SET password = ?, failed_attempt = 0, account_non_locked = 1");
        ps.setString(1, hash);
        int rows = ps.executeUpdate();
        System.out.println("Updated " + rows + " users with the new password.");
    }
}
