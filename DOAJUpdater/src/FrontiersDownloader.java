import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;
import org.w3c.dom.NodeList;

public class FrontiersDownloader {

	public static void downloadArticle(String doi, String outputFolder) throws IOException {
		
		System.out.println(doi);

		String articleURL = "https://www.frontiersin.org/articles/" + doi;
		String downloadURL = "https://www.frontiersin.org/articles/" + doi + "/xml/nlm";

		if (!Updater.exists(downloadURL)) {
			FileWriter writer = new FileWriter(outputFolder + "NotDownloaded.txt", true);
			writer.write(doi + System.lineSeparator());
			writer.close();
			return;
		}

		try {
			doi = doi.replaceAll("/", "_");
			String folderPath = outputFolder + "DownloadedArticles/Frontiers/";
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			DocumentBuilder db = dbf.newDocumentBuilder();
			org.w3c.dom.Document xml = db.parse(new URL(downloadURL).openStream());
			NodeList nodes = xml.getElementsByTagName("graphic");

			Document doc = Jsoup.connect(articleURL).get();
			Elements img = doc.getElementsByTag("a");
			for (Element el : img) {
				if (!el.hasAttr("href") || !el.hasAttr("target")) {
					continue;
				}
				String src = el.absUrl("href");
				if (!src.endsWith(".jpg")) {
					continue;
				}
				for (int i = 0; i < nodes.getLength(); i++) {
					org.w3c.dom.Element element = (org.w3c.dom.Element) nodes.item(i);
					String href = element.getAttribute("xlink:href");
					href = href.substring(0, href.lastIndexOf(".")).replace("0", "");
					String srcName = src.substring(src.lastIndexOf("/") + 1, src.lastIndexOf(".")).replace("0", "");
					;
					if (href.equals(srcName)) {
						element.setAttribute("xlink:href", src);
					}

				}

			}

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			Result output = new StreamResult(folderPath + doi + ".xml");
			Source input = new DOMSource(xml);
			transformer.transform(input, output);

		} catch (IOException | ParserConfigurationException | SAXException | TransformerFactoryConfigurationError
				| TransformerException e) {
			e.printStackTrace();
		}

	}
}
