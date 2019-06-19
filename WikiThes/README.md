# WikiThes
Adds Wikipedia categories to image collection.  

mongoIP, mongoPort, mongoDB, image_colection, data_folder and tmp_folder need to be specified in config.ini.  

Run in the following order:  
1. `python findwikiterms.py`  
2. `python idf.py`  
3. `python assigncatsDS.py` 
