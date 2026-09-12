# MMD skill reference

Canonical format: [MMD_Format.MD](../../mind-map/MMD_Format.MD) (parser/writer rules plus Appendix A emoticon ids).

The Python scripts reimplement `mind-map-model` (`MindMap`, `Topic.parse`, `ModelUtils`). They do not call the JVM.

## Commands

| Script | Role |
|--------|------|
| `scripts/parse.py FILE` | `.mmd` → JSON tree on stdout |
| `scripts/write.py --in TREE.json --out FILE.mmd` | JSON tree → canonical `.mmd` |
| `scripts/validate.py FILE` | structural checks + parse/write/parse; prints `OK` or errors |
| `scripts/crypto.py list FILE` | encrypted notes in a `.mmd` or JSON tree (path, hint, ciphertext) |
| `scripts/crypto.py decrypt --password-stdin --text CIPHER` | ciphertext → plaintext (password on stdin) |
| `scripts/crypto.py encrypt --password-stdin --text PLAIN` | plaintext → ciphertext (password on stdin) |

Exit status `0` on success, `1` on format, I/O, or decrypt errors.

Password: ask the user, then pass via stdin (`--password-stdin` with `--text` / `--file`) or env `MMD_NOTE_PASSWORD`. Do not put the password on the command line. The editor trims surrounding whitespace; the CLI does too.

Crypto matches Swing `CryptoUtils` as specified in [MMD_Format.MD — Encrypted notes](../../mind-map/MMD_Format.MD#encrypted-notes): AES-256-ECB PKCS5, key = SHA-256(UTF-8 password), payload = SHA-256(UTF-8 text) || text, Base64. Empty password on encrypt leaves the text unchanged. Wrong password prints `wrong password or invalid ciphertext`.

## What the writer canonicalizes

- Header title `[Scia Reto](https://sciareto.org) mind map` plus a Markdown hard break (three spaces)
- Map attribute `__version__=`1.1``
- Sorted attribute names, extras in `FILE` / `LINK` / `NOTE` / `TOPIC` order, snippets sorted by language
- Title escaping (`<br/>`, backslash specials) and `<pre>` HTML escaping

Byte-for-byte identity with an older file is not required. `validate.py` compares trees after a canonical rewrite.

## Emoticons

Topic attribute `mmd.emoticon` is a case-sensitive id from Appendix A of `MMD_Format.MD` (407 catalog ids such as `acorn`, `skull_old`, `www`). Unknown ids are stored but not drawn by the Swing panel. Do not write `empty`; omit the attribute instead.
