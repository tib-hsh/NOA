# Pipeline
This pipeline executes all programs from the NOA project in order to automate the workflow.

## System Requirements
Java 8  
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
find_abbreviations.py  
cluster_abbrev.py  
find_secondary_abbreviations.py  
findwikiterms.py  
idf.py  
assigncatsDS.py  
addwmIDS.jar  
MakeDownloadFile.py  
ImgDownloader.jar  
CreateSolrDB.py  
FinishPipeline.py  

**Data folder** (path set in config.ini):  
wikihypernym.json  
wikicats.json  
w2v_noa.txt  
translatedDisciplines.txt  
checklines.txt  


## Setup
Download nltk data with:
```console
import nltk  
nltk.download('all')
```

Edit config.ini (The folder paths can be left at their default values.):  
mongoip = \<Your Mongo IP>  
mongoport = \<Your Mongo PORT>  
mongodb = \<Your Mongo DB>  
date = \<initial start date (yyyy-MM-dd); this only needs to be set once>  
data_folder = \<data folder>  
tmp_folder = \<folder for temporary files>  
img_folder = \<folder images are saved in>  
article_folder = <folder articles are saved in; insert "none" to delete articles>  

To automate pipeline execution a crontab can be set up:  
Change PATH in NOA_Pipeline.sh to the result of
```console
echo $PATH
```
for your user.  

Example crontab (executes once a day at 7:00AM and writes all output to pipelog.txt):  
```console
cronjob -e 
0 7 * * *  cd /path/to/pipeline && sh NOA_Pipeline.sh > pipelog.txt 2>&1
```
