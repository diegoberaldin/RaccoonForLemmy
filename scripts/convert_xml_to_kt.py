#!/usr/bin/env python3
# -*- encoding: utf-8 -*-

import sys
from bs4 import BeautifulSoup

def read_l10n_from_file(input_path):
    res = []
    with open(input_path) as file_handle:
        soup = BeautifulSoup(file_handle, "xml")
        elements = soup.find_all("string")
        for element in elements:
            k = element["name"]
            v = element.string
            res.append({"key": k, "value": v})
    return res

def write_l10n_to_file(lang, messages, output_path):
    with open(output_path, "w") as file_handle:
        file_handle.write("package com.github.diegoberaldin.raccoonforlemmy.core.l10n.messages\n")
        file_handle.write("\n")
        file_handle.write("internal val {0}Strings =\n".format(lang.capitalize()))
        file_handle.write("    object : Strings {\n")
        for pair in messages:
            file_handle.write("        override val {0} = \"{1}\"\n".format(pair["key"], pair["value"]))
        file_handle.write("    }\n")

def convert(lang, input_path, output_path):
    messages = read_l10n_from_file(input_path)
    write_l10n_to_file(lang, messages, output_path)

def main():
    if len(sys.argv) < 2:
        print("Usage: {0} lang".format(sys.argv[0]))
        return
    lang = sys.argv[1]
    source_file = "../l10n/strings_{0}.xml".format(lang)
    dest_file = "../core/l10n/src/commonMain/kotlin/com/github/diegoberaldin/raccoonforlemmy/core/l10n/messages/{0}Strings.kt".format(lang.capitalize())
    convert(lang, source_file, dest_file)

if __name__ == "__main__":
    main()
