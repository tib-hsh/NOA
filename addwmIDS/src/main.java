import com.mongodb.*;
import org.bson.types.ObjectId;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT;

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
        extractCats(database);
        newDB(database);
        addCatsToDB(database);
    }

    private static void addCatsToDB(DB database) {
        DBCollection collection = database.getCollection(properties.getProperty("dbName"));
        DBCollection identifiers = database.getCollection("WMIdentifiers");
        BasicDBObject query = new BasicDBObject("wmcat", new BasicDBObject("$exists", false));
        DBCursor cursor = collection.find(query);
        cursor.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
        while(cursor.hasNext()){
            try {
                DBObject image = cursor.next();
                BasicDBList cats = (BasicDBList) image.get("wpcats");
                List<String> wmidentifiers = new ArrayList<>();
                for (Object cat: cats
                     ) {
                    String wpcat = (String)cat;
                    BasicDBObject catquery = new BasicDBObject("wpcat", wpcat);
                    DBObject oldIdentifiers = identifiers.find(catquery).one();
                    BasicDBObject newIdentifiers = new BasicDBObject();

                    // the next three lines add all three identifiers instead of only the commons category

                    /*newIdentifiers.put("wpcat", oldIdentifiers.get("wpcat"));
                    newIdentifiers.put("wmcat", oldIdentifiers.get("wmcat"));
                    newIdentifiers.put("wditem", oldIdentifiers.get("wditem"));*/

                    String wmcat = (String) oldIdentifiers.get("wmcat");
                    if(wmcat !=null) {
                        wmidentifiers.add(wmcat);
                    }
                }
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

    private static void newDB(DB db) throws IOException {
        File file = new File("UniqueCatstemp.txt");
        DBCollection collection = db.getCollection("WMIdentifiers");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while((line=br.readLine())!=null){
            BasicDBObject object = new BasicDBObject();
            object.put("wpcat", line);
            String wditem = null;
            String wmcat = null;
            try {
                URL apiURL = new URL("https://www.wikidata.org/w/api.php?action=wbgetentities&sites=enwiki&format=json&props=sitelinks&sitefilter=commonswiki&titles=Category%3A" + line.replaceAll(" ", "_"));
                BufferedReader in = new BufferedReader(new InputStreamReader(apiURL.openStream()));
                String inputLine;
                String json = "";
                while ((inputLine = in.readLine()) != null) {
                    json += inputLine;
                }
                BasicDBObject wm = BasicDBObject.parse(json);
                BasicDBObject entities = (BasicDBObject)wm.get("entities");
                Set<String> keySet = entities.keySet();
                wditem = keySet.iterator().next();
                DBObject item = (DBObject) entities.get(wditem);
                BasicDBObject sitelinks = (BasicDBObject)item.get("sitelinks");
                BasicDBObject commonswiki = (BasicDBObject)sitelinks.get("commonswiki");
                wmcat = (String)commonswiki.get("title");
                wmcat = wmcat.replaceAll("Category:", "");
                System.out.println("WPCat: " + line);
                System.out.println("WDItem: "+ wditem);
                System.out.println("WMCat: " + wmcat);

            }catch (Exception e){
                e.printStackTrace();
            }
            object.put("wmcat", wmcat);
            object.put("wditem", wditem);
            collection.insert(object);
        }
        collection.createIndex(new BasicDBObject("wpcat", 1));
    }


    public static void extractCats (DB database) throws IOException {
        DBCollection collection = database.getCollection(properties.getProperty("dbName"));
        DBCursor cursor = collection.find();
        List<String> list = new ArrayList<>();
        while(cursor.hasNext()) {
            BasicDBObject image = (BasicDBObject) cursor.next();
            try {
                BasicDBList terms = (BasicDBList) image.get("wpcats");
                for (Object term : terms) {
                    if(!list.contains(String.valueOf(term))){
                        list.add(String.valueOf(term));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File termfile = new File("UniqueCats.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(termfile));
        System.out.println(list.size());
        for (String s: list){
            bw.write(s + "\n");
        }
        bw.close();
    }
}
