import math

class Diem:
    def __init__(self, x=0, y=0, mau_sac="Đen"):
        self.x = x
        self.y = y
        self.mau_sac = mau_sac

    def HienThi(self):
        print(f"Tọa độ: ({self.x}, {self.y}), Màu sắc: {self.mau_sac}")

    def TinhTien(self, *args):
        if len(args) == 1:
            self.x += args[0]
        elif len(args) >= 2:
            self.x += args[0]
            self.y += args[1]

    def KhoangCach(self, d=None):
        if d is None:
            return math.sqrt(self.x**2 + self.y**2)
        return math.sqrt((self.x - d.x)**2 + (self.y - d.y)**2)

if __name__ == "__main__":
    d1 = Diem(3, 4, "Đỏ")
    d2 = Diem(6, 8, "Xanh")

    print("Điểm 1 ban đầu:")
    d1.HienThi()

    print(f"Khoảng cách từ d1 tới gốc tọa độ: {d1.KhoangCach():.2f}")
    print(f"Khoảng cách từ d1 tới d2: {d1.KhoangCach(d2):.2f}")

    d1.TinhTien(2)
    print("Điểm 1 sau khi tịnh tiến Ox thêm 2:")
    d1.HienThi()

    d1.TinhTien(1, -3)
    print("Điểm 1 sau khi tịnh tiến (Ox: 1, Oy: -3):")
    d1.HienThi()
