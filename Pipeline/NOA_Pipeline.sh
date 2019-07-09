#!/bin/sh

# replace this with the output from "echo $PATH"
PATH=/home/noa/anaconda3/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games:/snap/bin:/usr/lib/jvm/java-8-oracle/bin:/usr/lib/jvm/java-8-oracle/db/bin:/usr/lib/jvm/java-8-oracle/jre/bin

echo "------InitializePipeline------"
python3 InitializePipeline.py

echo "------DOAJUpdater------"
java -jar DOAJUpdater.jar -mongoCollection "AllArticles"

echo "------DOAJUpdater PMC------"
java -jar DOAJUpdater.jar -mongoCollection "AllArticles" -downloadPMC "true"

echo "------PaperParser------"
java -jar PaperParser.jar -mongoErrorCollection "NOA_Errors" -verbose 2

echo "------addDisciplines------"
java -jar addDiscipline.jar

echo "------Acronyms: find_abbreviations------"
python3 find_abbreviations.py

echo "------Acronyms: cluster_abbrev------"
python3 cluster_abbrev.py

echo "------Acronyms: find_secondary_abbreviations------"
python3 find_secondary_abbreviations.py

echo "------WikiThes: findwikiterms------"
python3 findwikiterms.py

echo "------WikiThes: idf------"
python3 idf.py

echo "------WikiThes: assigncatsDS------"
python3 assigncatsDS.py

echo "------addwmIDS------"
java -jar addwmIDS.jar

echo "------MakeDownloadFile------"
python3 MakeDownloadFile.py

echo "------ImgDownloader------"
java -jar ImgDownloader.jar tmp/DownloadURLs.txt

echo "------CreateSolrDB------"
python3 CreateSolrDB.py

# TODO: ImageClassifier

# We want to first classify images on HSH Server and then store the images on another server. If images are supposed to stay on initial server remove this step
/usr/bin/scp -i /home/noa/.ssh/id_rsa -o 'ProxyJump noa@exchange.osl.tib.eu' -r /home/noa/PipelineTest/images noa@noa21.osl.tib.eu:/noa/pictures

echo "------FinishPipeline------"
python3 FinishPipeline.py

# also remove this step if images are supposed to stay on server
rm -r images
rm -r tmp
