#!/usr/bin/env sh

echo "$1" | iconv -f utf-8 -t iso-8859-2 | ocamorph --bin /usr/local/bin/morphdb_hu.bin
