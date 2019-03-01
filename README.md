# NOA
This repository contains all code from the NOA project (noa.wp.hs-hannover.de). More documentation will be added soon.

## Add Wikimedia Categories
This programs finds all distinct Wikipedia Categories in a MongoDB, writes them into a new database and adds the corresponding categories from Wikimedia Commons as well as Wikidata items. It then adds the Wikimedia Commons categories to the original database. The created database can be deleted. The wikidata items are not added to the original database. This can be done with small changes to the source code.

## DOAJUpdater
Searches for new articles on DOAJ (Directory of Open Access Journals) in specified time frame, saves new DOIs in a textfile and downloads articles from Copernicus, Frontiers, Hindawi, Springer.

## Paper Parser
Parses articles in XML format (PMC Jats-XML or Springer). Output is a MongoDB article collection with the following fields:
journalName, journalVolume, journalIssue, pages
DOI
year
authors, numOfAuthors
abstract, abstractLength
body, bodyLength
license, licenseType
publisher
keywords, discipline
Bibliography
PublicationDate
numOfFindings (images), findingsRef (reference to image collection)

As well as an image collection with the following fields:
originDOI, source_id
captionTitle, captionBody, captionBodyLength
URL2Image
context


## MakeDownloadFile
Produces a download file from the MongoDB image database that can be use by ImgDownloader.

## ImgDownloader
Downloads images from different sources in multiple threads. Input is a file with lines structured like this: [image source] [image destination]

## NOA-Plumber
Work in progress. This will be the pipeline that integrates all the programs above.

## Search Engine
This is the interface for the search engine currently at noa.wp.hs-hannover.de. It includes two ways to upload images to Wikimedia Commons: 1. Using the random upload tool, which will show random images and let the user decide whether to upload or not. 2. Uploading images from the search results. Both function upload images under the user's own account.

## Wikipedia Categories
Adds categories from Wikipedia to images.

## addWMIDs
Adds the corresponding items from Wikidata and categories from Wikimedia Commons to the Wikipedia Categories.

## addDiscipline
Adds disciplines to articles using the categorization from https://rzblx1.uni-regensburg.de/ezeit/.



## Contributors
Jean Charbonnier, Lucia Sohmen, Frieda Josi, Christian Wartena, Birte Rohden, Janko Happe
