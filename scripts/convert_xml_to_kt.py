#!/usr/bin/env python3
# -*- encoding: utf-8 -*-
#
# This script converts the resource strings in Android XML format to Kotlin files for Lyricist.
#
# Input files are expected to be inside the l10n folder in root of the project and are expected to
# be named as follows
#   l10n/values-xxyy/strings.xml
# where xx is the language code and yy is the country code.
#
# Output files are saved in
#   core/l10n/src/commonMain/kotlin/com/github/diegoberaldin/raccoonforlemmy/core/l10n/messages
# and will have the XxYyStrings.kt naming scheme.
#
# Usage:
# python3 convert_xml_to_kt.py <lang_code> <country_code>
#

import sys
from bs4 import BeautifulSoup
import re

def read_l10n_from_file(input_path):
    res = []
    with open(input_path) as file_handle:
        soup = BeautifulSoup(file_handle, "xml")
        elements = soup.find_all("string")
        for element in elements:
            k = element["name"]
            v = unescape(element.string)
            res.append({"key": k, "value": v})
    return res

def unescape(str_xml):
    str_xml = re.sub(r"\n", "", str_xml)
    str_xml = str_xml.replace("&amp;", "&")
    str_xml = str_xml.replace("&lt;", "<")
    str_xml = str_xml.replace("&gt;", ">")
    return str_xml

def write_l10n_to_file(lang_code, country_code, messages, output_path):
    with open(output_path, "w") as file_handle:
        file_handle.write("package com.github.diegoberaldin.raccoonforlemmy.core.l10n.messages\n")
        file_handle.write("\n")
        if lang_code == "en" and len(country_code) == 0:
            file_handle.write("internal open class DefaultStrings : Strings {\n")
            for pair in messages:
                file_handle.write("    override val {0} = \"{1}\"\n".format(pair["key"], pair["value"]))
            file_handle.write("}\n")
        else:
            file_handle.write("internal val {0}{1}Strings =\n".format(lang_code.capitalize(), country_code.capitalize()))
            file_handle.write("    object : DefaultStrings() {\n")
            for pair in messages:
                file_handle.write("        override val {0} = \"{1}\"\n".format(pair["key"], pair["value"]))
            file_handle.write("    }\n")

def convert(lang_code, country_code, input_path, output_path):
    messages = read_l10n_from_file(input_path)
    write_l10n_to_file(lang_code, country_code, messages, output_path)

def main():
    if len(sys.argv) < 2:
        print("Usage: {0} lang_code country_code".format(sys.argv[0]))
        return
    lang_code = sys.argv[1]
    country_code = ""
    region_code = ""
    if len(sys.argv) > 2:
        country_code = sys.argv[2]
    source_file = "../l10n/values-{0}{1}/strings.xml".format(lang_code, country_code)
    if lang_code == "en" and len(country_code) == 0:
        dest_file = "../core/l10n/src/commonMain/kotlin/com/github/diegoberaldin/raccoonforlemmy/core/l10n/messages/DefaultStrings.kt"
    else:
        dest_file = "../core/l10n/src/commonMain/kotlin/com/github/diegoberaldin/raccoonforlemmy/core/l10n/messages/{0}{1}Strings.kt".format(lang_code.capitalize(), country_code.capitalize())
    convert(lang_code, country_code, source_file, dest_file)

if __name__ == "__main__":
    main()
