class ToanHoc:
    def __init__(self, *nso):
        self.ds_so = list(nso)

    def _lay_du_lieu(self, nso):
        if len(nso) > 0:
            return list(nso)
        return self.ds_so

    def TinhTong(self, *nso):
        nums = self._lay_du_lieu(nso)
        if not nums:
            return 0
        return sum(nums)

    def TinhTrungBinh(self, *nso):
        nums = self._lay_du_lieu(nso)
        if not nums:
            return 0
        return sum(nums) / len(nums)

    def TimMax(self, *nso):
        nums = self._lay_du_lieu(nso)
        if not nums:
            return None
        return max(nums)

    def TimMin(self, *nso):
        nums = self._lay_du_lieu(nso)
        if not nums:
            return None
        return min(nums)

    def HienThi(self):
        print("Danh sách các số:", self.ds_so)

if __name__ == "__main__":
    day_so = list(map(float, input("Nhập các số (cách nhau bởi dấu cách): ").split()))
    th = ToanHoc(*day_so)
    th.HienThi()
    print("Tổng:", th.TinhTong())
    print("Trung bình cộng:", th.TinhTrungBinh())
    print("Số lớn nhất:", th.TimMax())
    print("Số nhỏ nhất:", th.TimMin())
