# Provider-Harness Alignment

## تصمیم محصول

**IVAI یک Agent Harness محلی و provider-neutral است.** کاربر Provider، Account، endpoint، مدل، capability، credential reference، Direct Model و Combo ترتیبی را خودش مدیریت می‌کند. Gemini تنها اولین adapter برای اثبات streaming است؛ provider پیش‌فرض، backend، هویت UI یا target ضمنی Agent نیست.

> شبکه فقط برای یک درخواست foreground با هدف مشخص و پس از انتخاب آگاهانهٔ کاربر انجام می‌شود. IVAI هیچ backend مرکزی، proxy اجباری یا حساب کاربری اجباری ندارد.

## وضعیت فعلی Alpha

| حوزه | وضعیت در `main` | نتیجه |
|---|---|---|
| Provider Registry | `ProviderConnection`، `ProviderAccount`، `ProviderModel` و credential reference در Room محلی موجودند. | کاربر می‌تواند Gemini، OpenRouter و Custom OpenAI-compatible را به‌صورت رکوردهای مستقل مدیریت کند. |
| Credential boundary | Vault مبتنی بر Android Keystore نگهدارندهٔ secret است؛ Room فقط reference را حفظ می‌کند. | plaintext key وارد Room، state UI، trace یا export نمی‌شود. |
| Endpoint policy | endpoint سفارشی فقط HTTPS و host معتبر می‌پذیرد. | endpoint آزاد یا provider ضمنی وجود ندارد. |
| Chat target | Thread از Direct Model یا Combo انتخاب‌شدهٔ کاربر استفاده می‌کند. | Gemini target ثابت محصول نیست. |
| Router/Combo | Comboهای ترتیبی، capability matching، fallback کنترل‌شده و Attempt Trace محلی وجود دارند. | Router provider جدیدی را به‌صورت ضمنی اضافه نمی‌کند. |
| Agent profile | profile فقط Direct Model معتبر یا Combo فعال دارای candidate قابل‌استفاده می‌پذیرد. | target آزاد و نامعتبر پیش از persist و پیش از start run رد می‌شود. |
| Agent read-only tools | profile ابزارهای read/list/search را صریحاً انتخاب می‌کند؛ runtime آنها را به project همان profile و limitهای app-private محدود می‌کند. | observation فایل فقط در حافظه است و محتوای آن وارد Run Trace نمی‌شود. |
| Agent mutation | write در workspace خصوصی فقط پس از preview و Allow once انجام می‌شود. | always-allow و replay خودکار پس از restart وجود ندارد. |

## شواهد Provider-neutral بودن

1. Sidebar وضعیت ثابت Gemini و latency ساختگی نمایش نمی‌دهد؛ status آن از Combo/connection محلی یا empty state ساخته می‌شود.
2. گزینه‌های Agent از Provider Registry و Router محلی مشتق می‌شوند؛ فرم Agent ورودی آزاد `targetKind`، `targetId` یا account ندارد.
3. runtime target profile را پیش از شروع run دوباره اعتبارسنجی می‌کند تا profile قدیمی، حذف‌شده یا دست‌کاری‌شده bypass ایجاد نکند.
4. Custom OpenAI-compatible، OpenRouter و Gemini همگی adapterهای قابل‌انتخاب هستند، نه prerequisite محصول.

## Hardening باقیمانده پیش از GitHub Alpha

| اولویت | مورد | دلیل |
|---|---|---|
| P1 | RTL، accessibility، device و performance evidence | release gate رودمپ به artefact قابل‌تکرار نیاز دارد. |
| P1 | Signed APK، SHA-256، release notes و known limitations | پیش‌نیاز انتشار GitHub Alpha است. |

## Scope Guard

افزودن Provider، dependency، شبکه، storage، Agent tool یا background behavior فقط با task مستقل، معیار پذیرش، threat-model متناسب و validation کامل مجاز است. Shell، Termux، Shizuku، Accessibility automation، MCP process/server، automation پس‌زمینه، local model inference و backend مرکزی همچنان خارج از Alpha هستند.
