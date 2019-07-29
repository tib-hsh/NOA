
/**
 * Created by charbonn on 02.11.2016.
 */

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import metadata.Author;
import metadata.Citation;
import metadata.ID;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MongoDBRepo {

	private static MongoDBRepo instance;
	private static MongoDatabase db;

	private static MongoClient mongoClient;
	private static String date;
	private static String IP;

	private MongoDBRepo() {
	}

	public static MongoDBRepo getInstance() {
		if (IP == null && MongoDBRepo.getInstance() == null)
			throw new IllegalArgumentException("getInstance must be invoked with an Parameters first");
		else
			return MongoDBRepo.instance;
	}

	public static MongoDBRepo getInstance(String IP, int Port, String databaseName) {

		if (MongoDBRepo.instance == null) {
			MongoDBRepo.instance = new MongoDBRepo();
			MongoDBRepo.mongoClient = new MongoClient(IP, Port);
			MongoDBRepo.db = mongoClient.getDatabase(databaseName);
			date = System.currentTimeMillis() + "";
		}
		return MongoDBRepo.instance;
	}

	public void write(String journal, String graphicID, String captionBody, String captionTitle, byte[] image) {
		Document d = new Document("journalName", journal);
		d.append("graphicOID", graphicID).append("captionTitle", captionTitle).append("captionBody", captionBody)
				.append("image", image);
		db.getCollection("plos").insertOne(d);
	}

	public void writeError(String Path, String ExceptionText, String modus) {
		Document d = new Document("Exception", ExceptionText);
		d.append("path2file", Path);
		db.getCollection("Errors_" + date).insertOne(d);
	}

	public void writeJournal(ResultSetJournal rsj, boolean withDownload, String dataFolder) {
		Document d = new Document("journalName", rsj.getJournalName());
		d.put("_id", new ObjectId());
		d.append("DOI", rsj.getJournalDOI());

		List<Document> findings = new ArrayList<>();
		List<Object> findingsRef = new ArrayList<>();

		List<Document> Bibliography = new ArrayList<>();
		for (Citation c : rsj.getBibliography()) {
			List<Document> BibAuthors = new ArrayList<>();

			for (Author bibAuthor : c.getAuthors()) {
				BibAuthors.add(new Document("firstName", bibAuthor.getFirstName()).append("lastName",
						bibAuthor.getLastName()));
			}
			List<Document> IDs = new ArrayList<>();
			for (ID ID : c.getIDs()) {
				IDs.add(new Document("type", ID.getType()).append("number", ID.getNumber()));
			}
			Bibliography
					.add(new Document("Authors", BibAuthors).append("title", c.getTitle()).append("year", c.getYear())
							.append("journal", c.getJournal()).append("IDs", IDs).append("Text", c.getText()));
		}

		List<Document> Authors = new ArrayList<>();
		for (Author a : rsj.getAuthors()) {
			Authors.add(new Document("firstName", a.getFirstName()).append("lastName", a.getLastName()));
		}

		if (rsj.getPublicationYear() != null)
			d.append("year", rsj.getPublicationYear().replaceAll("\t", "").replaceAll(" ", ""));
		else {

			try {
				rsj.setPublicationYear(rsj.getFullDate().getYear());
				d.append("year", rsj.getPublicationYear());
			} catch (Exception e) {
				d.append("year", null);
			}
		}
		metadata.PublicationDate publicationDate = rsj.getFullDate();
		Document pdate;
		try {
			pdate = new Document();// ("day", publicationDate.getDay()).append("month",
									// publicationDate.getMonth()).append("year", publicationDate.getYear()) );
			try {
				pdate.append("day", publicationDate.getDay());
				if (pdate.get("day") == null) {
					pdate.put("day", "0");
				}
			} catch (NullPointerException E) {
				pdate.append("day", "0");
			}
			try {
				pdate.append("month", publicationDate.getMonth());
				if (pdate.get("month") == null) {
					pdate.put("month", "0");
				}

			} catch (NullPointerException E) {
				pdate.append("month", "0");
			}
			try {
				pdate.append("year", publicationDate.getYear());
				if (pdate.get("year") == null) {
					pdate.put("year", "0");
				}

			} catch (NullPointerException E) {
				pdate.append("year", "0");
			}
			// replace month name with number
			String month = (String) pdate.get("month");
			if (!(month.matches("\\d+"))) {
				Date date = new SimpleDateFormat("MMMM", Locale.ENGLISH).parse(month);
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				pdate.put("month", cal.get(Calendar.MONTH) + 1); // Zero based. 0-11

			}
		} catch (Exception e) {
			pdate = null;
		}

		// set licenseType
		String license = (String) rsj.getLicense();
		String licenseType = "";
		if (license == null) {
			licenseType = "invalid";
		} else if (license.contains("creativecommons.org/licenses/by/4.0")) {
			licenseType = "cc-by-4.0";
		} else if (license.contains("creativecommons.org/licenses/by/3.0")) {
			licenseType = "cc-by-3.0";
		} else if (license.contains("creativecommons.org/licenses/by/2.0")) {
			licenseType = "cc-by-2.0";
		} else if (license.contains("creativecommons.org/licenses/by/2.5")) {
			licenseType = "cc-by-2.5";
		} else if (license.contains("www.frontiersin.org/licenseagreement")) {
			licenseType = "frontiers";
		} else {
			licenseType = "nay";
		}

		for (Result a : rsj.getResultList()) {

			String s = "";
			if (rsj.getXMLPathComplete().contains("PMC")) {
				s = "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC" + rsj.getPmcID() + "/bin/" + a.getGraphicDOI()
						+ ".jpg";
			} else if (rsj.getXMLPathComplete().matches(".*[hH]indawi.*")) {
				String link = a.getGraphicDOI();
				if (link == null)
					link = "0";
				Matcher matcher = Pattern.compile("\\d+\\.(.*)").matcher(link);
				String figID;
				try {
					matcher.find();
					figID = matcher.group(1);
				} catch (Exception e) {
					figID = "0";
				}
				String doi = rsj.getJournalDOI();
				matcher = Pattern.compile(".?\\/(.*)").matcher(doi);
				try {
					matcher.find();
					link = matcher.group(1) + "." + figID;
				} catch (Exception e) {
					link = "no valid URL found";
				}
				s = ("https://www.hindawi.com/journals/" + rsj.getPublisherId() + "/" + link + ".jpg").toLowerCase();

			} else if (rsj.getXMLPathComplete().matches(".*[sS]pringer.*")) {

				String newDoi = rsj.getJournalDOI().substring(0, 7) + "%2F" + rsj.getJournalDOI().substring(8);
				s = "https://static-content.springer.com/image/art%3A" + newDoi + "/" + a.getGraphicDOI();
			} else if (rsj.getPublisher().matches(".*[fF]rontiers.*")) {
				s = a.getGraphicDOI();
			} else if (rsj.getXMLPathComplete().matches(".*[cC]opernicus.*")) {
				s = a.getGraphicDOI();
			}

			a.setImageURL(s);

			boolean downloading = withDownload;



			int lengthOfTitle = 0;

			int lengthOfBody = 0;
			Document img = new Document();
			img.put("_id", new ObjectId());

			String captionTitle = "";
			try{
				captionTitle = a.getLabel();
			}catch (NullPointerException e){

			}

			lengthOfTitle = captionTitle.length();



			if (a.getCaptionBody() != null)
				lengthOfBody = a.getCaptionBody().length();

			// find out if images are originally from a different source; produces a lot of
			// false positives
			boolean copyrightFlag = false;
			String caption = a.getCaptionBody();
			if (caption != null) {
				Matcher matcher = Pattern.compile("\\((19|20)\\d\\d").matcher(caption);
				Matcher matcher1 = Pattern.compile("(19|20)\\d\\d\\)").matcher(caption);
				if (matcher.find()) {
					copyrightFlag = true;
				} else if (matcher1.find()) {
					copyrightFlag = true;
				}
				try {
					BufferedReader br = new BufferedReader(new FileReader(dataFolder + "checklines.txt"));
					String line;
					while ((line = br.readLine()) != null) {
						if (caption.contains(line)) {
							copyrightFlag = true;
							break;
						}
					}
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			a.setCopyrightFlag(copyrightFlag);




			// insert TIB_URL

			// Here are the Items of the Image Collection definied.
			// Later added: discipline, wpterms, wpcats, acronym, imageType filled, TIB_URL
			findings.add(img.append("findingID", a.getFindingID()).append("DOI", rsj.getJournalDOI())
					.append("sourceID", d.get("_id")).append("title", rsj.getTitle())
					.append("journalName", rsj.getJournalName()).append("captionTitle", captionTitle)
					.append("captionBody", a.getCaptionBody()).append("captionTitleLength", lengthOfTitle)
					.append("captionBodyLength", lengthOfBody).append("URL", s) // Note: New begin
					.append("licenseType", licenseType).append("authors", Authors).append("imageType", "nay")
					.append("pubYear", pdate.get("year")).append("pubMonth", pdate.get("month"))
					.append("pubDay", pdate.get("day")).append("publisher", rsj.getPublisher())
					.append("context", a.getContext()).append("copyrightFlag", a.isCopyrightFlag()));
		}

		d.append("title", rsj.getTitle());
		d.append("authors", Authors);
		d.append("numOfAuthor", Authors.size());
		d.append("abstract", rsj.getAbstract());
		d.append("journalVolume", rsj.getVolume());
		d.append("journalIssue", rsj.getIssue());
		d.append("pages", rsj.getPages());
		d.append("license", rsj.getLicense());
		d.append("licenseType", licenseType);
		d.append("publisher", rsj.getPublisher());
		d.append("keywords", rsj.getKeywords());
		d.append("bibliography", Bibliography); // TODO Wichtig?
		d.append("publicationDate", pdate); // TODO aufsplitten
		d.append("formula", rsj.hasFormula); // TODO Wichtig?
		d.append("source", rsj.getSource());

		if (rsj.getAbstract() != null)
			d.append("abstractLength", rsj.getAbstract().length());
		else
			d.append("abstractLength", "Abstract is NULL");

		d.append("body", rsj.getBody());
		if (rsj.getBody() != null)
			d.append("bodyLength", rsj.getBody().length());
		else
			d.append("bodyLength", "Body is NULL");

		// d.append("findings",findings);

		d.append("numOfFindings", findings.size());
		d.append("path2file", rsj.getXMLPathComplete());

		if (rsj.getBody() != null && rsj.getBody() != "" && rsj.getTitle() != null && rsj.getTitle() != "" && rsj.getJournalDOI() != null && rsj.getJournalDOI() != "") {
			try {
				// db.getCollection("Version26.09.").insertOne(d);
				for (Document y : findings) {
					findingsRef.add(y.get("_id"));
					// db.getCollection("Corpus_Image_"+date).insertOne(y);
					db.getCollection(Main.mongoImageCol).insertOne(y);
				}
				d.append("findingsRef", findingsRef);

				// db.getCollection("Corpus_Journal_"+date).insertOne(d);
				db.getCollection(Main.mongoJournalcol).insertOne(d);
			} catch (Exception e) {
				System.out.println(e);
				System.out.println(rsj.getXMLPathComplete());
			}
		}
	}

	@Deprecated
	public void write(String journal, String Year, String DOI, int findingID, String captionBody, String imageURL,
			List<Author> Author, List<Author> Editor) {
		Document d = new Document("journalName", journal);
		List<Document> Authors = new ArrayList<>();
		List<Document> Editors = new ArrayList<>();

		for (Author a : Author) {
			Authors.add(new Document("firstName", a.getFirstName()).append("lastName", a.getLastName()));
		}
		for (Author a : Editor) {
			Editors.add(new Document("firstName", a.getFirstName()).append("lastName", a.getLastName()));
		}
		d.append("Year", Year).append("DOI", DOI).append("findingID", findingID).append("captionBody", captionBody)
				.append("ImageURL", imageURL).append("Authors", Authors).append("Editor", Editors);
		// db.getCollection("hindawi_"+date).insertOne(d);
	}

	@Deprecated
	public void writeError(String error) {
		Document d = new Document("Error", error);
		// db.getCollection("Errors_"+date).insertOne(d);
		db.getCollection(Main.mongoErrorCol).insertOne(d);
	}
}
