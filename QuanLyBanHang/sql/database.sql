-- 1. Tạo Database
CREATE DATABASE IF NOT EXISTS QuanLyBanHang CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE QuanLyBanHang;

-- Xóa bảng cũ nếu đã tồn tại để tránh lỗi
DROP TABLE IF EXISTS ChiTietDonHang;
DROP TABLE IF EXISTS DonHang;
DROP TABLE IF EXISTS SanPham;
DROP TABLE IF EXISTS NhanVien;
DROP TABLE IF EXISTS KhachHang;

-- 2. Tạo cấu trúc các bảng
CREATE TABLE SanPham (
    MaSanPham INT AUTO_INCREMENT PRIMARY KEY,
    TenSanPham VARCHAR(150) NOT NULL,
    DanhMuc VARCHAR(50) NOT NULL,
    GiaBan DECIMAL(14, 2) NOT NULL,
    TonKho INT NOT NULL DEFAULT 0
);

CREATE TABLE KhachHang (
    MaKhachHang INT AUTO_INCREMENT PRIMARY KEY,
    HoTen VARCHAR(100) NOT NULL,
    ThanhPho VARCHAR(100),
    Email VARCHAR(100) UNIQUE
);

CREATE TABLE NhanVien (
    MaNhanVien INT AUTO_INCREMENT PRIMARY KEY,
    HoTen VARCHAR(100) NOT NULL,
    BoPhan VARCHAR(100) NOT NULL,
    MaQuanLy INT NULL,
    CONSTRAINT fk_nv_quanly FOREIGN KEY (MaQuanLy) REFERENCES NhanVien(MaNhanVien) ON DELETE SET NULL
);

CREATE TABLE DonHang (
    MaDonHang INT AUTO_INCREMENT PRIMARY KEY,
    MaKhachHang INT NOT NULL,
    MaNhanVien INT NOT NULL,
    NgayDat DATETIME NOT NULL,
    TrangThai ENUM('DangXuLy', 'HoanThanh', 'DaHuy') NOT NULL DEFAULT 'DangXuLy',
    CONSTRAINT fk_dh_khachhang FOREIGN KEY (MaKhachHang) REFERENCES KhachHang(MaKhachHang) ON DELETE CASCADE,
    CONSTRAINT fk_dh_nhanvien FOREIGN KEY (MaNhanVien) REFERENCES NhanVien(MaNhanVien) ON DELETE RESTRICT
);

CREATE TABLE ChiTietDonHang (
    MaDonHang INT NOT NULL,
    MaSanPham INT NOT NULL,
    SoLuong INT NOT NULL,
    DonGia DECIMAL(14, 2) NOT NULL,
    TyLeGiamGia DECIMAL(4, 2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (MaDonHang, MaSanPham),
    CONSTRAINT fk_ctdh_donhang FOREIGN KEY (MaDonHang) REFERENCES DonHang(MaDonHang) ON DELETE CASCADE,
    CONSTRAINT fk_ctdh_sanpham FOREIGN KEY (MaSanPham) REFERENCES SanPham(MaSanPham) ON DELETE RESTRICT
);

-- ==========================================================
-- 3. NẠP DỮ LIỆU MẪU
-- ==========================================================

-- 12 Sản phẩm
INSERT INTO SanPham (MaSanPham, TenSanPham, DanhMuc, GiaBan, TonKho) VALUES
(1, 'Laptop Dell Inspiron 15', 'Laptop', 17500000, 15),
(2, 'MacBook Air M2', 'Laptop', 26000000, 8),
(3, 'Laptop Asus Zenbook 14', 'Laptop', 21500000, 12),
(4, 'Laptop Lenovo ThinkPad E14', 'Laptop', 19000000, 10),
(5, 'Chuột không dây Logitech M331', 'PhuKien', 350000, 45),
(6, 'Bàn phím cơ DareU EK87', 'PhuKien', 850000, 30),
(7, 'Tai nghe Sony WH-1000XM5', 'PhuKien', 6900000, 14),
(8, 'Điện thoại iPhone 15 Pro', 'DienThoai', 27500000, 18),
(9, 'Samsung Galaxy S24 Ultra', 'DienThoai', 28900000, 15),
(10, 'Xiaomi Redmi Note 13', 'DienThoai', 4990000, 25),
(11, 'Màn hình Dell UltraSharp 27', 'ManHinh', 9200000, 7),
(12, 'Màn hình LG 24 inch IPS', 'ManHinh', 3100000, 20);

-- 8 Khách hàng (khách 7, 8 chưa mua hàng để test LEFT JOIN)
INSERT INTO KhachHang (MaKhachHang, HoTen, ThanhPho, Email) VALUES
(1, 'Nguyen Van A', 'Ha Noi', 'nguyenvana@gmail.com'),
(2, 'Tran Thi B', 'TP HCM', 'tranthib@gmail.com'),
(3, 'Le Van C', 'Da Nang', 'levanc@gmail.com'),
(4, 'Pham Hoang D', 'Can Tho', 'phamhoangd@gmail.com'),
(5, 'Hoang Mai E', 'Ha Noi', 'hoangmaie@gmail.com'),
(6, 'Vu Dinh F', 'Hai Phong', 'vudinhf@gmail.com'),
(7, 'Dang Thu G', 'Hue', 'dangthug@gmail.com'),
(8, 'Doan Quoc H', 'TP HCM', 'doanquoch@gmail.com');

-- 4 Nhân viên (Nhân viên 1 làm quản lý cấp trên)
INSERT INTO NhanVien (MaNhanVien, HoTen, BoPhan, MaQuanLy) VALUES
(1, 'Tran Van Quan', 'Kinh Doanh', NULL),
(2, 'Nguyen Thi Huong', 'Kinh Doanh', 1),
(3, 'Le Tuan Kiet', 'Kinh Doanh', 1),
(4, 'Pham Minh Tam', 'Cham Soc Khach Hang', 1);

-- 15 Đơn hàng (1001 đến 1015)
INSERT INTO DonHang (MaDonHang, MaKhachHang, MaNhanVien, NgayDat, TrangThai) VALUES
(1001, 1, 1, '2026-01-05 09:30:00', 'HoanThanh'),
(1002, 2, 2, '2026-01-12 14:15:00', 'HoanThanh'),
(1003, 3, 3, '2026-01-18 10:00:00', 'HoanThanh'),
(1004, 4, 2, '2026-01-25 16:45:00', 'DaHuy'),
(1005, 5, 1, '2026-02-02 11:20:00', 'HoanThanh'),
(1006, 1, 3, '2026-02-08 08:30:00', 'HoanThanh'),
(1007, 6, 2, '2026-02-14 15:10:00', 'DangXuLy'),
(1008, 2, 1, '2026-02-20 13:40:00', 'HoanThanh'),
(1009, 3, 3, '2026-02-26 17:05:00', 'HoanThanh'),
(1010, 4, 2, '2026-03-01 09:00:00', 'HoanThanh'),
(1011, 5, 1, '2026-03-05 10:50:00', 'HoanThanh'),
(1012, 6, 3, '2026-03-10 14:35:00', 'DangXuLy'),
(1013, 1, 2, '2026-03-15 16:00:00', 'HoanThanh'),
(1014, 2, 1, '2026-03-20 11:15:00', 'DaHuy'),
(1015, 3, 3, '2026-03-25 15:30:00', 'HoanThanh');

-- 31 Chi tiết đơn hàng
INSERT INTO ChiTietDonHang (MaDonHang, MaSanPham, SoLuong, DonGia, TyLeGiamGia) VALUES
(1001, 1, 1, 17500000, 0.05),
(1001, 5, 2, 350000, 0.00),
(1002, 2, 1, 26000000, 0.10),
(1002, 6, 1, 850000, 0.00),
(1003, 8, 1, 27500000, 0.00),
(1003, 7, 1, 6900000, 0.05),
(1004, 10, 2, 4990000, 0.00),
(1004, 5, 1, 350000, 0.00),
(1005, 3, 1, 21500000, 0.05),
(1005, 6, 1, 850000, 0.00),
(1005, 11, 1, 9200000, 0.05),
(1006, 9, 1, 28900000, 0.08),
(1006, 7, 1, 6900000, 0.05),
(1007, 4, 1, 19000000, 0.00),
(1007, 12, 1, 3100000, 0.00),
(1008, 1, 1, 17500000, 0.05),
(1008, 5, 3, 350000, 0.00),
(1009, 8, 1, 27500000, 0.05),
(1009, 11, 1, 9200000, 0.00),
(1010, 10, 1, 4990000, 0.00),
(1010, 12, 2, 3100000, 0.05),
(1011, 2, 1, 26000000, 0.05),
(1011, 6, 2, 850000, 0.00),
(1012, 3, 1, 21500000, 0.00),
(1012, 5, 2, 350000, 0.00),
(1013, 9, 1, 28900000, 0.05),
(1013, 7, 1, 6900000, 0.10),
(1014, 4, 1, 19000000, 0.00),
(1014, 11, 1, 9200000, 0.05),
(1015, 8, 1, 27500000, 0.05),
(1015, 6, 1, 850000, 0.00);
