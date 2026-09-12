#!/usr/bin/env python3
"""Encrypt and decrypt MMD notes the same way as Swing CryptoUtils.

AES/ECB/PKCS5Padding, key = SHA-256(UTF-8 password), payload = SHA-256(text) || text,
then Java-compatible Base64. Empty password leaves the text unchanged (encrypt only).
"""

from __future__ import annotations

import argparse
import base64
import hashlib
import json
import os
import sys
from typing import Any, Dict, List, Optional, Tuple

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import mmd  # noqa: E402

ATTR_ENCRYPTED = "extras.note.encrypted"
ATTR_HINT = "extras.note.encrypted.hint"
PASSWORD_ENV = "MMD_NOTE_PASSWORD"


class DecryptError(ValueError):
    """Wrong password or ciphertext that cannot be decoded."""


_SBOX = bytes(
    (
        0x63, 0x7C, 0x77, 0x7B, 0xF2, 0x6B, 0x6F, 0xC5, 0x30, 0x01, 0x67, 0x2B, 0xFE, 0xD7, 0xAB, 0x76,
        0xCA, 0x82, 0xC9, 0x7D, 0xFA, 0x59, 0x47, 0xF0, 0xAD, 0xD4, 0xA2, 0xAF, 0x9C, 0xA4, 0x72, 0xC0,
        0xB7, 0xFD, 0x93, 0x26, 0x36, 0x3F, 0xF7, 0xCC, 0x34, 0xA5, 0xE5, 0xF1, 0x71, 0xD8, 0x31, 0x15,
        0x04, 0xC7, 0x23, 0xC3, 0x18, 0x96, 0x05, 0x9A, 0x07, 0x12, 0x80, 0xE2, 0xEB, 0x27, 0xB2, 0x75,
        0x09, 0x83, 0x2C, 0x1A, 0x1B, 0x6E, 0x5A, 0xA0, 0x52, 0x3B, 0xD6, 0xB3, 0x29, 0xE3, 0x2F, 0x84,
        0x53, 0xD1, 0x00, 0xED, 0x20, 0xFC, 0xB1, 0x5B, 0x6A, 0xCB, 0xBE, 0x39, 0x4A, 0x4C, 0x58, 0xCF,
        0xD0, 0xEF, 0xAA, 0xFB, 0x43, 0x4D, 0x33, 0x85, 0x45, 0xF9, 0x02, 0x7F, 0x50, 0x3C, 0x9F, 0xA8,
        0x51, 0xA3, 0x40, 0x8F, 0x92, 0x9D, 0x38, 0xF5, 0xBC, 0xB6, 0xDA, 0x21, 0x10, 0xFF, 0xF3, 0xD2,
        0xCD, 0x0C, 0x13, 0xEC, 0x5F, 0x97, 0x44, 0x17, 0xC4, 0xA7, 0x7E, 0x3D, 0x64, 0x5D, 0x19, 0x73,
        0x60, 0x81, 0x4F, 0xDC, 0x22, 0x2A, 0x90, 0x88, 0x46, 0xEE, 0xB8, 0x14, 0xDE, 0x5E, 0x0B, 0xDB,
        0xE0, 0x32, 0x3A, 0x0A, 0x49, 0x06, 0x24, 0x5C, 0xC2, 0xD3, 0xAC, 0x62, 0x91, 0x95, 0xE4, 0x79,
        0xE7, 0xC8, 0x37, 0x6D, 0x8D, 0xD5, 0x4E, 0xA9, 0x6C, 0x56, 0xF4, 0xEA, 0x65, 0x7A, 0xAE, 0x08,
        0xBA, 0x78, 0x25, 0x2E, 0x1C, 0xA6, 0xB4, 0xC6, 0xE8, 0xDD, 0x74, 0x1F, 0x4B, 0xBD, 0x8B, 0x8A,
        0x70, 0x3E, 0xB5, 0x66, 0x48, 0x03, 0xF6, 0x0E, 0x61, 0x35, 0x57, 0xB9, 0x86, 0xC1, 0x1D, 0x9E,
        0xE1, 0xF8, 0x98, 0x11, 0x69, 0xD9, 0x8E, 0x94, 0x9B, 0x1E, 0x87, 0xE9, 0xCE, 0x55, 0x28, 0xDF,
        0x8C, 0xA1, 0x89, 0x0D, 0xBF, 0xE6, 0x42, 0x68, 0x41, 0x99, 0x2D, 0x0F, 0xB0, 0x54, 0xBB, 0x16,
    )
)
_INV_SBOX = bytearray(256)
for _index, _value in enumerate(_SBOX):
    _INV_SBOX[_value] = _index
_INV_SBOX = bytes(_INV_SBOX)


def _xtime(value: int) -> int:
    return ((value << 1) ^ 0x1B) & 0xFF if value & 0x80 else (value << 1) & 0xFF


def _gf_mul(left: int, right: int) -> int:
    result = 0
    for _ in range(8):
        if right & 1:
            result ^= left
        left = _xtime(left)
        right >>= 1
    return result


def _expand_key(key: bytes) -> List[bytes]:
    words = [key[index : index + 4] for index in range(0, 32, 4)]
    rcon = 1
    for index in range(8, 60):
        temp = bytearray(words[index - 1])
        if index % 8 == 0:
            temp = temp[1:] + temp[:1]
            temp = bytearray(_SBOX[byte] for byte in temp)
            temp[0] ^= rcon
            rcon = _xtime(rcon)
        elif index % 8 == 4:
            temp = bytearray(_SBOX[byte] for byte in temp)
        words.append(bytes(left ^ right for left, right in zip(words[index - 8], temp)))
    return [b"".join(words[round_index * 4 : round_index * 4 + 4]) for round_index in range(15)]


def _add_round_key(state: bytes, round_key: bytes) -> bytes:
    return bytes(left ^ right for left, right in zip(state, round_key))


def _sub_bytes(state: bytes, table: bytes) -> bytes:
    return bytes(table[byte] for byte in state)


def _shift_rows(state: bytes) -> bytes:
    return bytes(
        (
            state[0],
            state[5],
            state[10],
            state[15],
            state[4],
            state[9],
            state[14],
            state[3],
            state[8],
            state[13],
            state[2],
            state[7],
            state[12],
            state[1],
            state[6],
            state[11],
        )
    )


def _inv_shift_rows(state: bytes) -> bytes:
    return bytes(
        (
            state[0],
            state[13],
            state[10],
            state[7],
            state[4],
            state[1],
            state[14],
            state[11],
            state[8],
            state[5],
            state[2],
            state[15],
            state[12],
            state[9],
            state[6],
            state[3],
        )
    )


def _mix_column(column: bytearray, coeffs: Tuple[int, int, int, int]) -> None:
    values = list(column)
    column[0] = (
        _gf_mul(values[0], coeffs[0])
        ^ _gf_mul(values[1], coeffs[1])
        ^ _gf_mul(values[2], coeffs[2])
        ^ _gf_mul(values[3], coeffs[3])
    )
    column[1] = (
        _gf_mul(values[0], coeffs[3])
        ^ _gf_mul(values[1], coeffs[0])
        ^ _gf_mul(values[2], coeffs[1])
        ^ _gf_mul(values[3], coeffs[2])
    )
    column[2] = (
        _gf_mul(values[0], coeffs[2])
        ^ _gf_mul(values[1], coeffs[3])
        ^ _gf_mul(values[2], coeffs[0])
        ^ _gf_mul(values[3], coeffs[1])
    )
    column[3] = (
        _gf_mul(values[0], coeffs[1])
        ^ _gf_mul(values[1], coeffs[2])
        ^ _gf_mul(values[2], coeffs[3])
        ^ _gf_mul(values[3], coeffs[0])
    )


def _mix_columns(state: bytes, inverse: bool) -> bytes:
    coeffs = (0x0E, 0x0B, 0x0D, 0x09) if inverse else (0x02, 0x03, 0x01, 0x01)
    mixed = bytearray(state)
    for column_index in range(4):
        offset = column_index * 4
        column = mixed[offset : offset + 4]
        _mix_column(column, coeffs)
        mixed[offset : offset + 4] = column
    return bytes(mixed)


def aes256_encrypt_block(block: bytes, round_keys: List[bytes]) -> bytes:
    state = _add_round_key(block, round_keys[0])
    for round_index in range(1, 14):
        state = _sub_bytes(state, _SBOX)
        state = _shift_rows(state)
        state = _mix_columns(state, inverse=False)
        state = _add_round_key(state, round_keys[round_index])
    state = _sub_bytes(state, _SBOX)
    state = _shift_rows(state)
    return _add_round_key(state, round_keys[14])


def aes256_decrypt_block(block: bytes, round_keys: List[bytes]) -> bytes:
    state = _add_round_key(block, round_keys[14])
    state = _inv_shift_rows(state)
    state = _sub_bytes(state, _INV_SBOX)
    for round_index in range(13, 0, -1):
        state = _add_round_key(state, round_keys[round_index])
        state = _mix_columns(state, inverse=True)
        state = _inv_shift_rows(state)
        state = _sub_bytes(state, _INV_SBOX)
    return _add_round_key(state, round_keys[0])


def _pkcs7_pad(data: bytes) -> bytes:
    pad = 16 - (len(data) % 16)
    return data + bytes([pad] * pad)


def _pkcs7_unpad(data: bytes) -> bytes:
    if not data or len(data) % 16:
        raise DecryptError("wrong password or invalid ciphertext")
    pad = data[-1]
    if pad < 1 or pad > 16 or data[-pad:] != bytes([pad] * pad):
        raise DecryptError("wrong password or invalid ciphertext")
    return data[:-pad]


def aes256_ecb_encrypt(key: bytes, data: bytes) -> bytes:
    round_keys = _expand_key(key)
    padded = _pkcs7_pad(data)
    return b"".join(
        aes256_encrypt_block(padded[index : index + 16], round_keys)
        for index in range(0, len(padded), 16)
    )


def aes256_ecb_decrypt(key: bytes, data: bytes) -> bytes:
    if len(data) % 16:
        raise DecryptError("wrong password or invalid ciphertext")
    round_keys = _expand_key(key)
    plain = b"".join(
        aes256_decrypt_block(data[index : index + 16], round_keys)
        for index in range(0, len(data), 16)
    )
    return _pkcs7_unpad(plain)


def sha256(data: bytes) -> bytes:
    return hashlib.sha256(data).digest()


def encrypt(password: str, text: str) -> str:
    if password is None or password == "":
        return text
    text_bytes = text.encode("utf-8")
    payload = sha256(text_bytes) + text_bytes
    key = sha256(password.encode("utf-8"))
    encoded = aes256_ecb_encrypt(key, payload)
    if encoded == payload:
        raise RuntimeError("Data can't be encrypted! Check encryption provider and settings!")
    return base64.b64encode(encoded).decode("ascii")


def decrypt(password: str, text: str) -> str:
    try:
        decoded = base64.b64decode(text, validate=True)
    except (ValueError, TypeError) as error:
        raise DecryptError("invalid ciphertext") from error
    try:
        decrypted = aes256_ecb_decrypt(sha256(password.encode("utf-8")), decoded)
    except DecryptError:
        raise
    if len(decrypted) < 32:
        raise DecryptError("wrong password or invalid ciphertext")
    digest = decrypted[:32]
    body = decrypted[32:]
    if digest != sha256(body):
        raise DecryptError("wrong password or invalid ciphertext")
    return body.decode("utf-8")


def is_encrypted_flag(value: Optional[str]) -> bool:
    return value is not None and value.lower() == "true"


def find_encrypted_notes(
    topic: Optional[mmd.Topic], path: Optional[List[str]] = None
) -> List[Dict[str, Any]]:
    if topic is None:
        return []
    current_path = (path or []) + [topic.text]
    found: List[Dict[str, Any]] = []
    if is_encrypted_flag(topic.attributes.get(ATTR_ENCRYPTED)) and "NOTE" in topic.extras:
        hint = topic.attributes.get(ATTR_HINT, "")
        found.append(
            {
                "path": current_path,
                "hint": hint if hint is not None else "",
                "ciphertext": topic.extras["NOTE"],
            }
        )
    for child in topic.children:
        found.extend(find_encrypted_notes(child, current_path))
    return found


def load_map(path: str) -> mmd.MindMap:
    with open(path, "r", encoding="utf-8", newline="") as handle:
        text = handle.read()
    stripped = text.lstrip()
    if path.endswith(".json") or stripped.startswith("{"):
        return mmd.loads_tree(text)
    return mmd.parse_mmd(text)


def _read_payload(args: argparse.Namespace, strip_text: bool) -> str:
    if args.text is not None:
        return args.text.strip() if strip_text else args.text
    if args.file is not None:
        with open(args.file, "r", encoding="utf-8") as handle:
            payload = handle.read()
        return payload.strip() if strip_text else payload
    payload = sys.stdin.read()
    return payload.strip() if strip_text else payload


def _read_password(args: argparse.Namespace) -> str:
    if args.password is not None:
        return args.password.strip()
    env_password = os.environ.get(PASSWORD_ENV)
    if env_password is not None:
        return env_password.strip()
    if args.password_stdin:
        return sys.stdin.readline().rstrip("\r\n").strip()
    raise SystemExit(
        "password required: ask the user, then pass it via %s or --password-stdin" % PASSWORD_ENV
    )


def _command_list(args: argparse.Namespace) -> int:
    try:
        mind_map = load_map(args.path)
    except (OSError, ValueError, mmd.MmdError) as error:
        sys.stderr.write("%s\n" % error)
        return 1
    notes = find_encrypted_notes(mind_map.root)
    sys.stdout.write(json.dumps(notes, ensure_ascii=False, indent=2) + "\n")
    return 0


def _command_decrypt(args: argparse.Namespace) -> int:
    try:
        password = _read_password(args)
        ciphertext = _read_payload(args, strip_text=True)
        sys.stdout.write(decrypt(password, ciphertext))
        return 0
    except DecryptError as error:
        sys.stderr.write("%s\n" % error)
        return 1
    except (OSError, ValueError) as error:
        sys.stderr.write("%s\n" % error)
        return 1


def _command_encrypt(args: argparse.Namespace) -> int:
    try:
        password = _read_password(args)
        plaintext = _read_payload(args, strip_text=False)
        sys.stdout.write(encrypt(password, plaintext))
        return 0
    except (OSError, ValueError, RuntimeError) as error:
        sys.stderr.write("%s\n" % error)
        return 1


def main() -> int:
    parser = argparse.ArgumentParser(description="Encrypt or decrypt Scia Reto MMD notes")
    sub = parser.add_subparsers(dest="command", required=True)

    list_parser = sub.add_parser("list", help="list encrypted notes in a .mmd or JSON tree")
    list_parser.add_argument("path", help="path to a .mmd or JSON tree file")
    list_parser.set_defaults(func=_command_list)

    decrypt_parser = sub.add_parser("decrypt", help="decrypt a note ciphertext to plaintext")
    decrypt_parser.add_argument("--text", help="Base64 ciphertext")
    decrypt_parser.add_argument("--file", help="file containing Base64 ciphertext")
    decrypt_parser.add_argument("--password", help=argparse.SUPPRESS)
    decrypt_parser.add_argument(
        "--password-stdin",
        action="store_true",
        help="read the password from the first line of stdin (use with --text or --file)",
    )
    decrypt_parser.set_defaults(func=_command_decrypt)

    encrypt_parser = sub.add_parser("encrypt", help="encrypt plaintext to a note ciphertext")
    encrypt_parser.add_argument("--text", help="plaintext")
    encrypt_parser.add_argument("--file", help="file containing plaintext")
    encrypt_parser.add_argument("--password", help=argparse.SUPPRESS)
    encrypt_parser.add_argument(
        "--password-stdin",
        action="store_true",
        help="read the password from the first line of stdin (use with --text or --file)",
    )
    encrypt_parser.set_defaults(func=_command_encrypt)

    args = parser.parse_args()
    if args.command in ("decrypt", "encrypt"):
        if args.password_stdin and args.text is None and args.file is None:
            sys.stderr.write("use --text or --file when reading the password from stdin\n")
            return 1
    return args.func(args)


if __name__ == "__main__":
    sys.exit(main())
