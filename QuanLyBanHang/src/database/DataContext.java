import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataContext {

    // Cấu hình Database (ví dụ với MySQL / PostgreSQL)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/my_database?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "your_password";

    // Singleton Instance
    private static volatile DataContext instance;

    // Các List<T> lưu trữ dữ liệu trong RAM (Thread-safe)
    private final List<User> users = new CopyOnWriteArrayList<>();
    private final List<Product> products = new CopyOnWriteArrayList<>();

    private DataContext() {
        // Khởi tạo và nạp dữ liệu ngay khi DataContext được tạo
        loadAllData();
    }

    public static DataContext getInstance() {
        if (instance == null) {
            synchronized (DataContext.class) {
                if (instance == null) {
                    instance = new DataContext();
                }
            }
        }
        return instance;
    }

    // Kết nối Database
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Nạp toàn bộ dữ liệu từ Database vào RAM
     */
    public synchronized void loadAllData() {
        loadUsers();
        loadProducts();
    }

    /**
     * Nạp danh sách Users từ DB
     */
    public void loadUsers() {
        String query = "SELECT id, name, email FROM users";
        List<User> tempList = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tempList.add(new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email")
                ));
            }

            // Ghi đè vào list trong RAM
            users.clear();
            users.addAll(tempList);
            System.out.println("Loaded " + users.size() + " users into RAM.");

        } catch (SQLException e) {
            System.err.println("Lỗi khi nạp users từ database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Nạp danh sách Products từ DB
     */
    public void loadProducts() {
        String query = "SELECT id, title, price FROM products";
        List<Product> tempList = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tempList.add(new Product(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getDouble("price")
                ));
            }

            // Ghi đè vào list trong RAM
            products.clear();
            products.addAll(tempList);
            System.out.println("Loaded " + products.size() + " products into RAM.");

        } catch (SQLException e) {
            System.err.println("Lỗi khi nạp products từ database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Trả về Unmodifiable List để bảo vệ dữ liệu trong RAM khỏi bị sửa đổi trực tiếp từ bên ngoài
    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public List<Product> getProducts() {
        return Collections.unmodifiableList(products);
    }
}