from pymongo import MongoClient
import collections
import json
import codecs
import pprint
import math
import numpy as np
import nltk
import configparser

config = configparser.ConfigParser()
config.read('config.ini', encoding='utf-8-sig')

mongoIP = config['DEFAULT']['mongoIP']
mongoPort = int(config['DEFAULT']['mongoPort'])
mongoDB = config['DEFAULT']['mongoDB']
image_collection = config['DEFAULT']['image_collection']
data_folder = config['DEFAULT']['data_folder']
tmp_folder = config['DEFAULT']['tmp_folder']

w2vec = {}
w2vdim = 0

"""
concr = {}
with open("concreteness.csv") as fin:
    fin.readline()
    for line in fin:
        fields = line.split('\t')
        w = fields[0]
        c = float(fields[2])
        if c > 0:
            concr[w] = c  
"""

with open(data_folder + "/w2v_noa.txt") as fin:
    for line in fin:
        w,v = line.split('\t')
        v = np.array(list(map(float,v.split())))
        #v = v / np.linalg.norm(v)
        w2vec[w] = v
        if w2vdim == 0:
            w2vdim = len(v)


with open(data_folder + "/wikicats.json") as fin:
    wikicats = json.loads(fin.read(),encoding="utf-8")
    
wikicats = {a.lower():wikicats[a] for a in wikicats}

    
with open(tmp_folder + "/idf.json") as fin:
    idf = json.loads(fin.read(),encoding="utf-8")

with open(data_folder + "/wikihypernym.json") as fin:
    hyper = json.loads(fin.read(),encoding="utf-8")    
    

client = MongoClient(mongoIP, mongoPort)
db = client[mongoDB]
collection = db[image_collection]

def aggregated_vector(text):
    matches = 0
    vector = np.zeros(w2vdim)
    words = nltk.word_tokenize(text)
    for w in words:
        if w in w2vec:
            vector = vector + w2vec[w]
            matches += 1
    if matches > 0:
        return vector/np.linalg.norm(vector)
    else:
        return vector
        

def intersection(a,b):
    i = 0
    for e in a:
        if e in b:
            i += 1
    return i

def most_specific(ts,n):
    #idfvals = [(t,idf.get(t,10) * concr.get(t,3.5)) for t in ts if idf.get(t,10) > 9.8]
    idfvals = [(t, idf.get(t,1.0)) for t in ts if idf.get(t,1.0) > 0.2]
    idfsorted =  sorted(idfvals, key=lambda x: x[1], reverse=True)
    #print(idfsorted[:n])
    best = [t for t,idf in idfsorted[:n]]
    return best
    
def best_fit(ts,text,n):
    capt_vector = aggregated_vector(text)
    simvals = [(t,np.dot(capt_vector,aggregated_vector(t).T)) for t in ts]
    simsorted =  sorted(simvals, key=lambda x: x[1], reverse=True)
    #print(simsorted[:n])
    best = [t for t,idf in simsorted[:n]]
    return best


    
def hypercats(c):
    result = set()
    result.add(c)
    
    hc = hyper.get(c,[])    
    result.update(hc)
   
    
    #if len(result) < 5:
    for h in hc:
        if h in hyper:
            hhc = hyper[h]
            if len(hhc) < 5:
                result.update(hhc)
        
    
    return list(result)
    
    
def categories(terms,caption):
    if len(terms) > 15:
        terms = most_specific(terms,15)
       
    if len(caption) > 2:
        terms = best_fit(terms,caption,5)
    else:
        terms = most_specific(terms,5)
   
    cats_per_term = {}
            
    for t in terms:
        t_cats = wikicats.get(t,[])
        cats_per_term[t] = {}
        for c in t_cats:
             cats_per_term[t][c] = hypercats(c)
    
    #pprint.pprint(cats_per_term)
    cats = {}
    for t in cats_per_term:
        for c in cats_per_term[t]:
            nrOfRel = 0
            for t1 in cats_per_term:
                if t1 == t:
                    continue
                for c1 in cats_per_term[t1]:
                    if intersection(cats_per_term[t1][c1],cats_per_term[t][c]) > 0:
                        #print(c,'+',c1)
                        nrOfRel += 1
            cats[c] = max(nrOfRel,cats.get(c,0))
    sortedcats =  sorted(cats.items(),key = lambda x:x[1],reverse=True)
    #print(sortedcats[:5])
    if len(sortedcats) > 0 and sortedcats[0][1] > 0:
        return [cat for cat,rn in sortedcats if rn > 0]
    else:
        return [cat for cat,rn in sortedcats]

records = collection.find({})
processed = 0
i = 0
for f in records:
    processed += 1
    if processed % 1000 == 0:
        print(processed, "records processed.")

    findingID = f['findingID']
    wikiterms = f.get('wpterms',[])
    caption = f['captionBody']
    if caption == None:
        caption = ""

    if wikiterms == None or len(wikiterms) == 0:
        continue
    i += 1
    #print(i)
    wpcats = categories(wikiterms,caption)[:5]

    #collection.update({'_id': f['_id']},{'$unset': {'wpcats': 1 }}, multi=True)
    collection.update({'_id': f['_id']},{'$set':   {'wpcats': wpcats}}, upsert=False, multi=False)
