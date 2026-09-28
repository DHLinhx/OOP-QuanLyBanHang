import math

class TamGiac:
    def __init__(self, a=0.0, b=0.0, c=0.0, mau_sac=""):
        self.a = a
        self.b = b
        self.c = c
        self.mau_sac = mau_sac

    def la_tam_giac(self):
        return (
            self.a > 0 and self.b > 0 and self.c > 0 and
            self.a + self.b > self.c and
            self.a + self.c > self.b and
            self.b + self.c > self.a
        )

    def chu_vi(self):
        if not self.la_tam_giac():
            return 0
        return self.a + self.b + self.c

    def dien_tich(self):
        if not self.la_tam_giac():
            return 0
        p = self.chu_vi() / 2
        return math.sqrt(p * (p - self.a) * (p - self.b) * (p - self.c))

    def loai_tam_giac(self):
        if not self.la_tam_giac():
            return "Không phải tam giác"

        a, b, c = sorted([self.a, self.b, self.c])
        la_vuong = round(a**2 + b**2, 5) == round(c**2, 5)
        la_can = (self.a == self.b) or (self.b == self.c) or (self.a == self.c)
        la_deu = (self.a == self.b == self.c)

        if la_deu:
            return "đều"
        if la_vuong and la_can:
            return "vuông – cân"
        if la_vuong:
            return "vuông"
        if la_can:
            return "cân"
        return "thường"

    def hien_thi(self):
        if not self.la_tam_giac():
            print("Ba cạnh đã nhập không tạo thành một tam giác!")
            return
        print(f"Ba cạnh: {self.a}, {self.b}, {self.c}")
        print(f"Màu sắc: {self.mau_sac}")
        print(f"Loại tam giác: {self.loai_tam_giac()}")
        print(f"Chu vi: {self.chu_vi():.2f}")
        print(f"Diện tích: {self.dien_tich():.2f}")

if __name__ == "__main__":
    a = float(input("Nhập cạnh a: "))
    b = float(input("Nhập cạnh b: "))
    c = float(input("Nhập cạnh c: "))
    mau = input("Nhập màu sắc: ")

    tg = TamGiac(a, b, c, mau)
    tg.hien_thi()
