import database.DataContext;
import queries.OrderQueries;
import queries.PersonQueries;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        DataContext ctx = new DataContext();
        Scanner scanner = new Scanner(System.in);

        System.out.println("==========================================================");
        System.out.println("        CHƯƠNG TRÌNH OOP QUẢN LÝ BÁN HÀNG - LINQ/STREAM    ");
        System.out.println("==========================================================");
        System.out.println("1. Chạy tất cả câu của NGƯỜI 2 (Câu 4, 13, 14, 16, 18, 25, 26, 27)");
        System.out.println("2. Chạy tất cả câu của NGƯỜI 3 (Câu 8, 9, 10, 11, 17, 20, 21, 23, 29, 30)");
        System.out.println("3. Chạy từng câu lẻ của Người 2 (Nhập số câu: 4, 13, 14, 16, 18, 25, 26, 27)");
        System.out.println("0. Thoát");
        System.out.println("----------------------------------------------------------");
        System.out.print("Chọn chức năng (mặc định chạy 1 nếu nhấn Enter): ");

        String chon = scanner.nextLine().trim();
        if (chon.isEmpty() || chon.equals("1")) {
            PersonQueries.chayTatCaCauNguoi2(ctx);
        } else if (chon.equals("2")) {
            OrderQueries.chayTatCa(ctx);
        } else if (chon.equals("3")) {
            System.out.print("Nhập số câu muốn chạy (4, 13, 14, 16, 18, 25, 26, 27): ");
            String cau = scanner.nextLine().trim();
            switch (cau) {
                case "4":
                    System.out.println("--- [Câu 4] Khách hàng ở TP.HCM ---");
                    PersonQueries.cau04_timKhachHangTPHCM(ctx).forEach(System.out::println);
                    break;
                case "13":
                    System.out.println("--- [Câu 13] Doanh thu từng khách hàng ---");
                    PersonQueries.cau13_doanhThuTungKhach(ctx).forEach((k, v) ->
                            System.out.printf("%s -> %,.0f VND%n", k, v));
                    break;
                case "14":
                    System.out.println("--- [Câu 14] Khách hàng chưa từng đặt hàng ---");
                    PersonQueries.cau14_khachChuaTungDatHang(ctx).forEach(System.out::println);
                    break;
                case "16":
                    System.out.println("--- [Câu 16] Thống kê nhân viên ---");
                    PersonQueries.cau16_thongKeNhanVien(ctx).forEach(System.out::println);
                    break;
                case "18":
                    System.out.println("--- [Câu 18] LEFT JOIN khách hàng với đơn hàng ---");
                    PersonQueries.cau18_leftJoinKhachDon(ctx).forEach((k, ds) -> {
                        System.out.println(k.getHoTen() + ": " + (ds.isEmpty() ? "[Chưa có đơn]" : ds.size() + " đơn"));
                        ds.forEach(d -> System.out.println("   + " + d));
                    });
                    break;
                case "25":
                    System.out.println("--- [Câu 25] Số khách theo tỉnh/thành phố ---");
                    PersonQueries.cau25_soKhachTheoThanhPho(ctx).forEach((tp, sl) ->
                            System.out.printf("%-15s : %d khách%n", tp, sl));
                    break;
                case "26":
                    System.out.println("--- [Câu 26] Các cặp khách hàng cùng tỉnh/thành phố ---");
                    PersonQueries.cau26_capKhachCungThanhPho(ctx).forEach(System.out::println);
                    break;
                case "27":
                    System.out.println("--- [Câu 27] Nhân viên chưa lập đơn nào ---");
                    PersonQueries.cau27_nhanVienChuaLapDonNao(ctx).forEach(System.out::println);
                    System.out.println("--- [Câu 27] Nhân viên không có đơn hoàn thành ---");
                    PersonQueries.cau27_nhanVienKhongCoDonHoanThanh(ctx).forEach(System.out::println);
                    break;
                default:
                    System.out.println("Câu hỏi không hợp lệ!");
            }
        } else {
            System.out.println("Tạm biệt!");
        }
    }
}
