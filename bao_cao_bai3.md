# Báo cáo: Tối ưu hóa tính năng Cập nhật giá hàng loạt

## Phần A: Phân tích & Đề xuất (Đa giải pháp)

**1. Phân tích (I/O)**
- **Dữ liệu đầu vào (Input):** `categoryName` (Tên danh mục, kiểu `String`), `discountPercentage` (Phần trăm giảm giá, kiểu `BigDecimal`). Truyền từ Controller xuống Service.
- **Kết quả đầu ra (Output):** 
  - Thành công: Cập nhật giá mới cho các sản phẩm thuộc danh mục và có trạng thái 'ACTIVE'.
  - Thất bại: Báo lỗi nếu phần trăm giảm giá không hợp lệ (âm hoặc > 100), hoặc không tìm thấy sản phẩm nào để cập nhật.

**2. Đề xuất 2 giải pháp Spring Data JPA**
- **Giải pháp 1:** Sử dụng `findByCategoryAndStatus` để lấy danh sách 50.000 entity lên bộ nhớ (RAM), dùng vòng lặp For/Stream để thay đổi giá trị, dựa vào cơ chế Dirty Checking của Hibernate hoặc gọi `saveAll()` để lưu lại.
- **Giải pháp 2:** Viết Custom Query bằng JPQL với các Annotation `@Modifying` và `@Query` để thực thi trực tiếp câu lệnh UPDATE xuống Database (Bulk Update).

## Phần B: So sánh và Lựa chọn

| Tiêu chí | Giải pháp 1 (Lấy toàn bộ lên RAM) | Giải pháp 2 (Bulk Update với @Modifying) |
| :--- | :--- | :--- |
| **Memory Footprint (Tiêu tốn RAM)** | Rất lớn. Load 50.000 object vào bộ nhớ cùng lúc dễ gây OutOfMemoryError. | Gần như bằng 0. Không load entity nào lên RAM, truy vấn được thực thi trực tiếp ở CSDL. |
| **Performance (Tốc độ / SQL sinh ra)** | Rất chậm. Có thể sinh ra 1 câu SELECT và 50.000 câu UPDATE riêng lẻ (nếu không cấu hình batching tốt). | Rất nhanh. Chỉ sinh ra đúng 1 câu lệnh UPDATE duy nhất xuống Database. |
| **Rủi ro tính đồng bộ dữ liệu** | An toàn với Persistence Context hiện tại vì các object được load và track trực tiếp bởi 1st Level Cache. | Có nguy cơ nếu đang có entity lưu trong 1st Level Cache do Bulk Update không tự động cập nhật lại các entity trong Cache. |

**Chốt lựa chọn:** Chọn **Giải pháp 2**. Với số lượng dữ liệu lớn (50.000 bản ghi), Giải pháp 2 tối ưu vượt trội về RAM và tốc độ (chỉ tốn 1 câu SQL), tránh được nguy cơ treo server. Rủi ro về Cache có thể khắc phục bằng cách thêm `clearAutomatically = true` trong `@Modifying`.

## Phần C: Thiết kế luồng logic (Giải pháp 2)

**Luồng xử lý tại tầng Service:**
1. Nhận input: `categoryName` và `discountPercentage`.
2. **Chặn Bẫy 1:** Kiểm tra `discountPercentage`. Nếu nhỏ hơn 0 hoặc lớn hơn 100 -> Ném ra `IllegalArgumentException`.
3. Gọi hàm `updatePriceByCategory` từ Repository. Hàm này thực thi câu lệnh SQL UPDATE và trả về số lượng bản ghi đã được thay đổi (`updatedCount`).
4. **Chặn Bẫy 2:** Kiểm tra `updatedCount`. Nếu `updatedCount == 0` -> Ném ra `RuntimeException("Không tìm thấy sản phẩm nào để cập nhật")`.
5. Nếu qua được các bước trên, giao dịch sẽ được commit thành công.
