#!/usr/bin/python

import sys
for line in sys.stdin:
	key = ""
	values = ""
	question_words_list = list()
	splits = line.split("\t")
        question = splits[0]
        category = splits[1]
        question_words = question.split()
        for word in question_words:
                word = word.strip().lower()
                cleanWord = ""
                for char in word:
                        if char in '+=_-!@#$%^&*(){}{}[]<>/,.?":;|\~0123456789':
                                char = ""
                        cleanWord += char
                cleanWord=cleanWord.replace("'","")
                if len(cleanWord) != 0:
			question_words_list.append(cleanWord)
                for i in range(len(question_words_list)-1):
			bigram = question_words_list[i]+"$"+question_words_list[i+1]
			print bigram,'\t',1



