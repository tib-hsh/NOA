from pymongo import MongoClient
import collections
import json
import codecs
import math
import argparse

argpar = argparse.ArgumentParser()
argpar.add_argument("-mongoIP", type=str, required=True)
argpar.add_argument("-mongoPort", type=int, required=True)
argpar.add_argument("-mongoDB", type=str, required=True)
argpar.add_argument("-imageCollection", type=str, required=True)
args = argpar.parse_args()

#dfs = {}
dfs = collections.Counter()
N = 0

client = MongoClient(args.mongoIP, args.mongoPort)
db = client[args.mongoDB]
collection = db[args.imageCollection]

processed = 0
records = collection.find({})
for r in records:
    processed += 1
    if processed%1000 == 0:
        print(processed, "records processed.")
    N = N+1
    terms = r.get('wpterms',[])
    dfs.update(terms)

        
idfs = {}
max_idf = math.log(float(N-1),2)
for term in dfs:
    df = dfs[term]
    idf = math.log(float(N-df)/float(df),2)
    if idf > 0:
        idfs[term] = idf/max_idf


        
#idfs = dict(sorted(idfs.items(), key=lambda x: x[1], reverse=True))
        
with codecs.open("idf.json", "w","utf-8") as fout:
    fout.write(json.dumps(idfs, indent=4))


