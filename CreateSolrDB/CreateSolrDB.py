from pymongo import MongoClient
from bson.objectid import ObjectId
import re
import sys
import configparser

config = configparser.ConfigParser()
config.read('config.ini', encoding='utf-8-sig')

mongoIP = config['DEFAULT']['mongoIP']
mongoPort = int(config['DEFAULT']['mongoPort'])
mongoDB = config['DEFAULT']['mongoDB']
article_collection = config['DEFAULT']['article_collection']
image_collection = config['DEFAULT']['image_collection']
solr_collection = config['DEFAULT']['solr_collection']


client = MongoClient(mongoIP, mongoPort)
db = client[mongoDB]
imageCollection = db[image_collection]
articleCollection = db[article_collection]
target_col = db[solr_collection]
imagecount = 0
i_skipped = 0

findings = imageCollection.find({})

for f in findings:
    DOI = f['DOI']
    source = f['sourceID']
    URL = f['URL']
    findingID = f['findingID']
    CAPTION = f['captionBody']

    article = articleCollection.find_one({"_id": ObjectId(source)})
    journalName = str(article['journalName'])
    pathJournalName = str(article['journalName']).replace(' ', '_')
    year = article['year']
    Dumb_DOI = str(DOI).replace('/', '_')
    publisher = str(article['source'])
    path = str(publisher) + '/' + str(pathJournalName) + '/' + str(year) + '/' + str(Dumb_DOI) + '/'
    root = "images/"
    path2 = root + path
    imagecount += 1
    DOI = str(Dumb_DOI).replace('_', '/')
    try:
        document = {"journalName": journalName,
                    "year": year,
                    "DOI": DOI,
                    "title": article['title'],
                    "authors": article['authors'],
                    "URL": f['URL'],
                    "TIB_URL": root + path + str(findingID) + ".jpg",
                    "captionBody": f['captionBody'],
                    "captionTitle": f['captionTitle'],
                    "copyrightFlag": f['copyrightFlag'],
                    "license": article['license'],
                    "licenseType": f['licenseType'],
                    "discipline": article['discipline'],
                    "imageType": f['imageType'],
                    "publicationDate": article['publicationDate']
                    }
        if 'acronym' in f:
            document['acronym'] = f['acronym']
        if 'wpcats' in f:
            document['wpcats'] = f['wpcats']
        if 'wmcat' in f:
            document['wmcat'] = f['wmcat']
        if 'DownloadError:' not in f:
            target_col.insert_one(document)
        else:
            i_skipped+=1
            print("Error skipped #"+str(i_skipped))
    except:
        print("An exception ocurred with image: " + URL)
print("end")
print(imagecount, "Images added to", solr_collection)
