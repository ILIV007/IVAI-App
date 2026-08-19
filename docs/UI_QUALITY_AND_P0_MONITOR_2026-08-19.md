# UI-Quality Lint Triage and Phase 7.5 / Alpha P0 Monitor — 2026-08-19

> **وضعیت:** این سند، تصمیم‌های مبتنی بر evidence برای lint و gateهای انتشار را ثبت می‌کند. هیچ نتیجهٔ participant، device، accessibility، network، signing یا Alpha در آن ساخته یا استنباط نشده است.
>
> **baseline:** `main` commit `1d743c0de99051a2c9b57ab97f7a99afa6702610` پس از [PR #103](https://github.com/ILIV007/IVAI-App/pull/103). Android quality و Secret scan برای همین commit موفق بوده‌اند.[1]

## مرز تصمیم

IVAI همچنان یک **Agent Harness محلی، Backendless، BYOK و Provider-neutral** است. این پایش هیچ Provider، endpoint، credential، runtime، backend، telemetry، discovery، HTTP، permission، signing یا انتشار جدیدی اضافه نمی‌کند. بسته‌های محلی تحقیق و release-candidate فقط provenance و reproducibility را آماده می‌کنند؛ آن‌ها evidence فیزیکی/participant یا مجوز Alpha نیستند.

## 1. Snapshot واقعی lint

اجرای `lintDebug` روی baseline، **0 error، 9 warning و 0 hint** گزارش کرد. [PR #103](https://github.com/ILIV007/IVAI-App/pull/103) تمام 10 finding کم‌ریسک UI-quality را با quality gate کامل و CI محافظت‌شده رفع کرد. همهٔ warningهای باقی‌مانده پیش از تصمیم Alpha باید owner و disposition روشن داشته باشند.[2]

| دسته | تعداد فعلی | findingهای دقیق | تصمیم/وضعیت | فاز/گیت بعدی |
|---|---:|---|---|---|
| `ModifierParameter` | 0 | هشت composable triage‌شده در `ChatsScreen`، foundation/sidebar، Projects/Settings و state viewها | **Resolved در PR #103.** `Modifier` اکنون نخستین optional parameter است؛ data، navigation و network تغییر نکردند. | Regression در quality gateهای هر candidate بعدی. |
| `AutoboxingStateCreation` | 0 | state گام wizard در `ProviderManagementSection` و `CreateComboSheet` | **Resolved در PR #103.** هر دو state با `mutableIntStateOf(1)` بدون تغییر transition یا persistence جایگزین شدند. | Regression در quality gateهای هر candidate بعدی. |
| Gradle/AGP/Compose/Lifecycle/Core updates | 8 warning | Gradle 9.7، AGP 9.3.1، Core 1.19، سه Lifecycle 2.11، Compose BOM 2026.08.00، Kotlin Compose 2.4.10 | **Deferred — خارج از UI-quality.** این‌ها migrationهای version هستند، نه نقص UI. Core 1.19 نیازمند `compileSdk 37` است؛ Lifecycle 2.11 و migration toolchain نیز matrix سازگار جداگانه می‌خواهند. | فقط در compatibility/build-system phase مستقل طبق [Compatibility Research](COMPATIBILITY_RESEARCH_2026-08-19.md). |
| `ObsoleteSdkInt` | 1 warning | `mipmap-anydpi-v26` با `minSdk 29` | **Deferred — تصمیم launcher policy.** انتقال نابهنگام XML به `mipmap-anydpi` قبلاً با `IconMixed` تعارض داشته است. | بعد از evidence launcher روی دستگاه و یک تصمیم محدود minSdk/fallback. |

> **نتیجهٔ اجرایی UI-quality:** increment کم‌ریسک کامل شده است. 9 warning باقی‌مانده عمداً برای صفر کردن ظاهری lint دستکاری نمی‌شوند، زیرا risk compatibility یا launcher را بالا می‌برند.

## 2. گیت و scope اجراشدهٔ UI lint hygiene

| مورد | قرارداد |
|---|---|
| Goal | حذف 8 `ModifierParameter` warning و 2 `AutoboxingStateCreation` hint بدون تغییر محصول یا مرزهای ایمنی — **Pass در PR #103**. |
| In scope | فقط signature/composable call-siteهای نام‌دار، دو state primitive، importهای لازم و regression test/screenshotِ واقعاً متاثر — **به‌طور کامل رعایت شد**. |
| خارج از scope | dependency/toolchain، launcher resource، Provider/Account/Model/Combo/Agent، Room/Vault، network/endpoint، credential، telemetry، Settings reset و Phase 8 runtime. |
| گیت پذیرش | `lintDebug` این 10 finding را گزارش نمی‌کند؛ کیفیت debug/release، 155 unit test بدون failure/error/skipped، Roborazzi/semantics مرتبط، guardهای Provider-neutral/RTL/contrast و CI محافظت‌شده سبز هستند. |
| ریسک کنترل‌شده | پیش از جابه‌جایی parameter، تمام call-siteها بررسی می‌شوند؛ callهای positional باید به named arguments یا ترتیب صحیح تبدیل شوند. هیچ suppress blanket اضافه نمی‌شود. |

## 3. P0 Monitor — Phase 7.5 و Alpha

### 3.1 Evidence deterministic آماده

| P0/gate | evidence برای `1d743c0` | وضعیت واقعی |
|---|---|---|
| CI محافظت‌شده | Secret scan و Android quality برای commit baseline موفق.[1] | **Pass برای baseline**؛ باید روی هر candidate آینده تکرار شود. |
| Release-candidate deterministic package | بستهٔ محلی unsigned همین commit با R8 release، `mapping.txt`، APK debug، reportهای unit/lint، checksum و manifest ساخته و verifier آن پاس شد. | **Ready / not a release pass.** 155 test، 0 failure/error/skipped؛ 10 artifact checksum شد. |
| Signed/public boundary | verifier صراحتاً APK release را unsigned تشخیص داد. هیچ tag، upload، GitHub Release یا binary عمومی ایجاد نشد. | **Pending / blocked for Alpha.** |
| Phase 7.5 research package | بستهٔ debug کنترل‌شده برای همین commit آماده و checksum-verifier آن پاس شد؛ worksheet عمداً blank است. | **Ready / no participant or device result.** |
| Security/architecture static evidence | scanهای candidate: credential pattern، cleartext/trust bypass، execution ممنوع، implicit Provider selection و global forced-LTR shell همگی پاس شدند. | **Pass برای baseline**؛ جایگزین test فیزیکی نیست. |
| Contrast، launcher safe zone، Provider-neutral، RTL و Phase 8/R8 readiness guards | guardهای repository روی baseline پاس شدند. | **Pass deterministic.** |

### 3.2 P0های واقعی که هنوز باز هستند

| P0 | evidence لازم | وضعیت در این sandbox | شرط بسته‌شدن |
|---|---|---|---|
| Usability/card sort/tree test/safety comprehension | 5–8 جلسه voluntary و de-identified، thresholds protocol و retest findingهای P0/P1 | **Pending.** validation record نتیجه‌ای ندارد. | فقط aggregateهای تأییدشده و de-identified طبق protocol ثبت شود. |
| Heuristic review | دو reviewer مستقل، reconciliation و صفر finding P0/P1 حل‌نشده | **Pending.** | worksheet مستقل و reconciliation واقعی. |
| Device matrix | حداقل یک compact و یک medium Android: install/upgrade/restart/rotation/offline، theme، font scale و launcher | **Pending.** محیط sandbox، `adb` نصب‌شده یا device متصل برای اجرای این evidence نداشت؛ این عدم دسترسی، failure اپ نیست. | نتایج واقعی بدون device ID و بدون دادهٔ حساس ثبت شوند. |
| Force-RTL و TalkBack | screenshot/note غیرحساس، swipe order و explore-by-touch برای کنترل‌های task-critical | **Pending.** | اجرای واقعی روی compact و medium device و ثبت finding/retest. |
| HTTPS loopback/private-LAN | endpoint صریح HTTPS، cancellation، timeout و offline بدون HTTP/discovery/scan/trust bypass | **Pending.** | evidence فیزیکی با endpoint و credential غیرحساس/مصرفی؛ هیچ raw network log یا secret وارد repository نشود. |
| Release signing/provenance | signed APK مالک، SHA-256، tag annotated، release notes، owner approval و independent download/hash | **Pending / Alpha blocked.** | فقط بعد از همهٔ P0/P1های بالا و روی یک commit دقیق. |

## 4. اقدامات بعدی و قواعد پایش

1. **UI lint hygiene** در [PR #103](https://github.com/ILIV007/IVAI-App/pull/103) کامل شد؛ 10 finding UI-only رفع شدند و 9 warning compatibility/launcher intentionally untouched مانده‌اند.
2. **Phase 7.5** فقط با controlled research package و app state پاک اجرا می‌شود. هیچ participant نباید credential وارد کند، Provider request بفرستد یا Agent write انجام دهد.[3]
3. **Device/network evidence** نتیجهٔ واقعی است، نه یک checklist تیک‌خورده. هر failure P0/P1 باید scenario را متوقف، یک remediation focused ایجاد و سپس با evidence تازه retest کند.
4. **Alpha** تا تکمیل participant/device/accessibility/network، signed artifact، SHA-256، tag، notes و owner approval، **Not approved** باقی می‌ماند.[4]
5. این سند snapshot دستی evidence است، نه یک claim دربارهٔ پایش background. زیرا P0های باقی‌مانده به مشاهده و approval واقعی انسان/دستگاه وابسته‌اند؛ job زمان‌بندی‌شده نمی‌تواند آن‌ها را Pass کند.

## References

[1] [Android quality run for main commit `1d743c0`](https://github.com/ILIV007/IVAI-App/actions/runs/32277336734)

[2] [Release Readiness Checklist](RELEASE_READINESS_CHECKLIST.md)

[3] [Phase 7.5 Usability and Heuristic Review Runbook](PHASE7_5_USABILITY_HEURISTIC_RUNBOOK.md)

[4] [GitHub Alpha Release Checklist](ALPHA_RELEASE.md)

[5] [Phase 7 UX Validation Record](PHASE7_UIUX_VALIDATION.md)

[6] [Compatibility Research](COMPATIBILITY_RESEARCH_2026-08-19.md)
