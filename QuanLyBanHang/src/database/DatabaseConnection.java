import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // --- CẤU HÌNH THÔNG TIN KẾT NỐI (Tùy chỉnh theo CSDL của bạn) ---
    // Ví dụ mẫu dưới đây dùng cho MySQL:
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/ten_database_cua_ban?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "mat_khau_cua_ban";

    // Khối static để nạp Driver khi class được load vào bộ nhớ
    static {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy JDBC Driver!");
            e.printStackTrace();
        }
    }

    // Phương thức tĩnh cung cấp kết nối mới
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    // Phương thức đóng tài nguyên an toàn (hỗ trợ đóng Connection thủ công nếu không dùng try-with-resources)
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng kết nối: " + e.getMessage());
            }
        }
    }

    // Hàm main để kiểm tra kết nối trực tiếp
    public static void main(String[] args) {
        System.out.println("Đang kết nối tới cơ sở dữ liệu...");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Kết nối cơ sở dữ liệu thành công!");
                System.out.println("Thông tin DB: " + conn.getMetaData().getDatabaseProductName() 
                                   + " " + conn.getMetaData().getDatabaseProductVersion());
            }
        } catch (SQLException e) {
            System.err.println("Kết nối thất bại!");
            e.printStackTrace();
        }
    }
}