package queries;

import database.DataContext;
import models.DonHang;
import models.KhachHang;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Các câu truy vấn về Khách hàng & Nhân viên.
 * Phần của Người 3 trong file này: Câu 13, 14, 18 (KhachHang LEFT JOIN DonHang).
 * Các câu 4, 16, 25, 26, 27 do người khác viết, KHÔNG sửa ở đây.
 *
 * Yêu cầu DataContext có các getter: getKhachHangs(), getDonHangs()
 * Yêu cầu KhachHang có getter: getMaKhachHang(), getHoTen()
 *
 * LƯU Ý: nội dung từng câu được suy ra từ sơ đồ phân công,
 * hãy đối chiếu với đề thật và chỉnh lại nếu khác.
 */
public class PersonQueries {

    /**
     * Câu 13: Liệt kê TẤT CẢ khách hàng kèm số đơn hàng đã đặt
     *         (khách chưa có đơn vẫn xuất hiện với số đơn = 0).
     *
     * SELECT kh.MaKhachHang, kh.HoTen, COUNT(dh.MaDonHang) AS SoDon
     * FROM KhachHang kh
     * LEFT JOIN DonHang dh ON kh.MaKhachHang = dh.MaKhachHang
     * GROUP BY kh.MaKhachHang, kh.HoTen
     * ORDER BY SoDon DESC;
     */
    public static Map<KhachHang, Long> cau13_soDonMoiKhachHang(DataContext ctx) {
        // Đếm số đơn theo mã khách hàng
        Map<String, Long> demDon = ctx.getDonHangs().stream()
                .collect(Collectors.groupingBy(DonHang::getMaKhachHang, Collectors.counting()));

        // Duyệt danh sách KHÁCH HÀNG (không phải danh sách đơn) để giữ khách chưa mua
        return ctx.getKhachHangs().stream()
                .sorted((a, b) -> Long.compare(
                        demDon.getOrDefault(b.getMaKhachHang(), 0L),
                        demDon.getOrDefault(a.getMaKhachHang(), 0L)))
                .collect(Collectors.toMap(
                        kh -> kh,
                        kh -> demDon.getOrDefault(kh.getMaKhachHang(), 0L),
                        (a, b) -> a, LinkedHashMap::new));
    }

    /**
     * Câu 14: Khách hàng CHƯA TỪNG đặt đơn hàng nào.
     *
     * SELECT kh.*
     * FROM KhachHang kh
     * LEFT JOIN DonHang dh ON kh.MaKhachHang = dh.MaKhachHang
     * WHERE dh.MaDonHang IS NULL;
     */
    public static List<KhachHang> cau14_khachChuaTungMua(DataContext ctx) {
        Set<String> daMua = ctx.getDonHangs().stream()
                .map(DonHang::getMaKhachHang)
                .collect(Collectors.toSet());

        return ctx.getKhachHangs().stream()
                .filter(kh -> !daMua.contains(kh.getMaKhachHang()))
                .collect(Collectors.toList());
    }

    /**
     * Câu 18: Tổng chi tiêu của TẤT CẢ khách hàng trên các đơn HOÀN THÀNH
     *         (khách chưa có đơn hoàn thành = 0), sắp xếp giảm dần.
     *
     * SELECT kh.MaKhachHang, kh.HoTen,
     *        COALESCE(SUM(ct.SoLuong * ct.DonGia * (1 - ct.TyLeGiamGia)), 0) AS TongChiTieu
     * FROM KhachHang kh
     * LEFT JOIN DonHang dh
     *        ON kh.MaKhachHang = dh.MaKhachHang AND dh.TrangThai = 'HOAN_THANH'
     * LEFT JOIN ChiTietDonHang ct ON dh.MaDonHang = ct.MaDonHang
     * GROUP BY kh.MaKhachHang, kh.HoTen
     * ORDER BY TongChiTieu DESC;
     */
    public static Map<KhachHang, Double> cau18_tongChiTieuMoiKhach(DataContext ctx) {
        // maKhachHang -> tổng tiền các đơn hoàn thành (dùng lại hàm của OrderQueries)
        Map<String, Double> chiTieu = OrderQueries.cau30_doanhThuTheoKhachHang(ctx);

        return ctx.getKhachHangs().stream()
                .sorted((a, b) -> Double.compare(
                        chiTieu.getOrDefault(b.getMaKhachHang(), 0.0),
                        chiTieu.getOrDefault(a.getMaKhachHang(), 0.0)))
                .collect(Collectors.toMap(
                        kh -> kh,
                        kh -> chiTieu.getOrDefault(kh.getMaKhachHang(), 0.0),
                        (a, b) -> a, LinkedHashMap::new));
    }

    // =====================================================================
    // CHẠY THỬ CÁC CÂU CỦA NGƯỜI 3 (gọi từ Main.java)
    // =====================================================================
    public static void chayCauNguoi3(DataContext ctx) {
        System.out.println("=== Câu 13: Số đơn của mỗi khách hàng ===");
        cau13_soDonMoiKhachHang(ctx).forEach((kh, n) ->
                System.out.printf("%s - %s : %d đơn%n", kh.getMaKhachHang(), kh.getHoTen(), n));

        System.out.println("\n=== Câu 14: Khách hàng chưa từng mua ===");
        cau14_khachChuaTungMua(ctx).forEach(kh ->
                System.out.printf("%s - %s%n", kh.getMaKhachHang(), kh.getHoTen()));

        System.out.println("\n=== Câu 18: Tổng chi tiêu (đơn hoàn thành) ===");
        cau18_tongChiTieuMoiKhach(ctx).forEach((kh, tien) ->
                System.out.printf("%s - %s : %,.0f%n", kh.getMaKhachHang(), kh.getHoTen(), tien));
    }
}