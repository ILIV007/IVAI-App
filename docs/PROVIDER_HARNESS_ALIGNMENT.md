# Provider-Harness Alignment

## تصمیم محصول

**IVAI یک Agent Harness محلی و provider-neutral است.** کاربر Provider، Account، endpoint، مدل، capability، credential reference، Direct Model و Combo ترتیبی را خودش مدیریت می‌کند. Gemini تنها اولین adapter برای اثبات streaming است؛ provider پیش‌فرض، backend، هویت UI یا target ضمنی Agent نیست.

> شبکه فقط برای یک درخواست foreground با هدف مشخص و پس از انتخاب آگاهانهٔ کاربر انجام می‌شود. IVAI هیچ backend مرکزی، proxy اجباری یا حساب کاربری اجباری ندارد.

## وضعیت فعلی Alpha

| حوزه | وضعیت در `main` | نتیجه |
|---|---|---|
| Provider Registry | `ProviderConnection`، `ProviderAccount`، `ProviderModel` و credential reference در Room محلی موجودند. | کاربر connection، account، endpoint، model و capability را خودش مدیریت می‌کند. |
| Preset catalog | presetهای local-only برای Gemini، OpenRouter، OpenAI، Groq، Mistral، Together، DeepSeek، Fireworks و xAI در UI موجودند. | preset فقط metadata و HTTPS endpoint پیشنهادی است؛ provider، مدل، credential یا درخواست شبکه به‌صورت ضمنی ایجاد نمی‌شود. |
| Credential boundary | Vault مبتنی بر Android Keystore نگهدارندهٔ secret است؛ Room فقط reference را حفظ می‌کند. | plaintext key وارد Room، state UI، trace یا export نمی‌شود. |
| Endpoint policy | endpoint cloud سفارشی فقط HTTPS و host معتبر می‌پذیرد. | endpoint آزاد، loopback، HTTP و provider ضمنی وجود ندارد. |
| Local model endpoint | Ollama، LM Studio، vLLM و endpoint local/LAN هنوز connection قابل‌اجرا نیستند. | این بخش تا threat model، Android cleartext allowlist محدود، warning UX، credential-less policy و testهای device تکمیل نشود عمداً blocked می‌ماند. |
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
5. catalog provider با Gemini انتخاب‌شده شروع نمی‌شود؛ کاربر preset یا Advanced custom را آگاهانه انتخاب می‌کند و همچنان model ID و capability را خودش تعیین می‌کند.

## Hardening باقیمانده پیش از GitHub Alpha

| اولویت | مورد | دلیل |
|---|---|---|
| P1 | Local endpoint trust mode برای Ollama، LM Studio، vLLM و serverهای user-managed | نیازمند threat model، cleartext allowlist محدود، warning UX، credential-less policy و device evidence است. |
| P1 | RTL، accessibility، device و performance evidence | release gate رودمپ به artefact قابل‌تکرار نیاز دارد. |
| P1 | Signed APK، SHA-256، release notes و known limitations | پیش‌نیاز انتشار GitHub Alpha است. |

## Scope Guard

افزودن Provider، dependency، شبکه، storage، Agent tool یا background behavior فقط با task مستقل، معیار پذیرش، threat-model متناسب و validation کامل مجاز است. catalog cloud preset یک تغییر metadata محلی است و adapter یا شبکهٔ جدیدی نصب نمی‌کند. Local endpoint transport تا پایان task مستقل trust mode وارد Alpha نمی‌شود. Shell، Termux، Shizuku، Accessibility automation، MCP process/server، automation پس‌زمینه، local inference engine داخل اپ و backend مرکزی همچنان خارج از Alpha هستند.
