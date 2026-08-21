# UI-Quality Lint Triage and Phase 7.5 / Alpha P0 Monitor — 2026-08-19

> **وضعیت:** این سند فقط evidence قابل‌اثبات lint، CI و packageهای deterministic را ثبت می‌کند. هیچ نتیجهٔ participant، device، accessibility، network، signing یا Alpha در آن ساخته یا استنباط نشده است.
>
> **baseline فعلی:** `main` commit `d604a1c2a0e83d19b9da94fc80dcb443b4109de7` پس از [PR #110](https://github.com/ILIV007/IVAI-App/pull/110). Android quality و Secret scan برای همین commit موفق بوده‌اند.[1]

## مرز تصمیم

IVAI همچنان یک **Agent Harness محلی، Backendless، BYOK و Provider-neutral** است. این پایش هیچ Provider، endpoint، credential، runtime، backend، telemetry، discovery، HTTP، permission، signing یا انتشار جدیدی اضافه نمی‌کند. بسته‌های محلی تحقیق و release-candidate فقط provenance و reproducibility را آماده می‌کنند؛ آن‌ها evidence فیزیکی/participant یا مجوز Alpha نیستند.

## 1. Snapshot واقعی lint و compatibility

اجرای `lintDebug` و CI محافظت‌شدهٔ baseline، **0 error، 0 warning و 0 hint** گزارش کردند. هیچ suppress blanket برای رسیدن به این نتیجه افزوده نشده است؛ هر finding در یک PR مستقل با build، test، lint و guard کامل validate شد.[1] [2]

| دسته | تعداد فعلی | وضعیت قابل‌اثبات | PR/گیت |
|---|---:|---|---|
| `ModifierParameter` | 0 | هشت composable با حفظ behavior و call-siteهای بررسی‌شده اصلاح شدند. | **Resolved** در [PR #103](https://github.com/ILIV007/IVAI-App/pull/103) |
| `AutoboxingStateCreation` | 0 | دو state wizard با `mutableIntStateOf` بدون تغییر transition یا persistence اصلاح شدند. | **Resolved** در [PR #103](https://github.com/ILIV007/IVAI-App/pull/103) |
| AGP / Gradle / API | 0 | AGP 9.3.1، Gradle 9.7.1، `compileSdk`/`targetSdk` 37 و resource shrinking با debug/release/R8، test و CI اعتبارسنجی شدند. | **Resolved** در [PR #105](https://github.com/ILIV007/IVAI-App/pull/105) |
| Core / Lifecycle | 0 | Core 1.19.0 و Lifecycle 2.11.0 فقط پس از تکمیل prerequisiteهای toolchain/API ارتقا یافتند. | **Resolved** در [PR #106](https://github.com/ILIV007/IVAI-App/pull/106) |
| Compose BOM / test rule | 0 | BOM 2026.08.00 و migration هر هشت test rule به v2 با 155 test واقعی گذشتند. | **Resolved** در [PR #107](https://github.com/ILIV007/IVAI-App/pull/107) و [#108](https://github.com/ILIV007/IVAI-App/pull/108) |
| Kotlin / KSP / Material 3 diagnostics | 0 | Kotlin Compose plugin 2.4.10 با KSP 2.3.11، Room KSP، R8 و source remediation کم‌ریسک اعتبارسنجی شد. | **Resolved** در [PR #109](https://github.com/ILIV007/IVAI-App/pull/109) |
| `ObsoleteSdkInt` launcher policy | 0 | minSdk 29 adaptive-only policy به `mipmap-anydpi` منتقل شد؛ bitmap fallback غیرقابل‌انتخاب حذف و generator/guard همگام شدند. | **Resolved** در [PR #110](https://github.com/ILIV007/IVAI-App/pull/110) |

> **نتیجهٔ اجرایی:** backlog deterministic lint و compatibility بسته است. هر upgrade بعدی دوباره یک migration مستقل است و نباید با این snapshot به‌صورت خودکار سازگار فرض شود.

## 2. Evidence deterministic آماده

| P0/gate | evidence موجود | وضعیت واقعی |
|---|---|---|
| CI محافظت‌شده | Android quality و Secret scan baseline نهایی پاس شده‌اند.[1] | **Pass برای baseline**؛ برای هر candidate جدید باید تکرار شود. |
| Build quality | debug/release minified، R8، Room/KSP، 160 test، lint صفر و guardهای Provider-neutral/RTL/contrast/Phase 8/R8/launcher پاس شده‌اند. | **Pass deterministic.** |
| Launcher policy | resource table debug APK، `mipmap-anydpi` adaptive XML، monochrome layer، safe-zone checksum و نبود bitmap fallback را تأیید می‌کند. | **Pass static.** این جایگزین مشاهدهٔ launcher روی دستگاه واقعی نیست. |
| Release-candidate deterministic package | آخرین package کنترل‌شده برای source launcher-policy با R8 release، mapping، checksum، manifest و 155 test توسط verifier پاس شد؛ APK unsigned بود. | **Ready / not a release pass.** برای Alpha باید دقیقاً از commit candidate نهایی بازتولید شود. |
| Phase 7.5 research package | protocol و checksum-verifier آماده‌اند؛ worksheet موجود عمداً blank است و package باید برای commit candidate نهایی بازتولید شود. | **Ready / no participant or device result.** |
| Signed/public boundary | هیچ keystore مالک، tag، upload، GitHub Release یا binary عمومی ساخته نشده است. | **Pending / Alpha blocked.** |

## 3. P0های واقعی که هنوز باز هستند

| P0 | evidence لازم | وضعیت در sandbox | شرط بسته‌شدن |
|---|---|---|---|
| Usability/card sort/tree test/safety comprehension | 5–8 جلسه voluntary و de-identified، thresholdهای protocol و retest findingهای P0/P1 | **Pending.** هیچ result ثبت نشده است. | فقط aggregateهای تأییدشده و de-identified طبق protocol ثبت شوند. |
| Heuristic review | دو reviewer مستقل، reconciliation و صفر finding P0/P1 حل‌نشده | **Pending.** | worksheet مستقل و reconciliation واقعی. |
| Device matrix | حداقل یک compact و یک medium Android: install/upgrade/restart/rotation/offline، theme، font scale و launcher | **Pending.** sandbox device/ADB قابل‌استفاده برای این evidence نداشت؛ این عدم دسترسی failure اپ نیست. | نتایج واقعی بدون device ID و بدون دادهٔ حساس ثبت شوند. |
| Force-RTL و TalkBack | screenshot/note غیرحساس، swipe order و explore-by-touch برای کنترل‌های task-critical | **Pending.** | اجرای واقعی روی compact و medium device و ثبت finding/retest. |
| HTTPS loopback/private-LAN | endpoint صریح HTTPS، cancellation، timeout و offline بدون HTTP/discovery/scan/trust bypass | **Pending.** | evidence فیزیکی با endpoint و credential غیرحساس/مصرفی؛ هیچ raw network log یا secret وارد repository نشود. |
| Release signing/provenance | signed APK مالک، SHA-256، tag annotated، release notes، owner approval و independent download/hash | **Pending / Alpha blocked.** | فقط بعد از همهٔ P0/P1های بالا و روی یک commit دقیق. |

## 4. اقدامات بعدی و قواعد پایش

1. Compatibility closure و lint صفر در [Compatibility Research](COMPATIBILITY_RESEARCH_2026-08-19.md) ثبت شده‌اند؛ این نتیجه هیچ P0 فیزیکی را Pass نمی‌کند.
2. **Phase 7.5** فقط با controlled research package و app state پاک اجرا می‌شود. هیچ participant نباید credential وارد کند، Provider request بفرستد یا Agent write انجام دهد.[3]
3. **Device/network evidence** نتیجهٔ واقعی است، نه checklist تیک‌خورده. هر failure P0/P1 باید scenario را متوقف، یک remediation focused ایجاد و سپس با evidence تازه retest کند.
4. **Alpha** تا تکمیل participant/device/accessibility/network، signed artifact، SHA-256، tag، notes و owner approval، **Not approved** باقی می‌ماند.[4]
5. این سند snapshot دستی evidence است، نه claim پایش background. زیرا P0های باقی‌مانده به مشاهده و approval واقعی انسان/دستگاه وابسته‌اند؛ job زمان‌بندی‌شده نمی‌تواند آن‌ها را Pass کند.

## References

[1] [Android quality run for main commit `d604a1c`](https://github.com/ILIV007/IVAI-App/actions/runs/32294048552)

[2] [Compatibility Research](COMPATIBILITY_RESEARCH_2026-08-19.md)

[3] [Phase 7.5 Usability and Heuristic Review Runbook](PHASE7_5_USABILITY_HEURISTIC_RUNBOOK.md)

[4] [GitHub Alpha Release Checklist](ALPHA_RELEASE.md)

[5] [Release Readiness Checklist](RELEASE_READINESS_CHECKLIST.md)
