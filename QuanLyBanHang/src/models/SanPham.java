package models;

import java.util.Objects;

/**
 * Lớp SanPham đại diện cho bảng SanPham trong cơ sở dữ liệu.
 */
public class SanPham {
    private String maSanPham;
    private String tenSanPham;
    private String danhMuc;
    private double giaBan;
    private int tonKho;

    public SanPham() {
    }

    public SanPham(String maSanPham, String tenSanPham, String danhMuc, double giaBan, int tonKho) {
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.danhMuc = danhMuc;
        this.giaBan = giaBan;
        this.tonKho = tonKho;
    }

    public String getMaSanPham() {
        return maSanPham;
    }

    public void setMaSanPham(String maSanPham) {
        this.maSanPham = maSanPham;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public void setTenSanPham(String tenSanPham) {
        this.tenSanPham = tenSanPham;
    }

    public String getDanhMuc() {
        return danhMuc;
    }

    public void setDanhMuc(String danhMuc) {
        this.danhMuc = danhMuc;
    }

    public double getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(double giaBan) {
        this.giaBan = giaBan;
    }

    public int getTonKho() {
        return tonKho;
    }

    public void setTonKho(int tonKho) {
        this.tonKho = tonKho;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SanPham sanPham = (SanPham) o;
        return Objects.equals(maSanPham, sanPham.maSanPham);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maSanPham);
    }

    @Override
    public String toString() {
        return String.format("SanPham[%s | %-28s | %-10s | %,.0f VND | Ton:%d]",
                maSanPham, tenSanPham, danhMuc, giaBan, tonKho);
    }
}
