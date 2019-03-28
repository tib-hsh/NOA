import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FrontiersDownloader {
	
	public static void downloadArticle(String doi, String outputFolder) throws IOException {
		
		String articleURL = "https://www.frontiersin.org/articles/" + doi;
		String downloadURL = "https://www.frontiersin.org/articles/" + doi + "/xml/nlm";
		
		if(!Updater.exists(downloadURL)) {
			FileWriter writer = new FileWriter(outputFolder + "NewArticleDOIs/NotDownloaded.txt", true);
			writer.write(doi + System.lineSeparator());
			writer.close();
			return;
		}
		
	    try {
	        URL website = new URL(downloadURL);
	        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
	        doi = doi.replaceAll("/", "_");
			String folderPath = outputFolder + "DownloadedArticles/Frontiers/" + doi + "/";
	        File f = new File(folderPath);
	        if(f.exists()) { 
	            return;
	        }			
	        File dir = new File(folderPath);
	        dir.mkdir();
		    FileOutputStream fos = new FileOutputStream(folderPath + doi + ".xml");
		    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		    fos.close();
		    downloadImages(articleURL, folderPath);
	    } catch (MalformedURLException e) {
	        e.printStackTrace();
	    }
	    
	}
	
	public static void downloadImages(String url, String folderPath) {
		try {
			
			Document doc = Jsoup.connect(url).get();
			Elements img = doc.getElementsByTag("a");
			for (Element el : img) {
				if(!el.hasAttr("href") || !el.hasAttr("target")) {
					continue;
				}				
				String src = el.absUrl("href");
				if(!src.endsWith(".jpg")) {
					continue;
				}
				System.out.println("Downloading Image: " + src);
				int indexname = src.lastIndexOf("/");

				if (indexname == src.length()) {
					src = src.substring(1, indexname);
				}

				indexname = src.lastIndexOf("/");
				String name = src.substring(indexname, src.length());

				URL srcURL = new URL(src);
				InputStream in = srcURL.openStream();
				OutputStream out = new BufferedOutputStream(new FileOutputStream(folderPath + name));
				for (int b; (b = in.read()) != -1;) {
					out.write(b);
				}
				out.close();
				in.close();
			}
		} catch (IOException ex) {
			System.err.println("There was an error");

			Logger.getLogger(FrontiersDownloader.class.getName()).log(Level.SEVERE, null, ex);

		}
	}	
}
