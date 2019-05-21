# CreateSolrDB
Creates new database collection with all fields needed by the search engine.

Run using the following parameters:  
`python CreateSolrDB.py -mongoIP [IP] -mongoPort [Port] -mongoDB [DB] -imageCollection [imageCollection] -articleCollection [articleCollection] -targetCollection [targetCollection]`

The following field names are used:  
journalName  
year  
DOI  
title  
authors  
URL  
TIB_URL  
captionBody  
captionTitle  
copyrightFlag  
license  
licenseType  
discipline  
imageType  
publicationDate  
acronym  
wpcats  
wmcat  
