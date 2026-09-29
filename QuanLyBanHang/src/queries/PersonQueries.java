package queries;

import database.DataContext;
import models.DonHang;
import models.KhachHang;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Các câu truy vấn về Khách hàng & Nhân viên.
 * Phần của Người 3 trong file này: Câu 13, 14, 18.
 * Các câu 4, 16, 25, 26, 27 do người khác viết, KHÔNG sửa ở đây.
 *
 * DataContext cần có: getKhachHangs(), getDonHangs(), getChiTietDonHangs()
 * KhachHang cần có:   getMaKhachHang(), getHoTen()
 *
 * Câu 13 dùng hàm dùng chung của OrderQueries (cùng package queries),
 * nên hai file này phải được gộp cùng nhau.
 */
public class PersonQueries {

    /**
     * Câu 13: Tính doanh thu từng khách, gồm cả khách có doanh thu bằng 0.
     *         Doanh thu = tổng tiền các đơn HOÀN THÀNH. Sắp xếp giảm dần.
     *
     * SELECT kh.MaKhachHang, kh.HoTen,
     *        COALESCE(SUM(ct.SoLuong * ct.DonGia * (1 - ct.TyLeGiamGia)), 0) AS DoanhThu
     * FROM KhachHang kh
     * LEFT JOIN DonHang dh
     *        ON kh.MaKhachHang = dh.MaKhachHang AND dh.TrangThai = 'HOAN_THANH'
     * LEFT JOIN ChiTietDonHang ct ON dh.MaDonHang = ct.MaDonHang
     * GROUP BY kh.MaKhachHang, kh.HoTen
     * ORDER BY DoanhThu DESC;
     */
    public static Map<KhachHang, Double> cau13_doanhThuTungKhach(DataContext ctx) {
        Map<String, Double> doanhThu = OrderQueries.doanhThuTheoMaKhach(ctx);

        // Duyệt danh sách KHÁCH HÀNG (không duyệt danh sách đơn) để giữ khách doanh thu 0
        return ctx.getKhachHangs().stream()
                .sorted((a, b) -> Double.compare(
                        doanhThu.getOrDefault(b.getMaKhachHang(), 0.0),
                        doanhThu.getOrDefault(a.getMaKhachHang(), 0.0)))
                .collect(Collectors.toMap(
                        kh -> kh,
                        kh -> doanhThu.getOrDefault(kh.getMaKhachHang(), 0.0),
                        (a, b) -> a, LinkedHashMap::new));
    }

    /**
     * Câu 14: Tìm khách hàng CHƯA TỪNG đặt hàng (không có đơn nào, mọi trạng thái).
     *
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
     *         Mỗi khách kèm danh sách đơn của họ; danh sách RỖNG nghĩa là
     *         khách chưa có đơn (tương ứng dòng có DonHang = NULL trong SQL).
     *
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
    // CHẠY THỬ CÁC CÂU CỦA NGƯỜI 3 (gọi từ Main.java)
    // =====================================================================
    public static void chayCauNguoi3(DataContext ctx) {
        System.out.println("=== Câu 13: Doanh thu từng khách (gồm khách = 0) ===");
        cau13_doanhThuTungKhach(ctx).forEach((kh, tien) ->
                System.out.printf("%s - %s : %,.0f%n", kh.getMaKhachHang(), kh.getHoTen(), tien));

        System.out.println("\n=== Câu 14: Khách chưa từng đặt hàng ===");
        cau14_khachChuaTungDatHang(ctx).forEach(kh ->
                System.out.printf("%s - %s%n", kh.getMaKhachHang(), kh.getHoTen()));

        System.out.println("\n=== Câu 18: LEFT JOIN khách hàng với đơn hàng ===");
        cau18_leftJoinKhachDon(ctx).forEach((kh, ds) -> {
            if (ds.isEmpty()) {
                System.out.printf("%s - %s | (NULL - chưa có đơn)%n",
                        kh.getMaKhachHang(), kh.getHoTen());
            } else {
                ds.forEach(d -> System.out.printf("%s - %s | Đơn %s | %s | %s%n",
                        kh.getMaKhachHang(), kh.getHoTen(),
                        d.getMaDonHang(), d.getNgayDat(), d.getTrangThai()));
            }
        });
    }
}