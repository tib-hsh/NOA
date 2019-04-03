import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private static Map<String, String> arguments = new HashMap<String, String>();
	static String outputFolder = "";
	static String fromDate;
	static String untilDate;
	static String mongoURI;
	static String mongoDB;
	static String mongoCollection;
	static boolean downloadPMC;

	public static void main(String[] args)
			throws IOException {

		// Dates in Format yyyy-MM-dd
		fromDate = java.time.LocalDate.now().toString();
		untilDate = java.time.LocalDate.now().toString();

		readArgs(args);

		if (downloadPMC) {
			PMCDownloader.downloadArticles(fromDate, untilDate, outputFolder);
		} else {
			MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoURI));
			MongoDatabase database = mongoClient.getDatabase(mongoDB);
			collection = database.getCollection("mongoCollection");

			new File(outputFolder + "DownloadedArticles/Copernicus").mkdirs();
			new File(outputFolder + "DownloadedArticles/Hindawi").mkdirs();
			new File(outputFolder + "DownloadedArticles/Springer").mkdirs();
			new File(outputFolder + "DownloadedArticles/Frontiers").mkdirs();
			new File(outputFolder + "NewArticleDOIs").mkdirs();

			System.setProperty("http.agent", "curl/7.51.0");
			// get first page of articles for specified time frame
			String xmlResponse = IOUtils
					.toString(new URL("https://www.doaj.org/oai.article?verb=ListRecords&metadataPrefix=oai_dc&from="
							+ fromDate + "&until=" + untilDate), Charset.forName("UTF-8"));
			JSONObject json = XML.toJSONObject(xmlResponse);
			JSONObject oaipmh = json.getJSONObject("OAI-PMH");
			JSONObject listRecords = oaipmh.getJSONObject("ListRecords");
			JSONObject tokenArray = listRecords.getJSONObject("resumptionToken");
			String token = tokenArray.getString("content");
			int cursor = tokenArray.getInt("cursor");
			int listSize = tokenArray.getInt("completeListSize");
			getDOIs(listRecords);

			System.out.println("Searching for new articles...");
			
			// while not at the end of the list fetch next page
			while (cursor < listSize) {
				xmlResponse = IOUtils.toString(
						new URL("https://www.doaj.org/oai.article?verb=ListRecords&resumptionToken=" + token),
						Charset.forName("UTF-8"));
				json = XML.toJSONObject(xmlResponse);
				oaipmh = json.getJSONObject("OAI-PMH");
				listRecords = oaipmh.getJSONObject("ListRecords");
				tokenArray = listRecords.getJSONObject("resumptionToken");
				if (tokenArray.has("content")) {
					token = tokenArray.getString("content");
				}
				cursor = tokenArray.getInt("cursor");
				getDOIs(listRecords);
				System.out.println(cursor + "/" + listSize);
				break;
			}

			mongoClient.close();

			for (Map.Entry<String, List<String>> entry : newArticles.entrySet()) {
				FileWriter writer = new FileWriter(outputFolder + "NewArticleDOIs/" + entry.getKey() + ".txt");
				for (String str : entry.getValue()) {
					writer.write(str + System.lineSeparator());
				}
				writer.close();

				if (entry.getKey().equals("Frontiers Media S.A.")) {
					System.out.println("Downloading articles from " + entry.getKey());
					for (String doi : entry.getValue()) {
						FrontiersDownloader.downloadArticle(doi, outputFolder);
					}
				} else if (entry.getKey().equals("Hindawi Limited")) {
					System.out.println("Downloading articles from " + entry.getKey());
					for (String doi : entry.getValue()) {
						HindawiDownloader.downloadArticle(doi, outputFolder);
					}
				} else if (entry.getKey().equals("SpringerOpen")) {
					System.out.println("Downloading articles from " + entry.getKey());
					for (String doi : entry.getValue()) {
						SpringerDownloader.downloadArticle(doi, outputFolder);
					}
				} else if (entry.getKey().equals("Copernicus Publications")) {
					System.out.println("Downloading articles from " + entry.getKey());
					for (String doi : entry.getValue()) {
						CopernicusDownloader.downloadArticle(doi, outputFolder);
					}
				}
				// Set true to search for and download articles from PMC instead of DOAJ
				downloadPMC = false;

			}
		}
	}

	private static void readArgs(String[] args) {
		if (args.length % 2 != 0)// ||args.length==0)
			throw new IllegalArgumentException("Wrong number of Arguments");
		if (args.length == 0)
			System.out.println("DEBUG MODE ACTIVE");

		for (int i = 0; i < args.length; i += 2) {
			if (!args[i].contains("-") || args[i].length() < 2)
				throw new IllegalArgumentException("Error with ParamList");
			arguments.put(args[i].substring(1, args[i].length()), args[i + 1]);
		}
		if (arguments.containsKey("outputFolder"))
			outputFolder = arguments.get("outputFolder") + "/";
		if (arguments.containsKey("mongoURI"))
			mongoURI = arguments.get("mongoURI");
		if (arguments.containsKey("mongoDB"))
			mongoDB = arguments.get("mongoDB");
		if (arguments.containsKey("mongoCollection"))
			mongoDB = arguments.get("mongoCollection");
		if (arguments.containsKey("fromDate"))
			fromDate = arguments.get("fromDate");
		if (arguments.containsKey("mongoImageCollection"))
			untilDate = arguments.get("untilDate");
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

	// adds DOIs from Hindawi, Frontiers, Copernicus and Springer to newArticles
	// DOIs that are already in MongoDB are skipped
	public static void getDOIs(JSONObject listRecords) {
		JSONArray records = listRecords.getJSONArray("record");
		for (Object r : records) {
			JSONObject record = (JSONObject) r;
			JSONObject metadata = record.getJSONObject("metadata");
			JSONObject oai_dc = metadata.getJSONObject("oai_dc:dc");
			String publisher = oai_dc.getString("dc:publisher");
			if (publisher.equals("Hindawi Limited") || publisher.equals("Frontiers Media S.A.")
					|| publisher.equals("Copernicus Publications") || publisher.equals("SpringerOpen")) {
				List<Object> identifiers = oai_dc.getJSONArray("dc:identifier").toList();
				for (Object id : identifiers) {
					String identifier = (String) id;
					// check if identifier is a DOI
					if (identifier.startsWith("10") && identifier.contains("/")) {
						String doi = identifier;
						BasicDBObject query = new BasicDBObject("DOI", doi);
						FindIterable<Document> fi = collection.find(query);
						MongoCursor<Document> cursor = fi.iterator();
						if (cursor.hasNext()) {
							continue;
						}
						newArticles.computeIfAbsent(publisher, v -> new ArrayList<>()).add(doi);
						break;
					}
				}

			}
		}
	}
}
