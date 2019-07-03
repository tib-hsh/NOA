import datetime
import configparser

current_date = str(datetime.datetime.now().date())
article_collection = "tmp_articles_" + str(current_date)
image_collection = "tmp_images_" + str(current_date)
solr_collection = "tmp_solr_" + str(current_date)

print("Initializing Pipeline for ", current_date)

config = configparser.ConfigParser()
config.read('config.ini', encoding='utf-8-sig')

# save the last recorded date to start looking for articles from this point
config['DEFAULT']['lastdate'] = config['DEFAULT']['date']
config['DEFAULT']['date'] = current_date
config['DEFAULT']['article_collection'] = article_collection
config['DEFAULT']['image_collection'] = image_collection
config['DEFAULT']['solr_collection'] = solr_collection

with open('config.ini', 'w') as configfile:
    config.write(configfile)
