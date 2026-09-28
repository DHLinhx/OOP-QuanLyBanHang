from abc import ABC, abstractmethod
import math

class HinhHoc(ABC):
    @abstractmethod
    def tinh_chu_vi(self):
        pass

    @abstractmethod
    def tinh_dien_tich(self):
        pass

class HinhTron(HinhHoc):
    def __init__(self, ban_kinh=0.0):
        self.ban_kinh = ban_kinh

    def tinh_chu_vi(self):
        return 2 * math.pi * self.ban_kinh

    def tinh_dien_tich(self):
        return math.pi * (self.ban_kinh ** 2)

class HinhChuNhat(HinhHoc):
    def __init__(self, dai=0.0, rong=0.0):
        self.dai = dai
        self.rong = rong

    def tinh_chu_vi(self):
        return (self.dai + self.rong) * 2

    def tinh_dien_tich(self):
        return self.dai * self.rong

class HinhTamGiac(HinhHoc):
    def __init__(self, a=0.0, b=0.0, c=0.0):
        self.a = a
        self.b = b
        self.c = c

    def tinh_chu_vi(self):
        return self.a + self.b + self.c

    def tinh_dien_tich(self):
        p = self.tinh_chu_vi() / 2
        return math.sqrt(p * (p - self.a) * (p - self.b) * (p - self.c))

if __name__ == "__main__":
    danh_sach_hinh = [
        HinhTron(5),
        HinhChuNhat(4, 7),
        HinhTamGiac(3, 4, 5)
    ]

    for hinh in danh_sach_hinh:
        ten_lop = type(hinh).__name__
        print(f"--- {ten_lop} ---")
        print(f"Chu vi: {hinh.tinh_chu_vi():.2f}")
        print(f"Diện tích: {hinh.tinh_dien_tich():.2f}")
