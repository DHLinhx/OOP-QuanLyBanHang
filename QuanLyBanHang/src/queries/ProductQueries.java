package queries;

import database.DataContext;
import models.ChiTietDonHang;
import models.DonHang;
import models.SanPham;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ProductQueries {

    static final String DANH_MUC_LAPTOP = "Laptop";
    private static List<ChiTietDonHang> dongBanThanhCong(DataContext ctx) {
        Set<String> maDon = ctx.getDonHangs().stream()
                .filter(OrderQueries.TINH_DOANH_THU)
                .map(DonHang::getMaDonHang)
                .collect(Collectors.toSet());
        return ctx.getChiTietDonHangs().stream()
                .filter(ct -> maDon.contains(ct.getMaDonHang()))
                .collect(Collectors.toList());
    }

    private static Map<String, Integer> soLuongBanTheoMaSP(DataContext ctx) {
        return dongBanThanhCong(ctx).stream()
                .collect(Collectors.groupingBy(
                        ChiTietDonHang::getMaSanPham,
                        Collectors.summingInt(ChiTietDonHang::getSoLuong)));
    }

    private static Map<String, Double> doanhThuTheoMaSP(DataContext ctx) {
        return dongBanThanhCong(ctx).stream()
                .collect(Collectors.groupingBy(
                        ChiTietDonHang::getMaSanPham,
                        Collectors.summingDouble(ChiTietDonHang::getThanhTien)));
    }

  
    private static String moTa(SanPham sp) {
        return String.format("%s | %s | %s | Giá: %,.0f | Tồn: %d",
                sp.getMaSanPham(), sp.getTenSanPham(), sp.getDanhMuc(),
                sp.getGiaBan(), sp.getTonKho());
    }

    public static class ThongKeDanhMuc {
        public final String danhMuc;
        public final int soLuongBan;
        public final double doanhThu;

        public ThongKeDanhMuc(String danhMuc, int soLuongBan, double doanhThu) {
            this.danhMuc = danhMuc;
            this.soLuongBan = soLuongBan;
            this.doanhThu = doanhThu;
        }

        @Override
        public String toString() {
            return String.format("%-15s | Số lượng bán: %4d | Doanh thu: %,.0f",
                    danhMuc, soLuongBan, doanhThu);
        }
    }

    public static class SanPhamKemGiaTB {
        public final SanPham sanPham;
        public final double giaTBDanhMuc;

        public SanPhamKemGiaTB(SanPham sanPham, double giaTBDanhMuc) {
            this.sanPham = sanPham;
            this.giaTBDanhMuc = giaTBDanhMuc;
        }

        @Override
        public String toString() {
            return String.format("%s | TB danh mục: %,.0f", moTa(sanPham), giaTBDanhMuc);
        }
    }

    public static class SanPhamDoanhThu {
        public final SanPham sanPham;
        public final double doanhThu;

        public SanPhamDoanhThu(SanPham sanPham, double doanhThu) {
            this.sanPham = sanPham;
            this.doanhThu = doanhThu;
        }

        @Override
        public String toString() {
            return String.format("%s | Doanh thu: %,.0f", moTa(sanPham), doanhThu);
        }
    }

    public static List<SanPham> cau1_lietKeSanPham(DataContext ctx) {
        return new ArrayList<>(ctx.getSanPhams());
    }
    public static List<SanPham> cau2_locLaptopTheoGia(DataContext ctx, double giaMin, double giaMax) {
        return ctx.getSanPhams().stream()
                .filter(sp -> DANH_MUC_LAPTOP.equalsIgnoreCase(sp.getDanhMuc()))
                .filter(sp -> sp.getGiaBan() >= giaMin && sp.getGiaBan() <= giaMax)
                .sorted(Comparator.comparingDouble(SanPham::getGiaBan).reversed())
                .collect(Collectors.toList());
    }

    public static List<SanPham> cau3_tonKhoDuoi10(DataContext ctx) {
        return ctx.getSanPhams().stream()
                .filter(sp -> sp.getTonKho() < 10)
                .sorted(Comparator.comparingInt(SanPham::getTonKho))
                .collect(Collectors.toList());
    }

    public static List<String> cau5_danhMucKhongTrung(DataContext ctx) {
        return ctx.getSanPhams().stream()
                .map(SanPham::getDanhMuc)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public static Map<SanPham, Integer> cau12_top3BanChay(DataContext ctx) {
        Map<String, Integer> sl = soLuongBanTheoMaSP(ctx);
        return ctx.getSanPhams().stream()
                .filter(sp -> sl.containsKey(sp.getMaSanPham()))
                .sorted(Comparator.comparingInt((SanPham sp) -> sl.get(sp.getMaSanPham()))
                        .reversed()
                        .thenComparing(SanPham::getMaSanPham))
                .limit(3)
                .collect(Collectors.toMap(
                        sp -> sp, sp -> sl.get(sp.getMaSanPham()),
                        (a, b) -> a, LinkedHashMap::new));
    }


    public static List<SanPham> cau15_chuaBanThanhCong(DataContext ctx) {
        Set<String> daBan = soLuongBanTheoMaSP(ctx).keySet();
        return ctx.getSanPhams().stream()
                .filter(sp -> !daBan.contains(sp.getMaSanPham()))
                .collect(Collectors.toList());
    }
    public static List<SanPham> cau19_phanTrang(DataContext ctx, int trang, int kichThuocTrang) {
        if (trang < 1 || kichThuocTrang < 1) {
            throw new IllegalArgumentException("trang và kichThuocTrang phải >= 1");
        }
        return ctx.getSanPhams().stream()
                .sorted(Comparator.comparing(SanPham::getMaSanPham))
                .skip((long) (trang - 1) * kichThuocTrang)
                .limit(kichThuocTrang)
                .collect(Collectors.toList());
    }


    public static int cau19_tongSoTrang(DataContext ctx, int kichThuocTrang) {
        return (int) Math.ceil(ctx.getSanPhams().size() / (double) kichThuocTrang);
    }
    public static List<ThongKeDanhMuc> cau22_thongKeTheoDanhMuc(DataContext ctx) {
        Map<String, SanPham> spTheoMa = ctx.getSanPhams().stream()
                .collect(Collectors.toMap(SanPham::getMaSanPham, sp -> sp, (a, b) -> a));

        Map<String, Integer> slTheoDM = new HashMap<>();
        Map<String, Double> dtTheoDM = new HashMap<>();
        for (ChiTietDonHang ct : dongBanThanhCong(ctx)) {
            SanPham sp = spTheoMa.get(ct.getMaSanPham());
            if (sp == null) {
                continue;
            }
            slTheoDM.merge(sp.getDanhMuc(), ct.getSoLuong(), Integer::sum);
            dtTheoDM.merge(sp.getDanhMuc(), ct.getThanhTien(), Double::sum);
        }

        List<ThongKeDanhMuc> kq = new ArrayList<>();
        for (String dm : cau5_danhMucKhongTrung(ctx)) {
            kq.add(new ThongKeDanhMuc(dm, slTheoDM.getOrDefault(dm, 0), dtTheoDM.getOrDefault(dm, 0.0)));
        }
        kq.sort((a, b) -> Double.compare(b.doanhThu, a.doanhThu));
        return kq;
    }

    public static List<SanPhamKemGiaTB> cau24_giaCaoHonTrungBinhDanhMuc(DataContext ctx) {
        Map<String, Double> giaTB = ctx.getSanPhams().stream()
                .filter(sp -> sp.getDanhMuc() != null)
                .collect(Collectors.groupingBy(
                        SanPham::getDanhMuc,
                        Collectors.averagingDouble(SanPham::getGiaBan)));

        List<SanPhamKemGiaTB> kq = new ArrayList<>();
        for (SanPham sp : ctx.getSanPhams()) {
            Double tb = giaTB.get(sp.getDanhMuc());
            if (tb != null && sp.getGiaBan() > tb) {
                kq.add(new SanPhamKemGiaTB(sp, tb));
            }
        }
        kq.sort(Comparator
                .comparing((SanPhamKemGiaTB x) -> x.sanPham.getDanhMuc())
                .thenComparing((SanPhamKemGiaTB x) -> x.sanPham.getGiaBan(), Comparator.reverseOrder()));
        return kq;
    }

    public static Map<String, List<SanPhamDoanhThu>> cau28_top2DoanhThuMoiDanhMuc(DataContext ctx) {
        Map<String, Double> dtTheoSP = doanhThuTheoMaSP(ctx);

        Map<String, List<SanPhamDoanhThu>> nhom = ctx.getSanPhams().stream()
                .filter(sp -> sp.getDanhMuc() != null && dtTheoSP.containsKey(sp.getMaSanPham()))
                .map(sp -> new SanPhamDoanhThu(sp, dtTheoSP.get(sp.getMaSanPham())))
                .collect(Collectors.groupingBy(x -> x.sanPham.getDanhMuc(), TreeMap::new, Collectors.toList()));

        Map<String, List<SanPhamDoanhThu>> kq = new TreeMap<>();
        for (Map.Entry<String, List<SanPhamDoanhThu>> e : nhom.entrySet()) {
            List<SanPhamDoanhThu> top2 = e.getValue().stream()
                    .sorted((a, b) -> Double.compare(b.doanhThu, a.doanhThu))
                    .limit(2)
                    .collect(Collectors.toList());
            kq.put(e.getKey(), top2);
        }
        return kq;
    }

    public static void chayTatCa(DataContext ctx) {
        System.out.println("Câu 1: Liệt kê sản phẩm ");
        cau1_lietKeSanPham(ctx).forEach(sp -> System.out.println(moTa(sp)));

        // Khoảng giá ví dụ, hãy đổi theo đề / dữ liệu thật
        double giaMin = 10_000_000, giaMax = 30_000_000;
        System.out.printf("%nCâu 2: Laptop giá từ %,.0f đến %,.0f (giảm dần) %n", giaMin, giaMax);
        cau2_locLaptopTheoGia(ctx, giaMin, giaMax).forEach(sp -> System.out.println(moTa(sp)));

        System.out.println("\n Câu 3: Sản phẩm tồn kho dưới 10 ");
        cau3_tonKhoDuoi10(ctx).forEach(sp -> System.out.println(moTa(sp)));

        System.out.println("\n Câu 5: Danh mục không trùng lặp ");
        cau5_danhMucKhongTrung(ctx).forEach(System.out::println);

        System.out.println("\nCâu 12: Top 3 sản phẩm bán nhiều nhất ");
        cau12_top3BanChay(ctx).forEach((sp, sl) ->
                System.out.printf("%s | Đã bán: %d%n", moTa(sp), sl));

        System.out.println("\nCâu 15: Sản phẩm chưa bán thành công");
        cau15_chuaBanThanhCong(ctx).forEach(sp -> System.out.println(moTa(sp)));

        int kichThuoc = 5;
        System.out.printf("%n Câu 19: Phân trang (mỗi trang %d, tổng %d trang) %n",
                kichThuoc, cau19_tongSoTrang(ctx, kichThuoc));
        for (int trang = 1; trang <= cau19_tongSoTrang(ctx, kichThuoc); trang++) {
            System.out.println("--- Trang " + trang + " ---");
            cau19_phanTrang(ctx, trang, kichThuoc).forEach(sp -> System.out.println(moTa(sp)));
        }

        System.out.println("\n Câu 22: Thống kê theo danh mục ");
        cau22_thongKeTheoDanhMuc(ctx).forEach(System.out::println);

        System.out.println("\n Câu 24: Sản phẩm giá cao hơn TB danh mục ");
        cau24_giaCaoHonTrungBinhDanhMuc(ctx).forEach(System.out::println);

        System.out.println("\n Câu 28: Top 2 doanh thu mỗi danh mục ");
        cau28_top2DoanhThuMoiDanhMuc(ctx).forEach((dm, ds) -> {
            System.out.println("[" + dm + "]");
            ds.forEach(x -> System.out.println("  " + x));
        });
    }
}