package models;

public class SanPham {
    private String maSanPham;
    private String TenSanPham;
    private String DanhMuc;
    private int GiaBan;
    private int TonKho;

    public SanPham(String maSanPham, String TenSanPham, 
                          String DanhMuc, int GiaBan, int TonKho){
        this.maSanPham = maSanPham;
        this.TenSanPham = TenSanPham;
        this.DanhMuc = DanhMuc;
        this.GiaBan = GiaBan;
        this.TonKho = TonKho;
    }
    public String getMaSanPham() {return maSanPham; }
    public String getTenSanPham() {return TenSanPham;}
    public String getDanhMuc() {return DanhMuc;}
    public int getGiaBan() {return GiaBan;}
    public int getTonKho() {return TonKho;} 

    public void setMaSanPham(String maSanPham) {this.maSanPham=maSanPham;}
    public void setTenSanPham(String TenSanPham) {this.TenSanPham=TenSanPham;}
    public void setDanhMuc(String DanhMuc) {this.DanhMuc=DanhMuc;}
    public void setGiaBan(int GiaBan){this.GiaBan=GiaBan; }
    public void setTonKho(int TonKho) {this.TonKho=TonKho;}
}

