import com.mongodb.*;
import org.bson.types.ObjectId;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Created by SohmenL on 07.11.2018.
 */
public class main {
    static Properties properties = new Properties();

    public static void main (String[] args) throws IOException {
        BufferedInputStream stream = new BufferedInputStream(new FileInputStream("wmIDs.properties"));
        properties.load(stream);
        stream.close();
        MongoClient mongoClient = new MongoClient(new MongoClientURI(properties.getProperty("mongoURI")));
        DB database = mongoClient.getDB(properties.getProperty("dbName"));
        addCatsToDB(database);
    }
    private static void addCatsToDB(DB database) {

        //database information
        DBCollection collection = database.getCollection(properties.getProperty("imageCollection"));
        DBCollection identifiers = database.getCollection("WMIdentifiers");
        BasicDBObject query = new BasicDBObject("wmcat", new BasicDBObject("$exists", false));
        DBCursor cursor = collection.find(query);
        cursor.addOption(Bytes.QUERYOPTION_NOTIMEOUT);

        //loop through all images
        while(cursor.hasNext()){
            try {
                DBObject image = cursor.next();
                BasicDBList cats = (BasicDBList) image.get("wpcats");
                List<String> wmidentifiers = new ArrayList<>();

                //loop through all Wikipedia categories of an image
                for (Object cat: cats
                        ) {
                    String wpcat = (String)cat;

                    //search WMIdentifiers database for the category
                    BasicDBObject catquery = new BasicDBObject("wpcat", wpcat);
                    DBObject oldIdentifiers = identifiers.find(catquery).one();

                    //if the category doesn't exist, add it + corresponding Wikidata and Wikimedia Commons information
                    if(oldIdentifiers==null){
                        BasicDBObject object = new BasicDBObject();
                        object.put("wpcat", wpcat);
                        String wditem = null;
                        String wmcat = null;

                        //call to Wikidata API
                        try {
                            URL apiURL = new URL("https://www.wikidata.org/w/api.php?action=wbgetentities&sites=enwiki&format=json&props=sitelinks&sitefilter=commonswiki&titles=Category%3A" + wpcat.replaceAll(" ", "_"));
                            BufferedReader in = new BufferedReader(new InputStreamReader(apiURL.openStream()));
                            String inputLine;
                            String json = "";
                            while ((inputLine = in.readLine()) != null) {
                                json += inputLine;
                            }

                            //parse API response
                            BasicDBObject wm = BasicDBObject.parse(json);
                            BasicDBObject entities = (BasicDBObject)wm.get("entities");
                            Set<String> keySet = entities.keySet();
                            wditem = keySet.iterator().next();
                            DBObject item = (DBObject) entities.get(wditem);
                            BasicDBObject sitelinks = (BasicDBObject)item.get("sitelinks");
                            BasicDBObject commonswiki = (BasicDBObject)sitelinks.get("commonswiki");
                            wmcat = (String)commonswiki.get("title");
                            wmcat = wmcat.replaceAll("Category:", "");

                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        //insert new information into WMIdentifiers database
                        object.put("wmcat", wmcat);
                        object.put("wditem", wditem);
                        identifiers.insert(object);

                        //continue with the new object
                        oldIdentifiers = object;
                    }


                    // the next four lines add all three identifiers instead of only the commons category

                    /*BasicDBObject newIdentifiers = new BasicDBObject();
                    newIdentifiers.put("wpcat", oldIdentifiers.get("wpcat"));
                    newIdentifiers.put("wmcat", oldIdentifiers.get("wmcat"));
                    newIdentifiers.put("wditem", oldIdentifiers.get("wditem"));*/

                    //only Wikimedia Commons category is added to the database for now
                    String wmcat = (String) oldIdentifiers.get("wmcat");
                    if(wmcat !=null) {
                        wmidentifiers.add(wmcat);
                    }
                }

                //insert list of Wikimedia Commons categories; update database
                image.put("wmcat", wmidentifiers);
                ObjectId id = (ObjectId) image.get("_id");
                BasicDBObject idquery = new BasicDBObject("_id", id);
                collection.update(idquery, image);
                System.out.println(image.get("_id"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
