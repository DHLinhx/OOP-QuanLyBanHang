package models;

/**
 * Enum biểu diễn các trạng thái của một đơn hàng.
 * Tương ứng ENUM('DangXuLy', 'HoanThanh', 'DaHuy') trong MySQL.
 */
public enum TrangThaiDonHang {
    DANG_XU_LY("DangXuLy", "Đang xử lý"),
    HOAN_THANH("HoanThanh", "Hoàn thành"),
    DA_HUY("DaHuy", "Đã hủy");

    private final String maTrangThai;
    private final String moTa;

    TrangThaiDonHang(String maTrangThai, String moTa) {
        this.maTrangThai = maTrangThai;
        this.moTa = moTa;
    }

    public String getMaTrangThai() {
        return maTrangThai;
    }

    public String getMoTa() {
        return moTa;
    }

    @Override
    public String toString() {
        return maTrangThai;
    }
}
