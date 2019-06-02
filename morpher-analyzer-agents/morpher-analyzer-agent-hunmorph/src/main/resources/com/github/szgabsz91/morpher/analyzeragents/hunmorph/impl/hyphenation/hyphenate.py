#!/usr/bin/env python
# -*- coding: utf-8 -*-

import pyphen
import sys

input = unicode(sys.argv[1], 'UTF-8')

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

result = reduce(lambda result, substring: result + '|' + substring, substrings)
print "Result: {0}".format(result.encode('UTF-8'))
