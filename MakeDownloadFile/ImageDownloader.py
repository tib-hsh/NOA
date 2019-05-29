from pymongo import MongoClient
from bson.objectid import ObjectId
import configparser

config = configparser.ConfigParser()
config.read('config.ini', encoding='utf-8-sig')

mongoIP = config['DEFAULT']['mongoIP']
mongoPort = int(config['DEFAULT']['mongoPort'])
mongoDB = config['DEFAULT']['mongoDB']
article_collection = config['DEFAULT']['article_collection']
image_collection = config['DEFAULT']['image_collection']

client = MongoClient(mongoIP, mongoPort)
db = client[mongoDB]
article_collection = db[article_collection]
img_collection = db[image_collection]

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
        with open("DownloadURLs.txt", 'a', encoding="utf-8") as myfile:
            print(path)
            print(URL)
            extension = URL.split('.')[-1]
            myfile.write(URL + " " + root + path + str(findingID) + "." + extension + "\n")
print("end")
print(imagecount)
print("Image URLs found")
