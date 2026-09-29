package queries;

import database.DataContext;
import models.ChiTietDonHang;
import models.DonHang;
import models.KhachHang;
import models.NhanVien;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * =========================================================================
 * PHẦN TRUY VẤN CỦA NGƯỜI 2: KHÁCH HÀNG & NHÂN VIÊN
 * =========================================================================
 * Phụ trách 8 câu truy vấn:
 *   - Nhóm T_KH1: Câu 4, 25, 26 (Đơn bảng & Self-join)
 *   - Nhóm T_KH2: Câu 13, 14, 18 (Left join DonHang)
 *   - Nhóm T_KH3: Câu 16, 27 (Doanh số & Nhân viên chưa có đơn)
 * =========================================================================
 */
public class PersonQueries {

    // =====================================================================
    // HÀM TIỆN ÍCH DÙNG CHUNG NỘI BỘ
    // =====================================================================

    /**
     * Tính tổng tiền cho từng đơn hàng hoàn thành:
     * Tổng tiền đơn = SUM(SoLuong * DonGia * (1 - TyLeGiamGia))
     */
    private static Map<String, Double> tinhTongTienDonHoanThanh(DataContext ctx) {
        Set<String> donHoanThanhIds = ctx.getDonHangs().stream()
                .filter(DonHang::isHoanThanh)
                .map(DonHang::getMaDonHang)
                .collect(Collectors.toSet());

        return ctx.getChiTietDonHangs().stream()
                .filter(ct -> donHoanThanhIds.contains(ct.getMaDonHang()))
                .collect(Collectors.groupingBy(
                        ChiTietDonHang::getMaDonHang,
                        Collectors.summingDouble(ChiTietDonHang::getThanhTien)
                ));
    }

    // =====================================================================
    // NHÓM T_KH1: ĐƠN BẢNG & SELF-JOIN (CÂU 4, 25, 26)
    // =====================================================================

    /**
     * Câu 4: Tìm khách hàng ở TP.HCM.
     *
     * SQL tương đương:
     * SELECT *
     * FROM KhachHang
     * WHERE ThanhPho = 'TP HCM';
     */
    public static List<KhachHang> cau04_timKhachHangTPHCM(DataContext ctx) {
        return ctx.getKhachHangs().stream()
                .filter(kh -> kh.getThanhPho() != null &&
                        (kh.getThanhPho().equalsIgnoreCase("TP HCM") ||
                         kh.getThanhPho().equalsIgnoreCase("TP.HCM") ||
                         kh.getThanhPho().toLowerCase().contains("hcm")))
                .collect(Collectors.toList());
    }

    /**
     * Câu 25: Thống kê số lượng khách hàng theo từng tỉnh/thành phố.
     * Sắp xếp giảm dần theo số lượng khách.
     *
     * SQL tương đương:
     * SELECT ThanhPho, COUNT(*) AS SoLuongKhach
     * FROM KhachHang
     * GROUP BY ThanhPho
     * ORDER BY SoLuongKhach DESC;
     */
    public static Map<String, Long> cau25_soKhachTheoThanhPho(DataContext ctx) {
        Map<String, Long> demTheoTP = ctx.getKhachHangs().stream()
                .collect(Collectors.groupingBy(
                        KhachHang::getThanhPho,
                        Collectors.counting()
                ));

        return demTheoTP.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldVal, newVal) -> oldVal,
                        LinkedHashMap::new
                ));
    }

    /**
     * DTO lưu cặp khách hàng cùng tỉnh/thành phố (Câu 26).
     */
    public static class CapKhachHang {
        private final KhachHang khachHang1;
        private final KhachHang khachHang2;
        private final String thanhPho;

        public CapKhachHang(KhachHang khachHang1, KhachHang khachHang2, String thanhPho) {
            this.khachHang1 = khachHang1;
            this.khachHang2 = khachHang2;
            this.thanhPho = thanhPho;
        }

        public KhachHang getKhachHang1() { return khachHang1; }
        public KhachHang getKhachHang2() { return khachHang2; }
        public String getThanhPho() { return thanhPho; }

        @Override
        public String toString() {
            return String.format("[%s] (Mã %s) %-15s <---> (Mã %s) %-15s",
                    thanhPho,
                    khachHang1.getMaKhachHang(), khachHang1.getHoTen(),
                    khachHang2.getMaKhachHang(), khachHang2.getHoTen());
        }
    }

    /**
     * Câu 26: Tìm các cặp khách hàng ở cùng một tỉnh/thành phố (Self Join).
     * Điều kiện: a.MaKhachHang < b.MaKhachHang để tránh ghép đôi với chính mình
     * và loại bỏ các cặp hoán vị đối xứng (A, B) và (B, A).
     *
     * SQL tương đương:
     * SELECT a.MaKhachHang AS MaKH1, a.HoTen AS TenKH1,
     *        b.MaKhachHang AS MaKH2, b.HoTen AS TenKH2,
     *        a.ThanhPho
     * FROM KhachHang a
     * JOIN KhachHang b
     *   ON a.ThanhPho = b.ThanhPho AND a.MaKhachHang < b.MaKhachHang;
     */
    public static List<CapKhachHang> cau26_capKhachCungThanhPho(DataContext ctx) {
        List<KhachHang> dsKhach = ctx.getKhachHangs();
        List<CapKhachHang> ketQua = new ArrayList<>();

        for (int i = 0; i < dsKhach.size(); i++) {
            KhachHang k1 = dsKhach.get(i);
            for (int j = i + 1; j < dsKhach.size(); j++) {
                KhachHang k2 = dsKhach.get(j);
                if (k1.getThanhPho() != null && k1.getThanhPho().equalsIgnoreCase(k2.getThanhPho())) {
                    ketQua.add(new CapKhachHang(k1, k2, k1.getThanhPho()));
                }
            }
        }
        return ketQua;
    }

    // =====================================================================
    // NHÓM T_KH2: LEFT JOIN DONHANG (CÂU 13, 14, 18)
    // =====================================================================

    /**
     * Câu 13: Tính doanh thu từng khách, gồm cả khách có doanh thu bằng 0.
     * Doanh thu = tổng tiền các đơn HOÀN THÀNH. Sắp xếp giảm dần theo doanh thu.
     *
     * SQL tương đương:
     * SELECT kh.MaKhachHang, kh.HoTen,
     *        COALESCE(SUM(ct.SoLuong * ct.DonGia * (1 - ct.TyLeGiamGia)), 0) AS DoanhThu
     * FROM KhachHang kh
     * LEFT JOIN DonHang dh
     *        ON kh.MaKhachHang = dh.MaKhachHang AND dh.TrangThai = 'HoanThanh'
     * LEFT JOIN ChiTietDonHang ct ON dh.MaDonHang = ct.MaDonHang
     * GROUP BY kh.MaKhachHang, kh.HoTen
     * ORDER BY DoanhThu DESC;
     */
    public static Map<KhachHang, Double> cau13_doanhThuTungKhach(DataContext ctx) {
        Map<String, Double> tienMoiDon = tinhTongTienDonHoanThanh(ctx);

        Map<String, Double> doanhThuTheoKhach = ctx.getDonHangs().stream()
                .filter(DonHang::isHoanThanh)
                .collect(Collectors.groupingBy(
                        DonHang::getMaKhachHang,
                        Collectors.summingDouble(dh -> tienMoiDon.getOrDefault(dh.getMaDonHang(), 0.0))
                ));

        return ctx.getKhachHangs().stream()
                .sorted((a, b) -> Double.compare(
                        doanhThuTheoKhach.getOrDefault(b.getMaKhachHang(), 0.0),
                        doanhThuTheoKhach.getOrDefault(a.getMaKhachHang(), 0.0)))
                .collect(Collectors.toMap(
                        kh -> kh,
                        kh -> doanhThuTheoKhach.getOrDefault(kh.getMaKhachHang(), 0.0),
                        (oldVal, newVal) -> oldVal,
                        LinkedHashMap::new
                ));
    }

    /**
     * Câu 14: Tìm khách hàng CHƯA TỪNG đặt hàng (không có bất kỳ đơn nào).
     *
     * SQL tương đương:
     * SELECT kh.*
     * FROM KhachHang kh
     * LEFT JOIN DonHang dh ON kh.MaKhachHang = dh.MaKhachHang
     * WHERE dh.MaDonHang IS NULL;
     */
    public static List<KhachHang> cau14_khachChuaTungDatHang(DataContext ctx) {
        Set<String> daDat = ctx.getDonHangs().stream()
                .map(DonHang::getMaKhachHang)
                .collect(Collectors.toSet());

        return ctx.getKhachHangs().stream()
                .filter(kh -> !daDat.contains(kh.getMaKhachHang()))
                .collect(Collectors.toList());
    }

    /**
     * Câu 18: LEFT JOIN khách hàng với đơn hàng.
     * Mỗi khách hàng kèm danh sách đơn đã đặt; nếu rỗng tương ứng NULL trong SQL.
     *
     * SQL tương đương:
     * SELECT kh.MaKhachHang, kh.HoTen, dh.MaDonHang, dh.NgayDat, dh.TrangThai
     * FROM KhachHang kh
     * LEFT JOIN DonHang dh ON kh.MaKhachHang = dh.MaKhachHang
     * ORDER BY kh.MaKhachHang, dh.NgayDat;
     */
    public static Map<KhachHang, List<DonHang>> cau18_leftJoinKhachDon(DataContext ctx) {
        Map<String, List<DonHang>> donTheoKhach = ctx.getDonHangs().stream()
                .collect(Collectors.groupingBy(DonHang::getMaKhachHang));

        Map<KhachHang, List<DonHang>> kq = new LinkedHashMap<>();
        for (KhachHang kh : ctx.getKhachHangs()) {
            List<DonHang> ds = new ArrayList<>(
                    donTheoKhach.getOrDefault(kh.getMaKhachHang(), new ArrayList<>()));
            ds.sort((a, b) -> a.getNgayDat().compareTo(b.getNgayDat()));
            kq.put(kh, ds);
        }
        return kq;
    }

    // =====================================================================
    // NHÓM T_KH3: DOANH SỐ & NHÂN VIÊN (CÂU 16, 27)
    // =====================================================================

    /**
     * DTO lưu thống kê của từng nhân viên (Câu 16).
     */
    public static class ThongKeNhanVien {
        private final NhanVien nhanVien;
        private final long soDon;
        private final double tongDoanhThu;

        public ThongKeNhanVien(NhanVien nhanVien, long soDon, double tongDoanhThu) {
            this.nhanVien = nhanVien;
            this.soDon = soDon;
            this.tongDoanhThu = tongDoanhThu;
        }

        public NhanVien getNhanVien() { return nhanVien; }
        public long getSoDon() { return soDon; }
        public double getTongDoanhThu() { return tongDoanhThu; }

        @Override
        public String toString() {
            return String.format("%-4s | %-18s | %-20s | Đơn hoàn thành: %2d | Doanh thu: %,13.0f VND",
                    nhanVien.getMaNhanVien(), nhanVien.getHoTen(), nhanVien.getBoPhan(),
                    soDon, tongDoanhThu);
        }
    }

    /**
     * Câu 16: Thống kê số đơn và doanh thu từng nhân viên.
     * Bao gồm tất cả nhân viên (kể cả nhân viên chưa có doanh số).
     *
     * SQL tương đương:
     * SELECT nv.MaNhanVien, nv.HoTen, nv.BoPhan,
     *        COUNT(DISTINCT dh.MaDonHang) AS SoDonHang,
     *        COALESCE(SUM(ct.SoLuong * ct.DonGia * (1 - ct.TyLeGiamGia)), 0) AS TongDoanhThu
     * FROM NhanVien nv
     * LEFT JOIN DonHang dh ON nv.MaNhanVien = dh.MaNhanVien AND dh.TrangThai = 'HoanThanh'
     * LEFT JOIN ChiTietDonHang ct ON dh.MaDonHang = ct.MaDonHang
     * GROUP BY nv.MaNhanVien, nv.HoTen, nv.BoPhan
     * ORDER BY TongDoanhThu DESC;
     */
    public static List<ThongKeNhanVien> cau16_thongKeNhanVien(DataContext ctx) {
        Map<String, Double> tienMoiDon = tinhTongTienDonHoanThanh(ctx);

        Map<String, List<DonHang>> donTheoNV = ctx.getDonHangs().stream()
                .filter(DonHang::isHoanThanh)
                .collect(Collectors.groupingBy(DonHang::getMaNhanVien));

        List<ThongKeNhanVien> ketQua = new ArrayList<>();
        for (NhanVien nv : ctx.getNhanViens()) {
            List<DonHang> dsDon = donTheoNV.getOrDefault(nv.getMaNhanVien(), Collections.emptyList());
            long soDon = dsDon.size();
            double tongTien = dsDon.stream()
                    .mapToDouble(dh -> tienMoiDon.getOrDefault(dh.getMaDonHang(), 0.0))
                    .sum();
            ketQua.add(new ThongKeNhanVien(nv, soDon, tongTien));
        }

        ketQua.sort((a, b) -> Double.compare(b.getTongDoanhThu(), a.getTongDoanhThu()));
        return ketQua;
    }

    /**
     * Câu 27 (Trường hợp 1): Danh sách nhân viên CHƯA LẬP ĐƯỢC ĐƠN HÀNG NÀO.
     *
     * SQL tương đương:
     * SELECT nv.*
     * FROM NhanVien nv
     * LEFT JOIN DonHang dh ON nv.MaNhanVien = dh.MaNhanVien
     * WHERE dh.MaDonHang IS NULL;
     */
    public static List<NhanVien> cau27_nhanVienChuaLapDonNao(DataContext ctx) {
        Set<String> maNVDaLapDon = ctx.getDonHangs().stream()
                .map(DonHang::getMaNhanVien)
                .collect(Collectors.toSet());

        return ctx.getNhanViens().stream()
                .filter(nv -> !maNVDaLapDon.contains(nv.getMaNhanVien()))
                .collect(Collectors.toList());
    }

    /**
     * Câu 27 (Trường hợp 2): Danh sách nhân viên KHÔNG CÓ ĐƠN HOÀN THÀNH.
     *
     * SQL tương đương:
     * SELECT nv.*
     * FROM NhanVien nv
     * WHERE nv.MaNhanVien NOT IN (
     *     SELECT dh.MaNhanVien FROM DonHang dh WHERE dh.TrangThai = 'HoanThanh'
     * );
     */
    public static List<NhanVien> cau27_nhanVienKhongCoDonHoanThanh(DataContext ctx) {
        Set<String> maNVDaHoanThanh = ctx.getDonHangs().stream()
                .filter(DonHang::isHoanThanh)
                .map(DonHang::getMaNhanVien)
                .collect(Collectors.toSet());

        return ctx.getNhanViens().stream()
                .filter(nv -> !maNVDaHoanThanh.contains(nv.getMaNhanVien()))
                .collect(Collectors.toList());
    }

    // =====================================================================
    // CHẠY THỬ TOÀN BỘ CÁC CÂU CỦA NGƯỜI 2
    // =====================================================================
    public static void chayTatCaCauNguoi2(DataContext ctx) {
        System.out.println("===============================================================================");
        System.out.println("                  BÁO CÁO KẾT QUẢ TRUY VẤN - NGƯỜI 2                           ");
        System.out.println("===============================================================================");

        System.out.println("\n>>> [Câu 4] Tìm khách hàng ở TP.HCM:");
        cau04_timKhachHangTPHCM(ctx).forEach(System.out::println);

        System.out.println("\n>>> [Câu 13] Doanh thu từng khách hàng (gồm cả khách = 0):");
        cau13_doanhThuTungKhach(ctx).forEach((kh, tien) ->
                System.out.printf("  %s -> Doanh thu: %,13.0f VND%n", kh, tien));

        System.out.println("\n>>> [Câu 14] Khách hàng chưa từng đặt hàng:");
        cau14_khachChuaTungDatHang(ctx).forEach(kh ->
                System.out.printf("  %s%n", kh));

        System.out.println("\n>>> [Câu 16] Thống kê số đơn & doanh thu từng nhân viên:");
        cau16_thongKeNhanVien(ctx).forEach(tk ->
                System.out.printf("  %s%n", tk));

        System.out.println("\n>>> [Câu 18] LEFT JOIN khách hàng với đơn hàng:");
        cau18_leftJoinKhachDon(ctx).forEach((kh, ds) -> {
            if (ds.isEmpty()) {
                System.out.printf("  %s - %s | (NULL - Chưa từng có đơn hàng)%n",
                        kh.getMaKhachHang(), kh.getHoTen());
            } else {
                ds.forEach(d -> System.out.printf("  %s - %-15s | Đơn: %-4s | Ngày: %s | Trạng thái: %s%n",
                        kh.getMaKhachHang(), kh.getHoTen(),
                        d.getMaDonHang(), d.getNgayDat(), d.getTrangThai()));
            }
        });

        System.out.println("\n>>> [Câu 25] Thống kê số lượng khách hàng theo từng tỉnh/thành phố:");
        cau25_soKhachTheoThanhPho(ctx).forEach((tp, sl) ->
                System.out.printf("  %-12s : %2d khách hàng%n", tp, sl));

        System.out.println("\n>>> [Câu 26] Các cặp khách hàng ở cùng tỉnh/thành phố (Self Join):");
        cau26_capKhachCungThanhPho(ctx).forEach(c ->
                System.out.printf("  %s%n", c));

        System.out.println("\n>>> [Câu 27] Danh sách nhân viên chưa lập được đơn hàng nào:");
        List<NhanVien> chuaLap = cau27_nhanVienChuaLapDonNao(ctx);
        if (chuaLap.isEmpty()) {
            System.out.println("  (Không có nhân viên nào chưa lập đơn)");
        } else {
            chuaLap.forEach(nv -> System.out.printf("  %s%n", nv));
        }

        System.out.println("\n>>> [Câu 27 - Mở rộng] Nhân viên không có đơn hàng HOÀN THÀNH:");
        List<NhanVien> khongHoanThanh = cau27_nhanVienKhongCoDonHoanThanh(ctx);
        if (khongHoanThanh.isEmpty()) {
            System.out.println("  (Tất cả nhân viên đều có ít nhất 1 đơn hoàn thành)");
        } else {
            khongHoanThanh.forEach(nv -> System.out.printf("  %s%n", nv));
        }
        System.out.println("===============================================================================\n");
    }

    // Giữ lại để tương thích nếu các thành viên khác gọi
    public static void chayCauNguoi3(DataContext ctx) {
        chayTatCaCauNguoi2(ctx);
    }
}