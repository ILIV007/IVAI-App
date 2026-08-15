# RTL and BiDi Contract

## Scope

The current app shell is English and LTR. User and model content can be Persian, Arabic, Hebrew, Urdu, English, or mixed text. Content direction must be resolved at the paragraph or block level instead of forcing the entire app into one direction.

## Required behavior

| Content | Direction rule |
|---|---|
| App chrome, navigation, settings labels | LTR until full localization is explicitly scheduled |
| User and assistant prose | Content-based BiDi direction |
| Code fences, inline code, paths, URLs, model IDs, tokens, timestamps, numbers | Explicit LTR isolation |
| Markdown tables | Preserve per-cell readable direction; do not reverse column order implicitly |
| Composer | Content-based input direction; cursor, selection and paste must work for mixed text |

## Test matrix

Every change to message rendering, markdown, composer, navigation, or typography must be checked with English, Persian, Arabic, Hebrew, Urdu, mixed Persian/English text, inline code, URLs, file paths, model IDs, and numeric/token strings. Visual tests must cover both light and dark themes when a theme-affecting component changes.

## Non-goals

This document does not authorize full UI localization or an RTL app shell. Those require a separate product decision and localization plan.
