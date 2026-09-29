package models;
public class ChiTietDonHang {
    private String maDonHang;
    private String maSanPham;
    private int soLuong;
    private double donGia;
    private double tyLeGiamGia;

    public ChiTietDonHang(String maDonHang, String maSanPham, int soLuong,
                          double donGia, double tyLeGiamGia) {
        this.maDonHang = maDonHang;
        this.maSanPham = maSanPham;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.tyLeGiamGia = tyLeGiamGia;
    }

    public String getMaDonHang() { return maDonHang; }
    public String getMaSanPham() { return maSanPham; }
    public int getSoLuong() { return soLuong; }
    public double getDonGia() { return donGia; }
    public double getTyLeGiamGia() { return tyLeGiamGia; }

    public void setMaDonHang(String maDonHang) { this.maDonHang = maDonHang; }
    public void setMaSanPham(String maSanPham) { this.maSanPham = maSanPham; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public void setDonGia(double donGia) { this.donGia = donGia; }
    public void setTyLeGiamGia(double tyLeGiamGia) { this.tyLeGiamGia = tyLeGiamGia; }

    public double getThanhTien() {
        return soLuong * donGia * (1 - tyLeGiamGia);
    }

    @Override
    public String toString() {
        return String.format("ChiTiet[%s | %s | SL=%d | Gia=%,.0f | Giam=%.0f%% | ThanhTien=%,.0f]",
                maDonHang, maSanPham, soLuong, donGia, tyLeGiamGia * 100, getThanhTien());
    }
}