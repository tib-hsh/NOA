# DOAJUpdater
Searches for new articles on DOAJ (Directory of Open Access Journals) in specified time frame, saves new DOIs in a textfile and downloads articles from Copernicus, Frontiers, Hindawi, Springer.

## How to use
The program searches for all new articles on DOAJ using OAI-PMH.
In Updater.java change fromDate and stopDate to the time frame you want to search in (yyyy-MM-dd).
To check for and download new articles published on PMC instead of DOAJ set downloadPMC to true.
To skip certain articles place a .txt file with a list of the DOIs to be skipped in the project folder and name it "DOIsToSkip.txt" or change the variable filename in Updater.java.
The Programm also skips articles which are already in MongoDB.
When running the program, DOIs of new articles are saved to NewArticleDOIs/[publisher_name].txt and all articles published by Copernicus, Hindawi and Springer are downloaded to DownloadedArticles/[publisher_name]/[doi].xml. For Frontiers a folder for each article is created and in addition to the .xml images from the article are downloaded.