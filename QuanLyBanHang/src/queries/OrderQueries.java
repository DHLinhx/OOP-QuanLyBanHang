package queries;

import database.DataContext;
import models.ChiTietDonHang;
import models.DonHang;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Các câu truy vấn về Đơn hàng & Chi tiết đơn hàng (phần của Người 3):
 * Câu 8, 9, 10, 11, 17, 20, 21, 23, 29, 30.
 *
 * Yêu cầu DataContext có các getter:
 *   getDonHangs(), getChiTietDonHangs()
 *
 * LƯU Ý: nội dung từng câu dưới đây được suy ra từ sơ đồ phân công.
 * Hãy đối chiếu với đề thật và chỉnh lại phần lọc/sắp xếp nếu khác.
 */
public class OrderQueries {

    // =====================================================================
    // HÀM DÙNG CHUNG
    // =====================================================================

    /** Sắp xếp Map theo giá trị giảm dần, giữ thứ tự bằng LinkedHashMap. */
    private static <K, V extends Comparable<V>> Map<K, V> sapXepGiamDan(Map<K, V> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<K, V>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));
    }

    /** Tổng tiền của từng đơn: maDonHang -> tổng thành tiền các dòng chi tiết. */
    public static Map<String, Double> tongTienTheoDon(DataContext ctx) {
        return ctx.getChiTietDonHangs().stream()
                .collect(Collectors.groupingBy(
                        ChiTietDonHang::getMaDonHang,
                        Collectors.summingDouble(ChiTietDonHang::getThanhTien)));
    }

    /** Tổng tiền của các đơn ở trạng thái HOAN_THANH: maDonHang -> tổng tiền. */
    public static Map<String, Double> tongTienDonHoanThanh(DataContext ctx) {
        Map<String, Double> tong = tongTienTheoDon(ctx);
        return ctx.getDonHangs().stream()
                .filter(DonHang::isHoanThanh)
                .collect(Collectors.toMap(
                        DonHang::getMaDonHang,
                        d -> tong.getOrDefault(d.getMaDonHang(), 0.0)));
    }

    // =====================================================================
    // NHÓM B: CHI TIẾT ĐƠN HÀNG (tính tiền, số lượng bán) - Câu 8, 9, 10, 11
    // =====================================================================

    /**
     * Câu 8: Liệt kê các dòng chi tiết đơn hàng kèm thành tiền,
     *        sắp xếp thành tiền giảm dần.
     *
     * SELECT MaDonHang, MaSanPham, SoLuong, DonGia, TyLeGiamGia,
     *        SoLuong * DonGia * (1 - TyLeGiamGia) AS ThanhTien
     * FROM ChiTietDonHang
     * ORDER BY ThanhTien DESC;
     */
    public static List<ChiTietDonHang> cau8_thanhTienTungDong(DataContext ctx) {
        return ctx.getChiTietDonHangs().stream()
                .sorted(Comparator.comparingDouble(ChiTietDonHang::getThanhTien).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Câu 9: Tổng tiền của mỗi đơn hàng, sắp xếp giảm dần.
     *
     * SELECT MaDonHang, SUM(SoLuong * DonGia * (1 - TyLeGiamGia)) AS TongTien
     * FROM ChiTietDonHang
     * GROUP BY MaDonHang
     * ORDER BY TongTien DESC;
     */
    public static Map<String, Double> cau9_tongTienMoiDon(DataContext ctx) {
        return sapXepGiamDan(tongTienTheoDon(ctx));
    }

    /**
     * Câu 10: Tổng số lượng đã bán của từng sản phẩm, giảm dần.
     *
     * SELECT MaSanPham, SUM(SoLuong) AS TongSoLuong
     * FROM ChiTietDonHang
     * GROUP BY MaSanPham
     * ORDER BY TongSoLuong DESC;
     */
    public static Map<String, Integer> cau10_soLuongBanTheoSanPham(DataContext ctx) {
        Map<String, Integer> kq = ctx.getChiTietDonHangs().stream()
                .collect(Collectors.groupingBy(
                        ChiTietDonHang::getMaSanPham,
                        Collectors.summingInt(ChiTietDonHang::getSoLuong)));
        return sapXepGiamDan(kq);
    }

    /**
     * Câu 11: Doanh thu (sau giảm giá) của từng sản phẩm, giảm dần.
     *
     * SELECT MaSanPham, SUM(SoLuong * DonGia * (1 - TyLeGiamGia)) AS DoanhThu
     * FROM ChiTietDonHang
     * GROUP BY MaSanPham
     * ORDER BY DoanhThu DESC;
     */
    public static Map<String, Double> cau11_doanhThuTheoSanPham(DataContext ctx) {
        Map<String, Double> kq = ctx.getChiTietDonHangs().stream()
                .collect(Collectors.groupingBy(
                        ChiTietDonHang::getMaSanPham,
                        Collectors.summingDouble(ChiTietDonHang::getThanhTien)));
        return sapXepGiamDan(kq);
    }

    // =====================================================================
    // NHÓM C: GIÁ TRỊ TRUNG BÌNH, MAX ĐƠN HÀNG - Câu 20, 21, 23
    // =====================================================================

    /**
     * Câu 20: Giá trị trung bình của một đơn hàng.
     *         (Trung bình trên TỔNG TIỀN TỪNG ĐƠN, không phải trên từng dòng chi tiết.)
     *
     * SELECT AVG(TongTien) FROM (
     *     SELECT MaDonHang, SUM(SoLuong * DonGia * (1 - TyLeGiamGia)) AS TongTien
     *     FROM ChiTietDonHang GROUP BY MaDonHang
     * ) t;
     */
    public static double cau20_giaTriTrungBinhDon(DataContext ctx) {
        return tongTienTheoDon(ctx).values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);
    }

    /**
     * Câu 21: Đơn hàng có giá trị lớn nhất (trả về tất cả đơn nếu đồng hạng).
     *
     * SELECT MaDonHang, SUM(SoLuong * DonGia * (1 - TyLeGiamGia)) AS TongTien
     * FROM ChiTietDonHang
     * GROUP BY MaDonHang
     * HAVING TongTien = (SELECT MAX(t.TongTien) FROM (
     *     SELECT SUM(SoLuong * DonGia * (1 - TyLeGiamGia)) AS TongTien
     *     FROM ChiTietDonHang GROUP BY MaDonHang) t);
     */
    public static Map<String, Double> cau21_donGiaTriLonNhat(DataContext ctx) {
        Map<String, Double> tong = tongTienTheoDon(ctx);
        double max = tong.values().stream()
                .mapToDouble(Double::doubleValue).max().orElse(0);
        return tong.entrySet().stream()
                .filter(e -> Math.abs(e.getValue() - max) < 1e-6)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));
    }

    /**
     * Câu 23: Các đơn hàng có giá trị lớn hơn giá trị trung bình, giảm dần.
     *
     * SELECT MaDonHang, SUM(SoLuong * DonGia * (1 - TyLeGiamGia)) AS TongTien
     * FROM ChiTietDonHang
     * GROUP BY MaDonHang
     * HAVING TongTien > (SELECT AVG(t.TongTien) FROM (
     *     SELECT SUM(SoLuong * DonGia * (1 - TyLeGiamGia)) AS TongTien
     *     FROM ChiTietDonHang GROUP BY MaDonHang) t)
     * ORDER BY TongTien DESC;
     */
    public static Map<String, Double> cau23_donLonHonTrungBinh(DataContext ctx) {
        Map<String, Double> tong = tongTienTheoDon(ctx);
        double tb = cau20_giaTriTrungBinhDon(ctx);
        Map<String, Double> kq = tong.entrySet().stream()
                .filter(e -> e.getValue() > tb)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return sapXepGiamDan(kq);
    }

    // =====================================================================
    // NHÓM D: ĐƠN HOÀN THÀNH & TỔNG HỢP DOANH THU - Câu 17, 29, 30
    // =====================================================================

    /**
     * Câu 17: Danh sách các đơn HOÀN THÀNH kèm tổng tiền, giảm dần.
     *
     * SELECT dh.MaDonHang, dh.NgayDat,
     *        SUM(ct.SoLuong * ct.DonGia * (1 - ct.TyLeGiamGia)) AS TongTien
     * FROM DonHang dh
     * JOIN ChiTietDonHang ct ON dh.MaDonHang = ct.MaDonHang
     * WHERE dh.TrangThai = 'HOAN_THANH'
     * GROUP BY dh.MaDonHang, dh.NgayDat
     * ORDER BY TongTien DESC;
     */
    public static Map<String, Double> cau17_donHoanThanhKemTongTien(DataContext ctx) {
        return sapXepGiamDan(tongTienDonHoanThanh(ctx));
    }

    /**
     * Câu 29: Tổng doanh thu của tất cả đơn HOÀN THÀNH.
     *
     * SELECT SUM(ct.SoLuong * ct.DonGia * (1 - ct.TyLeGiamGia)) AS TongDoanhThu
     * FROM DonHang dh
     * JOIN ChiTietDonHang ct ON dh.MaDonHang = ct.MaDonHang
     * WHERE dh.TrangThai = 'HOAN_THANH';
     */
    public static double cau29_tongDoanhThuHoanThanh(DataContext ctx) {
        return tongTienDonHoanThanh(ctx).values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    /**
     * Câu 30: Doanh thu (đơn HOÀN THÀNH) theo từng khách hàng, giảm dần.
     *         Key là mã khách hàng.
     *
     * SELECT dh.MaKhachHang,
     *        SUM(ct.SoLuong * ct.DonGia * (1 - ct.TyLeGiamGia)) AS DoanhThu
     * FROM DonHang dh
     * JOIN ChiTietDonHang ct ON dh.MaDonHang = ct.MaDonHang
     * WHERE dh.TrangThai = 'HOAN_THANH'
     * GROUP BY dh.MaKhachHang
     * ORDER BY DoanhThu DESC;
     */
    public static Map<String, Double> cau30_doanhThuTheoKhachHang(DataContext ctx) {
        Map<String, Double> tong = tongTienTheoDon(ctx);
        Map<String, Double> kq = ctx.getDonHangs().stream()
                .filter(DonHang::isHoanThanh)
                .collect(Collectors.groupingBy(
                        DonHang::getMaKhachHang,
                        Collectors.summingDouble(d -> tong.getOrDefault(d.getMaDonHang(), 0.0))));
        return sapXepGiamDan(kq);
    }

    // =====================================================================
    // CHẠY THỬ TẤT CẢ (gọi từ Main.java để kiểm tra kết quả)
    // =====================================================================
    public static void chayTatCa(DataContext ctx) {
        System.out.println("=== Câu 8: Thành tiền từng dòng chi tiết ===");
        cau8_thanhTienTungDong(ctx).forEach(System.out::println);

        System.out.println("\n=== Câu 9: Tổng tiền mỗi đơn ===");
        cau9_tongTienMoiDon(ctx).forEach((k, v) ->
                System.out.printf("%s : %,.0f%n", k, v));

        System.out.println("\n=== Câu 10: Số lượng bán theo sản phẩm ===");
        cau10_soLuongBanTheoSanPham(ctx).forEach((k, v) ->
                System.out.printf("%s : %d%n", k, v));

        System.out.println("\n=== Câu 11: Doanh thu theo sản phẩm ===");
        cau11_doanhThuTheoSanPham(ctx).forEach((k, v) ->
                System.out.printf("%s : %,.0f%n", k, v));

        System.out.println("\n=== Câu 17: Đơn hoàn thành kèm tổng tiền ===");
        cau17_donHoanThanhKemTongTien(ctx).forEach((k, v) ->
                System.out.printf("%s : %,.0f%n", k, v));

        System.out.printf("%n=== Câu 20: Giá trị trung bình đơn: %,.0f%n",
                cau20_giaTriTrungBinhDon(ctx));

        System.out.println("\n=== Câu 21: Đơn có giá trị lớn nhất ===");
        cau21_donGiaTriLonNhat(ctx).forEach((k, v) ->
                System.out.printf("%s : %,.0f%n", k, v));

        System.out.println("\n=== Câu 23: Đơn lớn hơn giá trị trung bình ===");
        cau23_donLonHonTrungBinh(ctx).forEach((k, v) ->
                System.out.printf("%s : %,.0f%n", k, v));

        System.out.printf("%n=== Câu 29: Tổng doanh thu đơn hoàn thành: %,.0f%n",
                cau29_tongDoanhThuHoanThanh(ctx));

        System.out.println("\n=== Câu 30: Doanh thu theo khách hàng (đơn hoàn thành) ===");
        cau30_doanhThuTheoKhachHang(ctx).forEach((k, v) ->
                System.out.printf("%s : %,.0f%n", k, v));
    }
}