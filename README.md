# NOA
This repository contains all code from the NOA project (noa.wp.hs-hannover.de). The project collects scientific open access articles, extracts images and corresponding metadata and adds further enriching data. The images can accessed via a search engine using Solr. From there, they can uploaded to Wikimedia Commons.

The code represents the full pipeline of the project which is as following:

1. Download articles from different sources

2. Parse the article content and metadata into a MongoDB collection.

3. Download the images.

4. Add more metadata such as image types and Wikipedia categories.

5. Create the search index.


**Note:** This repository is a work in progress. The code is not complete yet and there may still be errors. More code and changes will be added as the project evolves.


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

## Acronyms
Resolves acronyms in figure captions by searching the article the long version of an acronym. Both the acronym and the long version are added to the image in the database and can be indexed by Solr.

## WikiCats
Builds the base documents for adding Wikipedia categories to the images using a Wikipedia dump. This takes a long time and only has to be done once. It should be repeated from time to time with the newest dump to reflect changes in Wikipedia.

## WikiThes
Adds Wikipedia categories to the images using the documents produced by WikiCats. This is done by extracting terms from the captions and the image context and findings relevant Wikipedia articles with these terms using inverse document frequency. Categories that have a lot of relevant articles will be assigned to the corresponding image.

## addWMIDS
This programs finds all distinct Wikipedia Categories in a MongoDB, writes them into a new database and adds the corresponding categories from Wikimedia Commons as well as Wikidata items. It then adds the Wikimedia Commons categories to the original database. The created database can be deleted. The wikidata items are not added to the original database. This can be done with small changes to the source code.

## addDiscipline
Adds disciplines to articles using the categorization from https://rzblx1.uni-regensburg.de/ezeit/.

## CreateSolrDB
Creates new database collection with all fields needed by the search engine.

## Search Engine
This is the interface for the search engine currently at noa.wp.hs-hannover.de. It includes two ways to upload images to Wikimedia Commons: 1. Using the random upload tool, which will show random images and let the user decide whether to upload or not. 2. Uploading images from the search results. Both function upload images under the user's own account.




## Contributors
Jean Charbonnier, Lucia Sohmen, Frieda Josi, Christian Wartena, Birte Rohden, Janko Happe
