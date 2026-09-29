package queries;

import database.DataContext;
import models.ChiTietDonHang;
import models.DonHang;
import models.KhachHang;
import models.TrangThaiDonHang;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Các câu truy vấn về Đơn hàng & Chi tiết đơn hàng (phần của Người 3):
 *   Câu 8, 9, 10, 11, 17, 20, 21, 23, 29, 30.
 * Các câu 6, 7 do người khác viết, KHÔNG sửa ở đây.
 *
 * DataContext cần có: getKhachHangs(), getDonHangs(), getChiTietDonHangs()
 * KhachHang cần có:   getMaKhachHang(), getHoTen(), getThanhPho(), getEmail()
 *
 * QUY ƯỚC (ghi rõ để cả nhóm thống nhất):
 *  - Thành tiền một dòng = SoLuong * DonGia * (1 - TyLeGiamGia).
 *  - Tổng tiền một đơn   = tổng thành tiền các dòng chi tiết của đơn đó.
 *  - "Doanh thu" / "đã mua" = chỉ tính đơn HOAN_THANH (xem TINH_DOANH_THU).
 *  - Câu 9, 20, 21 xét TẤT CẢ đơn (đề không nói trạng thái).
 */
public class OrderQueries {

    // =====================================================================
    // HÀM DÙNG CHUNG
    // =====================================================================

    /**
     * Điều kiện đơn được tính vào doanh thu / chi tiêu của khách.
     * Nếu nhóm muốn đổi quy ước, chỉ cần sửa đúng dòng này.
     */
    static final Predicate<DonHang> TINH_DOANH_THU = DonHang::isHoanThanh;

    /** Sắp xếp Map theo giá trị giảm dần (giữ thứ tự bằng LinkedHashMap). */
    static <K, V extends Comparable<V>> Map<K, V> sapXepGiamDan(Map<K, V> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<K, V>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));
    }

    /**
     * Tổng tiền của MỌI đơn hàng (theo thứ tự trong danh sách đơn).
     * Đơn chưa có dòng chi tiết nào có tổng = 0.
     */
    static Map<String, Double> tongTienMoiDon(DataContext ctx) {
        Map<String, Double> tongDong = ctx.getChiTietDonHangs().stream()
                .collect(Collectors.groupingBy(
                        ChiTietDonHang::getMaDonHang,
                        Collectors.summingDouble(ChiTietDonHang::getThanhTien)));
        Map<String, Double> kq = new LinkedHashMap<>();
        for (DonHang d : ctx.getDonHangs()) {
            kq.put(d.getMaDonHang(), tongDong.getOrDefault(d.getMaDonHang(), 0.0));
        }
        return kq;
    }

    /** Giá trị trung bình của một đơn hàng (trung bình trên TỔNG TỪNG ĐƠN). */
    static double giaTriTrungBinhDon(DataContext ctx) {
        return tongTienMoiDon(ctx).values().stream()
                .mapToDouble(Double::doubleValue).average().orElse(0);
    }

    /** maKhachHang -> doanh thu (chỉ các đơn thỏa TINH_DOANH_THU). */
    static Map<String, Double> doanhThuTheoMaKhach(DataContext ctx) {
        Map<String, Double> tong = tongTienMoiDon(ctx);
        return ctx.getDonHangs().stream()
                .filter(TINH_DOANH_THU)
                .collect(Collectors.groupingBy(
                        DonHang::getMaKhachHang,
                        Collectors.summingDouble(d -> tong.getOrDefault(d.getMaDonHang(), 0.0))));
    }

    /** maKhachHang -> số đơn (chỉ các đơn thỏa TINH_DOANH_THU). */
    static Map<String, Long> soDonTheoMaKhach(DataContext ctx) {
        return ctx.getDonHangs().stream()
                .filter(TINH_DOANH_THU)
                .collect(Collectors.groupingBy(DonHang::getMaKhachHang, Collectors.counting()));
    }

    // =====================================================================
    // CÁC LỚP KẾT QUẢ (cho câu 21, 23, 30)
    // =====================================================================

    /** Kết quả câu 21: đơn hàng kèm thông tin khách hàng. */
    public static class DonHangKemKhach {
        public final DonHang donHang;
        public final KhachHang khachHang;
        public final double tongTien;

        public DonHangKemKhach(DonHang donHang, KhachHang khachHang, double tongTien) {
            this.donHang = donHang;
            this.khachHang = khachHang;
            this.tongTien = tongTien;
        }

        @Override
        public String toString() {
            String kh = (khachHang == null)
                    ? "(không tìm thấy khách " + donHang.getMaKhachHang() + ")"
                    : String.format("%s - %s - %s - %s", khachHang.getMaKhachHang(),
                            khachHang.getHoTen(), khachHang.getThanhPho(), khachHang.getEmail());
            return String.format("Đơn %s | Ngày %s | %s | Tổng tiền: %,.0f%n   Khách hàng: %s",
                    donHang.getMaDonHang(), donHang.getNgayDat(), donHang.getTrangThai(),
                    tongTien, kh);
        }
    }

    /** Kết quả câu 23: đơn hủy / đang xử lý và tổng giá trị thất thu / chờ duyệt. */
    public static class KetQuaCau23 {
        /** Đơn (DA_HUY hoặc DANG_XU_LY) -> tổng tiền của đơn đó. */
        public final Map<DonHang, Double> donHangs = new LinkedHashMap<>();
        /** Tổng giá trị các đơn DA_HUY (thất thu). */
        public double thatThu = 0;
        /** Tổng giá trị các đơn DANG_XU_LY (chờ duyệt). */
        public double choDuyet = 0;
    }

    /** Kết quả câu 30: một dòng của ma trận khách hàng. */
    public static class BaoCaoKhachHang {
        public final String maKhachHang;
        public final String hoTen;
        public final long tongSoDon;
        public final int tongSanPham;
        public final double tongChiTieu;

        public BaoCaoKhachHang(String maKhachHang, String hoTen, long tongSoDon,
                               int tongSanPham, double tongChiTieu) {
            this.maKhachHang = maKhachHang;
            this.hoTen = hoTen;
            this.tongSoDon = tongSoDon;
            this.tongSanPham = tongSanPham;
            this.tongChiTieu = tongChiTieu;
        }

        @Override
        public String toString() {
            return String.format("%-6s | %-25s | Số đơn: %2d | Số SP: %3d | Chi tiêu: %,.0f",
                    maKhachHang, hoTen, tongSoDon, tongSanPham, tongChiTieu);
        }
    }

    // =====================================================================
    // CÂU 8, 9, 10, 11: CHI TIẾT ĐƠN HÀNG, TÍNH TIỀN
    // =====================================================================

    /**
     * Câu 8: Xem chi tiết và thành tiền của một đơn hàng (đề: đơn 1001).
     *
     * SELECT ct.MaSanPham, ct.SoLuong, ct.DonGia, ct.TyLeGiamGia,
     *        ct.SoLuong * ct.DonGia * (1 - ct.TyLeGiamGia) AS ThanhTien
     * FROM ChiTietDonHang ct
     * WHERE ct.MaDonHang = '1001';
     */
    public static List<ChiTietDonHang> cau8_chiTietDon(DataContext ctx, String maDonHang) {
        return ctx.getChiTietDonHangs().stream()
                .filter(ct -> ct.getMaDonHang().equals(maDonHang))
                .collect(Collectors.toList());
    }

    /** Câu 8 đúng theo đề: đơn 1001. */
    public static List<ChiTietDonHang> cau8_chiTietDon1001(DataContext ctx) {
        return cau8_chiTietDon(ctx, "1001");
    }

    /**
     * Câu 9: Tính tổng tiền từng đơn.
     *
     * SELECT dh.MaDonHang,
     *        COALESCE(SUM(ct.SoLuong * ct.DonGia * (1 - ct.TyLeGiamGia)), 0) AS TongTien
     * FROM DonHang dh
     * LEFT JOIN ChiTietDonHang ct ON dh.MaDonHang = ct.MaDonHang
     * GROUP BY dh.MaDonHang
     * ORDER BY dh.MaDonHang;
     */
    public static Map<String, Double> cau9_tongTienTungDon(DataContext ctx) {
        return tongTienMoiDon(ctx);
    }

    /**
     * Câu 10: Tính tổng doanh thu các đơn HOÀN THÀNH.
     *
     * SELECT SUM(ct.SoLuong * ct.DonGia * (1 - ct.TyLeGiamGia)) AS TongDoanhThu
     * FROM DonHang dh
     * JOIN ChiTietDonHang ct ON dh.MaDonHang = ct.MaDonHang
     * WHERE dh.TrangThai = 'HOAN_THANH';
     */
    public static double cau10_tongDoanhThuHoanThanh(DataContext ctx) {
        Map<String, Double> tong = tongTienMoiDon(ctx);
        return ctx.getDonHangs().stream()
                .filter(DonHang::isHoanThanh)
                .mapToDouble(d -> tong.getOrDefault(d.getMaDonHang(), 0.0))
                .sum();
    }

    /**
     * Câu 11: Thống kê doanh thu theo tháng (đơn hoàn thành), tháng tăng dần.
     *
     * SELECT YEAR(dh.NgayDat) AS Nam, MONTH(dh.NgayDat) AS Thang,
     *        SUM(ct.SoLuong * ct.DonGia * (1 - ct.TyLeGiamGia)) AS DoanhThu
     * FROM DonHang dh
     * JOIN ChiTietDonHang ct ON dh.MaDonHang = ct.MaDonHang
     * WHERE dh.TrangThai = 'HOAN_THANH'
     * GROUP BY YEAR(dh.NgayDat), MONTH(dh.NgayDat)
     * ORDER BY Nam, Thang;
     */
    public static Map<YearMonth, Double> cau11_doanhThuTheoThang(DataContext ctx) {
        Map<String, Double> tong = tongTienMoiDon(ctx);
        return ctx.getDonHangs().stream()
                .filter(TINH_DOANH_THU)
                .collect(Collectors.groupingBy(
                        d -> YearMonth.from(d.getNgayDat()),
                        TreeMap::new,
                        Collectors.summingDouble(d -> tong.getOrDefault(d.getMaDonHang(), 0.0))));
    }

    // =====================================================================
    // CÂU 17: KHÁCH CÓ ÍT NHẤT 2 ĐƠN HOÀN THÀNH
    // =====================================================================

    /**
     * Câu 17: Tìm khách hàng có ít nhất 2 đơn HOÀN THÀNH (kèm số đơn, giảm dần).
     *
     * SELECT kh.MaKhachHang, kh.HoTen, COUNT(*) AS SoDonHoanThanh
     * FROM KhachHang kh
     * JOIN DonHang dh ON kh.MaKhachHang = dh.MaKhachHang
     * WHERE dh.TrangThai = 'HOAN_THANH'
     * GROUP BY kh.MaKhachHang, kh.HoTen
     * HAVING COUNT(*) >= 2
     * ORDER BY SoDonHoanThanh DESC;
     */
    public static Map<KhachHang, Long> cau17_khachTu2DonHoanThanh(DataContext ctx) {
        Map<String, Long> dem = ctx.getDonHangs().stream()
                .filter(DonHang::isHoanThanh)
                .collect(Collectors.groupingBy(DonHang::getMaKhachHang, Collectors.counting()));

        Map<KhachHang, Long> kq = new LinkedHashMap<>();
        for (KhachHang kh : ctx.getKhachHangs()) {
            long n = dem.getOrDefault(kh.getMaKhachHang(), 0L);
            if (n >= 2) {
                kq.put(kh, n);
            }
        }
        return sapXepGiamDan(kq);
    }

    // =====================================================================
    // CÂU 20, 21, 23: GIÁ TRỊ TRUNG BÌNH, LỚN NHẤT, HỦY / ĐANG XỬ LÝ
    // =====================================================================

    /**
     * Câu 20: Tìm đơn có giá trị trên trung bình (giảm dần).
     *
     * SELECT MaDonHang, SUM(SoLuong * DonGia * (1 - TyLeGiamGia)) AS TongTien
     * FROM ChiTietDonHang
     * GROUP BY MaDonHang
     * HAVING TongTien > (SELECT AVG(t.TongTien) FROM (
     *     SELECT SUM(SoLuong * DonGia * (1 - TyLeGiamGia)) AS TongTien
     *     FROM ChiTietDonHang GROUP BY MaDonHang) t)
     * ORDER BY TongTien DESC;
     */
    public static Map<String, Double> cau20_donTrenTrungBinh(DataContext ctx) {
        double tb = giaTriTrungBinhDon(ctx);
        Map<String, Double> kq = new LinkedHashMap<>();
        for (Map.Entry<String, Double> e : tongTienMoiDon(ctx).entrySet()) {
            if (e.getValue() > tb) {
                kq.put(e.getKey(), e.getValue());
            }
        }
        return sapXepGiamDan(kq);
    }

    /**
     * Câu 21: Tìm đơn hàng có giá trị cao nhất, kèm thông tin khách hàng
     *         (nếu nhiều đơn đồng hạng thì trả về tất cả).
     *
     * SELECT dh.MaDonHang, SUM(ct.SoLuong * ct.DonGia * (1 - ct.TyLeGiamGia)) AS TongTien,
     *        kh.MaKhachHang, kh.HoTen, kh.ThanhPho, kh.Email
     * FROM DonHang dh
     * JOIN ChiTietDonHang ct ON dh.MaDonHang = ct.MaDonHang
     * JOIN KhachHang kh ON dh.MaKhachHang = kh.MaKhachHang
     * GROUP BY dh.MaDonHang, kh.MaKhachHang, kh.HoTen, kh.ThanhPho, kh.Email
     * ORDER BY TongTien DESC
     * LIMIT 1;   -- (dùng HAVING = MAX(...) nếu muốn lấy cả đơn đồng hạng)
     */
    public static List<DonHangKemKhach> cau21_donGiaTriCaoNhat(DataContext ctx) {
        Map<String, Double> tong = tongTienMoiDon(ctx);
        double max = tong.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);

        Map<String, KhachHang> khachTheoMa = ctx.getKhachHangs().stream()
                .collect(Collectors.toMap(KhachHang::getMaKhachHang, kh -> kh, (a, b) -> a));

        List<DonHangKemKhach> kq = new ArrayList<>();
        for (DonHang d : ctx.getDonHangs()) {
            double t = tong.getOrDefault(d.getMaDonHang(), 0.0);
            if (Math.abs(t - max) < 1e-6) {
                kq.add(new DonHangKemKhach(d, khachTheoMa.get(d.getMaKhachHang()), t));
            }
        }
        return kq;
    }

    /**
     * Câu 23: Tìm các đơn bị HỦY hoặc ĐANG XỬ LÝ, và tổng giá trị
     *         thất thu (đơn hủy) / chờ duyệt (đơn đang xử lý).
     *
     * SELECT dh.MaDonHang, dh.TrangThai,
     *        COALESCE(SUM(ct.SoLuong * ct.DonGia * (1 - ct.TyLeGiamGia)), 0) AS TongTien
     * FROM DonHang dh
     * LEFT JOIN ChiTietDonHang ct ON dh.MaDonHang = ct.MaDonHang
     * WHERE dh.TrangThai IN ('DA_HUY', 'DANG_XU_LY')
     * GROUP BY dh.MaDonHang, dh.TrangThai;
     *
     * -- Tổng theo trạng thái:
     * SELECT dh.TrangThai, SUM(ct.SoLuong * ct.DonGia * (1 - ct.TyLeGiamGia)) AS TongGiaTri
     * FROM DonHang dh JOIN ChiTietDonHang ct ON dh.MaDonHang = ct.MaDonHang
     * WHERE dh.TrangThai IN ('DA_HUY', 'DANG_XU_LY')
     * GROUP BY dh.TrangThai;
     */
    public static KetQuaCau23 cau23_donHuyVaDangXuLy(DataContext ctx) {
        Map<String, Double> tong = tongTienMoiDon(ctx);
        KetQuaCau23 kq = new KetQuaCau23();
        for (DonHang d : ctx.getDonHangs()) {
            double t = tong.getOrDefault(d.getMaDonHang(), 0.0);
            if (d.getTrangThai() == TrangThaiDonHang.DA_HUY) {
                kq.donHangs.put(d, t);
                kq.thatThu += t;
            } else if (d.getTrangThai() == TrangThaiDonHang.DANG_XU_LY) {
                kq.donHangs.put(d, t);
                kq.choDuyet += t;
            }
        }
        return kq;
    }

    // =====================================================================
    // CÂU 29, 30: AOV VÀ MA TRẬN KHÁCH HÀNG
    // =====================================================================

    /**
     * Câu 29: Giá trị đơn hàng trung bình (AOV) của mỗi khách hàng ĐÃ MUA
     *         (chỉ khách có ít nhất 1 đơn hoàn thành), giảm dần.
     *         AOV = tổng chi tiêu / số đơn.
     *
     * SELECT kh.MaKhachHang, kh.HoTen,
     *        SUM(ct.SoLuong * ct.DonGia * (1 - ct.TyLeGiamGia)) / COUNT(DISTINCT dh.MaDonHang) AS AOV
     * FROM KhachHang kh
     * JOIN DonHang dh ON kh.MaKhachHang = dh.MaKhachHang
     * JOIN ChiTietDonHang ct ON dh.MaDonHang = ct.MaDonHang
     * WHERE dh.TrangThai = 'HOAN_THANH'
     * GROUP BY kh.MaKhachHang, kh.HoTen
     * ORDER BY AOV DESC;
     */
    public static Map<KhachHang, Double> cau29_aovMoiKhach(DataContext ctx) {
        Map<String, Double> chiTieu = doanhThuTheoMaKhach(ctx);
        Map<String, Long> soDon = soDonTheoMaKhach(ctx);

        Map<KhachHang, Double> kq = new LinkedHashMap<>();
        for (KhachHang kh : ctx.getKhachHangs()) {
            long n = soDon.getOrDefault(kh.getMaKhachHang(), 0L);
            if (n > 0) {
                kq.put(kh, chiTieu.getOrDefault(kh.getMaKhachHang(), 0.0) / n);
            }
        }
        return sapXepGiamDan(kq);
    }

    /**
     * Câu 30: Báo cáo ma trận khách hàng: Tên, Tổng số đơn, Tổng sản phẩm đã mua,
     *         Tổng chi tiêu (tính trên đơn HOÀN THÀNH; khách chưa mua hiện 0).
     *         "Tổng sản phẩm" = tổng SoLuong. Sắp xếp theo chi tiêu giảm dần.
     *
     * SELECT kh.HoTen,
     *        COUNT(DISTINCT dh.MaDonHang) AS TongSoDon,
     *        COALESCE(SUM(ct.SoLuong), 0) AS TongSanPham,
     *        COALESCE(SUM(ct.SoLuong * ct.DonGia * (1 - ct.TyLeGiamGia)), 0) AS TongChiTieu
     * FROM KhachHang kh
     * LEFT JOIN DonHang dh
     *        ON kh.MaKhachHang = dh.MaKhachHang AND dh.TrangThai = 'HOAN_THANH'
     * LEFT JOIN ChiTietDonHang ct ON dh.MaDonHang = ct.MaDonHang
     * GROUP BY kh.MaKhachHang, kh.HoTen
     * ORDER BY TongChiTieu DESC;
     */
    public static List<BaoCaoKhachHang> cau30_maTranKhachHang(DataContext ctx) {
        // maDonHang -> maKhachHang (chỉ đơn được tính)
        Map<String, String> donCuaKhach = ctx.getDonHangs().stream()
                .filter(TINH_DOANH_THU)
                .collect(Collectors.toMap(DonHang::getMaDonHang, DonHang::getMaKhachHang, (a, b) -> a));

        // maKhachHang -> tổng số lượng sản phẩm
        Map<String, Integer> soLuongTheoKhach = ctx.getChiTietDonHangs().stream()
                .filter(ct -> donCuaKhach.containsKey(ct.getMaDonHang()))
                .collect(Collectors.groupingBy(
                        ct -> donCuaKhach.get(ct.getMaDonHang()),
                        Collectors.summingInt(ChiTietDonHang::getSoLuong)));

        Map<String, Long> soDon = soDonTheoMaKhach(ctx);
        Map<String, Double> chiTieu = doanhThuTheoMaKhach(ctx);

        List<BaoCaoKhachHang> kq = new ArrayList<>();
        for (KhachHang kh : ctx.getKhachHangs()) {
            String ma = kh.getMaKhachHang();
            kq.add(new BaoCaoKhachHang(ma, kh.getHoTen(),
                    soDon.getOrDefault(ma, 0L),
                    soLuongTheoKhach.getOrDefault(ma, 0),
                    chiTieu.getOrDefault(ma, 0.0)));
        }
        kq.sort((a, b) -> Double.compare(b.tongChiTieu, a.tongChiTieu));
        return kq;
    }

    // =====================================================================
    // CHẠY THỬ TẤT CẢ (gọi từ Main.java để kiểm tra kết quả)
    // =====================================================================
    public static void chayTatCa(DataContext ctx) {
        System.out.println("=== Câu 8: Chi tiết và thành tiền đơn 1001 ===");
        List<ChiTietDonHang> ct8 = cau8_chiTietDon1001(ctx);
        ct8.forEach(System.out::println);
        System.out.printf("Tổng tiền đơn 1001: %,.0f%n",
                ct8.stream().mapToDouble(ChiTietDonHang::getThanhTien).sum());

        System.out.println("\n=== Câu 9: Tổng tiền từng đơn ===");
        cau9_tongTienTungDon(ctx).forEach((k, v) -> System.out.printf("Đơn %s : %,.0f%n", k, v));

        System.out.printf("%n=== Câu 10: Tổng doanh thu đơn hoàn thành: %,.0f%n",
                cau10_tongDoanhThuHoanThanh(ctx));

        System.out.println("\n=== Câu 11: Doanh thu theo tháng ===");
        cau11_doanhThuTheoThang(ctx).forEach((k, v) -> System.out.printf("%s : %,.0f%n", k, v));

        System.out.println("\n=== Câu 17: Khách có ít nhất 2 đơn hoàn thành ===");
        cau17_khachTu2DonHoanThanh(ctx).forEach((kh, n) ->
                System.out.printf("%s - %s : %d đơn%n", kh.getMaKhachHang(), kh.getHoTen(), n));

        System.out.printf("%n=== Câu 20: Đơn trên trung bình (TB = %,.0f) ===%n",
                giaTriTrungBinhDon(ctx));
        cau20_donTrenTrungBinh(ctx).forEach((k, v) -> System.out.printf("Đơn %s : %,.0f%n", k, v));

        System.out.println("\n=== Câu 21: Đơn có giá trị cao nhất ===");
        cau21_donGiaTriCaoNhat(ctx).forEach(System.out::println);

        System.out.println("\n=== Câu 23: Đơn hủy / đang xử lý ===");
        KetQuaCau23 c23 = cau23_donHuyVaDangXuLy(ctx);
        c23.donHangs.forEach((d, t) -> System.out.printf("%s | Tổng: %,.0f%n", d, t));
        System.out.printf("Tổng thất thu (đã hủy)    : %,.0f%n", c23.thatThu);
        System.out.printf("Tổng chờ duyệt (đang xử lý): %,.0f%n", c23.choDuyet);

        System.out.println("\n=== Câu 29: AOV của mỗi khách đã mua ===");
        cau29_aovMoiKhach(ctx).forEach((kh, v) ->
                System.out.printf("%s - %s : %,.0f%n", kh.getMaKhachHang(), kh.getHoTen(), v));

        System.out.println("\n=== Câu 30: Ma trận khách hàng ===");
        cau30_maTranKhachHang(ctx).forEach(System.out::println);
    }
}