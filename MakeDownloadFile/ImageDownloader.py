from pymongo import MongoClient
from bson.objectid import ObjectId
import argparse

argpar = argparse.ArgumentParser()
argpar.add_argument("-mongoIP", type=str, required=True)
argpar.add_argument("-mongoPort", type=int, required=True)
argpar.add_argument("-mongoDB", type=str, required=True)
argpar.add_argument("-imageCollection", type=str, required=True)
argpar.add_argument("-articleCollection", type=str, required=True)
argpar.add_argument("-filename", type=str, required=True)
args = argpar.parse_args()

client = MongoClient(args.mongoIP, args.mongoPort)
db = client[args.mongoDB]
article_collection = db[args.articleCollection]
img_collection = db[args.imageCollection]

imagecount = 0
i_skipped = 0

findings = article_collection.find({"numOfFindings": {"$gt": 0}}, no_cursor_timeout=True)

for f in findings:
    journalName = str(f['journalName'])
    pathJournalName = str(f['journalName']).replace(' ', '_')
    year = f['year']
    DOI = f['DOI']
    Dumb_DOI = str(DOI).replace('/', '_')
    for j in f['findingsRef']:
        id = j
        image = img_collection.find_one({'_id': ObjectId(j)})
        if image is None:
            continue
        URL = image['URL']
        findingID = image['findingID']
        path2file = f['path2file']
        publisher = str(f['source'])
        path = publisher + '/' + pathJournalName + '/' + year + '/' + Dumb_DOI + '/'
        root = "images/"
        imagecount += 1
        with open(args.filename, 'a', encoding="utf-8") as myfile:
            print(path)
            print(URL)
            extension = URL.split('.')[-1]
            myfile.write(URL + " " + root + path + str(findingID) + "." + extension + "\n")
print("end")
print(imagecount)
print("Image URLs found")
