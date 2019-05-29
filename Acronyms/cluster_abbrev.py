from pymongo import MongoClient
import json
import codecs
import sys
import configparser

config = configparser.ConfigParser()
config.read('config.ini', encoding='utf-8-sig')

mongoIP = config['DEFAULT']['mongoIP']
mongoPort = int(config['DEFAULT']['mongoPort'])
mongoDB = config['DEFAULT']['mongoDB']
image_collection = config['DEFAULT']['image_collection']

client = MongoClient(mongoIP, mongoPort)
db = client[mongoDB]
db_images = db[image_collection]

abbreviations = {}

has_abbreviation = db_images.find({'acronym':{'$exists': 'true'}})

for img in has_abbreviation:
    if 'acronym' in img:
        for abbreviation in img['acronym']:
            acro = abbreviation[0]
            definition = abbreviation[1]
            if definition == None:
                continue
            defs = abbreviations.get(acro,[])
            if definition not in defs:
                defs.append(definition)
                abbreviations[acro] = defs

nrOfAbbr = len(abbreviations)
nrOfUnamb = len([ab for ab in abbreviations if len(abbreviations[ab]) == 1])
nrOfExpansions = sum([len(abbreviations[ab]) for ab in abbreviations])

print("Number of Abbreviations = ",nrOfAbbr)
print("Number of Unambiguous Abbreviations = ",nrOfUnamb)
print("Average Number of Expansions = ", float(nrOfExpansions)/float(nrOfAbbr))

def substitutionsfehler(b1,b2):
    if b1 == b2:
        return 0
    else:
        return 1

def edit_distance(v, w):
    matrix = [[0 for j in range(len(w) + 1)] for i in range(len(v) + 1)]
    for i in range(len(v)+1):
        for j in range(len(w)+1):
            if i > 0 and j > 0:
                val1 = matrix[i-1][j] + 1
                val2 = matrix[i][j-1] + 1
                val3 = matrix[i-1][j-1] + substitutionsfehler(v[i-1],w[j-1]) 
                matrix[i][j] = min(val1, val2, val3)
            elif i > 0:
                matrix[i][j] = matrix[i-1][j] + 1
            elif j > 0:
                matrix[i][j] = matrix[i][j-1] + 1
            else:
                matrix[i][j] = 0 #Die erste Zelle
    return matrix[len(v)][len(w)]
    
    
ext_abr = {}

def case_ins_same(d1,d2):
    if d1.lower() == d2.lower():
        return True 
    else:
        return False
    
def different_order(d1,d2):
    # abdominal aortic aneurysms  <==>  Aortic abdominal aneurysms
    # Might not be the same but almost indistinguishable for the search engine
    d1w = d1.lower().split()
    d2w = d2.lower().split()
    same = True
    for w in d1w:
        if w not in d2w:
            same = False
            break
    return same

def ed_dist_sum(d1,d2):
    ed = 0
    d1w = d1.lower().split()
    d2w = d2.lower().split()
    if len(d1w) != len(d2w):
        return len(d1)
    for i in range(len(d1w)):
        ed += edit_distance(d1w[i],d2w[i])
    return ed
        
    
def similar(d1,d2):
    if case_ins_same(d1,d2):
        return True 
    elif different_order(d1,d2):
        #print(d1," <==> ",d2)
        return True 
    elif ed_dist_sum(d1,d2) < 5:
        #print(d1," <==> ",d2)
        return True
    
    return False

def allsimilar(defs,definition):
    sim = True
    for d in defs:
        if not similar(d,definition):
            sim = False
            break
    return sim
    

for abr in abbreviations:
    expansions = abbreviations[abr]
    groups = [[expansions[0]]]
    for exp in expansions[1:]:
        found = False
        for group in groups:
            if allsimilar(group,exp):
                found = True
                group.append(exp)
                break
        if not found:
            groups.append([exp])
    ext_abr[abr] = groups
    
    
nrOfUnamb = len([ab for ab in ext_abr if len(ext_abr[ab]) == 1])
nrOfExpansions = sum([len(ext_abr[ab]) for ab in ext_abr])

print("Number of Unambiguous Abbreviations = ",nrOfUnamb)
print("Average Number of Expansions = ", float(nrOfExpansions)/float(nrOfAbbr))


data_file = codecs.open('abbreviations_clust.json','w','utf-8')    
json.dump(ext_abr,data_file)
data_file.close()
