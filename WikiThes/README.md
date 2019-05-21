# WikiThes
Adds Wikipedia categories to image collection.

Run using the following order and arguments:  
1. `python findwikiterms.py -mongoIP [IP] -mongoPort [Port] -mongoDB [DB] -imageCollection [imageCollection]`  
2. `python idf.py -mongoIP [IP] -mongoPort [Port] -mongoDB [DB] -imageCollection [imageCollection]`  
3. `python assigncatsDS.py -mongoIP [IP] -mongoPort [Port] -mongoDB [DB] -imageCollection [imageCollection]` 
