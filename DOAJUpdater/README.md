# DOAJUpdater
Searches for new articles on DOAJ (Directory of Open Access Journals) in specified time frame, saves new DOIs in a textfile and downloads articles from Copernicus, Frontiers, Hindawi, Springer.

## How to use
The program searches for all new articles on DOAJ using OAI-PMH.
Use arguments -fromDate and -untilDate to set the time frame you want to search in (yyyy-MM-dd). By default only both dates are set to the current date.
To check for and download new articles published on PMC instead of DOAJ set downloadPMC to true.
The Programm also skips articles which are already in MongoDB.
When running the program, DOIs of new articles are saved to NewArticleDOIs/[publisher_name].txt and all articles published by Copernicus, Hindawi, Frontiers and Springer are downloaded to DownloadedArticles/[publisher_name]/[doi].xml. For Frontiers the URLs to images from the article are written into the xml.