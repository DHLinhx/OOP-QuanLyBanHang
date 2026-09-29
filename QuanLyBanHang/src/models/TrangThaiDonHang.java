package models;

public enum TrangThaiDonHang {
    DANG_XU_LY("DangXuLy", "Đang xử lý"),
    HOAN_THANH("HoanThanh", "Hoàn thành"),
    DA_HUY("DaHuy", "Đã hủy");

    private final String dbValue;
    private final String moTa;

    TrangThaiDonHang(String dbValue, String moTa) {
        this.dbValue = dbValue;
        this.moTa = moTa;
    }

    public String getDbValue() {
        return dbValue;
    }

    public String getMoTa() {
        return moTa;
    }

    /**
     * Chuyển giá trị chuỗi từ Database ("DangXuLy", "HoanThanh", "DaHuy") sang Enum
     */
    public static TrangThaiDonHang fromString(String value) {
        if (value != null) {
            for (TrangThaiDonHang status : TrangThaiDonHang.values()) {
                if (status.dbValue.equalsIgnoreCase(value.trim()) 
                        || status.name().equalsIgnoreCase(value.trim())) {
                    return status;
                }
            }
        }
        throw new IllegalArgumentException("Không tìm thấy trạng thái phù hợp cho: " + value);
    }

    @Override
    public String toString() {
        return moTa;
    }
}
