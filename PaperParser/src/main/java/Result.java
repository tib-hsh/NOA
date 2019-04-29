import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by charbonn on 02.11.2016.
 */

@Getter
@Setter
public class Result
{
	ResultSetJournal rsj;
	int findingID;
	String DOI;
	String captionTitle;
	String captionBody;
	String label;
	String graphicDOI;
	String graphicID;
	String path;
	String imageURL;
	int statusCode;
	byte[] image;
	boolean URL_Error;
	java.util.List<String> context = new ArrayList<>();
	String figID;
	String figGroupID;
	boolean copyrightFlag;


	public void Restul(ResultSetJournal rsj)
	{
		this.rsj = rsj;
	}

	public void download() throws IOException
	{
		URL u = new URL(this.getImageURL());
		HttpURLConnection huc = (HttpURLConnection) u.openConnection();
		huc.setRequestMethod("HEAD");
		huc.connect();
		int code = huc.getResponseCode();
		statusCode = code;

		if (statusCode != 404)
		{
			InputStream in;

			try {
				URL resourceUrl, base, next;
				HttpURLConnection conn;
				String location;

				while (true)
				{
					resourceUrl = new URL(this.getImageURL());
					conn = (HttpURLConnection) resourceUrl.openConnection();
					conn.setConnectTimeout(15000);
					conn.setReadTimeout(15000);
					conn.setInstanceFollowRedirects(false);   // Make the logic below easier to detect redirections
					conn.setRequestProperty("User-Agent", "Mozilla/5.0...");
					switch (conn.getResponseCode())
					{
						case HttpURLConnection.HTTP_MOVED_PERM:

						case HttpURLConnection.HTTP_MOVED_TEMP:
							location = conn.getHeaderField("Location");
							base = new URL(this.getImageURL());
							next = new URL(base, location);  // Deal with relative URLs
							this.setImageURL(next.toExternalForm());
							continue;
					}
					break;
				}
				in = new BufferedInputStream(conn.getInputStream());
			} catch (Exception e)
			{
				this.setURL_Error(true);
				return;
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int n = 0;
			while (-1 != (n = in.read(buf)))
			{
				out.write(buf, 0, n);
			}
			out.close();
			in.close();
			byte[] response = out.toByteArray();
			this.image = response;
		} else {
			MongoDBRepo.getInstance().writeError("404");
		}
	}

	public void save2disk() throws IOException
	{

		graphicID = graphicDOI.substring(graphicDOI.length() - 4, graphicDOI.length());
		path = graphicDOI.substring(graphicDOI.length() - 25, graphicDOI.length());
		URL url = new URL("http://journals.plos.org/ploscompbiol/article/figure/image?size=large&id=" + graphicDOI);
		InputStream in = new BufferedInputStream(url.openStream());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int n = 0;
		while (-1 != (n = in.read(buf)))
		{
			out.write(buf, 0, n);
		}
		out.close();
		in.close();
		byte[] response = out.toByteArray();
		FileOutputStream fos = new FileOutputStream("C://paper//" + path + ".png");
		fos.write(response);
		fos.close();
	}

	public void save2dbWithImage() throws IOException
	{
		InputStream in;
		URL url = new URL("https://www.hindawi.com/journals/" + rsj.getPublisherId().toLowerCase() + "/" + rsj.getPublicationYear() + "/" + graphicDOI + ".jpg");
		try {
			in = new BufferedInputStream(url.openStream());
		} catch (Exception e)
		{
			System.out.println(e);
			return;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		byte[] buf = new byte[1024];
		int n = 0;
		while (-1 != (n = in.read(buf)))
		{
			out.write(buf, 0, n);
		}

		out.close();
		in.close();
		byte[] response = out.toByteArray();
		try {
			FileOutputStream fos = new FileOutputStream("C://hindawi_images//" + rsj.getJournalDOI().replace('\\', '_').replace('/', '_') + "---" + this.findingID + ".jpg");
			fos.write(response);
			fos.close();
			System.out.println("Wrote to Disk: " + rsj.getJournalDOI() + this.findingID + ".jpg");
		} catch (Exception e)
		{
			System.out.println(e);
			System.out.println("ERROR WITH :::::  " + rsj.getJournalDOI() + this.findingID + ".jpg");
		}
	}


}
