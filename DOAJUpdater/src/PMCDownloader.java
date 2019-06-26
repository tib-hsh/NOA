import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.ini4j.Wini;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class PMCDownloader {

	public static void downloadArticles(String fromDate, String untilDate, String outputFolder, String mongoCollection) throws IOException {
		new File(outputFolder + "DownloadedArticles/PMC").mkdirs();

		// Get all articles published on PMC in specified timeframe
		String xmlResponse = IOUtils
				.toString(new URL("https://www.ncbi.nlm.nih.gov/pmc/oai/oai.cgi?verb=ListIdentifiers&from=" + fromDate
						+ "&until=" + untilDate + "&metadataPrefix=pmc"), Charset.forName("UTF-8"));
		List<String> pmcIDs = new ArrayList<String>();
		Matcher m = Pattern.compile("<identifier>oai:pubmedcentral\\.nih\\.gov:[0-9]*<\\/identifier>")
				.matcher(xmlResponse);
		while (m.find()) {
			pmcIDs.add(m.group());
		}

		// If there are more than 500 articles, check resumptionToken for next query
		while (xmlResponse.contains("<resumptionToken>")) {
			String token = xmlResponse.split("<resumptionToken>")[1];
			token = token.split("<\\/resumptionToken>")[0];
			xmlResponse = IOUtils.toString(new URL(
					"https://www.ncbi.nlm.nih.gov/pmc/oai/oai.cgi?verb=ListIdentifiers&resumptionToken=" + token),
					Charset.forName("UTF-8"));
			m = Pattern.compile("<identifier>oai:pubmedcentral\\.nih\\.gov:[0-9]*<\\/identifier>").matcher(xmlResponse);
			while (m.find()) {
				pmcIDs.add(m.group());
			}
		}
		pmcIDs.replaceAll(s -> s.replaceFirst("<identifier>", ""));
		pmcIDs.replaceAll(s -> s.replaceFirst("<\\/identifier>", ""));

		FileWriter writer;
		writer = new FileWriter(outputFolder + "NewArticleDOIs/PMC.txt");
		for (String str : pmcIDs) {
			writer.write(str + System.lineSeparator());
		}
		writer.close();

		Wini ini = new Wini(new File("config.ini"));
		MongoClient mongoClient = new MongoClient(new MongoClientURI(
				"mongodb://" + ini.get("DEFAULT", "mongoip") + "/" + ini.get("DEFAULT", "mongoport")));
		MongoDatabase database = mongoClient.getDatabase(ini.get("DEFAULT", "mongodb"));
		outputFolder = ini.get("DEFAULT", "tmp_folder") + "/";
		MongoCollection<Document> collection = database.getCollection(mongoCollection);

		int i = 0;
		for (String pmcID : pmcIDs) {
			i++;
			System.out.println("Downloading Article " + i + "/" + pmcIDs.size());
			String filePath = outputFolder + "DownloadedArticles/PMC/PMC"
					+ pmcID.replaceAll("oai:pubmedcentral\\.nih\\.gov:", "") + ".xml";
			File f = new File(filePath);
			if (f.exists() && !f.isDirectory()) {
				continue;
			}

			// Get DOI of article and check if it is already in DB
			String idConverterURL = "https://www.ncbi.nlm.nih.gov/pmc/utils/idconv/v1.0/?ids=PMC"
					+ pmcID.replaceAll("oai:pubmedcentral\\.nih\\.gov:", "") + "&format=json";
			JSONObject jsonPMC = new JSONObject(IOUtils.toString(new URL(idConverterURL), Charset.forName("UTF-8")));
			JSONObject records = jsonPMC.getJSONArray("records").getJSONObject(0);
			if (records.has("doi")) {
				BasicDBObject query = new BasicDBObject("DOI", records.getString("doi"));
				FindIterable<Document> fi = collection.find(query);
				MongoCursor<Document> cursor = fi.iterator();
				if (cursor.hasNext()) {
					continue;
				}
			}

			// Download article
			String downloadURL = "https://www.ncbi.nlm.nih.gov/pmc/oai/oai.cgi?verb=GetRecord&identifier=" + pmcID
					+ "&metadataPrefix=pmc";
			if (!Updater.exists(downloadURL)) {
				writer = new FileWriter(outputFolder + "NewArticleDOIs/NotDownloaded.txt", true);
				writer.write(pmcID + System.lineSeparator());
				writer.close();
				return;
			}
			try {
				URL website = new URL(downloadURL);
				ReadableByteChannel rbc = Channels.newChannel(website.openStream());
				FileOutputStream fos = new FileOutputStream(filePath);
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		mongoClient.close();
	}
}
