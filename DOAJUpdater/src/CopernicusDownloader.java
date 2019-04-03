import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;

public class CopernicusDownloader {

	public static void downloadArticle(String doi, String outputFolder) throws IOException {
		
		System.out.println(doi);
		
		String url = "https://doi.org/" + doi;
		if(!Updater.exists(url)) {
			FileWriter writer = new FileWriter(outputFolder + "NewArticleDOIs/NotDownloaded.txt", true);
			writer.write(doi + System.lineSeparator());
			writer.close();
			return;
		}
	    
		Response response = Jsoup.connect(url).followRedirects(true).execute();
		String articleURL = response.url().toString();
		articleURL = articleURL + doi.substring(8) + ".xml";
		if(!Updater.exists(articleURL)) {
			FileWriter writer = new FileWriter(outputFolder + "NewArticleDOIs/NotDownloaded.txt");
			writer.write(doi + System.lineSeparator());
			writer.close();
			return;
		}
		
	    try {
	        URL website = new URL(articleURL);
	        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
	        doi = doi.replaceAll("/", "_");
	        String filePath = outputFolder + "DownloadedArticles/Copernicus/" + doi + ".xml";
	        File f = new File(filePath);
	        if(f.exists() && !f.isDirectory()) { 
	            return;
	        }
		    FileOutputStream fos = new FileOutputStream(filePath);
		    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		    fos.close();
	    } catch (MalformedURLException e) {
	        e.printStackTrace();
	    }

	}
}

