import metadata.Author;
import metadata.Citation;
import metadata.ID;
import org.apache.log4j.*;
import org.ini4j.Wini;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
//TODO year should be from FullDate
//TODO cleanup
//TODO check Abstract and Body functions; getSections; condition
//TODO Main no longer implements Text. Does this change anything?

/**
 * Created by charbonn on 02.11.2016.
 */
public class Main
{

	/*
	* DATA HANDLING
	*/
	public static List<ResultSetJournal> resultTorso = new ArrayList<>();
	public static HashMap figureContext = new HashMap();
	public static HashMap references = new HashMap();
	public static int withFormula = 0;
	public static int articles = 0;
	public static long numberOfDocs = -1;
	public static long currentDoc = 0;
	static String location = "Fill in the Paper Location here";
	static String outputEncoding = "UTF-8";
	static int VERBOSE = 1;
	static String mongoIP = "IP for mongoDB Server";
	static int mongoPort = 27017; //default
	static String mongoDataBase = "DB Name";
	static String mongoErrorCol = "NOA_Errors";
	static String mongoJournalcol = "NOA_Journals";
	static String mongoImageCol = "NOA_Images";
	static boolean withDownload = false; //Download Images as binary data?
	static int outPutFormat = 2; // 0=path, 1= current/overall, 2=percentage.
	static String uniqueHash = "uniquehashString to fill";
	private static Logger logger = Logger.getRootLogger();
	private static Map<String, String> map = new HashMap<String, String>();
	private static long startTime = 0;

	public static void main(String[] args) throws IOException
	{
		
		File f = new File("config.ini");
		if(f.exists() && !f.isDirectory()) { 
			Wini ini = new Wini(new File("config.ini"));
			mongoIP = ini.get("DEFAULT", "mongoip");
			mongoPort =  ini.get("DEFAULT", "mongoport", int.class);
			mongoDataBase = ini.get("DEFAULT", "mongodb");
			mongoJournalcol = ini.get("DEFAULT", "article_collection");
			mongoImageCol = ini.get("DEFAULT", "image_collection");
		}
		readArgs(args);


		//////// Starte logic
		generateNoaLabel();


		System.out.println("Start PaperParser");
		if (VERBOSE > 0) {
			startTime = System.currentTimeMillis();
			numberOfDocs = Files.walk(Paths.get(location))
					.parallel()
					.filter(p -> !p.toFile().isDirectory())
					.count();
			System.out.println("Parsing Papers from: " + location);
			System.out.println("Saving it in MongoDB : " + mongoIP + ":" + mongoPort + "   dbName:" + mongoDataBase);
			System.out.println("Number of Documents overall: " + numberOfDocs);
		}

		try (Stream<Path> paths = Files.walk(Paths.get(location))) {
			paths.forEach(yearPath -> {
				if (Files.isRegularFile(yearPath)) {
					try {

						parseXML(yearPath.toAbsolutePath().toString());

					} catch (IOException e) {
						e.printStackTrace();
					} catch (ParserConfigurationException e) {
						e.printStackTrace();
					} catch (SAXException e) {
						e.printStackTrace();
					} catch (XMLStreamException e) {
						e.printStackTrace();
					}
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Artikel mit Bilden die Formeln in der Unterschrift haben: " + withFormula + " von " + articles);
		System.out.println("--- DONE ----");

	}

	private static void readArgs(String[] args)
	{
		if (args.length % 2 != 0)//||args.length==0)
			throw new IllegalArgumentException("Wrong number of Arguments");
		if (args.length == 0)
			System.out.println("DEBUG MODE ACTIVE");

		for (int i = 0; i < args.length; i += 2)//for(String s : args)
		{
			if (!args[i].contains("-") || args[i].length() < 2)
				throw new IllegalArgumentException("Error with ParamList");
			map.put(args[i].substring(1, args[i].length()), args[i + 1]);
		}
		if (map.containsKey("paperLoc"))
			location = map.get("paperLoc");//"D:\\Artikel\\Test Collection\\Springer";
		outputEncoding = "UTF-8";
		if (map.containsKey("verbose"))
			VERBOSE = Integer.parseInt(map.get("verbose"));
		if (map.containsKey("mongoIP"))
			mongoIP = map.get("mongoIP"); //"141.71.5.22";
		if (map.containsKey("mongoPort"))
			mongoPort = Integer.parseInt(map.get("mongoPort")); //27017;
		if (map.containsKey("mongoDatabase"))
			mongoDataBase = map.get("mongoDatabase");
		if (map.containsKey("mongoErrorCollection"))
			mongoErrorCol = map.get("mongoErrorCollection");
		if (map.containsKey("mongoImageCollection"))
			mongoImageCol = map.get("mongoImageCollection");
		if (map.containsKey("mongoJournalCollection"))
			mongoJournalcol = map.get("mongoJournalCollection");

		withDownload = false; //Download Images as binary data?
		outPutFormat = 2; // 0=path, 1= current/overall, 2=percentage.

		try {
			SimpleLayout layout = new SimpleLayout();
			ConsoleAppender consoleAppender = new ConsoleAppender(layout);
			logger.addAppender(consoleAppender);
			FileAppender fileAppender = new FileAppender(layout, "logs/" + uniqueHash + ".log", true);
			logger.addAppender(fileAppender);
			// ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF:
			logger.setLevel(Level.ALL);
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	public static int countFilesInDir(File folder, int count)
	{
		File[] files = folder.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				count++;
			} else {
				countFilesInDir(file, count);
			}
		}

		return count;
	}

	private static void generateNoaLabel()
	{
		System.out.println("888b    888  .d88888b.         d8888 ");
		System.out.println("8888b   888 d88P\" \"Y88b       d88888 ");
		System.out.println("88888b  888 888     888      d88P888 ");
		System.out.println("888Y88b 888 888     888     d88P 888 ");
		System.out.println("888 Y88b888 888     888    d88P  888 ");
		System.out.println("888  Y88888 888     888   d88P   888 ");
		System.out.println("888   Y8888 Y88b. .d88P  d8888888888 ");
		System.out.println("888    Y888  \"Y88888P\"  d88P     888 ");
		System.out.println();
		System.out.println("8888888b.                                     8888888b.                   ");
		System.out.println("888   Y88b                                    888   Y88b                  ");
		System.out.println("888    888                                    888    888                  ");
		System.out.println("888   d88P  8888b.  88888b.   .d88b.  888d888 888   d88P  8888b.  888d888 .d8888b   .d88b.  888d888 ");
		System.out.println("8888888P\"      \"88b 888 \"88b d8P  Y8b 888P\"   8888888P\"      \"88b 888P\"   88K      d8P  Y8b 888P\"   ");
		System.out.println("888        .d888888 888  888 88888888 888     888        .d888888 888     \"Y8888b. 88888888 888     ");
		System.out.println("888        888  888 888 d88P Y8b.     888     888        888  888 888          X88 Y8b.     888     ");
		System.out.println("888        \"Y888888 88888P\"   \"Y8888  888     888        \"Y888888 888      88888P'  \"Y8888  888     ");
		System.out.println("                    888                                                   ");
		System.out.println("                    888                                                   ");
		System.out.println("                    888                                                   ");


	}



	public static void parseXML(String xmlSource) throws IOException, ParserConfigurationException, SAXException, XMLStreamException
	{
		long starting = System.currentTimeMillis();

		ResultSetJournal rsj = new ResultSetJournal();
		Boolean zip = false;
		InputStream stream = null;
		if (xmlSource.endsWith("zip")) {
			Charset charset = ISO_8859_1;
			ZipFile zipFile = new ZipFile(xmlSource, charset);
			for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements(); ) {
				ZipEntry entry = e.nextElement();
				if (entry.getName().endsWith("zip")) {
					continue;
				}
				if (entry.getName().endsWith("xml")) {
					xmlSource = entry.getName();
					stream = zipFile.getInputStream(entry);
					zip = true;
				}
			}
		}

		rsj.setXMLPathComplete((xmlSource));

		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		//docBuilderFactory.setValidating(true);
		docBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		org.w3c.dom.Document document = null;


		rsj.setFile(new File(xmlSource));

		if (zip) {
			document = docBuilder.parse(stream);
		} else {
			try {
				if (xmlSource.endsWith("xml")) {
					document = docBuilder.parse(new File(xmlSource));
				}
			} catch (Exception e) {
				MongoDBRepo.getInstance(mongoIP, mongoPort, mongoDataBase).writeError(xmlSource, e.toString(), "modus");
				System.out.println("Could not been read: " + xmlSource + "\n" + e);
				rsj.setError(xmlSource + ":   " + e);
				document = null;
			}
		}

		if (document != null) {
			//set source:
			if (xmlSource.contains("PMC")) {
				rsj.setSource("PMC");
			} else if (xmlSource.contains("Frontiers")) {
				rsj.setSource("Frontiers");
			} else if (xmlSource.contains("Springer")) {
				rsj.setSource("Springer");
			} else if (xmlSource.contains("Hindawi")) {
				rsj.setSource("Hindawi");
			} else if (xmlSource.contains("Copernicus")) {
				rsj.setSource("Copernicus");
			}

			mapReferences(rsj, document.getDocumentElement(), document.getDocumentElement());
			//System.out.print("mapreferences"); System.out.println( System.currentTimeMillis() - start);
			//start = System.currentTimeMillis();
			getAllTokens(rsj, document.getDocumentElement());
			doSomething(rsj, document.getDocumentElement());
			//System.out.print("doSomething");System.out.println(System.currentTimeMillis() - start);
			//start = System.currentTimeMillis();
			articles += 1;
			if (rsj.hasFormula) withFormula += 1;
			// (rsj, article, article);
			//getAllTokens(rsj, article);
			//doSomething(rsj, article);
			context(rsj);
			//System.out.print("context");System.out.println(System.currentTimeMillis() - start);
			//start = System.currentTimeMillis();
			MongoDBRepo.getInstance(mongoIP, mongoPort, mongoDataBase).writeJournal(rsj, withDownload);
			//System.out.print("mondowrite");System.out.println(System.currentTimeMillis() - start);
			references.clear();
			figureContext.clear();

			switch (VERBOSE) {
				case 0:
					break;
				case 1:
					System.out.printf("%-10d / %-10d\n", ++currentDoc, numberOfDocs);
					break;
				case 2:
					printProgress(startTime, numberOfDocs, ++currentDoc);
					System.out.println(" Processed: " + rsj.getXMLPathComplete());
					break;
				default:
					System.out.println("Wrote: " + rsj.getXMLPathComplete());
					break;
			}

		}

	}

	private static void mapReferences(ResultSetJournal rsj, Node node, Node root)
	{
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);

			if (currentNode.getNodeName().equals("xref") && !currentNode.hasChildNodes()) {
				try {
					String key = currentNode.getAttributes().getNamedItem("rid").getTextContent();
					String value = findReference(key, root);
					if (value == null) {
						Matcher matcher = Pattern.compile("\\d+").matcher(key);
						String m = "";
						while (matcher.find()) {
							m = matcher.group();
						}
						value = m;
					}
					if (value != null) {
						references.put(key, value);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				//calls this method for all the children with node type Element
				mapReferences(rsj, currentNode, root);
			}
		}
	}

	//finds a given reference and returns its label
	private static String findReference(String key, Node node)
	{
		String value = null;
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);

			if (currentNode.getNodeName().equals("ref")
					&& currentNode.getAttributes().getNamedItem("id").getTextContent().equals(key)) {
				NodeList childList = currentNode.getChildNodes();
				for (int j = 0; j < childList.getLength(); j++) {
					Node childNode = childList.item(j);
					if (childNode.getNodeName().equals("label")) {
						value = childNode.getTextContent();
						return value;

					}
				}


			} else if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				//calls this method for all the children with node type Element
				value = findReference(key, currentNode);
				if (value != null) {
					return value;
				}

			}
		}
		return value;
	}

	private static boolean condition(Node currentNode, String key)
	{
		Boolean condition1 = false;
		Boolean condition2 = false;
		Boolean condition3 = false;
		try {
			condition1 = currentNode.getAttributes().getNamedItem("content-type").getTextContent().equals("numbered");
		} catch (Exception e) {
		}
		try {
			condition2 = currentNode.getAttributes().getNamedItem("id").getTextContent().equals(key);
		} catch (Exception e) {
		}
		try {
			condition3 = currentNode.getAttributes().getNamedItem("rid").getTextContent().equals(key);
		} catch (Exception e) {
		}
		Boolean condition = (condition1 && (condition2 || condition3));
		return condition;


	}

	private static void getAllTokens(ResultSetJournal rsj, Node root)
	{
		getAbstract(rsj, root);
		getBody(rsj, root);
	}

	private static void getAbstract(ResultSetJournal rsj, Node root)
	{
		if ((root.getNodeType() == Node.TEXT_NODE)) {
			rsj.setAbstract("No abstract found");
			return;
		}
		NodeList nodeList = root.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);
			try {
				if (currentNode.getNodeName().matches("[aA]bstract")) {
					try {
						rsj.setAbstract(getContentNoWhiteSpace(currentNode));
						return;
					} catch (Exception e) {
						System.out.println("Error getAbstract:" + e);
					}
				}
			} catch (NullPointerException e) {

			}


		}
		if (rsj.getAbstract() == null) {

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node currentNode = nodeList.item(i);

				if (currentNode.getNodeType() == Node.ELEMENT_NODE) {

					if (currentNode.getNodeName().matches("[Bb]ody")) {
						continue;
					} else {
						if (currentNode.hasChildNodes()) {
							getAbstract(rsj, currentNode);
						}
					}
				}
			}
		}
	}

	private static void getAllSections(ResultSetJournal rsj, Node root)
	{
		{
			NodeList nodeList = root.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node currentNode = nodeList.item(i);
				try {
					if (currentNode.getNodeName().equals("sec")) {
						try {
							rsj.getSections().add(getContentNoWhiteSpace(currentNode));
						} catch (Exception e) {
							System.out.println("Error getAllSections:" + e);
						}
					}
				} catch (NullPointerException e) {
					System.out.println("Error getAllSections:" + e);

				}
			}
		}
	}

	private static void getBody(ResultSetJournal rsj, Node root)
	{
		NodeList nodeList = root.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);
			try {
				if (currentNode.getNodeName().matches("[bB]ody")) {
					try {
						getAllSections(rsj, currentNode);
						rsj.setBody(getContentNoWhiteSpace(currentNode));
						if (rsj.getBody().equals(null)) {
							rsj.setBody("No body in document");
						}
					} catch (Exception e) {
						System.out.println("Error getBody:" + e);
					}
				}
			} catch (NullPointerException e) {
				System.out.println("Error getBody:" + e);
			}
		}
		if (rsj.getBody() == null) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node currentNode = nodeList.item(i);
				if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
					getBody(rsj, currentNode);
				}
			}
		}
	}

	private static void doSomething(ResultSetJournal rsj, Node node)
	{
		String DOI = null;
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);
			if (currentNode.getNodeName().equals("article-id")) {
				//ID id = new ID();
				try {
					if (currentNode.getAttributes().item(0).getNodeValue().equals("doi")) {
						try {
							rsj.setJournalDOI(currentNode.getTextContent().trim());
						} catch (Exception e) {
							rsj.setJournalDOI("NO DOI FOUND");
						}
					}
					if (currentNode.getAttributes().item(0).getNodeValue().equals("publisher-id")) {
						if (rsj.getIssue() == null) {
							rsj.setIssue("Article ID " + currentNode.getTextContent());
						}
					}
					if (currentNode.getAttributes().item(0).getNodeValue().equals("pmc")) {
						rsj.setPmcID(currentNode.getTextContent());

					}
				} catch (NullPointerException e) {
//
				}
			}


			if (currentNode.getNodeName().equals("ArticleDOI")) {
				rsj.setJournalDOI(currentNode.getTextContent());
			}
			if (currentNode.getNodeName().matches("issue|meta:IssueId")) {
				if (currentNode.getParentNode().getNodeName().matches("article-meta|meta:Info")) {
					rsj.setIssue(currentNode.getTextContent());
				}
			}
			if (currentNode.getNodeName().equals("meta:DOI")) {
				if (currentNode.getParentNode().getNodeName().equals("meta:Info")) {
					rsj.setJournalDOI(currentNode.getTextContent());
				}
			}
			if (currentNode.getNodeName().equals("elocation-id")) {
				if (currentNode.getParentNode().getNodeName().matches("article-meta") || rsj.getIssue().equals(null)) {
					rsj.setIssue(currentNode.getTextContent());
				}
			}
			if (currentNode.getNodeName().equals("article-title") || currentNode.getNodeName().equals("ArticleTitle")) {
				try {
					if (currentNode.getParentNode().getNodeName().equals("title-group") || currentNode.getParentNode().getNodeName().equals("ArticleInfo")) {
						rsj.setTitle(getContentNoWhiteSpace(currentNode));
					}


				} catch (Exception e) {
					rsj.setTitle("NO TAG 'article-title' FOUND");
				}
			}

			if (currentNode.getNodeName().matches("[Ll]icense|CopyrightComment")) {
				try {
					String license = getContentNoWhiteSpace(currentNode);
					try {
						license = currentNode.getAttributes().getNamedItem("xlink:href").getTextContent() + " " + license;
					} catch (Exception e) {
					}
					rsj.setLicense(license);

				} catch (Exception e) {
					rsj.setLicense("NO TAG 'license' FOUND");
				}
			}
			if (currentNode.getNodeName().matches("[aA]bstract")) {
				try {
					rsj.setAbstract(getContentNoWhiteSpace(currentNode));

				} catch (Exception e) {
					System.out.println("Error getAbstract:" + e);
				}
			}
			if (currentNode.getNodeName().matches("[Bb]ody")) {
				try {
					rsj.setBody(getContentNoWhiteSpace(currentNode));

				} catch (Exception e) {
					System.out.println("Error getBod<:" + e);
				}
			}
			if (currentNode.getNodeName().matches("volume|meta:VolumeId")) {
				try {
					if (currentNode.getParentNode().getNodeName().matches("article-meta|meta:Info"))
						rsj.setVolume(currentNode.getTextContent());

				} catch (Exception e) {
					rsj.setVolume("NO TAG 'volume' FOUND");
				}
			}
			if (currentNode.getNodeName().matches("Keyword|kwd")) {
				rsj.getKeywords().add(getContentNoWhiteSpace(currentNode));
			}


			if (currentNode.getNodeName().matches("publisher-name|PublisherName")) {
				try {
					if (currentNode.getParentNode().getParentNode().getNodeName().equals("journal-meta") || currentNode.getParentNode().getNodeName().equals("PublisherInfo"))
						rsj.setPublisher(currentNode.getTextContent());

				} catch (Exception e) {
					rsj.setPublisher("NO TAG 'publisher' FOUND");
				}
			}
			if (currentNode.getNodeName().equals("page-count") && rsj.getPages() == null) {
				try {
					rsj.setPages(currentNode.getAttributes().item(0).getTextContent());

				} catch (Exception e) {
					rsj.setPages("NO TAG 'pages' FOUND");
				}
			}
			if (currentNode.getNodeName().matches("fpage|ArticleFirstPage")) {
				if (currentNode.getParentNode().getNodeName().matches("article-meta|ArticleInfo")) {
					String firstPage = currentNode.getTextContent();
					String lastPage = "";
					if (currentNode.getNextSibling().getNodeName().matches("lpage|ArticleLastPage")) {
						lastPage = currentNode.getNextSibling().getTextContent();
					} else if (currentNode.getNextSibling().getNextSibling().getNodeName().matches("lpage|ArticleLastPage")) {
						lastPage = currentNode.getNextSibling().getNextSibling().getTextContent();
					}
					rsj.setPages(firstPage + "-" + lastPage);
				}
			}

			if (currentNode.getNodeName().equals("contrib")) {
				try {
					if (currentNode.getAttributes().item(0).getNodeValue().equals("author")) {

						try {
							for (int j = 0; j < currentNode.getChildNodes().getLength(); j++) {
								Author a = new Author();
								if (currentNode.getChildNodes().item(j).getNodeName().equals("name")) {
									for (int h = 0; h < currentNode.getChildNodes().item(j).getChildNodes().getLength(); h++) {
										if (currentNode.getChildNodes().item(j).getChildNodes().item(h).getNodeName().equals("surname"))
											a.setLastName(currentNode.getChildNodes().item(j).getChildNodes().item(h).getTextContent());


										if (currentNode.getChildNodes().item(j).getChildNodes().item(h).getNodeName().equals("given-names"))
											a.setFirstName(currentNode.getChildNodes().item(j).getChildNodes().item(h).getTextContent());
									}

								}
								if (a.getFirstName() != null && a.getLastName() != null)
									rsj.getAuthors().add(a);

							}
						} catch (Exception e) {
							rsj.setJournalName("NO AUTHOR FOUND");
						}

					}

				} catch (NullPointerException e) {

				}
			}
			if (currentNode.getNodeName().equals("Author")) {

				try {

					for (int j = 0; j < currentNode.getChildNodes().getLength(); j++) {
						Author a = new Author();
						if (currentNode.getChildNodes().item(j).getNodeName().equals("AuthorName")) {
							for (int h = 0; h < currentNode.getChildNodes().item(j).getChildNodes().getLength(); h++) {
								if (currentNode.getChildNodes().item(j).getChildNodes().item(h).getNodeName().equals("FamilyName"))
									if (a.getLastName() == null) {
										a.setLastName(currentNode.getChildNodes().item(j).getChildNodes().item(h).getTextContent());
									} else {
										a.setLastName(a.getLastName() + " " + currentNode.getChildNodes().item(j).getChildNodes().item(h).getTextContent());
									}


								if (currentNode.getChildNodes().item(j).getChildNodes().item(h).getNodeName().equals("GivenName"))
									if (a.getFirstName() == null) {
										a.setFirstName(currentNode.getChildNodes().item(j).getChildNodes().item(h).getTextContent());
									} else {
										a.setFirstName(a.getFirstName() + " " + currentNode.getChildNodes().item(j).getChildNodes().item(h).getTextContent());
									}
							}
						}
						if (a.getFirstName() != null && a.getLastName() != null)
							rsj.getAuthors().add(a);
					}
				} catch (Exception e) {
					rsj.setJournalName("NO AUTHOR FOUND");
				}
			}
			if (currentNode.getNodeName().equals("journal-title")) {
				try {
					rsj.setJournalName(currentNode.getTextContent());
				} catch (Exception e) {
					rsj.setJournalName("NO NAME FOUND");
				}
			}
			if (currentNode.getNodeName().equals("JournalTitle")) {
				if (currentNode.getParentNode().getNodeName().equals("JournalInfo")) {
					try {
						rsj.setJournalName(currentNode.getTextContent());
					} catch (Exception e) {
						rsj.setJournalName("NO NAME FOUND");
					}
				}
			}
			if (currentNode.getNodeName().equals("journal-id") && !rsj.getXMLPathComplete().contains("PMC")) //TODO test Hindawi links
			{
				try {
					if (currentNode.getAttributes().item(0).getNodeValue().equals("publisher-id")) {
						try {
							rsj.setPublisherId(currentNode.getTextContent());
						} catch (Exception e) {
							rsj.setPublisherId("NO ID FOUND");
						}
					}
				} catch (NullPointerException e) {

				}
			}

			if (rsj.getFullDate() == null || rsj.getFullDate().getYear() == null || rsj.getFullDate().getMonth() == null || rsj.getFullDate().getDay() == null || rsj.getFullDate().getRank() > 1) {
				getDate(currentNode, rsj);
			}

			if (currentNode.getNodeName().equals("p")) {
				if (currentNode.getTextContent().contains("Fig.")) {
					{
						Boolean match = false;
						String key = "Ch1.F";
						List<String> keys = new ArrayList<>();
						String textContent = currentNode.getTextContent();
						Pattern pattern = Pattern.compile("Fig\\.\\u00A0(\\d+)");
						Matcher matcher = pattern.matcher((textContent));
						String figID = "";
						while (matcher.find()) {
							match = true;
							keys.add((key + matcher.group(1)));
							figID = matcher.group();
						}
						if (match) {
							List<String> value = new ArrayList<>();

							for (int l = 0; l < keys.size(); l++) {
								if (figureContext.get(keys.get(l)) == null) {
									value = new ArrayList<>();
									value.add(getContentNoWhiteSpace(currentNode, figID));
									figureContext.put(keys.get(l), value);
								} else {
									value = (List<String>) figureContext.get(keys.get(l));
									String container = getContentNoWhiteSpace(currentNode, figID);
									if (!value.contains(container)) {
										value.add(getContentNoWhiteSpace(currentNode, figID));
									}
									figureContext.put(keys.get(l), value);
								}
							}
						}
					}
				}
			}
			if (currentNode.getNodeName().matches("xref|InternalRef")) {
				try {
					if (currentNode.getAttributes().getNamedItem("ref-type").getTextContent().equals("fig")) {
						String key = currentNode.getAttributes().getNamedItem("rid").getTextContent();
						List<String> value = new ArrayList<>();
						if (figureContext.get(key) == null) {
							value = new ArrayList<>();
							value.add(getContentNoWhiteSpace(currentNode.getParentNode(), key));
							figureContext.put(key, value);
						} else {
							value = (List<String>) figureContext.get(key);
							if (!value.contains(getContentNoWhiteSpace(currentNode.getParentNode(), key))) {
								value.add(getContentNoWhiteSpace(currentNode.getParentNode(), key));
							}
							figureContext.put(key, value);
						}
					}
				} catch (Exception e) {
					//e.printStackTrace();
				}
				try {
					if (currentNode.getAttributes().getNamedItem("RefID").getTextContent().startsWith("Fig")) {
						String key = currentNode.getAttributes().getNamedItem("RefID").getTextContent();
						List<String> value = new ArrayList<>();
						if (figureContext.get(key) == null) {
							value = new ArrayList<>();
							value.add(getContentNoWhiteSpace(currentNode.getParentNode(), key));
							figureContext.put(key, value);
						} else {
							value = (List<String>) figureContext.get(key);
							if (!value.contains(getContentNoWhiteSpace(currentNode.getParentNode(), key))) {
								value.add(getContentNoWhiteSpace(currentNode.getParentNode(), key));
							}
							figureContext.put(key, value);
						}
					}
				} catch (Exception e) {
					//e.printStackTrace();
				}
			}
			try {
				if (currentNode.getNodeName().matches("inline-formula|disp-formula")) {

					if (node.getParentNode().getNodeName().matches("Caption|caption")) {
						rsj.setHasFormula(true);
					} else if (node.getParentNode().getParentNode().getNodeName().matches("Caption|caption")) {
						rsj.setHasFormula(true);
					} else if (node.getParentNode().getParentNode().getParentNode().getNodeName().matches("Caption|caption")) {
						rsj.setHasFormula(true);
					}
				}

				if (currentNode.getNodeName().matches("ref|Citation")) {
					Citation citation = new Citation();
					getCitation(currentNode, citation);
					rsj.Bibliography.add(citation);
				}
				if (currentNode.getNodeName().matches("fig|Figure")) {
					Result r = new Result();
					r.setDOI(DOI);
					NodeList fig = currentNode.getChildNodes();
					for (int j = 0; j < fig.getLength(); j++) {
						Node figNode = fig.item(j);
						switch (figNode.getNodeName()) {
							case "Caption":
								r.setCaptionBody(getContentNoWhiteSpace(figNode));
								break;
							case "label":
								r.setLabel(getContentNoWhiteSpace(figNode));
								break;

							case "caption":
								//TODO:  This might be an error source if a Caption has more than 1 <p> section. 0 and 2 are left over, because they are only newline feed commands
								try {
									if (currentNode.getParentNode().getNodeName().equals("fig-group")) {
										for (int g = 0; g < currentNode.getChildNodes().getLength(); g++) {
											if (currentNode.getParentNode().getChildNodes().item(g).getNodeName().equals("caption")) {
												r.setCaptionBody(getContentNoWhiteSpace(currentNode.getParentNode().getChildNodes().item(g)) + getContentNoWhiteSpace(figNode));

											}


										}
									} else {
										r.setCaptionBody(getContentNoWhiteSpace(figNode));

									}
								} catch (Exception e) {
									r.setCaptionBody("NO BODY FOUND");
								}
								break;
							case "MediaObject":
								r.setGraphicDOI(figNode.getChildNodes().item(1).getAttributes().getNamedItem("FileRef").getNodeValue());
								break;
							case "graphic":
								r.setGraphicDOI(figNode.getAttributes().getNamedItem("xlink:href").getNodeValue());
								break;
							default:
								break;
						}
					}
					try {
						r.setFigID(currentNode.getAttributes().getNamedItem("id").getTextContent());
					} catch (Exception E) {
					}
					try {
						r.setFigID(currentNode.getAttributes().getNamedItem("ID").getTextContent());
					} catch (Exception E) {
					}


					Node iter = currentNode.getParentNode();
					if (iter.getNodeName().equals("fig-group"))
						r.setFigGroupID(iter.getAttributes().getNamedItem("id").getTextContent());
					if (r.getCaptionBody() == null && iter != null) {
						NodeList figgroup = iter.getChildNodes();
						for (int j = 0; j < figgroup.getLength(); j++) {
							Node figNode = figgroup.item(j);
							switch (figNode.getNodeName()) {

								case "label":
									r.setLabel(getContentNoWhiteSpace(figNode));
									break;
								case "caption":
									//TODO:  This might be an error source if a Caption has more than 1 <p> section. 0 and 2 are left over, because they are only newline feed commands
									try {

										r.setCaptionBody(getContentNoWhiteSpace(figgroup.item(j)));
									} catch (Exception e) {
										r.setCaptionBody("NO BODY FOUND");
									}
									break;
								case "graphic":
									r.setGraphicDOI(figNode.getAttributes().getNamedItem("xlink:href").getNodeValue());
									break;
								default:
									break;
							}
						}
					}
					r.setRsj(rsj);
					r.setFindingID(rsj.getResultList().size());
					rsj.getResultList().add(r);
				}
				if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
					//calls this method for all the children with node type Element
					doSomething(rsj, currentNode);
				}
			} catch (Exception e) {
				//TODO
			}
		}
	}

	private static void getDate(Node currentNode, ResultSetJournal rsj)
	{

		//Dates are contained in different tags. Some tags are more accurate than others, but not all tags are usually found in the same article.
		// The date information is ranked according to what tag it comes from. It can only be overwritten by higher ranked tags

		//Get date for Springer
		if (currentNode.getNodeName().equals("OnlineDate")) {
			if (currentNode.getParentNode().getNodeName().equals("ArticleHistory")) {
				NodeList children = currentNode.getChildNodes();
				metadata.PublicationDate date = new metadata.PublicationDate();
				for (int m = 0; m < children.getLength(); m++) {
					Node childNode = children.item(m);


					if (childNode.getNodeName().equals("Day")) {
						date.setDay(childNode.getTextContent());
					}
					if (childNode.getNodeName().equals("Month")) {
						date.setMonth(childNode.getTextContent());
					}
					if (childNode.getNodeName().equals("Year")) {
						date.setYear(childNode.getTextContent());
					}
				}
				date.setRank(1);
				rsj.setFullDate(date);
				rsj.setPublicationYear(date.getYear());
			}
		} else if (currentNode.getNodeName().equals("Accepted")) {
			if (rsj.getFullDate() != null) {
				if (rsj.getFullDate().getRank() < 2) {
					return;
				}
			}
			if (currentNode.getParentNode().getNodeName().equals("ArticleHistory")) {
				NodeList children = currentNode.getChildNodes();
				metadata.PublicationDate date = new metadata.PublicationDate();
				for (int m = 0; m < children.getLength(); m++) {
					Node childNode = children.item(m);


					if (childNode.getNodeName().equals("Day")) {
						date.setDay(childNode.getTextContent());
					}
					if (childNode.getNodeName().equals("Month")) {
						date.setMonth(childNode.getTextContent());
					}
					if (childNode.getNodeName().equals("Year")) {
						date.setYear(childNode.getTextContent());
					}
				}
				date.setRank(2);
				rsj.setFullDate(date);
				rsj.setPublicationYear(date.getYear());
			}


		}

		//Get date for other formats: try node name pub-date(usually Frontiers and PMC)
		//some pub-date tags are applicable: with attribute epub (1.priority) or ppub (2.priority) or no attribute
		if (currentNode.getNodeName().equals("pub-date")) {
			if (currentNode.hasAttributes()) {
				if (currentNode.getAttributes().item(0).getNodeValue().equals("epub") || currentNode.getAttributes().getNamedItem("pub-type").getNodeValue().equals("epub")) {
					NodeList children = currentNode.getChildNodes();
					metadata.PublicationDate date = new metadata.PublicationDate();
					for (int m = 0; m < children.getLength(); m++) {
						Node childNode = children.item(m);


						if (childNode.getNodeName().equals("day")) {
							date.setDay(childNode.getTextContent());
						}
						if (childNode.getNodeName().equals("month")) {
							date.setMonth(childNode.getTextContent());
						}
						if (childNode.getNodeName().equals("year")) {
							date.setYear(childNode.getTextContent());
						}
					}
					date.setRank(1);
					rsj.setFullDate(date);
					rsj.setPublicationYear(date.getYear());
					return;
				} else if (currentNode.getAttributes().item(0).getNodeValue().equals("ppub") || currentNode.getAttributes().getNamedItem("pub-type").getNodeValue().equals("ppub")) {
					if (rsj.getFullDate() != null) {
						if (rsj.getFullDate().getRank() < 3) {
							return;
						}
					}
					NodeList children = currentNode.getChildNodes();
					metadata.PublicationDate date = new metadata.PublicationDate();
					for (int m = 0; m < children.getLength(); m++) {
						Node childNode = children.item(m);


						if (childNode.getNodeName().equals("day")) {
							date.setDay(childNode.getTextContent());
						}
						if (childNode.getNodeName().equals("month")) {
							date.setMonth(childNode.getTextContent());
						}
						if (childNode.getNodeName().equals("year")) {
							date.setYear(childNode.getTextContent());
						}
					}
					date.setRank(3);
					rsj.setFullDate(date);
					rsj.setPublicationYear(date.getYear());
					return;
				} else if (currentNode.getAttributes().item(0).getNodeValue().equals("archival-date") || currentNode.getAttributes().getNamedItem("pub-type").getNodeValue().equals("archival-date")) {
					if (rsj.getFullDate() != null) {
						if (rsj.getFullDate().getRank() < 2) {
							return;
						}
					}
					NodeList children = currentNode.getChildNodes();
					metadata.PublicationDate date = new metadata.PublicationDate();
					for (int m = 0; m < children.getLength(); m++) {
						Node childNode = children.item(m);


						if (childNode.getNodeName().equals("day")) {
							date.setDay(childNode.getTextContent());
						}
						if (childNode.getNodeName().equals("month")) {
							date.setMonth(childNode.getTextContent());
						}
						if (childNode.getNodeName().equals("year")) {
							date.setYear(childNode.getTextContent());
						}
					}
					date.setRank(2);
					rsj.setFullDate(date);
					rsj.setPublicationYear(date.getYear());
				}


				//if there are no attributes (usually Copernicus):
			} else {
				NodeList children = currentNode.getChildNodes();
				metadata.PublicationDate date = new metadata.PublicationDate();
				for (int m = 0; m < children.getLength(); m++) {
					Node childNode = children.item(m);


					if (childNode.getNodeName().equals("day")) {
						date.setDay(childNode.getTextContent());
					}
					if (childNode.getNodeName().equals("month")) {
						date.setMonth(childNode.getTextContent());
					}
					if (childNode.getNodeName().equals("year")) {
						date.setYear(childNode.getTextContent());
					}
				}
				date.setRank(1);
				rsj.setFullDate(date);
				rsj.setPublicationYear(date.getYear());
			}
		}


		//try node name date (usually Hindawi, sometimes PMC
		if (currentNode.getNodeName().equals("date")) {
			if (currentNode.hasAttributes()) {
				if (currentNode.getAttributes().item(0).getNodeValue().equals("accepted") || currentNode.getAttributes().getNamedItem("date-type").getNodeValue().equals("accepted")) {
					if (rsj.getFullDate() != null) {
						if (rsj.getFullDate().getRank() < 3) {
							return;
						}
					}
					NodeList children = currentNode.getChildNodes();
					metadata.PublicationDate date = new metadata.PublicationDate();
					for (int m = 0; m < children.getLength(); m++) {
						Node childNode = children.item(m);


						if (childNode.getNodeName().equals("day")) {
							date.setDay(childNode.getTextContent());
						}
						if (childNode.getNodeName().equals("month")) {
							date.setMonth(childNode.getTextContent());
						}
						if (childNode.getNodeName().equals("year")) {
							date.setYear(childNode.getTextContent());
						}
					}
					date.setRank(3);
					rsj.setFullDate(date);
					rsj.setPublicationYear(date.getYear());
				} else if (currentNode.getAttributes().item(0).getNodeValue().equals("pub") || currentNode.getAttributes().getNamedItem("date-type").getNodeValue().equals("pub")) {
					NodeList children = currentNode.getChildNodes();
					metadata.PublicationDate date = new metadata.PublicationDate();
					for (int m = 0; m < children.getLength(); m++) {
						Node childNode = children.item(m);


						if (childNode.getNodeName().equals("day")) {
							date.setDay(childNode.getTextContent());
						}
						if (childNode.getNodeName().equals("month")) {
							date.setMonth(childNode.getTextContent());
						}
						if (childNode.getNodeName().equals("year")) {
							date.setYear(childNode.getTextContent());
						}
					}
					date.setRank(1);
					rsj.setFullDate(date);
					rsj.setPublicationYear(date.getYear());
				}

			}
		}

	}

	private static String getContentNoWhiteSpace(Node node, String key)
	{

		String content = "";
		if (node.getNodeName().matches("Para|p")) {
			NodeList childNodes = node.getChildNodes();
			for (int g = 0; g < childNodes.getLength(); g++) {
				Node child = childNodes.item(g);
				try {
					content += getContentNoWhiteSpace(child, key).replaceAll(key, ("#figure#"));
				} catch (Exception e) {
				}
			}
			content += "\n";

		} else if (node.getNodeName().matches("xref|InternalRef")) {
			try {
				if (node.getAttributes().getNamedItem("ref-type").getTextContent().equals("fig")) {
					if (node.getAttributes().getNamedItem("rid").getTextContent().equals(key)) {
						content += "#figure#";

					}
				} else if (!node.hasChildNodes()) {
					content += (" " + references.get(node.getAttributes().getNamedItem("rid").getTextContent()) + " ");
				} else {
					content += node.getTextContent();
				}
			} catch (Exception e) {

			}
			try {
				if (node.getAttributes().getNamedItem("RefID").getTextContent().startsWith("Fig")) {
					if (node.getAttributes().getNamedItem("RefID").getTextContent().equals(key)) {
						content += "#figure#";

					}
				} else if (!node.hasChildNodes()) {
					content += (" " + references.get(node.getAttributes().getNamedItem("rid").getTextContent()) + " ");
				} else {
					content += node.getTextContent();
				}

			} catch (Exception e) {

			}

		} else if (node.hasChildNodes()) {
			NodeList childNodes = node.getChildNodes();
			for (int g = 0; g < childNodes.getLength(); g++) {
				Node child = childNodes.item(g);
				try {
					content += getContentNoWhiteSpace(child, key);
				} catch (Exception e) {

				}
			}
		} else if (node.getNodeType() == Node.TEXT_NODE) {

			String content1 = node.getNodeValue().replaceAll("\n", " ").replaceAll("\t", " ").trim();
			if (!(content1.endsWith(" "))) {
				content1 += " ";
			}
			content += content1;

		}
		return (content.trim().replaceAll("\\s+", " ") + " ");
	}

	private static void getCitation(Node citationNode, Citation citation)
	{
		try {
			NodeList citationPartList = citationNode.getChildNodes();
			if (citation.getText() == null) {
				citation.setText(getContentNoWhiteSpace(citationNode));
			}

			for (int j = 0; j < citationPartList.getLength(); j++) {
				Node citationPart = citationPartList.item(j);

				if (citationPart.getNodeName().matches("mixed-citation|BibUnstructured")) {
					citation.setText(citationPart.getTextContent().trim());
				}
				if (citationPart.getNodeName().matches("BibAuthorName|name")) {
					Author author = new Author();
					for (int h = 0; h < citationPart.getChildNodes().getLength(); h++) {
						if (citationPart.getChildNodes().item(h).getNodeName().matches("FamilyName|given-names"))
							if (author.getLastName() == null) {
								author.setLastName(citationPart.getChildNodes().item(h).getTextContent());
							} else {
								author.setLastName(author.getLastName() + " " + citationPart.getChildNodes().item(h).getTextContent());
							}


						if (citationPart.getChildNodes().item(h).getNodeName().matches("GivenName|Initials|surname"))
							if (author.getFirstName() == null) {
								author.setFirstName(citationPart.getChildNodes().item(h).getTextContent());
							} else {
								author.setFirstName(author.getFirstName() + " " + citationPart.getChildNodes().item(h).getTextContent());
							}


					}
					if (author.getFirstName() != null && author.getLastName() != null)
						citation.getAuthors().add(author);

				} else if (citationPart.getNodeName().matches("ArticleTitle|article-title")) {
					citation.setTitle(citationPart.getTextContent().trim());
				} else if (citationPart.getNodeName().matches("Year|year")) {
					citation.setYear(citationPart.getTextContent().trim());
				} else if (citationPart.getNodeName().matches("JournalTitle|source")) {
					citation.setJournal(citationPart.getTextContent().trim());
				} else if (citationPart.getNodeName().matches("Handle")) {
					ID id = new ID();
					id.setNumber(citationPart.getTextContent().trim());
					id.setType(citationPart.getParentNode().getAttributes().item(0).getNodeValue());
					citation.getIDs().add(id);
				} else if (citationPart.getNodeName().matches("pub-id")) {
					ID id = new ID();
					id.setType(citationPart.getAttributes().item(0).getNodeValue());
					id.setNumber(citationPart.getTextContent().trim());
					citation.getIDs().add(id);

				}


				if (citationPart.getNodeType() == Node.ELEMENT_NODE) {
					getCitation(citationPart, citation);
				}

			}
		} catch (Exception e) {

		}
	}


	private static String getContentNoWhiteSpace(Node node)
	{
		try {
			String content = "";
			if (node.getNodeName().matches("inline-formula|disp-formula")) {

				if (node.getParentNode().getNodeName().matches("Caption|caption|article-title|ArticleTitle")) {
					content += getFormula(node);
				} else if (node.getParentNode().getParentNode().getNodeName().matches("Caption|caption")) {
					content += getFormula(node);
				} else if (node.getParentNode().getParentNode().getParentNode().getNodeName().matches("Caption|caption")) {
					content += getFormula(node);
				} else if (node.getAttributes().getNamedItem("ref-type") != null) {
					if (node.getAttributes().getNamedItem("ref-type").getTextContent().equals("fig")) {
						content += getFormula(node);
					}
				} else {
					content += node.getTextContent().replaceAll("\n", "").replaceAll("\t", "").trim();
				}


			} else if (node.getNodeName().equals("sub")) {
				content += ("<sub>" + node.getTextContent() + "</sub>");

			} else if (node.getNodeName().equals("sup")) {
				content += ("<sup>" + node.getTextContent() + "</sup>");

			} else if (node.getNodeName().matches("Heading|title")) {
				content += (node.getTextContent() + "\n");

			} else if (node.getNodeName().matches("Para|p")) {
				NodeList childNodes = node.getChildNodes();
				for (int g = 0; g < childNodes.getLength(); g++) {
					Node child = childNodes.item(g);
					content += getContentNoWhiteSpace(child);
				}
				content += "\n";

			} else if (node.getNodeName().equals("xref")) {
				if (node.getParentNode().getNodeName().equals("article-title")) {

				} else if (!node.hasChildNodes()) {
					content += (" " + references.get(node.getAttributes().getNamedItem("rid").getTextContent()) + " ");
				} else {
					content += node.getTextContent();
				}
			} else if (node.hasChildNodes()) {
				NodeList childNodes = node.getChildNodes();
				for (int g = 0; g < childNodes.getLength(); g++) {
					Node child = childNodes.item(g);
					content += getContentNoWhiteSpace(child);
				}
			} else if (node.getNodeType() == Node.TEXT_NODE) {

				String content1 = node.getNodeValue().replaceAll("\n", " ").replaceAll("\t", " ").trim();
				if (!(content1.endsWith(" "))) {
					content1 += " ";
				}
				content += content1;
			}
			return (content.trim().replaceAll("\\s+", " ") + " ");
		} catch (Exception e) {

		}
		return "Error";

	}

	private static String getFormula(Node node)
	{

		//TODO find a good solution for formulas. Currently, only the text part will be shown but no formatting
		/*String formula= null;
		try {
			formula = convertFormula(node);
		} catch (SaxonApiException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		NodeList list = node.getChildNodes();
    	for (int j=0; j<list.getLength(); j++){
    		if(list.item(j).getNodeName().equals("mml2tex")){
				formula=list.item(j).getTextContent();
			}
		}


		if (formula.contains("overset")){
			String overset = "\\\\overset\\{\\\\mathrm\\{‾}}";
			String overline = "\\\\overline{";
			formula = formula.replaceAll(overset, overline);
			overset = "\\\\overset\\{ˇ}";
			String check ="\\\\check";
		}




		formula=" <math>"+formula+"</math> ";*/
		//return formula;
		return node.getTextContent();
	}

	private static void context(ResultSetJournal rsj)
	//this function matches the result figures with any figures that were referenced in the text, so each figure object will include the context
	{
		for (int i = 0; i < rsj.resultList.size(); i++) {
			if (figureContext.containsKey(rsj.resultList.get(i).figID))
				rsj.resultList.get(i).setContext((List<String>) figureContext.get(rsj.resultList.get(i).figID));
			else
				rsj.resultList.get(i).setContext((List<String>) figureContext.get(rsj.resultList.get(i).figGroupID));
		}
	}

	private static void printProgress(long startTime, long total, long current)
	{
		long eta = current == 0 ? 0 :
				(total - current) * (System.currentTimeMillis() - startTime) / current;

		String etaHms = current == 0 ? "N/A" :
				String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
						TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
						TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

		StringBuilder string = new StringBuilder(140);
		int percent = (int) (current * 100 / total);
		string
				.append('\r')
				.append(String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")))
				.append(String.format(" %d%% [", percent))
				.append(String.join("", Collections.nCopies(percent, "=")))
				.append('>')
				.append(String.join("", Collections.nCopies(100 - percent, " ")))
				.append(']')
				.append(String.join("", Collections.nCopies((int) (Math.log10(total)) - (int) (Math.log10(current)), " ")))
				.append(String.format(" %d/%d, ETA: %s", current, total, etaHms));

		System.out.print(string + "  ");
	}


}
