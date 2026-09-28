import os

class MonHoc:
    def __init__(self, ma_mon="", ten_mon="", so_tiet=0):
        self.ma_mon = ma_mon
        self.ten_mon = ten_mon
        self.so_tiet = so_tiet

    def __str__(self):
        return f"{self.ma_mon} - {self.ten_mon} ({self.so_tiet} tiết)"

class HocVien:
    def __init__(self, id_card="", ho_ten="", nam_sinh=0, ds_mon_hoc=None):
        self.id_card = id_card
        self.ho_ten = ho_ten
        self.nam_sinh = nam_sinh
        self.ds_mon_hoc = ds_mon_hoc if ds_mon_hoc is not None else []

    def them_mon_hoc(self, mon):
        self.ds_mon_hoc.append(mon)

    def hien_thi(self):
        print(f"CMND/CCCD/Khai sinh: {self.id_card}")
        print(f"Họ tên: {self.ho_ten}")
        print(f"Năm sinh: {self.nam_sinh}")
        print("Môn học đăng ký:")
        if not self.ds_mon_hoc:
            print("  (Chưa đăng ký môn nào)")
        else:
            for mon in self.ds_mon_hoc:
                print(f"  + {mon}")

class TrungTamAI:
    def __init__(self):
        self.ds_hoc_vien = []

    def nhap_hoc_vien(self):
        print("\n--- NHẬP THÔNG TIN HỌC VIÊN ---")
        id_card = input("Nhập số CMND/CCCD/Khai sinh: ").strip()
        ho_ten = input("Nhập họ tên: ").strip()
        nam_sinh = int(input("Nhập năm sinh: ").strip())

        so_mon = int(input("Nhập số lượng môn học đăng ký: ").strip())
        ds_mon = []
        for i in range(so_mon):
            print(f"Nhập thông tin môn thứ {i + 1}:")
            ma = input("  Mã môn: ").strip()
            ten = input("  Tên môn: ").strip()
            tiet = int(input("  Số tiết: ").strip())
            ds_mon.append(MonHoc(ma, ten, tiet))

        hv = HocVien(id_card, ho_ten, nam_sinh, ds_mon)
        self.ds_hoc_vien.append(hv)
        self.luu_file()
        print("Đã thêm học viên và lưu vào file dssv.txt thành công!")

    def luu_file(self, filename="dssv.txt"):
        with open(filename, "w", encoding="utf-8") as f:
            for hv in self.ds_hoc_vien:
                f.write(f"{hv.id_card}|{hv.ho_ten}|{hv.nam_sinh}\n")
                f.write(f"{len(hv.ds_mon_hoc)}\n")
                for mon in hv.ds_mon_hoc:
                    f.write(f"{mon.ma_mon}|{mon.ten_mon}|{mon.so_tiet}\n")

    def doc_file(self, filename="dssv.txt"):
        if not os.path.exists(filename):
            return
        self.ds_hoc_vien = []
        with open(filename, "r", encoding="utf-8") as f:
            while True:
                line = f.readline()
                if not line:
                    break
                parts = line.strip().split("|")
                if len(parts) < 3:
                    continue
                id_card, ho_ten, nam_sinh = parts[0], parts[1], int(parts[2])
                so_mon = int(f.readline().strip())
                ds_mon = []
                for _ in range(so_mon):
                    mon_line = f.readline().strip().split("|")
                    ds_mon.append(MonHoc(mon_line[0], mon_line[1], int(mon_line[2])))
                self.ds_hoc_vien.append(HocVien(id_card, ho_ten, nam_sinh, ds_mon))

    def hien_thi_tat_ca(self):
        print("\n--- DANH SÁCH TẤT CẢ HỌC VIÊN ---")
        if not self.ds_hoc_vien:
            print("Danh sách hiện đang trống.")
            return
        for i, hv in enumerate(self.ds_hoc_vien, 1):
            print(f"\n[Học viên {i}]")
            hv.hien_thi()

    def hien_thi_it_nhat_hai_mon(self):
        print("\n--- DANH SÁCH HỌC VIÊN ĐĂNG KÝ TỪ 2 MÔN TRỞ LÊN ---")
        loc_hv = [hv for hv in self.ds_hoc_vien if len(hv.ds_mon_hoc) >= 2]
        if not loc_hv:
            print("Không có học viên nào đăng ký từ 2 môn trở lên.")
            return
        for i, hv in enumerate(loc_hv, 1):
            print(f"\n[Học viên {i}]")
            hv.hien_thi()

    def mon_hoc_nhieu_dang_ky_nhat(self):
        print("\n--- MÔN HỌC ĐƯỢC NHIỀU HỌC VIÊN ĐĂNG KÝ NHẤT ---")
        dem = {}
        info_mon = {}
        for hv in self.ds_hoc_vien:
            for mon in hv.ds_mon_hoc:
                dem[mon.ma_mon] = dem.get(mon.ma_mon, 0) + 1
                info_mon[mon.ma_mon] = mon.ten_mon

        if not dem:
            print("Chưa có môn học nào được đăng ký.")
            return

        max_count = max(dem.values())
        print(f"Số lượt đăng ký nhiều nhất: {max_count}")
        for ma, count in dem.items():
            if count == max_count:
                print(f"- Mã môn: {ma}, Tên môn: {info_mon[ma]}")

    def thong_ke_mon_hoc(self):
        print("\n--- THỐNG KÊ SỐ LƯỢNG HỌC VIÊN THEO TỪNG MÔN ---")
        dem = {}
        info_mon = {}
        for hv in self.ds_hoc_vien:
            for mon in hv.ds_mon_hoc:
                dem[mon.ma_mon] = dem.get(mon.ma_mon, 0) + 1
                info_mon[mon.ma_mon] = mon.ten_mon

        if not dem:
            print("Chưa có dữ liệu môn học.")
            return

        for ma, count in dem.items():
            print(f"Mã: {ma} | Tên: {info_mon[ma]} | Số học viên đăng ký: {count}")

def main():
    tt = TrungTamAI()
    tt.doc_file()

    while True:
        print("\n====== QUẢN LÝ HỌC VIÊN TRUNG TÂM AI ======")
        print("1. Nhập học viên và lưu vào file dssv.txt")
        print("2. Hiển thị tất cả học viên đã đăng ký")
        print("3. Hiển thị học viên đăng ký ít nhất 2 môn")
        print("4. Hiển thị môn học có nhiều học viên đăng ký nhất")
        print("5. Thống kê số lượng học viên theo từng môn học")
        print("0. Thoát chương trình")
        chon = input("Chọn chức năng: ").strip()

        if chon == "1":
            tt.nhap_hoc_vien()
        elif chon == "2":
            tt.hien_thi_tat_ca()
        elif chon == "3":
            tt.hien_thi_it_nhat_hai_mon()
        elif chon == "4":
            tt.mon_hoc_nhieu_dang_ky_nhat()
        elif chon == "5":
            tt.thong_ke_mon_hoc()
        elif chon == "0":
            break
        else:
            print("Lựa chọn không hợp lệ, vui lòng chọn lại!")

if __name__ == "__main__":
    main()
