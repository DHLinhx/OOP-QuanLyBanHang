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

public class PersonQueries {

    public static Map<KhachHang, Double> cau13_doanhThuTungKhach(DataContext ctx) {
        Map<String, Double> doanhThu = OrderQueries.doanhThuTheoMaKhach(ctx);
        return ctx.getKhachHangs().stream()
                .sorted((a, b) -> Double.compare(
                        doanhThu.getOrDefault(b.getMaKhachHang(), 0.0),
                        doanhThu.getOrDefault(a.getMaKhachHang(), 0.0)))
                .collect(Collectors.toMap(
                        kh -> kh,
                        kh -> doanhThu.getOrDefault(kh.getMaKhachHang(), 0.0),
                        (a, b) -> a, LinkedHashMap::new));
    }

    public static List<KhachHang> cau14_khachChuaTungDatHang(DataContext ctx) {
        Set<String> daDat = ctx.getDonHangs().stream()
                .map(DonHang::getMaKhachHang)
                .collect(Collectors.toSet());

        return ctx.getKhachHangs().stream()
                .filter(kh -> !daDat.contains(kh.getMaKhachHang()))
                .collect(Collectors.toList());
    }
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

    public static void chayCauNguoi3(DataContext ctx) {
        System.out.println(" Câu 13: Doanh thu từng khách (gồm khách = 0)");
        cau13_doanhThuTungKhach(ctx).forEach((kh, tien) ->
                System.out.printf("%s - %s : %,.0f%n", kh.getMaKhachHang(), kh.getHoTen(), tien));

        System.out.println("\nCâu 14: Khách chưa từng đặt hàng ");
        cau14_khachChuaTungDatHang(ctx).forEach(kh ->
                System.out.printf("%s - %s%n", kh.getMaKhachHang(), kh.getHoTen()));

        System.out.println("\n Câu 18: LEFT JOIN khách hàng với đơn hàng ");
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