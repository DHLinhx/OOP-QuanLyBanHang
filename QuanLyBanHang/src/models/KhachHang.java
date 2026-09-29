package models;

import java.util.Objects;

/**
 * Lớp KhachHang đại diện cho bảng KhachHang trong cơ sở dữ liệu.
 * Nhiệm vụ của: Người 2 (Khách hàng & Nhân viên).
 */
public class KhachHang {
    private String maKhachHang;
    private String hoTen;
    private String thanhPho;
    private String email;

    public KhachHang() {
    }

    public KhachHang(String maKhachHang, String hoTen, String thanhPho, String email) {
        this.maKhachHang = maKhachHang;
        this.hoTen = hoTen;
        this.thanhPho = thanhPho;
        this.email = email;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getThanhPho() {
        return thanhPho;
    }

    public void setThanhPho(String thanhPho) {
        this.thanhPho = thanhPho;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KhachHang khachHang = (KhachHang) o;
        return Objects.equals(maKhachHang, khachHang.maKhachHang);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maKhachHang);
    }

    @Override
    public String toString() {
        return String.format("KhachHang[%s | %-16s | %-10s | %s]",
                maKhachHang, hoTen, thanhPho, email);
    }
}
