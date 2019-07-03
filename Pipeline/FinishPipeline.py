from pymongo import MongoClient
import configparser
import shutil

config = configparser.ConfigParser()
config.read('config.ini', encoding='utf-8-sig')

mongoIP = config['DEFAULT']['mongoIP']
mongoPort = int(config['DEFAULT']['mongoPort'])
mongoDB = config['DEFAULT']['mongoDB']
article_collection = config['DEFAULT']['article_collection']
image_collection = config['DEFAULT']['image_collection']
solr_collection = config['DEFAULT']['solr_collection']
article_folder = config['DEFAULT']['article_folder']
tmp_folder = config['DEFAULT']['tmp_folder']
date = config['DEFAULT']['date']

client = MongoClient(mongoIP, mongoPort)
db = client[mongoDB]

def add_collection(c, t):
    collection = db[c]
    target = db[t]
    for doc in collection.find():
        target.insert(doc)
    # uncomment to automatically remove temporary collections
    # collection.drop()
        
add_collection(article_collection, "AllArticles")
add_collection(image_collection, "AllImages")
add_collection(solr_collection, "AllImagesSolr")

if article_folder != "none":
    shutil.move(tmp_folder + "/DownloadedArticles", article_folder + "/articles_" + date)
    
# name for log file is configured in crontab, if configuration differs change file name here
shutil.move("pipelog.txt", "logs/pipelog_" + date + ".txt")