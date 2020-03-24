#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import pyphen
import functools
import sys

input = sys.argv[1]

dic = pyphen.Pyphen(lang = 'hu_HU')

sortedPrefixLengths = sorted(map(lambda prefix: len(prefix), map(lambda pair: pair[0], dic.iterate(input))))
sortedPrefixLengths.insert(0, 0)
sortedPrefixLengths.append(len(input))

substrings = []
for index, position in enumerate(sortedPrefixLengths[:-1]):
    startPosition = sortedPrefixLengths[index]
    endPosition = sortedPrefixLengths[index + 1]
    substring = input[startPosition : endPosition]
    substrings.append(substring)

result = functools.reduce(lambda result, substring: result + '|' + substring, substrings)
print("Result: {0}".format(result))
