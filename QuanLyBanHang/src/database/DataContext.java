package database;

import models.ChiTietDonHang;
import models.DonHang;
import models.KhachHang;
import models.NhanVien;
import models.SanPham;
import models.TrangThaiDonHang;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DataContext cung cấp dữ liệu trong bộ nhớ (In-memory Mock Data).
 * Dữ liệu được đọc và phân tích trực tiếp từ file sql/data.sql
 * để phục vụ các truy vấn LINQ to Objects (Java Stream API).
 */
public class DataContext {
    private List<SanPham> sanPhams = new ArrayList<>();
    private List<KhachHang> khachHangs = new ArrayList<>();
    private List<NhanVien> nhanViens = new ArrayList<>();
    private List<DonHang> donHangs = new ArrayList<>();
    private List<ChiTietDonHang> chiTietDonHangs = new ArrayList<>();

    public DataContext() {
        napDuLieuTuFileSQL();
    }

    public List<SanPham> getSanPhams() { return sanPhams; }
    public List<KhachHang> getKhachHangs() { return khachHangs; }
    public List<NhanVien> getNhanViens() { return nhanViens; }
    public List<DonHang> getDonHangs() { return donHangs; }
    public List<ChiTietDonHang> getChiTietDonHangs() { return chiTietDonHangs; }

    /**
     * Tìm đường dẫn file data.sql và đọc nạp dữ liệu vào các danh sách đối tượng.
     */
    private void napDuLieuTuFileSQL() {
        File fileSql = timFileSQL();
        if (fileSql != null && fileSql.exists()) {
            try {
                String noiDung = new String(Files.readAllBytes(fileSql.toPath()), StandardCharsets.UTF_8);
                phanTichVaNapDuLieu(noiDung);
                System.out.printf("[DataContext] Đã nạp dữ liệu từ '%s': %d SP, %d KH, %d NV, %d ĐH, %d CTĐH.%n",
                        fileSql.getName(), sanPhams.size(), khachHangs.size(), nhanViens.size(),
                        donHangs.size(), chiTietDonHangs.size());
                return;
            } catch (IOException e) {
                System.err.println("[DataContext] Lỗi khi đọc file data.sql: " + e.getMessage());
            }
        }

        // Dự phòng nếu không tìm thấy file
        System.out.println("[DataContext] Không tìm thấy file data.sql, nạp dữ liệu dự phòng mặc định.");
        khoiTaoDuLieuDuPhong();
    }

    /**
     * Tìm vị trí file data.sql qua các thư mục phổ biến.
     */
    private File timFileSQL() {
        String[] cacDuongDan = {
                "sql/data.sql",
                "QuanLyBanHang/sql/data.sql",
                "../sql/data.sql",
                "../../sql/data.sql"
        };
        for (String path : cacDuongDan) {
            File f = new File(path);
            if (f.exists()) return f;
        }
        return null;
    }

    /**
     * Phân tích các câu lệnh INSERT INTO trong file data.sql
     */
    private void phanTichVaNapDuLieu(String sql) {
        int idx = 0;
        int len = sql.length();

        while (idx < len) {
            int insertPos = timTuKhoaKhongPhanBietHoaThuong(sql, "INSERT INTO", idx);
            if (insertPos == -1) break;

            // Tìm tên bảng sau INSERT INTO
            int posTableStart = insertPos + "INSERT INTO".length();
            while (posTableStart < len && Character.isWhitespace(sql.charAt(posTableStart))) {
                posTableStart++;
            }
            int posTableEnd = posTableStart;
            while (posTableEnd < len && (Character.isLetterOrDigit(sql.charAt(posTableEnd)) || sql.charAt(posTableEnd) == '_')) {
                posTableEnd++;
            }
            String tableName = sql.substring(posTableStart, posTableEnd).trim();

            // Tìm từ khóa VALUES
            int valuesPos = timTuKhoaKhongPhanBietHoaThuong(sql, "VALUES", posTableEnd);
            if (valuesPos == -1) {
                idx = posTableEnd;
                continue;
            }

            int p = valuesPos + "VALUES".length();
            boolean insideQuotes = false;

            // Đọc các bộ giá trị (...) cho đến khi gặp dấu ';'
            while (p < len) {
                char c = sql.charAt(p);
                if (c == '\'') {
                    insideQuotes = !insideQuotes;
                } else if (!insideQuotes && c == ';') {
                    p++;
                    break;
                } else if (!insideQuotes && c == '(') {
                    // Bắt đầu 1 tuple
                    int tupleStart = p + 1;
                    int tupleEnd = tupleStart;
                    boolean inTupleQuotes = false;
                    while (tupleEnd < len) {
                        char tc = sql.charAt(tupleEnd);
                        if (tc == '\'') {
                            inTupleQuotes = !inTupleQuotes;
                        } else if (!inTupleQuotes && tc == ')') {
                            break;
                        }
                        tupleEnd++;
                    }
                    String tupleContent = sql.substring(tupleStart, tupleEnd);
                    List<String> fields = tachCacTruong(tupleContent);
                    themDoiTuong(tableName, fields);
                    p = tupleEnd + 1;
                    continue;
                }
                p++;
            }
            idx = p;
        }
    }

    /**
     * Tách các trường phân tách bởi dấu phẩy, tôn trọng chuỗi có dấu nháy đơn
     */
    private List<String> tachCacTruong(String tupleContent) {
        List<String> ketQua = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < tupleContent.length(); i++) {
            char c = tupleContent.charAt(i);
            if (c == '\'') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                ketQua.add(chuanHoaGiaTri(sb.toString()));
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        if (sb.length() > 0) {
            ketQua.add(chuanHoaGiaTri(sb.toString()));
        }
        return ketQua;
    }

    private String chuanHoaGiaTri(String s) {
        s = s.trim();
        if (s.startsWith("'") && s.endsWith("'") && s.length() >= 2) {
            return s.substring(1, s.length() - 1);
        }
        if (s.equalsIgnoreCase("NULL")) {
            return null;
        }
        return s;
    }

    private void themDoiTuong(String tableName, List<String> f) {
        try {
            switch (tableName.toUpperCase()) {
                case "SANPHAM":
                    if (f.size() >= 5) {
                        sanPhams.add(new SanPham(
                                f.get(0),
                                f.get(1),
                                f.get(2),
                                Double.parseDouble(f.get(3)),
                                Integer.parseInt(f.get(4))
                        ));
                    }
                    break;
                case "KHACHHANG":
                    if (f.size() >= 4) {
                        khachHangs.add(new KhachHang(
                                f.get(0),
                                f.get(1),
                                f.get(2),
                                f.get(3)
                        ));
                    }
                    break;
                case "NHANVIEN":
                    if (f.size() >= 4) {
                        nhanViens.add(new NhanVien(
                                f.get(0),
                                f.get(1),
                                f.get(2),
                                f.get(3) // Có thể null
                        ));
                    }
                    break;
                case "DONHANG":
                    if (f.size() >= 5) {
                        String ngayStr = f.get(3).split(" ")[0]; // Lấy phần yyyy-MM-dd
                        LocalDate ngayDat = LocalDate.parse(ngayStr);
                        TrangThaiDonHang tt = TrangThaiDonHang.HOAN_THANH;
                        String ttStr = f.get(4);
                        if (ttStr.equalsIgnoreCase("DangXuLy")) {
                            tt = TrangThaiDonHang.DANG_XU_LY;
                        } else if (ttStr.equalsIgnoreCase("DaHuy")) {
                            tt = TrangThaiDonHang.DA_HUY;
                        }
                        donHangs.add(new DonHang(
                                f.get(0),
                                ngayDat,
                                tt,
                                f.get(1), // MaKhachHang
                                f.get(2)  // MaNhanVien
                        ));
                    }
                    break;
                case "CHITIETDONHANG":
                    if (f.size() >= 5) {
                        chiTietDonHangs.add(new ChiTietDonHang(
                                f.get(0), // MaDonHang
                                f.get(1), // MaSanPham
                                Integer.parseInt(f.get(2)),
                                Double.parseDouble(f.get(3)),
                                Double.parseDouble(f.get(4))
                        ));
                    }
                    break;
            }
        } catch (Exception ex) {
            System.err.println("[DataContext] Lỗi nạp dòng: " + f + " -> " + ex.getMessage());
        }
    }

    private int timTuKhoaKhongPhanBietHoaThuong(String text, String keyword, int fromIndex) {
        String lowerText = text.toLowerCase();
        String lowerKey = keyword.toLowerCase();
        return lowerText.indexOf(lowerKey, fromIndex);
    }

    /**
     * Dữ liệu dự phòng trong bộ nhớ (nếu không đọc được file).
     */
    private void khoiTaoDuLieuDuPhong() {
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

        khachHangs.add(new KhachHang("1", "Nguyen Van A", "Ha Noi", "nguyenvana@gmail.com"));
        khachHangs.add(new KhachHang("2", "Tran Thi B", "TP HCM", "tranthib@gmail.com"));
        khachHangs.add(new KhachHang("3", "Le Van C", "Da Nang", "levanc@gmail.com"));
        khachHangs.add(new KhachHang("4", "Pham Hoang D", "Can Tho", "phamhoangd@gmail.com"));
        khachHangs.add(new KhachHang("5", "Hoang Mai E", "Ha Noi", "hoangmaie@gmail.com"));
        khachHangs.add(new KhachHang("6", "Vu Dinh F", "Hai Phong", "vudinhf@gmail.com"));
        khachHangs.add(new KhachHang("7", "Dang Thu G", "Hue", "dangthug@gmail.com"));
        khachHangs.add(new KhachHang("8", "Doan Quoc H", "TP HCM", "doanquoch@gmail.com"));

        nhanViens.add(new NhanVien("1", "Tran Van Quan", "Kinh Doanh", null));
        nhanViens.add(new NhanVien("2", "Nguyen Thi Huong", "Kinh Doanh", "1"));
        nhanViens.add(new NhanVien("3", "Le Tuan Kiet", "Kinh Doanh", "1"));
        nhanViens.add(new NhanVien("4", "Pham Minh Tam", "Cham Soc Khach Hang", "1"));

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
