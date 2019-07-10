mkdir Build
mkdir Build/data

cd ../addDiscipline && mvn install
cd .. && mv addDiscipline/target/addDiscipline-1.0-SNAPSHOT-jar-with-dependencies.jar Pipeline/Build/addDiscipline.jar

cd addwmIDS && mvn install
cd .. && mv addwmIDS/target/addwmIDS-1.0-SNAPSHOT-jar-with-dependencies.jar Pipeline/Build/addwmIDS.jar

cd DOAJUpdater && mvn install
cd .. && mv DOAJUpdater/target/DOAJUpdater-0.0.1-SNAPSHOT-jar-with-dependencies.jar Pipeline/Build/DOAJUpdater.jar

cd ImgDownloader && mvn install
cd .. && mv ImgDownloader/target/ImgDownloader-1.0-SNAPSHOT-jar-with-dependencies.jar Pipeline/Build/ImgDownloader.jar

cd PaperParser && mvn install
cd .. && mv PaperParser/target/PaperParser-1.0-SNAPSHOT-jar-with-dependencies.jar Pipeline/Build/PaperParser.jar

cp PaperParser/checklines.txt Pipeline/Build/data/checklines.txt
cp Pipeline/NOA_Pipeline.sh Pipeline/Build/NOA_Pipeline.sh
cp Pipeline/config.ini Pipeline/Build/config.ini
cp Pipeline/InitializePipeline.py Pipeline/Build/InitializePipeline.py
cp Pipeline/FinishPipeline.py Pipeline/Build/FinishPipeline.py
cp Acronyms/find_abbreviations.py Pipeline/Build/find_abbreviations.py
cp Acronyms/cluster_abbrev.py Pipeline/Build/cluster_abbrev.py
cp Acronyms/find_secondary_abbreviations.py Pipeline/Build/find_secondary_abbreviations.py
cp WikiThes/findwikiterms.py Pipeline/Build/findwikiterms.py
cp WikiThes/idf.py Pipeline/Build/idf.py
cp WikiThes/assigncatsDS.py Pipeline/Build/assigncatsDS.py
cp MakeDownloadFile/MakeDownloadFile.py Pipeline/Build/MakeDownloadFile.py
cp CreateSolrDB/CreateSolrDB.py Pipeline/Build/CreateSolrDB.py

cd Pipeline/Build 
wget https://github.com/tib-hsh/NOA/releases/download/v0.1/data.tar.gz
tar -xvzf data.tar.gz
rm data.tar.gz