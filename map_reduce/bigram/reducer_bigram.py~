#!/usr/bin/python

import sys
import string
count = 0

for line in sys.stdin:
    line = line.strip()
    key,values= line.split("\t")
    #values = values.strip()
    key = key.strip()
    #count = 0 	
    #values = values.split(",")
    if current_key == None:
        current_key = key

    if current_key != key:
	print key,'\t',count
	count = 0
	current_key = key

    if current_key == key:
	count+=1
