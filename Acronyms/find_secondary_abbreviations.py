import time
from nltk.tokenize import WhitespaceTokenizer
from pymongo import MongoClient
tokenizer = WhitespaceTokenizer()
import json


## Christian's server section
client = MongoClient('mongodb://141.71.5.22:27017/')
#db = client.migration
#collection = db.Corpus2b
db = client.NewSchema
db_images = db.AllImages
db_articles = db.AllArticles

total_time = time.time()
start = time.time()

##########################################################################################
#           build the cluster dictionary
##########################################################################################
print('Stage 1')
with open("abbreviations_clust.json") as data_file:
    abrclust = json.loads(data_file.read())
normalform = {}
for abr in abrclust:
     for cluster in abrclust[abr]:
         if not isinstance(cluster, str):
             for definition in cluster[1:]:
                 if definition in normalform:
                     print('conflict: ' + definition + ' ' + cluster[0] + ' ' + normalform[definition])
                 normalform[definition] = cluster[0]
                 #print(definition + ' --> '  +  cluster[0])


##########################################################################################
#           build the dictionary from the query
##########################################################################################
print('Stage 2')

# only grab instances where an abrev is found
has_abbreviation = db_images.find({'acronym':{'$exists': 'true'}})

#journal name is its key
dict_of_journals = {}


def get_journalname(paper_id):
    title = ""
    papers = db_articles.find({"_id": paper_id })
    for paper in papers:
        title = paper['title']
        break
    return title
    

for img in has_abbreviation:
    journal_name = get_journalname(img['source_id']) 
    journalabbr = dict_of_journals.get(journal_name,{})   
    if 'acronym' in img:
        for abbreviation in img['acronym']:
            acro = abbreviation[0]
            definition = abbreviation[1]
            if definition == None:
                continue
            #print(acro,definition)
            if definition in normalform:
                definition = normalform[definition]     
            defs = journalabbr.get(acro,[])
            if definition not in defs:
               defs.append(definition)
            journalabbr[acro] = defs
        dict_of_journals[journal_name] =  journalabbr


#####################################################
# updating mongodb with all results
#####################################################
print('Stage 3')

has_abbreviation = db_images.find({'acronym':{'$exists': 'true'}})
updates = 0
for img in has_abbreviation:
    if 'acronym' in img:
        i = 0
        for abbreviation in img['acronym']:
            acro = abbreviation[0]
            definition = abbreviation[1]
            if definition == None:
                journal_name = get_journalname(img['source_id'])
                if  acro in dict_of_journals[journal_name]:
                    journaldef = dict_of_journals[journal_name][acro]
                    if len(journaldef) == 1:
                        updates += 1
                        #print(paper['DOI'],findingID,acro,journaldef[0])
                        db_images.update(img,{'$set': {'acronym.' + str(i): (acro,journaldef[0])}},upsert=False, multi=False)
        i += 1

print("Definitions found: ", updates)
print("total time (with mongodb update):", time.time() - total_time)