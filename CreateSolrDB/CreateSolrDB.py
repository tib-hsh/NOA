from pymongo import MongoClient
from bson.objectid import ObjectId
import re
import sys

params={}

if __name__ == "__main__":
    sizeOfArgs = len(sys.argv)
    sizeOfArgs-=1
    if sizeOfArgs == 0 or sizeOfArgs%2!=0:
        raise Exception('Wrong number of arguments')
    for e in range((int)(sizeOfArgs/2)):
        key=sys.argv[e*2+1]
        val=sys.argv[e*2+2]
        if "-" not in key or len(key)<2:
            raise Exception('Error with Param #'+str(e))
        params[key[1:]]=val
    print("Got Params: ", params)


client = MongoClient(params['mongoIP'], int(params['mongoPort']))
db = client[params['mongoDB']]
imageCollection = db[params['imageCollection']]
journalCollection = db[params['articleCollection']]
target_col = db[params['targetCollection']]
imagecount = 0
i_skipped = 0

findings = imageCollection.find({})

for f in findings:
    DOI = f['DOI']
    source = f['sourceID']
    URL = f['URL']
    findingID = f['findingID']
    CAPTION = f['captionBody']

    article = journalCollection.find_one({"_id": ObjectId(source)})
    journalName = str(article['journalName'])
    pathJournalName = str(article['journalName']).replace(' ', '_')
    year = article['year']
    Dumb_DOI = str(DOI).replace('/', '_')
    path2file = article['path2file']
    publisher= ""
    publisher = str(article['publisher'])
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
    path = str(publisher) + '/' + str(pathJournalName) + '/' + str(year) + '/' + str(Dumb_DOI) + '/'
    root = "images/"
    path2 = root + path
    imagecount += 1
    DOI = str(Dumb_DOI).replace('_', '/')
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
    print(DOI)
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
print("end")
print(imagecount, "Images added to", params['targetCollection'])
