
import sys
for line in sys.stdin:
	key = ""
	values = ""
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
                        print cleanWord,'\t',1


