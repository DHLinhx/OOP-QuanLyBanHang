class GiaoDich:
    def __init__(self, ma_gd="", ngay_gd="", don_gia=0.0, so_luong=0):
        self.ma_gd = ma_gd
        self.ngay_gd = ngay_gd
        self.don_gia = don_gia
        self.so_luong = so_luong

    def thanh_tien(self):
        return self.so_luong * self.don_gia

    def xuat(self):
        print(f"Mã GD: {self.ma_gd} | Ngày: {self.ngay_gd} | Đơn giá: {self.don_gia:,.0f} | Số lượng: {self.so_luong}", end="")

class GiaoDichVang(GiaoDich):
    def __init__(self, ma_gd="", ngay_gd="", don_gia=0.0, so_luong=0, loai_vang="9999"):
        super().__init__(ma_gd, ngay_gd, don_gia, so_luong)
        self.loai_vang = loai_vang

    def thanh_tien(self):
        return self.so_luong * self.don_gia

    def xuat(self):
        super().xuat()
        print(f" | Loại vàng: {self.loai_vang} | Thành tiền: {self.thanh_tien():,.0f}")

class GiaoDichTienTe(GiaoDich):
    def __init__(self, ma_gd="", ngay_gd="", ti_gia=0.0, so_luong=0, loai_tien="USD", loai_gd="mua"):
        super().__init__(ma_gd, ngay_gd, ti_gia, so_luong)
        self.loai_tien = loai_tien
        self.loai_gd = loai_gd.lower()

    def thanh_tien(self):
        if self.loai_gd == "mua":
            return self.so_luong * self.don_gia
        return (self.so_luong * self.don_gia) * 1.05

    def loi_nhuan(self):
        if self.loai_gd == "bán":
            return (self.so_luong * self.don_gia) * 0.05
        return 0.0

    def xuat(self):
        super().xuat()
        print(f" | Ngoại tệ: {self.loai_tien} | Loại GD: {self.loai_gd} | Thành tiền: {self.thanh_tien():,.0f}")

class QuanLyGiaoDich:
    def __init__(self):
        self.ds_giao_dich = []

    def them_giao_dich(self, gd):
        self.ds_giao_dich.append(gd)

    def nhap_danh_sach(self):
        n = int(input("Nhập số lượng giao dịch cần thêm: "))
        for i in range(n):
            print(f"\n--- Nhập giao dịch thứ {i + 1} ---")
            loai = input("Chọn loại giao dịch (1: Vàng, 2: Tiền tệ): ").strip()
            ma = input("Nhập mã giao dịch: ").strip()
            ngay = input("Nhập ngày giao dịch (dd/mm/yyyy): ").strip()
            sl = int(input("Nhập số lượng: "))

            if loai == "1":
                dg = float(input("Nhập đơn giá: "))
                loai_vang = input("Nhập loại vàng (18k/24k/9999): ").strip()
                self.them_giao_dich(GiaoDichVang(ma, ngay, dg, sl, loai_vang))
            else:
                ti_gia = float(input("Nhập tỷ giá (đơn giá): "))
                loai_tien = input("Nhập loại tiền (USD/EUR/AUD): ").strip().upper()
                loai_gd = input("Nhập loại GD (mua/bán): ").strip()
                self.them_giao_dich(GiaoDichTienTe(ma, ngay, ti_gia, sl, loai_tien, loai_gd))

    def xuat_danh_sach(self):
        print("\n========== DANH SÁCH TẤT CẢ GIAO DỊCH ==========")
        if not self.ds_giao_dich:
            print("Danh sách giao dịch đang rỗng.")
            return
        for gd in self.ds_giao_dich:
            gd.xuat()

    def tong_so_luong_tung_loai(self):
        sl_vang = sum(gd.so_luong for gd in self.ds_giao_dich if isinstance(gd, GiaoDichVang))
        sl_tiente = sum(gd.so_luong for gd in self.ds_giao_dich if isinstance(gd, GiaoDichTienTe))
        print("\n--- TỔNG SỐ LƯỢNG TỪNG LOẠI ---")
        print(f"Tổng số lượng giao dịch Vàng: {sl_vang}")
        print(f"Tổng số lượng giao dịch Tiền tệ: {sl_tiente}")

    def tong_thanh_tien_tung_loai(self):
        tien_vang = sum(gd.thanh_tien() for gd in self.ds_giao_dich if isinstance(gd, GiaoDichVang))
        tien_tiente = sum(gd.thanh_tien() for gd in self.ds_giao_dich if isinstance(gd, GiaoDichTienTe))
        print("\n--- TỔNG THÀNH TIỀN TỪNG LOẠI ---")
        print(f"Tổng thành tiền giao dịch Vàng: {tien_vang:,.0f} VNĐ")
        print(f"Tổng thành tiền giao dịch Tiền tệ: {tien_tiente:,.0f} VNĐ")

    def tinh_loi_nhuan_theo_ngay(self, ngay):
        loi_nhuan = sum(
            gd.loi_nhuan()
            for gd in self.ds_giao_dich
            if isinstance(gd, GiaoDichTienTe) and gd.ngay_gd == ngay
        )
        print(f"Lợi nhuận giao dịch tiền tệ ngày {ngay}: {loi_nhuan:,.0f} VNĐ")

def main():
    ql = QuanLyGiaoDich()

    ql.them_giao_dich(GiaoDichVang("V01", "20/10/2024", 8500000, 2, "9999"))
    ql.them_giao_dich(GiaoDichVang("V02", "20/10/2024", 5500000, 3, "18k"))
    ql.them_giao_dich(GiaoDichTienTe("TT01", "20/10/2024", 25000, 1000, "USD", "mua"))
    ql.them_giao_dich(GiaoDichTienTe("TT02", "20/10/2024", 25000, 500, "USD", "bán"))

    while True:
        print("\n====== QUẢN LÝ GIAO DỊCH ======")
        print("1. Thêm giao dịch")
        print("2. Xuất danh sách giao dịch")
        print("3. Tổng số lượng từng loại")
        print("4. Tổng thành tiền từng loại")
        print("5. Tính lợi nhuận theo ngày")
        print("0. Thoát")
        chon = input("Chọn chức năng: ").strip()

        if chon == "1":
            ql.nhap_danh_sach()
        elif chon == "2":
            ql.xuat_danh_sach()
        elif chon == "3":
            ql.tong_so_luong_tung_loai()
        elif chon == "4":
            ql.tong_thanh_tien_tung_loai()
        elif chon == "5":
            ngay = input("Nhập ngày cần tính (dd/mm/yyyy): ").strip()
            ql.tinh_loi_nhuan_theo_ngay(ngay)
        elif chon == "0":
            break
        else:
            print("Lựa chọn không hợp lệ!")

if __name__ == "__main__":
    main()
