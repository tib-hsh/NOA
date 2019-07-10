from pymongo import MongoClient
import configparser
import json
from bson.objectid import ObjectId

config = configparser.ConfigParser()
config.read('config.ini', encoding='utf-8-sig')

mongoIP = config['DEFAULT']['mongoIP']
mongoPort = int(config['DEFAULT']['mongoPort'])
mongoDB = config['DEFAULT']['mongoDB']
article_collection = "AllArticles"
image_collection = "AllImages"
solr_collection = "AllImagesSolr"

client = MongoClient(mongoIP, mongoPort)
db = client[mongoDB]
articles = db[article_collection]
images = db[image_collection]
solr = db[solr_collection]

# group by DOI
cursor = articles.aggregate( 
            [
                {
                    "$group": { 
                        "_id": {"DOI": "$DOI"}, 
                        "count":{"$sum": 1}
                    }
                }
            ]

        )

article_count = 0
for group in cursor:
    # if count > 1 there are duplicates
    if group['count'] > 1:
        duplicates = articles.find({"DOI": group['_id']['DOI']})
        # skip first article
        duplicates.skip(1)
        # delete remaining duplicates and corresponding images
        for d in duplicates:
            id = d['_id']
            articles.delete_one({"_id": ObjectId(id)})
            article_count += 1
            images.delete_many({"sourceID": ObjectId(id)})

print("Deleted " + str(article_count) + " duplicates from AllArticles")
     
     
# do the same thing with Solr DB, except with TIB_URL as unique key
cursor = solr.aggregate( 
            [
                {
                    "$group": { 
                        "_id": {"TIB_URL": "$TIB_URL"}, 
                        "count":{"$sum": 1}
                    }
                }
            ]

        )
        
solr_count = 0
for group in cursor:
    if group['count'] > 1:
        duplicates = solr.find({"TIB_URL": group['_id']['TIB_URL']})
        duplicates.skip(1)
        for d in duplicates:
            id = d['_id']
            solr.delete_one({"_id": ObjectId(id)})
            solr_count += 1
print("Deleted " + str(solr_count) + " duplicates from AllImagesSolr")
