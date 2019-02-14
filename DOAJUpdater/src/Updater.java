import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.json.*;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class Updater {

	static Map<String, List<String>> newArticles = new HashMap<String, List<String>>();
	static List<String> doisToSkip;
	static MongoCollection<Document> collection;
	
	public static void main(String[] args)
			throws JSONException, MalformedURLException, IOException, ParseException, InterruptedException {
		
		//Dates in Format yyyy-MM-dd
		String fromDate = "2019-02-10";
		String untilDate = "2019-02-12";

		// Set true to search for and download articles from PMC instead of DOAJ
		boolean downloadPMC = false;

		doisToSkip = new ArrayList<String>();
		String filename = "DOIsToSkip.txt";
		File f = new File(filename);
		if (f.exists() && !f.isDirectory()) {
			doisToSkip = FileUtils.readLines(f, "utf-8");
		}

		if (downloadPMC) {
			PMCDownloader.downloadArticles(fromDate, untilDate);
		} else {
			Properties properties = new Properties();
	        BufferedInputStream stream = new BufferedInputStream(new FileInputStream("DOAJUpdater.properties"));
	        properties.load(stream);
	        stream.close();
			MongoClient mongoClient = new MongoClient(new MongoClientURI(properties.getProperty("mongoURI")));
			MongoDatabase database = mongoClient.getDatabase(properties.getProperty("dbName"));
			collection = database.getCollection("AllArticles");
				
			new File("DownloadedArticles/Copernicus").mkdirs();
			new File("DownloadedArticles/Hindawi").mkdirs();
			new File("DownloadedArticles/Springer").mkdirs();
			new File("DownloadedArticles/Frontiers").mkdirs();
			new File("NewArticleDOIs").mkdirs();

			//get first page of articles for specified time frame
			String xmlResponse = IOUtils.toString(new URL("https://www.doaj.org/oai.article?verb=ListRecords&metadataPrefix=oai_dc&from=" + fromDate + "&until=" + untilDate), Charset.forName("UTF-8"));
			JSONObject json = XML.toJSONObject(xmlResponse);
			JSONObject oaipmh = json.getJSONObject("OAI-PMH");
			JSONObject listRecords = oaipmh.getJSONObject("ListRecords");
			JSONObject tokenArray = listRecords.getJSONObject("resumptionToken");
			String token = tokenArray.getString("content");
			int cursor = tokenArray.getInt("cursor");
			int listSize = tokenArray.getInt("completeListSize");
			getDOIs(listRecords);
					
			//while not at the end of the list fetch next page
			while (cursor < listSize) { 				
				xmlResponse = IOUtils.toString(new URL("https://www.doaj.org/oai.article?verb=ListRecords&resumptionToken=" + token), Charset.forName("UTF-8"));
				json = XML.toJSONObject(xmlResponse);
				oaipmh = json.getJSONObject("OAI-PMH");
				listRecords = oaipmh.getJSONObject("ListRecords");
				tokenArray = listRecords.getJSONObject("resumptionToken");
				token = tokenArray.getString("content");
				cursor = tokenArray.getInt("cursor");
				getDOIs(listRecords);
				System.out.println(cursor + "/" + listSize);
			}
			
			mongoClient.close();
			
			for (Map.Entry<String, List<String>> entry : newArticles.entrySet()) {
				FileWriter writer = new FileWriter("NewArticleDOIs/" + entry.getKey() + ".txt");
				for (String str : entry.getValue()) {
					writer.write(str + System.lineSeparator());
				}
				writer.close();

				if (entry.getKey().equals("Frontiers Media S.A.")) {
					System.out.println("Downloading articles from " + entry.getKey());
					for (String str : entry.getValue()) {
						FrontiersDownloader.downloadArticle(str);
					}
				} else if (entry.getKey().equals("Hindawi Limited")) {
					System.out.println("Downloading articles from " + entry.getKey());
					for (String str : entry.getValue()) {
						HindawiDownloader.downloadArticle(str);
					}
				} else if (entry.getKey().equals("SpringerOpen")) {
					System.out.println("Downloading articles from " + entry.getKey());
					for (String str : entry.getValue()) {
						SpringerDownloader.downloadArticle(str);
					}
				} else if (entry.getKey().equals("Copernicus Publications")) {
					System.out.println("Downloading articles from " + entry.getKey());
					for (String str : entry.getValue()) {
						CopernicusDownloader.downloadArticle(str);
					}
				}
			}
		}
	}

	// checks if URL exists
	public static boolean exists(String URLName) {
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(URLName).openConnection();
			return (con.getResponseCode() == 200);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	//adds DOIs from Hindawi, Frontiers, Copernicus and Springer to newArticles. DOIs that are already in MongoDB are skipped
	public static void getDOIs(JSONObject listRecords) {
		JSONArray records = listRecords.getJSONArray("record");
		for(Object r : records) {
			JSONObject record = (JSONObject)r;
			JSONObject metadata = record.getJSONObject("metadata");
			JSONObject oai_dc = metadata.getJSONObject("oai_dc:dc");
			String publisher = oai_dc.getString("dc:publisher");
			if(publisher.equals("Hindawi Limited") || publisher.equals("Frontiers Media S.A.") || publisher.equals("Copernicus Publications") || publisher.equals("SpringerOpen")) {		
				List<Object> identifiers = oai_dc.getJSONArray("dc:identifier").toList();
				for(Object id : identifiers) {
					String identifier = (String)id;
					//check if identifier is a DOI
					if(identifier.startsWith("10") && identifier.contains("/")) {
						String doi = identifier;
						if(!doisToSkip.contains(doi)) {
							BasicDBObject query = new BasicDBObject("DOI", doi);
							FindIterable<Document> fi = collection.find(query);
							MongoCursor<Document> cursor = fi.iterator();
							if(cursor.hasNext()) {
								continue;
							}
							newArticles.computeIfAbsent(publisher, v -> new ArrayList<>()).add(doi);
						}			
						break;
					}
				}
				
			}
		}
	}
}
