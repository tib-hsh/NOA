import com.mongodb.*;

import org.apache.commons.io.FileUtils;
import org.ini4j.Wini;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class addDiscipline {

	private static Map<String, String> arguments = new HashMap<String, String>();
	static String mongoAdress;
	static String dbName;
	static String collectionName;
	static String imageCollectionName;
	static String dataFolder = "";
	static String tmpFolder = "";

	public static void main(String[] args) throws IOException {

		File f = new File("config.ini");
		if(f.exists() && !f.isDirectory()) { 
			Wini ini = new Wini(new File("config.ini"));
			mongoAdress = "mongodb://" + ini.get("DEFAULT", "mongoip") + "/" + ini.get("DEFAULT", "mongoport");
			dbName = ini.get("DEFAULT", "mongodb");
			collectionName = ini.get("DEFAULT", "article_collection");
			imageCollectionName = ini.get("DEFAULT", "image_collection");
			dataFolder = ini.get("DEFAULT", "data_folder") + "/";
			tmpFolder = ini.get("DEFAULT", "tmp_folder") + "/";
		}
		readArgs(args);
		MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoAdress));
		DB database = mongoClient.getDB(dbName);
		DBCollection collection = database.getCollection(collectionName);
		DBCollection imageCollection = database.getCollection(imageCollectionName);
		DBObject query = new BasicDBObject("journalName", new BasicDBObject("$exists", true));
		List<String> journals = collection.distinct("journalName", query);
		int i = 0;
		for (String journal : journals) {
			i++;
			if (i >= 0) {
				System.out.println(i + "/" + journals.size());
				System.out.println(journal);
				if (journal != null) {
					String result = requestEZB(journal);
					System.out.println(result);

					FileWriter writer = new FileWriter(tmpFolder + "disciplines.csv", true);
					writer.write(result + System.lineSeparator());
					writer.close();
				}
			}
		}
		mongoClient.close();

		writeFileToDB(tmpFolder + "disciplines.csv");
	}

	private static void readArgs(String[] args) {
		if (args.length % 2 != 0)
			throw new IllegalArgumentException("Wrong number of Arguments");

		for (int i = 0; i < args.length; i += 2) {
			if (!args[i].contains("-") || args[i].length() < 2)
				throw new IllegalArgumentException("Error with ParamList");
			arguments.put(args[i].substring(1, args[i].length()), args[i + 1]);
		}

		if (arguments.containsKey("mongoURI"))
			mongoAdress = arguments.get("mongoURI");
		if (arguments.containsKey("mongoDB"))
			dbName = arguments.get("mongoDB");
		if (arguments.containsKey("mongoCollection"))
			collectionName = arguments.get("mongoCollection");
	}

	private static String requestEZB(String journal) throws IOException {
		String request = "https://rzblx1.uni-regensburg.de/ezeit/searchres.phtml?bibid=UBTIB&colors=7&lang=de&jq_type1=QS&jq_term1="
				+ journal.replaceAll("\\s+", "+");
		URL url = new URL(request);
		URLConnection urlConnection = url.openConnection();
		urlConnection.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		String line = "";
		String newRequest = "";
		String updatedJournal = "";
		while ((line = br.readLine()) != null) {
			if (line.contains("defListContentTitle")) {
				String nextline = br.readLine();
				if (nextline.contains("Fachgebiet")) {
					updatedJournal = requestEZBredirect(journal, request);
					return updatedJournal;
				}
				// if discipline is not found on initial page redirect to journal
			} else if (line.contains("Link zu den Details")) {
				Matcher m = Pattern.compile("a href=\\\"(.+?)\\\" title").matcher(line);

				while (m.find()) {
					newRequest = (("https://rzblx1.uni-regensburg.de/ezeit/" + m.group(1))).replaceAll("&amp;", "&");
					updatedJournal = requestEZBredirect(journal, newRequest);
					if (updatedJournal != null)
						return updatedJournal;
				}
			}
		}
		br.close();
		return journal;
	}

	private static String requestEZBredirect(String journal, String request) throws IOException {

		List<String> lines = FileUtils.readLines(new File(dataFolder + "translatedDisciplines.txt"), "utf-8");
		HashMap<String, String> dictionary = new HashMap<String, String>();
		for (String l : lines) {
			String[] parts = l.split(";");
			dictionary.put(parts[0], parts[1]);
		}
		URL url = new URL(request);
		URLConnection urlConnection = url.openConnection();
		urlConnection.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		String line = "";
		String updatedJournal = "";

		while ((line = br.readLine()) != null) {
			if (line.contains("defListContentTitle")) {
				String nextline = br.readLine();

				if (nextline.contains("Fachgebiet")) {

					nextline = br.readLine();
					nextline = br.readLine();
					nextline = br.readLine();

					if (nextline.contains("a href")) {
						Matcher m = Pattern.compile(">([a-zA-Z]+(.*?))<").matcher(nextline);
						updatedJournal = journal + ";";
						while (m.find()) {
							updatedJournal += dictionary.get(m.group(1)) + ";";
						}
					}
				}
			}
		}
		br.close();
		return updatedJournal;
	}

	public static void writeFileToDB(String fileName) throws FileNotFoundException, IOException {
		HashMap<String, List<String>> map = new HashMap<String, List<String>>();
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(";");
				List<String> disciplines = new ArrayList<String>();
				for (int i = 1; i <= parts.length - 1; i++) {
					disciplines.add(parts[i]);
				}
				map.put(parts[0], disciplines);
			}
		}

		MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoAdress));
		DB database = mongoClient.getDB(dbName);
		DBCollection collection = database.getCollection(collectionName);
		DBCollection imageCollection = database.getCollection(imageCollectionName);

		int i = 0;
		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
			i++;
			System.out.println(i + "/" + map.size());
			BasicDBObject query = new BasicDBObject().append("journalName", entry.getKey());
			BasicDBObject newDocument = new BasicDBObject();
			newDocument.append("$set", new BasicDBObject().append("discipline", entry.getValue()));
			collection.updateMulti(query, newDocument);
			imageCollection.updateMulti(query, newDocument);

		}
		mongoClient.close();
	}
}
