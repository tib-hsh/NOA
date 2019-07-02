import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class SpringerDownloader {

	public static void downloadArticle(String doi, String outputFolder) throws IOException {
		
		System.out.println(doi);
		
		String url = "http://api.springer.com/openaccess/app?api_key=4df688ea8f1b339c208d1b8ef316f174&q=doi%3A" + doi;
		if(!Updater.exists(url)) {
			FileWriter writer = new FileWriter(outputFolder + "NotDownloaded.txt", true);
			writer.write(doi + System.lineSeparator());
			writer.close();
			return;
		}
		
	    try {
	        URL website = new URL(url);
	        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
	        doi = doi.replaceAll("/", "_");
	        String filePath = outputFolder + "DownloadedArticles/Springer/" + doi + ".xml";
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
