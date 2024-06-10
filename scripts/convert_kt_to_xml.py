#!/usr/bin/env python3
# -*- encoding: utf-8 -*-

import sys

def extract_pairs(input_path):
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


def write_pairs_to_file(messages, output_path):
    with open(output_path, "w") as file_handle:
        file_handle.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        file_handle.write("<resources>\n")
        for pair in messages:
            file_handle.write("    <string name=\"{0}\">{1}</string>\n".format(pair["key"], pair["value"]))
        file_handle.write("</resources>\n")


def convert(input_path, output_path):
    messages =extract_pairs(input_path)
    write_pairs_to_file(messages, output_path)


def main():
    if len(sys.argv) < 2:
        print("Usage: {0} lang".format(sys.argv[0]))
        return
    lang = sys.argv[1]
    source_file = "../core/l10n/src/commonMain/kotlin/com/github/diegoberaldin/raccoonforlemmy/core/l10n/messages/{0}Strings.kt".format(lang.capitalize())
    dest_file = "strings_{0}.xml".format(lang)
    convert(source_file, dest_file)


if __name__ == "__main__":
    main()