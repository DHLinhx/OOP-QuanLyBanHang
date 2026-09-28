import math

class HinhTron:
    def __init__(self, ban_kinh=0.0):
        self.ban_kinh = ban_kinh

    def chu_vi(self):
        return 2 * math.pi * self.ban_kinh

    def dien_tich(self):
        return math.pi * (self.ban_kinh ** 2)

    def hien_thi(self):
        print(f"Bán kính: {self.ban_kinh}")
        print(f"Chu vi: {self.chu_vi():.2f}")
        print(f"Diện tích: {self.dien_tich():.2f}")

if __name__ == "__main__":
    r = float(input("Nhập bán kính hình tròn: "))
    ht = HinhTron(r)
    ht.hien_thi()
