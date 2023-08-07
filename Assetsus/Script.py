import re
import deepl
import time

with open("cedict_ts.u8", encoding="utf8") as file:
    contents = file.read()

#print(contents)
lines = contents.split('\n')
jsonlist = []

i = 0
for line in lines:
    patternus = r'(.+) (.+) \[(.+)\] /(.+)/'
    result = re.match(patternus, line)
    x = re.sub(r'\[.+\]', '', result.groups()[3])
    i += 1
    jsonlist.append(x)

with open("toTranslate.txt", "w", encoding="utf8") as file2:
    file2.writelines(jsonlist)


#for line in jsonlist:
#    print(line)
'''
file1 = open("output.txt", "a", encoding="utf8")
for iter in range(12574, 12574):
    auth_key = "9929945e-f947-82bb-76f4-8253cfc0087d:fx"
    translator = deepl.Translator(auth_key)
    result = translator.translate_text(jsonlist[iter], target_lang="PL")
    file1.write(result.text + '\n')
    print(result.text)
    time.sleep(0.25)

file1.close()
'''