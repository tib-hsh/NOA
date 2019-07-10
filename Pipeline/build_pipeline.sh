mkdir PipelineBuild

cd ../addDiscipline && mvn install
cd .. && mv addDiscipline/target/addDiscipline-1.0-SNAPSHOT-jar-with-dependencies.jar PipelineBuild/addDiscipline.jar

cd addwmIDS && mvn install
cd .. && mv addwmIDS/target/addwmIDS-1.0-SNAPSHOT-jar-with-dependencies.jar PipelineBuild/addwmIDS.jar

cd DOAJUpdater && mvn install
cd .. && mv DOAJUpdater/target/DOAJUpdater-0.0.1-SNAPSHOT-jar-with-dependencies.jar PipelineBuild/DOAJUpdater.jar

cd ImgDownloader && mvn install
cd .. && mv ImgDownloader/target/ImgDownloader-1.0-SNAPSHOT-jar-with-dependencies.jar PipelineBuild/ImgDownloader.jar

cd PaperParser && mvn install
cd .. && mv PaperParser/target/PaperParser-1.0-SNAPSHOT-jar-with-dependencies.jar PipelineBuild/PaperParser.jar

cp PaperParser/checklines.txt PipelineBuild/data/checklines.txt
cp Pipeline/NOA_Pipeline.sh PipelineBuild/NOA_Pipeline.sh
cp Pipeline/config.ini PipelineBuild/config.ini
cp Pipeline/InitializePipeline.py PipelineBuild/InitializePipeline.py
cp Pipeline/FinishPipeline.py PipelineBuild/FinishPipeline.py
cp Acronyms/find_abbreviations.py PipelineBuild/find_abbreviations.py
cp Acronyms/cluster_abbrev.py PipelineBuild/cluster_abbrev.py
cp Acronyms/find_secondary_abbreviations.py PipelineBuild/find_secondary_abbreviations.py
cp WikiThes/findwikiterms.py PipelineBuild/findwikiterms.py
cp WikiThes/idf.py PipelineBuild/idf.py
cp WikiThes/assigncatsDS.py PipelineBuild/assigncatsDS.py
cp MakeDownloadFile/MakeDownloadFile.py PipelineBuild/MakeDownloadFile.py
cp CreateSolrDB/CreateSolrDB.py PipelineBuild/CreateSolrDB.py

cd PipelineBuild 
wget https://github.com/tib-hsh/NOA/releases/download/v0.1/data.tar.gz
tar -xvzf data.tar.gz
rm data.tar.gz