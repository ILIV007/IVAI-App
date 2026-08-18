#!/usr/bin/env bash
# Enforces the Phase 7 policy: app shells and prose remain locale-directed.
# Narrow LTR is allowed only for the two message footers and code renderers,
# where technical tokens/timestamps/code need stable directionality.
set -Eeuo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

pattern='CompositionLocalProvider\(LocalLayoutDirection provides LayoutDirection\.Ltr\)'
bidi_file='app/src/main/java/dev/iliv007/ivai/ui/components/BidiMessageView.kt'
markdown_file='app/src/main/java/dev/iliv007/ivai/ui/components/MarkdownRenderer.kt'

[[ -f "$bidi_file" ]] || { echo "RTL guard failed: missing $bidi_file" >&2; exit 1; }
[[ -f "$markdown_file" ]] || { echo "RTL guard failed: missing $markdown_file" >&2; exit 1; }

bidi_result="$(awk '
  /^[[:space:]]*fun TerminalCodeBlock\(/ { in_terminal = 1 }
  in_terminal && /^[[:space:]]*fun [[:alnum:]_]+\(/ && $0 !~ /fun TerminalCodeBlock\(/ { in_terminal = 0 }
  /User Bubble Footer|AI Bubble Footer/ { in_footer = 1 }
  /CompositionLocalProvider\(LocalLayoutDirection provides LayoutDirection\.Ltr\)/ {
    count++
    if (!(in_footer || in_terminal)) {
      printf "unexpected:%d\n", NR
      bad = 1
    }
    in_footer = 0
  }
  END {
    if (count != 3) {
      printf "count:%d\n", count
      bad = 1
    }
    exit bad
  }
' "$bidi_file")" || {
  printf '%s\n' "$bidi_result" >&2
  echo "RTL guard failed: BidiMessageView LTR must remain limited to message footers or TerminalCodeBlock." >&2
  exit 1
}

markdown_result="$(awk '
  /^[[:space:]]*fun MarkdownCodeBlockView\(/ { in_code_block = 1 }
  in_code_block && /^[[:space:]]*fun [[:alnum:]_]+\(/ && $0 !~ /fun MarkdownCodeBlockView\(/ { in_code_block = 0 }
  /CompositionLocalProvider\(LocalLayoutDirection provides LayoutDirection\.Ltr\)/ {
    count++
    if (!in_code_block) {
      printf "unexpected:%d\n", NR
      bad = 1
    }
  }
  END {
    if (count != 1) {
      printf "count:%d\n", count
      bad = 1
    }
    exit bad
  }
' "$markdown_file")" || {
  printf '%s\n' "$markdown_result" >&2
  echo "RTL guard failed: MarkdownRenderer LTR must remain limited to MarkdownCodeBlockView." >&2
  exit 1
}

total="$(git grep -n -E "$pattern" -- app/src/main/java | wc -l | tr -d '[:space:]')"
[[ "$total" == 4 ]] || {
  git grep -n -E "$pattern" -- app/src/main/java >&2 || true
  echo "RTL guard failed: expected exactly four bounded LTR providers, found $total." >&2
  exit 1
}

printf '%s\n' 'PASS: Bidi message footer LTR exceptions are bounded'
printf '%s\n' 'PASS: code-renderer LTR exceptions are bounded'
printf '%s\n' 'PASS: no global or unreviewed production forced-LTR provider exists'
