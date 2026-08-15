# P4-01 — Provider Registry Foundation

## Goal

IVAI را از یک chat با adapter ثابت Gemini به هارنس provider-neutral منتقل کن؛ کاربر باید بتواند connectionها، accountها، endpointها، مدل‌های دستی، capabilityها و referenceهای vault را به‌صورت محلی مدیریت کند. این task فقط foundation داده و domain است؛ هنوز Router fallback یا UI کامل نمی‌سازد.

## Context files to read

- `AGENTS.md`
- `docs/ROADMAP.md`
- `docs/PROVIDER_HARNESS_ALIGNMENT.md`
- `docs/SECURITY.md`
- `app/src/main/java/dev/iliv007/ivai/provider/ProviderContract.kt`
- `app/src/main/java/dev/iliv007/ivai/data/local/WorkspaceEntities.kt`
- `app/src/main/java/dev/iliv007/ivai/security/EncryptedSecretVault.kt`

## In scope

1. Room entities, DAOها و repository برای ProviderConnection، ProviderAccount و ModelDefinition.
2. Provider kindهای `CUSTOM_OPENAI_COMPATIBLE`، `OPENROUTER` و `GEMINI`.
3. endpointِ کاربر-مدیریت‌شده با URL validation، privacy metadata و active state.
4. credential reference مجزا از ciphertext؛ plaintext هرگز وارد Room، UI state، log، trace یا export نشود.
5. manual model ID و capability metadata.
6. migration امن DB و repository tests.
7. registry یا resolver که adapterها را بر اساس provider kind پیدا کند؛ runtime نباید Gemini را hard-wire کند.

## Out of scope

- تماس زنده با API واقعی یا secret واقعی.
- Combo fallback، retry، circuit breaker، usage trace یا tool calling runtime.
- UI کامل Provider Management.
- localhost/http endpoint؛ سیاست endpoint محلی فقط با threat model و UX هشدار در task جدا.
- Agent tool execution، Drive backup، Local model، MCP، Voice و Automation.

## Constraints

- Local-first، Backendless و BYOK غیرقابل‌مذاکره‌اند.
- Custom endpoint در این task فقط `https` و host معتبر می‌پذیرد.
- Adapter هیچ plaintext credential دریافت نمی‌کند.
- Gemini نخستین adapter نمونه است؛ هیچ default provider یا model در persistence اعمال نشود.
- شناسه‌های پایداری که Combo و Agent بعداً به آن‌ها ارجاع می‌دهند قابل‌rename/delete policy باشند.
- migration بدون destructive fallback؛ test migration الزامی است.

## Acceptance Criteria

| ID | معیار |
|---|---|
| AC-1 | کاربر بتواند رکورد محلی جدا برای Gemini، OpenRouter و Custom OpenAI-compatible بسازد؛ هیچ‌کدام بدون انتخاب کاربر default نیستند. |
| AC-2 | account فقط `credentialReference` دارد؛ جست‌وجوی Room/export/log برای plaintext secret نتیجه‌ای ندارد. |
| AC-3 | endpoint نامعتبر، غیر HTTPS، username/password در URL، fragment و host محلی رد می‌شوند. |
| AC-4 | manual model ID و capabilityها قابل ثبت و بازخوانی هستند. |
| AC-5 | registry از `ProviderKind` به adapter ثبت‌شده نگاشت می‌شود و runtime Gemini را ثابت نمی‌سازد. |
| AC-6 | migration، repository، validation و redaction tests سبز هستند. |
| AC-7 | `assembleDebug`، `testDebugUnitTest`، `lintDebug` و secret scan سبزند. |

## Commands to run

```bash
./gradlew clean assembleDebug testDebugUnitTest lintDebug --no-daemon
```

## Expected artifacts

- schema v2 Room و migration test؛
- provider registry/repository و unit tests؛
- decision/update در `docs/PROVIDER_HARNESS_ALIGNMENT.md`؛
- PR متمرکز با scope و security review.

## Risks

- migration داده‌های chat محلی؛
- validation ناکافی endpoint سفارشی؛
- اتصال accidental مستقیم UI به vault؛
- defaultهای پنهان Gemini؛
- نام‌گذاری ناپایدار connection/model برای Comboهای بعدی.

## Handoff report

شمارهٔ schema، migration coverage، موارد رد‌شدهٔ URL، تعداد تست، خروجی build/lint و هر deviation از این task را ثبت کن.
