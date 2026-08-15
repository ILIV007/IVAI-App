# Provider-Harness Alignment

## تصمیم محصول

**IVAI یک Agent Harness محلی و provider-neutral است.** کاربر باید بتواند Provider، Account، endpoint، مدل، capability، credential reference و سیاست استفاده را خودش تعریف، فعال، غیرفعال، آزمایش و در Comboهای ترتیبی استفاده کند. Gemini تنها اولین adapter نمونه برای اثبات stream است و نباید به‌عنوان provider پیش‌فرضِ غیرقابل‌تغییر، هویت UI، schema یا Agent runtime عمل کند.

> شبکه فقط برای یک درخواست foreground با هدف مشخص و پس از انتخاب آگاهانهٔ کاربر استفاده می‌شود. IVAI هیچ backend مرکزی، proxy اجباری یا حساب کاربری اجباری ندارد.

## Source of Truth

این سند رودمپ مرجع بارگذاری‌شدهٔ مالک را به وضعیت فعلی مخزن نگاشت می‌کند. ترتیب مصوب بحرانی چنین است: **Governance → UI/RTL → Local Data/Security → One-provider proof → Provider Management/Router → Basic Agent → Hardening/Release**.

## وضعیت فعلی و انحراف‌ها

| حوزه | وضع موجود در `main` | ارزیابی | اقدام اصلاحی |
|---|---|---|---|
| قرارداد Provider | قرارداد streaming نرمال‌شده و adapter Gemini وجود دارد. | پایهٔ درست، اما registry ندارد. | قرارداد به connection/model/capability/provider kind گسترش یابد. |
| Provider مدیریت‌شده توسط کاربر | UI Settings و Router عمدتاً Mock است؛ اتصال Gemini در runtime hard-wired است. | انحراف مهم از BYOK و آزادی Provider. | ProviderConnection، Account، ManualModel و credential-reference محلی ایجاد شود. |
| Credential | Vault Keystore و `clearAll` وجود دارد. | پایهٔ درست. | UI فقط status mask‌شده، store/update/delete و Test Connection دریافت کند؛ plaintext هرگز state UI یا Room نشود. |
| Chat | یک vertical slice Gemini به UI canonical متصل است. | proof مفید، ولی provider-neutral نیست. | انتخاب Chat به `ModelTarget` یا `Combo` منتقل شود؛ Gemini از runtime ثابت حذف شود. |
| Router/Combo | mockهای UI وجود دارند. | هنوز محصولی نیست. | ComboEntryهای ترتیبی، capability matching، timeout/retry محدود، fallback و Attempt Trace اضافه شود. |
| Agent Harness | mock profile و trace UI وجود دارد. | هنوز runtime واقعی نیست. | پس از Router: AgentProfile، policy، safe tools، approval و trace محلی اجرا شود. |

## دامنهٔ فاز بعدی: Provider Registry Foundation

### در محدوده

1. مدل‌های provider-neutral برای `ProviderConnection`، `ProviderAccount`، `ModelDefinition` و `ModelCapability` در Room.
2. شناسهٔ نوع Provider شامل `CUSTOM_OPENAI_COMPATIBLE`، `OPENROUTER` و `GEMINI`؛ هر connection دارای endpoint اختیاری/قابل‌اعتبارسنجی و بدون secret plaintext.
3. credential reference به Vault، status mask‌شده، فعال/غیرفعال‌سازی connection و manual model registration.
4. repository transaction-safe، migration Room و تست‌های process/reopen و redaction.
5. بازنویسی runtime تا adapterها از registry انتخاب شوند، نه از ساخت Gemini ثابت.

### خارج از محدودهٔ این task

- تماس زنده با credential واقعی؛
- Combo fallback و Circuit Breaker (task بعدی Router)؛
- Google Drive backup، مدل Local، MCP، Voice و Automation؛
- Agent tool execution؛
- افشای API key در تست، log، DB، export یا UI.

## معیار پذیرش

| معیار | شرط عبور |
|---|---|
| آزادی Provider | کاربر بتواند حداقل Custom OpenAI-compatible، OpenRouter و Gemini را به شکل رکورد محلی مستقل اضافه/ویرایش/غیرفعال کند. |
| حفاظت Secret | Room فقط `credentialReference` را نگه دارد؛ UI status را mask کند؛ export پیش‌فرض هیچ secretی نداشته باشد. |
| Endpoint ایمن | endpoint سفارشی HTTPS باشد؛ localhost فقط با رضایت و هشدار صریح در task بعدی مجاز شود. |
| مدل‌های دستی | مدل ID بدون hardcode بتواند برای یک connection ثبت شود و capabilityها مشخص باشند. |
| انطباق Router | مدل‌ها و connectionها به‌گونه‌ای ذخیره شوند که ComboEntry فقط referenceهای پایدار را نگه دارد. |
| کیفیت | migration، repository و redaction tests سبز؛ build، unit test، lint و secret scan موفق. |

## ترتیب ادامه

1. **Provider Registry Foundation** — schema، repository و vault references.
2. **Provider Management UI** — provider cards، account status mask‌شده، endpoint/manual model و Test Connection.
3. **Custom OpenAI-compatible و OpenRouter adapters** — همراه contract tests و error mapping.
4. **IVAI Router و Combo** — fallback ترتیبی، capability matching، retry/fallback policy و trace.
5. **Basic Agent Alpha** — safe tools، approval، budgets و run trace.

## Scope Guard

هر تغییر جدید باید نشان دهد که کدام Provider capability gap را پر می‌کند. اضافه‌کردن Provider، dependency، شبکه یا background behavior بدون Task Packet و Acceptance Criteria ممنوع است. Gemini همچنان adapter آزمایشی باقی می‌ماند، اما دیگر provider پیش‌فرض یا جایگزین Router نیست.
