import re

with open("outputtest.txt", encoding="utf8") as file:
    contentsPL = file.read()

with open("dictTest.txt", encoding="utf8") as file1:
    contentsEng = file1.read()

contentsPL = contentsPL.split('\n')
contentsEng = contentsEng.split('\n')

contents = []
for i in range(12574):
    x = re.sub(r'/.+/', '/'+contentsPL[i]+'/', contentsEng[i])
    contents.append(x)


file2 = open("resultTest.txt", "w", encoding="utf8")
for c in contents:
    file2.write(c + '\n')
file2.close()
