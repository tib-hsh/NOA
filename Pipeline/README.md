# Pipeline
This pipeline executes all programs from the NOA project in order to automate the workflow.  
Articles from different sources are downloaded and parsed into MongoDB starting from the last date of execution. The current and last date are set in InitializePipeline.py. Additional metadata such as disciplines and Wikimedia categories are then added. The images from the articles are downloaded and will be classified (work in progress). In our case the images are then moved to another server (this step can be removed in NOA_Pipeline.sh).  
For each execution temporary Mongo collections for articles, images and our Solr search engine are created. At the end in FinishPipeline.py these temporary collections are added to collections AllArticles, AllImages and AllImagesSolr, where all previous data is stored. The temporary collections can then optionally be removed automatically (uncomment line in FinishPipeline.py).
Logs are stored in logs/pipelog_[date].txt. The output file configured in crontab must match the filename in FinishPipeline.py.


## System Requirements
Java 8  
MongoDB  
Python 3  
numpy  (tested with v1.14.3)  
pymongo (tested with v3.4.0)  
nltk (tested with v3.3)  

## Required Files
**Root folder:**  
NOA_Pipeline.sh  
config.ini  
InitializePipeline.py  
DOAJUpdater.jar  
PaperParser.jar  
addDiscipline.jar  
find_abbreviations.py (Acronyms)  
cluster_abbrev.py (Acronyms)  
find_secondary_abbreviations.py (Acronyms)  
findwikiterms.py (WikiThes)  
idf.py (WikiThes)  
assigncatsDS.py (WikiThes)  
addwmIDS.jar  
MakeDownloadFile.py  
ImgDownloader.jar  
CreateSolrDB.py  
FinishPipeline.py

These files are found in this repository in directories of the same name. In some cases the directoriy is listed in brackets after the filename. Jar files need to be build using Maven.

**Data folder** (path set in config.ini):  
wikihypernym.json (created with wikicats/provided soon)  
wikicats.json (created with wikicats/provided soon)  
w2v_noa.txt (provided soon)  
translatedDisciplines.txt (addDiscipline)  
checklines.txt (PaperParser)  


## Setup
1. Download nltk data with:
```console
import nltk  
nltk.download('all')
```

2. Edit config.ini (The folder paths can be left at their default values.):  
mongoip = \<Your Mongo IP>  
mongoport = \<Your Mongo PORT>  
mongodb = \<Your Mongo DB>  
date = \<initial start date (yyyy-MM-dd); this only needs to be set once>  
data_folder = \<data folder>  
tmp_folder = \<folder for temporary files>  
img_folder = \<folder images are saved in>  
article_folder = <folder articles are saved in; insert "none" to delete articles>  

3. To automate pipeline execution a crontab can be set up:  
Change PATH in NOA_Pipeline.sh to the output of:
```console
echo $PATH
``` 

Example crontab (executes once a day at 7:00AM and writes all output to pipelog.txt):  
```console
cronjob -e 
0 7 * * *  cd /path/to/pipeline && sh NOA_Pipeline.sh > pipelog.txt 2>&1
```
