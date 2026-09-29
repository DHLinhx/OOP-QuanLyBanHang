package models;

import java.time.LocalDate;
public class DonHang {
    private String maDonHang;
    private LocalDate ngayDat;
    private TrangThaiDonHang trangThai;
    private String maKhachHang;
    private String maNhanVien;

    public DonHang(String maDonHang, LocalDate ngayDat, TrangThaiDonHang trangThai,
                   String maKhachHang, String maNhanVien) {
        this.maDonHang = maDonHang;
        this.ngayDat = ngayDat;
        this.trangThai = trangThai;
        this.maKhachHang = maKhachHang;
        this.maNhanVien = maNhanVien;
    }

    public String getMaDonHang() { return maDonHang; }
    public LocalDate getNgayDat() { return ngayDat; }
    public TrangThaiDonHang getTrangThai() { return trangThai; }
    public String getMaKhachHang() { return maKhachHang; }
    public String getMaNhanVien() { return maNhanVien; }

    public void setMaDonHang(String maDonHang) { this.maDonHang = maDonHang; }
    public void setNgayDat(LocalDate ngayDat) { this.ngayDat = ngayDat; }
    public void setTrangThai(TrangThaiDonHang trangThai) { this.trangThai = trangThai; }
    public void setMaKhachHang(String maKhachHang) { this.maKhachHang = maKhachHang; }
    public void setMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }

    public boolean isHoanThanh() {
        return trangThai == TrangThaiDonHang.HOAN_THANH;
    }
    @Override
    public String toString() {
        return String.format("DonHang[%s | %s | %s | KH=%s | NV=%s]",
                maDonHang, ngayDat, trangThai, maKhachHang, maNhanVien);
    }
}