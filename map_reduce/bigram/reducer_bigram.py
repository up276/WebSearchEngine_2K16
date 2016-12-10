#!/usr/bin/python

import sys
import string
count = 0

for line in sys.stdin:
    line = line.strip()
    key,values= line.split("\t")
    key = key.strip()
    if current_key == None:
        current_key = key

    if current_key != key:
	print key,'\t',count
	count = 0
	current_key = key

    if current_key == key:
	count+=1
