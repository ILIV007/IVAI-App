# P2-02 — Local Storage, Reset, and Archive Closeout

## هدف

این بسته Exit Gate باقی‌ماندهٔ فاز ۲ را برای دادهٔ محلی تکمیل می‌کند. پیاده‌سازی یک workspace کاملاً app-private، حذف هماهنگ داده‌های محلی، و export/import نسخه‌دار و checksummed با پسوند `.ivai` فراهم می‌کند. این کار هیچ Provider، credential-entry UI، تماس شبکه، backend، telemetry، backup ابری یا Agent runtime اضافه نمی‌کند.

## محدوده

| حوزه | خروجی |
|---|---|
| Workspace | `ProjectWorkspace` با ریشهٔ `filesDir/ivai/projects`، شناسهٔ پروژه و مسیر نسبی معتبر، canonicalization و جلوگیری از traversal/absolute path. |
| Snapshot داده | `LocalWorkspaceRepository` با snapshot transaction و restore اتمیک برای project، thread و message. |
| Archive | فرمت binary نسخه‌دار با magic `IVAI`، SHA-256 checksum، محدودیت اندازه، validate کامل قبل از commit، staging directory و rollback best-effort. |
| Secret isolation | Archive تنها entityهای Room و فایل‌های workspace را می‌خواند؛ به DataStore، Android Keystore یا `EncryptedSecretVault` وابستگی ندارد. |
| Delete all data | حذف DB workspace، فایل‌های app-private و ciphertext/aliasهای vault بدون decrypt یا بازگرداندن plaintext. |
| آزمون | Round-trip archive، checksum failure بدون تغییر state، traversal rejection، delete-all-data، vault clear-all و reopen پایدار Room. |

## فرمت archive

```text
header: magic (IVAI) | format version (1) | payload size | SHA-256(payload)
payload: created-at | projects | threads | messages | workspace files
```

Checksum فقط integrity تصادفی/خرابی فایل را بررسی می‌کند و امضای هویت یا encryption نیست. Archive فاز Alpha به‌صورت محلی است و هیچ Secret، DataStore preference یا Keystore key را دربرنمی‌گیرد. Backup رمزنگاری‌شده و cloud-drive backup خارج از محدودهٔ Alpha باقی می‌مانند.

## معیار پذیرش

| معیار | انتظار |
|---|---|
| Containment | مسیرهای `..`، absolute path، separator نامعتبر و project ID نامعتبر قبل از هر write/import رد شوند. |
| Import safety | magic، version، اندازه، checksum، duplicate ID/path، foreign key و enumهای پیام پیش از commit اعتبارسنجی شوند. |
| Atomic database restore | پاک‌سازی و ورود داده‌های Room در یک transaction انجام شود. |
| File restore | فایل‌ها ابتدا در staging app-private نوشته شوند؛ workspace سابق در صورت failure قابل rollback best-effort باشد. |
| Secret safety | vault decrypt نمی‌شود و ciphertext/aliasها فقط توسط reset حذف می‌شوند. |
| Recovery proxy | reopen دیتابیس همان project/threadهای ذخیره‌شده را بازگرداند. |
| Validation | `assembleDebug`، `testDebugUnitTest` و `lintDebug` محلی و CI شاخهٔ محافظت‌شده سبز باشند. |

## محدودیت‌های آگاهانه

این بسته هنوز UI انتخاب فایل/Storage Access Framework، UX تأیید destructive reset، migration از schema آینده، آزمون Android Keystore روی دستگاه واقعی، low-storage/device matrix و backup رمزنگاری‌شده را پیاده‌سازی نمی‌کند. این موارد برای فاز hardening یا قابلیت‌های UI وابسته باید در taskهای مستقل و قابل‌review انجام شوند.

## مراجع

[1]: [Security Architecture](../SECURITY.md)
[2]: [Execution Roadmap](../ROADMAP.md)
[3]: [P2-01 — Local Data and Security Foundation](P2-01-local-data-security-foundation.md)
