# Yêu cầu & Tiêu chí nghiệm thu

## Yêu cầu

| Mục | Nội dung |
| --- | --- |
| App name | <!-- Tên app --> |
| Package name | <!-- com.nphstudio.xxx --> |
| App mẫu | <!-- Link Google Play hoặc APK --> |
| Deadline | <!-- Ngày --> |
| Ghi chú | <!-- Yêu cầu đặc biệt nếu có --> |

## Tiêu chí nghiệm thu

| # | Tiêu chí | Mô tả | Kết quả |
| --- | --- | --- | --- |
| 1 | Build AAB | `bundleRelease` không lỗi, output AAB hợp lệ | |
| 2 | Chạy trên device | App không crash trên device thật | |
| 3 | Package name | applicationId khớp với yêu cầu (dev không tự đổi) | |
| 4 | Giống app mẫu | Giống chức năng và bố cục, khác UI/icon/theme | |
| 5 | MO.md đầy đủ | Liệt kê đầy đủ vị trí ads quan sát từ app mẫu | |
| 6 | Ads đúng vị trí | Mỗi placement trong MO.md được đặt đúng nameSpace | |
| 7 | Ads không block | onAdFailed luôn cho user tiếp tục | |
| 8 | Splash ad | Mở app > splash ad > chuyển màn bình thường | |
| 9 | Resume ad | Minimize > quay lại > hiện resume ad | |
| 10 | Banner đúng chỗ | Hiện đúng vị trí theo MO.md | |
| 11 | Cleanup | NphAds.destroy() có trong mọi Activity.onDestroy() | |
| 12 | Dùng SDK đúng cách | Chỉ dùng NphAds.*, không gọi trực tiếp AdMob, không sửa app/libs/ | |
| 13 | License key | apiKey trong MyApp.kt không bị thay đổi so với ban đầu | |
| 14 | Code sạch | Không commit local.properties, jks, google-services.json | |
| 15 | Nộp đủ | Source code + AAB + APK + MO.md | |

## Phân loại

- **PASS**: 15/15
- **MINOR FIX**: 13-14/15, lỗi nhỏ sửa nhanh
- **REJECT**: dưới 13/15 hoặc lỗi nghiêm trọng (crash, thiếu ads, sai package, đổi apiKey/packageName)

## Ghi chú nghiệm thu

<!-- NPH Lab ghi nhận kết quả nghiệm thu ở đây -->
