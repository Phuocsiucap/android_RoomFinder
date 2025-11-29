# Hướng Dẫn Thiết Lập Admin Role

## Cách 1: Set Admin Role Thủ Công (Khuyến nghị cho lần đầu)

### Bước 1: Truy cập Firebase Console
1. Mở trình duyệt và vào: https://console.firebase.google.com/
2. Chọn project **RoomFinder** của bạn

### Bước 2: Vào Firestore Database
1. Click vào **Firestore Database** ở menu bên trái
2. Chọn tab **Data**

### Bước 3: Tìm User Cần Set Admin
1. Tìm collection `users`
2. Click vào collection `users`
3. Tìm document của user bạn muốn set làm admin (có thể tìm theo email hoặc userId)

### Bước 4: Thêm/Sửa Field `role`
1. Click vào document của user
2. Nếu chưa có field `role`:
   - Click **Add field**
   - Field name: `role`
   - Field type: chọn **string**
   - Value: nhập `admin`
3. Nếu đã có field `role`:
   - Click vào giá trị hiện tại
   - Sửa thành `admin`
4. Click **Update** để lưu

### Bước 5: Xác Nhận
- Field `role` phải có giá trị là `admin` (chữ thường)
- User sẽ thấy nút "Quản trị viên" trong Profile sau khi đăng nhập lại

---

## Cách 2: Sử Dụng Helper Function (Cho Admin)

Nếu bạn đã là admin, có thể thêm chức năng set role cho user khác trong UserListActivity.

---

## Cách 3: Set Admin Cho Chính Mình (Lần Đầu)

### Nếu bạn chưa có tài khoản:
1. Đăng ký tài khoản mới trong app
2. Lấy **userId** từ Firebase Console (trong collection `users`)
3. Set `role = "admin"` cho userId đó

### Nếu bạn đã có tài khoản:
1. Đăng nhập vào app
2. Vào Profile để xem email của bạn
3. Vào Firebase Console → Firestore → `users`
4. Tìm document có email của bạn
5. Set `role = "admin"`

---

## Lưu Ý Quan Trọng

⚠️ **Bảo mật:**
- Chỉ set admin cho những user đáng tin cậy
- Admin có quyền xóa user, quản lý ads, xem thống kê
- Nên có ít nhất 1 admin để quản lý hệ thống

✅ **Sau khi set admin:**
- User cần đăng xuất và đăng nhập lại để app nhận role mới
- Hoặc restart app
- Nút "Quản trị viên" sẽ xuất hiện trong Profile

---

## Kiểm Tra Role Hiện Tại

Để kiểm tra role của user:
1. Vào Firebase Console → Firestore → `users`
2. Mở document của user
3. Xem field `role`:
   - `admin` = Quản trị viên
   - `landlord` = Chủ trọ
   - `customer` = Khách hàng (mặc định)
   - Không có field = Mặc định là `customer`

---

## Troubleshooting

**Nút Admin không hiện:**
- Kiểm tra field `role` có đúng là `admin` (chữ thường) không
- Đăng xuất và đăng nhập lại
- Restart app
- Kiểm tra Logcat để xem role được load đúng không

**Không thể truy cập Admin Dashboard:**
- Kiểm tra lại role trong Firestore
- Đảm bảo đã đăng nhập đúng tài khoản
- Xem Logcat để biết lỗi cụ thể

