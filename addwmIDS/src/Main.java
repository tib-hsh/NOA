import com.mongodb.*;
import org.bson.types.ObjectId;
import org.ini4j.Wini;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by SohmenL on 07.11.2018.
 */
public class Main {
	private static Map<String, String> arguments = new HashMap<String, String>();
	static String mongoURI;
	static String mongoDB;
	static String mongoCollection;
	static long numberOfImages;
	static int count = 0;

	public static void main(String[] args) throws IOException {
		File f = new File("config.ini");
		if (f.exists() && !f.isDirectory()) {
			Wini ini = new Wini(new File("config.ini"));
			mongoURI = "mongodb://" + ini.get("DEFAULT", "mongoip") + "/" + ini.get("DEFAULT", "mongoport");
			mongoDB = ini.get("DEFAULT", "mongodb");
			mongoCollection = ini.get("DEFAULT", "image_collection");
		}
		readArgs(args);
		MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoURI));
		DB database = mongoClient.getDB(mongoDB);
		addCatsToDB(database);
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
			mongoURI = arguments.get("mongoURI");
		if (arguments.containsKey("mongoDB"))
			mongoDB = arguments.get("mongoDB");
		if (arguments.containsKey("mongoCollection"))
			mongoCollection = arguments.get("mongoCollection");
	}

	private static void addCatsToDB(DB database) {

		// database information
		DBCollection collection = database.getCollection(mongoCollection);
		DBCollection identifiers = database.getCollection("WMIdentifiers");
		BasicDBObject query = new BasicDBObject("wmcat", new BasicDBObject("$exists", false));
		DBCursor cursor = collection.find(query);
		cursor.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
		numberOfImages = collection.count();

		// loop through all images
		while (cursor.hasNext()) {
			try {
				DBObject image = cursor.next();
				BasicDBList cats = (BasicDBList) image.get("wpcats");
				List<String> wmidentifiers = new ArrayList<>();

				// loop through all Wikipedia categories of an image
				for (Object cat : cats) {
					String wpcat = (String) cat;

					// search WMIdentifiers database for the category
					BasicDBObject catquery = new BasicDBObject("wpcat", wpcat);
					DBObject oldIdentifiers = identifiers.find(catquery).one();

					// if the category doesn't exist, add it + corresponding Wikidata and Wikimedia
					// Commons information
					if (oldIdentifiers == null) {
						BasicDBObject object = new BasicDBObject();
						object.put("wpcat", wpcat);
						String wditem = null;
						String wmcat = null;

						// call to Wikidata API
						try {
							URL apiURL = new URL(
									"https://www.wikidata.org/w/api.php?action=wbgetentities&sites=enwiki&format=json&props=sitelinks&sitefilter=commonswiki&titles=Category%3A"
											+ wpcat.replaceAll(" ", "_"));
							BufferedReader in = new BufferedReader(new InputStreamReader(apiURL.openStream()));
							String inputLine;
							String json = "";
							while ((inputLine = in.readLine()) != null) {
								json += inputLine;
							}

							// parse API response
							BasicDBObject wm = BasicDBObject.parse(json);
							BasicDBObject entities = (BasicDBObject) wm.get("entities");
							Set<String> keySet = entities.keySet();
							wditem = keySet.iterator().next();
							DBObject item = (DBObject) entities.get(wditem);
							BasicDBObject sitelinks = (BasicDBObject) item.get("sitelinks");
							if(sitelinks != null) {
								BasicDBObject commonswiki = (BasicDBObject) sitelinks.get("commonswiki");
								if (commonswiki != null) {
									wmcat = (String) commonswiki.get("title");
									wmcat = wmcat.replaceAll("Category:", "");
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						// insert new information into WMIdentifiers database
						object.put("wmcat", wmcat);
						object.put("wditem", wditem);
						identifiers.insert(object);

						// continue with the new object
						oldIdentifiers = object;
					}

					// the next four lines add all three identifiers instead of only the commons
					// category

					/*
					 * BasicDBObject newIdentifiers = new BasicDBObject();
					 * newIdentifiers.put("wpcat", oldIdentifiers.get("wpcat"));
					 * newIdentifiers.put("wmcat", oldIdentifiers.get("wmcat"));
					 * newIdentifiers.put("wditem", oldIdentifiers.get("wditem"));
					 */

					// only Wikimedia Commons category is added to the database for now
					String wmcat = (String) oldIdentifiers.get("wmcat");
					if (wmcat != null) {
						wmidentifiers.add(wmcat);
					}
				}

				// insert list of Wikimedia Commons categories; update database
				image.put("wmcat", wmidentifiers);
				ObjectId id = (ObjectId) image.get("_id");
				BasicDBObject idquery = new BasicDBObject("_id", id);
				collection.update(idquery, image);
				count++;
				System.out.print(count*100/numberOfImages + "%\r");
			} catch (Exception e) {
				e.printStackTrace();
			}		
		}
		System.out.println("Finished.");
	}
}
