#!/usr/bin/env python3
# -*- encoding: utf-8 -*-
#
# This script converts the resource strings from Kotlin files for Lyricist to Android XML format.
#
# Input files are expected to be inside
#   core/l10n/src/commonMain/kotlin/com/github/diegoberaldin/raccoonforlemmy/core/l10n/messages
# and to be named as XxYxStrings.kt where xx is the language code and yy is the country code.
#
# Output files are saved in the l10n directory in the root of the project
#  l10n/values-_xxyy/strings.xml
#
# Usage:
# python3 convert_xml_to_kt.py <lang_code> <country_code>
#

import os
import sys

def read_l10n_from_file(input_path):
    res = []
    with open(input_path) as file_handle:
        lines = [l.strip() for l in file_handle.readlines() if len(l.strip()) > 0]
        for i in range(len(lines)):
            tokens = [t.strip() for t in lines[i].split("=") if len(t.strip()) > 0]
            if len(tokens[0]) <= 13:
                continue
            if len(tokens) == 2:
                k = tokens[0][13:]
                v = tokens[1][1:-1]
                res.append({"key": k, "value": v})
            elif tokens[0].startswith("override val") and i < len(lines) - 1:
                k = tokens[0][13:]
                v = lines[i + 1][1:-1]
                res.append({"key": k, "value": v})
    return res


def escape(str_xml):
    str_xml = str_xml.replace("&", "&amp;")
    str_xml = str_xml.replace("<", "&lt;")
    str_xml = str_xml.replace(">", "&gt;")
    return str_xml


def write_l10n_to_file(messages, output_path):
    with open(output_path, "w") as file_handle:
        file_handle.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        file_handle.write("<resources>\n")
        for pair in messages:
            sanitizedValue = escape(pair["value"])
            file_handle.write("    <string name=\"{0}\">{1}</string>\n".format(pair["key"], sanitizedValue))
        file_handle.write("</resources>\n")


def convert(input_path, output_path):
    messages = read_l10n_from_file(input_path)
    write_l10n_to_file(messages, output_path)


def main():
    if len(sys.argv) < 2:
        print("Usage: {0} lang_code country_code".format(sys.argv[0]))
        return
    lang_code = sys.argv[1]
    country_code = ""
    if len(sys.argv) > 2:
        country_code = sys.argv[2]
    source_file = "../core/l10n/src/commonMain/kotlin/com/github/diegoberaldin/raccoonforlemmy/core/l10n/messages/{0}{1}Strings.kt".format(lang_code.capitalize(), country_code.capitalize())
    dest_file = "../l10n/values-{0}{1}/strings.xml".format(lang_code, country_code)
    if not os.path.exists("../l10n/values-{0}{1}".format(lang_code, country_code)):
        os.makedirs("../l10n/values-{0}{1}".format(lang_code, country_code))
    convert(source_file, dest_file)


if __name__ == "__main__":
    main()
