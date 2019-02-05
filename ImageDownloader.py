from pymongo import MongoClient
from bson.objectid import ObjectId

current = 0
client = MongoClient('ENTER MONGO IP', 'PORT')
db = client['NewSchema']
##### Read Collection
article_collection = db['AllArticles']
img_collection = db['AllImages']
imagecount = 0
i_skipped = 0
######
##########
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
        URL = image['URL2Image']
        findingID = image['findingID']
        path2file = f['path2file']
        CAPTION = image['captionBody']
        publisher = ""
        publisher = str(f['publisher'])
        if "Hindawi" in path2file:
            publisher = "Hindawi"
        elif "Springer" in path2file:
            publisher = "Springer"
        elif "PubMed" in path2file:
            publisher = "PubMed"
        elif "PMC" in path2file:
            publisher = "PubMed"
        elif "Copernicus" in publisher:
            publisher = "Copernicus"
        elif "frontiers" in path2file:
            publisher = "Frontiers"
        elif "Frontiers" in path2file:
            publisher = "Frontiers"
        path = publisher + '/' + pathJournalName + '/' + year + '/' + Dumb_DOI + '/'
        root = "images/"
        path2 = root + path
        imagecount += 1
    with open("D:\Image_Files_URLS_betaCorpus_vtest1.txt", 'a', encoding="utf-8") as myfile:
        print(path)
        print(URL)
        myfile.write(URL + " " + root + path + str(findingID) + ".jpg" + "\n")
print("end")
print(imagecount)
print("Image URLs found")
