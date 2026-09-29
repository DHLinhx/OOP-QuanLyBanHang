package models;

import java.util.Objects;

/**
 * Lớp NhanVien đại diện cho bảng NhanVien trong cơ sở dữ liệu.
 * Nhiệm vụ của: Người 2 (Khách hàng & Nhân viên).
 */
public class NhanVien {
    private String maNhanVien;
    private String hoTen;
    private String boPhan;
    private String maQuanLy; // Khóa ngoại tự tham chiếu (NULL nếu là cấp quản lý cao nhất)

    public NhanVien() {
    }

    public NhanVien(String maNhanVien, String hoTen, String boPhan, String maQuanLy) {
        this.maNhanVien = maNhanVien;
        this.hoTen = hoTen;
        this.boPhan = boPhan;
        this.maQuanLy = maQuanLy;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getBoPhan() {
        return boPhan;
    }

    public void setBoPhan(String boPhan) {
        this.boPhan = boPhan;
    }

    public String getMaQuanLy() {
        return maQuanLy;
    }

    public void setMaQuanLy(String maQuanLy) {
        this.maQuanLy = maQuanLy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NhanVien nhanVien = (NhanVien) o;
        return Objects.equals(maNhanVien, nhanVien.maNhanVien);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maNhanVien);
    }

    @Override
    public String toString() {
        return String.format("NhanVien[%s | %-18s | %-20s | QL=%s]",
                maNhanVien, hoTen, boPhan, (maQuanLy != null ? maQuanLy : "None"));
    }
}
