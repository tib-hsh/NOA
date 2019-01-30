import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.Document;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by charbonn on 20.11.2018.
 */
public class MongoOrganizier
{
	private static MongoOrganizier instance;
	private static MongoDatabase db;

	private static MongoClient mongoClient;
	private static String IP;
	private static String PORT;
	private static String defaultDB;

	private MongoOrganizier()
	{
	}


	public static MongoOrganizier getInstance()
	{
		if (IP == null && MongoOrganizier.getInstance() == null)
			throw new IllegalArgumentException("getInstance must be invoked with an Parameters first");
		else
			return MongoOrganizier.instance;
	}

	public static MongoOrganizier getInstance(String IP, int Port)
	{
		//remove mongoDB Logging
		Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
		mongoLogger.setLevel(Level.SEVERE);

		if (MongoOrganizier.instance == null) {
			MongoOrganizier.IP = IP;
			MongoOrganizier.PORT = Port + "";
			MongoOrganizier.instance = new MongoOrganizier();
			MongoOrganizier.mongoClient = new MongoClient(IP, Port);
			//MongoOrganizier.db = mongoClient.getDatabase(databaseName);
		}
		else
			System.out.println("MongoAlreadyEstablished!");
		return MongoOrganizier.instance;
	}

	public static MongoOrganizier getInstance(String IP, int Port, String standardDB)
	{
		MongoOrganizier.defaultDB = standardDB;
		return getInstance(IP, Port);
	}


	private static void CopyFromTo(MongoCollection<Document> col, MongoCollection<Document> tar)
	{

		// Performing a read operation on the collection.
		FindIterable<Document> fi = col.find();
		MongoCursor<Document> cursor = fi.iterator();
		try {
			while (cursor.hasNext()) {
				Document d = cursor.next();
				//System.out.println(d.toJson());
				tar.insertOne(d);
			}
		} finally {
			cursor.close();
		}
	}

	public static void BuildIndex(String coll_name, String field)
	{
		BuildIndex(coll_name, MongoOrganizier.defaultDB, field);
	}

	public static void BuildIndex(String coll_name, ArrayList<String> fields)
	{
		BuildIndex(coll_name, MongoOrganizier.defaultDB, fields);
	}

	public static void BuildIndex(String coll_name, String db_name, String field)
	{
		ArrayList<String> arr = new ArrayList<String>();
		arr.add(field);
		BuildIndex(coll_name, db_name, arr);
	}

	public static void BuildIndex(String coll_name, String db_name, ArrayList<String> fields)
	{
		MongoDatabase db = mongoClient.getDatabase(db_name);
		MongoCollection<Document> col = db.getCollection(coll_name);
		BsonDocument bs = new BsonDocument();
		for (String s : fields) {
			bs.append(s, new BsonInt32(1));
		}
		col.createIndex(bs);
	}

	public static void Drop(String coll_name)
	{
		Drop(coll_name, MongoOrganizier.defaultDB);
	}

	public static void Drop(String coll_name, String db_name)
	{
		MongoDatabase db = mongoClient.getDatabase(db_name);
		MongoCollection<Document> col = db.getCollection(coll_name);
		col.drop();
	}

	public static void Copy(String source_coll, String target_coll)
	{
		Copy(source_coll, MongoOrganizier.defaultDB, target_coll, MongoOrganizier.defaultDB);
	}

	public static void Copy(String source_coll, String source_db_name, String target_coll, String target_db_name)
	{
		MongoDatabase source_db = mongoClient.getDatabase(source_db_name);
		MongoDatabase tar_db = mongoClient.getDatabase(target_db_name);


		MongoCollection<Document> source = source_db.getCollection(source_coll);
		MongoCollection<Document> tar = tar_db.getCollection(target_coll);

		CopyFromTo(source, tar);
	}
}
