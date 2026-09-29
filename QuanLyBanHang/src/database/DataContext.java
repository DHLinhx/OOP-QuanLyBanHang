<<<<<<< HEAD
=======
package database;

import models.ChiTietDonHang;
import models.DonHang;
import models.KhachHang;
import models.NhanVien;
import models.SanPham;
import models.TrangThaiDonHang;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DataContext cung cấp dữ liệu trong bộ nhớ (In-memory Mock Data)
 * để thực hiện các truy vấn LINQ to Objects (Java Streams).
 * Khớp hoàn toàn với CSDL mẫu trong file sql/data.sql.
 */
public class DataContext {
    private List<SanPham> sanPhams = new ArrayList<>();
    private List<KhachHang> khachHangs = new ArrayList<>();
    private List<NhanVien> nhanViens = new ArrayList<>();
    private List<DonHang> donHangs = new ArrayList<>();
    private List<ChiTietDonHang> chiTietDonHangs = new ArrayList<>();

    public DataContext() {
        khoiTaoDuLieuMau();
    }

    public List<SanPham> getSanPhams() { return sanPhams; }
    public List<KhachHang> getKhachHangs() { return khachHangs; }
    public List<NhanVien> getNhanViens() { return nhanViens; }
    public List<DonHang> getDonHangs() { return donHangs; }
    public List<ChiTietDonHang> getChiTietDonHangs() { return chiTietDonHangs; }

    private void khoiTaoDuLieuMau() {
        // 1. 12 Sản phẩm
        sanPhams.add(new SanPham("1", "Laptop Dell Inspiron 15", "Laptop", 17500000, 15));
        sanPhams.add(new SanPham("2", "MacBook Air M2", "Laptop", 26000000, 8));
        sanPhams.add(new SanPham("3", "Laptop Asus Zenbook 14", "Laptop", 21500000, 12));
        sanPhams.add(new SanPham("4", "Laptop Lenovo ThinkPad E14", "Laptop", 19000000, 10));
        sanPhams.add(new SanPham("5", "Chuột không dây Logitech M331", "PhuKien", 350000, 45));
        sanPhams.add(new SanPham("6", "Bàn phím cơ DareU EK87", "PhuKien", 850000, 30));
        sanPhams.add(new SanPham("7", "Tai nghe Sony WH-1000XM5", "PhuKien", 6900000, 14));
        sanPhams.add(new SanPham("8", "Điện thoại iPhone 15 Pro", "DienThoai", 27500000, 18));
        sanPhams.add(new SanPham("9", "Samsung Galaxy S24 Ultra", "DienThoai", 28900000, 15));
        sanPhams.add(new SanPham("10", "Xiaomi Redmi Note 13", "DienThoai", 4990000, 25));
        sanPhams.add(new SanPham("11", "Màn hình Dell UltraSharp 27", "ManHinh", 9200000, 7));
        sanPhams.add(new SanPham("12", "Màn hình LG 24 inch IPS", "ManHinh", 3100000, 20));

        // 2. 8 Khách hàng (khách 7, 8 chưa mua hàng để test LEFT JOIN)
        khachHangs.add(new KhachHang("1", "Nguyen Van A", "Ha Noi", "nguyenvana@gmail.com"));
        khachHangs.add(new KhachHang("2", "Tran Thi B", "TP HCM", "tranthib@gmail.com"));
        khachHangs.add(new KhachHang("3", "Le Van C", "Da Nang", "levanc@gmail.com"));
        khachHangs.add(new KhachHang("4", "Pham Hoang D", "Can Tho", "phamhoangd@gmail.com"));
        khachHangs.add(new KhachHang("5", "Hoang Mai E", "Ha Noi", "hoangmaie@gmail.com"));
        khachHangs.add(new KhachHang("6", "Vu Dinh F", "Hai Phong", "vudinhf@gmail.com"));
        khachHangs.add(new KhachHang("7", "Dang Thu G", "Hue", "dangthug@gmail.com"));
        khachHangs.add(new KhachHang("8", "Doan Quoc H", "TP HCM", "doanquoch@gmail.com"));

        // 3. 4 Nhân viên (Nhân viên 1 làm quản lý cấp trên)
        nhanViens.add(new NhanVien("1", "Tran Van Quan", "Kinh Doanh", null));
        nhanViens.add(new NhanVien("2", "Nguyen Thi Huong", "Kinh Doanh", "1"));
        nhanViens.add(new NhanVien("3", "Le Tuan Kiet", "Kinh Doanh", "1"));
        nhanViens.add(new NhanVien("4", "Pham Minh Tam", "Cham Soc Khach Hang", "1"));

        // 4. 15 Đơn hàng (1001 đến 1015)
        donHangs.add(new DonHang("1001", LocalDate.of(2026, 1, 5), TrangThaiDonHang.HOAN_THANH, "1", "1"));
        donHangs.add(new DonHang("1002", LocalDate.of(2026, 1, 12), TrangThaiDonHang.HOAN_THANH, "2", "2"));
        donHangs.add(new DonHang("1003", LocalDate.of(2026, 1, 18), TrangThaiDonHang.HOAN_THANH, "3", "3"));
        donHangs.add(new DonHang("1004", LocalDate.of(2026, 1, 25), TrangThaiDonHang.DA_HUY, "4", "2"));
        donHangs.add(new DonHang("1005", LocalDate.of(2026, 2, 2), TrangThaiDonHang.HOAN_THANH, "5", "1"));
        donHangs.add(new DonHang("1006", LocalDate.of(2026, 2, 8), TrangThaiDonHang.HOAN_THANH, "1", "3"));
        donHangs.add(new DonHang("1007", LocalDate.of(2026, 2, 14), TrangThaiDonHang.DANG_XU_LY, "6", "2"));
        donHangs.add(new DonHang("1008", LocalDate.of(2026, 2, 20), TrangThaiDonHang.HOAN_THANH, "2", "1"));
        donHangs.add(new DonHang("1009", LocalDate.of(2026, 2, 26), TrangThaiDonHang.HOAN_THANH, "3", "3"));
        donHangs.add(new DonHang("1010", LocalDate.of(2026, 3, 1), TrangThaiDonHang.HOAN_THANH, "4", "2"));
        donHangs.add(new DonHang("1011", LocalDate.of(2026, 3, 5), TrangThaiDonHang.HOAN_THANH, "5", "1"));
        donHangs.add(new DonHang("1012", LocalDate.of(2026, 3, 10), TrangThaiDonHang.DANG_XU_LY, "6", "3"));
        donHangs.add(new DonHang("1013", LocalDate.of(2026, 3, 15), TrangThaiDonHang.HOAN_THANH, "1", "2"));
        donHangs.add(new DonHang("1014", LocalDate.of(2026, 3, 20), TrangThaiDonHang.DA_HUY, "2", "1"));
        donHangs.add(new DonHang("1015", LocalDate.of(2026, 3, 25), TrangThaiDonHang.HOAN_THANH, "3", "3"));

        // 5. 31 Chi tiết đơn hàng
        chiTietDonHangs.add(new ChiTietDonHang("1001", "1", 1, 17500000, 0.05));
        chiTietDonHangs.add(new ChiTietDonHang("1001", "5", 2, 350000, 0.00));
        chiTietDonHangs.add(new ChiTietDonHang("1002", "2", 1, 26000000, 0.10));
        chiTietDonHangs.add(new ChiTietDonHang("1002", "6", 1, 850000, 0.00));
        chiTietDonHangs.add(new ChiTietDonHang("1003", "8", 1, 27500000, 0.00));
        chiTietDonHangs.add(new ChiTietDonHang("1003", "7", 1, 6900000, 0.05));
        chiTietDonHangs.add(new ChiTietDonHang("1004", "10", 2, 4990000, 0.00));
        chiTietDonHangs.add(new ChiTietDonHang("1004", "5", 1, 350000, 0.00));
        chiTietDonHangs.add(new ChiTietDonHang("1005", "3", 1, 21500000, 0.05));
        chiTietDonHangs.add(new ChiTietDonHang("1005", "6", 1, 850000, 0.00));
        chiTietDonHangs.add(new ChiTietDonHang("1005", "11", 1, 9200000, 0.05));
        chiTietDonHangs.add(new ChiTietDonHang("1006", "9", 1, 28900000, 0.08));
        chiTietDonHangs.add(new ChiTietDonHang("1006", "7", 1, 6900000, 0.05));
        chiTietDonHangs.add(new ChiTietDonHang("1007", "4", 1, 19000000, 0.00));
        chiTietDonHangs.add(new ChiTietDonHang("1007", "12", 1, 3100000, 0.00));
        chiTietDonHangs.add(new ChiTietDonHang("1008", "1", 1, 17500000, 0.05));
        chiTietDonHangs.add(new ChiTietDonHang("1008", "5", 3, 350000, 0.00));
        chiTietDonHangs.add(new ChiTietDonHang("1009", "8", 1, 27500000, 0.05));
        chiTietDonHangs.add(new ChiTietDonHang("1009", "11", 1, 9200000, 0.00));
        chiTietDonHangs.add(new ChiTietDonHang("1010", "10", 1, 4990000, 0.00));
        chiTietDonHangs.add(new ChiTietDonHang("1010", "12", 2, 3100000, 0.05));
        chiTietDonHangs.add(new ChiTietDonHang("1011", "2", 1, 26000000, 0.05));
        chiTietDonHangs.add(new ChiTietDonHang("1011", "6", 2, 850000, 0.00));
        chiTietDonHangs.add(new ChiTietDonHang("1012", "3", 1, 21500000, 0.00));
        chiTietDonHangs.add(new ChiTietDonHang("1012", "5", 2, 350000, 0.00));
        chiTietDonHangs.add(new ChiTietDonHang("1013", "9", 1, 28900000, 0.05));
        chiTietDonHangs.add(new ChiTietDonHang("1013", "7", 1, 6900000, 0.10));
        chiTietDonHangs.add(new ChiTietDonHang("1014", "4", 1, 19000000, 0.00));
        chiTietDonHangs.add(new ChiTietDonHang("1014", "11", 1, 9200000, 0.05));
        chiTietDonHangs.add(new ChiTietDonHang("1015", "8", 1, 27500000, 0.05));
        chiTietDonHangs.add(new ChiTietDonHang("1015", "6", 1, 850000, 0.00));
    }
}
>>>>>>> fe18cda451e2eb009a51e715156f944bc8f5d9d4
