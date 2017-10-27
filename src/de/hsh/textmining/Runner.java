package de.hsh.textmining;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by charbonn on 17.10.2017.
 */
public class Runner implements Runnable
{
	String s;
	Runner(String given)
	{
	this.s=given;}

	@Override
	public void run()
	{
		String[] split = s.split(" ");
		//System.out.println(spli);
		byte[] img=null;
		try
		{
			img = download(split[0]);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		if(img.length>0)
			try {
				save2Disk(img,split[1]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		else
			writeError(split[0],split[1]);
	}

	private void writeError(String s0, String s1)
	{
		try(FileWriter fw = new FileWriter("Error.txt", true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw))
		{
			out.println(s0+"#"+s1);
		} catch (IOException e) {
			//exception handling left as an exercise for the reader
		}
	}

	private void save2Disk(byte[] img, String s) throws IOException
	{
		Path pathToFile = Paths.get(s);
		Files.createDirectories(pathToFile.getParent());
		//Files.createFile(pathToFile);
		FileOutputStream fos = new FileOutputStream(s);
		fos.write(img);
		fos.close();
		System.out.println(s);

	}

	private byte[] download(String gotten) throws IOException
	{
		String url=gotten;
		java.net.URL u = new URL(url);
		HttpURLConnection huc =  (HttpURLConnection)u.openConnection ();
		huc.setRequestMethod ("HEAD");
		huc.connect () ;
		int code = huc.getResponseCode() ;
		int statusCode=code;
		if(statusCode!=404)
		{
			InputStream in;
			try {
				URL resourceUrl, base, next;
				HttpURLConnection conn;
				String location;
				while (true)
				{
					resourceUrl = new URL(url);
					conn        = (HttpURLConnection) resourceUrl.openConnection();
					conn.setConnectTimeout(15000);
					conn.setReadTimeout(15000);
					conn.setInstanceFollowRedirects(false);   // Make the logic below easier to detect redirections
					conn.setRequestProperty("User-Agent", "Mozilla/5.0...");
					switch (conn.getResponseCode())

					{
						case HttpURLConnection.HTTP_MOVED_PERM:
						case HttpURLConnection.HTTP_MOVED_TEMP:
							location = conn.getHeaderField("Location");
							base     = new URL(url);
							next     = new URL(base, location);  // Deal with relative URLs
							url=next.toExternalForm();
							continue;
					}
					break;
				}
				in= new BufferedInputStream(conn.getInputStream());
				//in = new BufferedInputStream(u.openStream());
			}
			catch(Exception e)
			{
				//System.out.println(e);
				//this.setURL_Error(true);
				return new byte[0];
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int n = 0;
			while (-1!=(n=in.read(buf)))
			{
				out.write(buf, 0, n);
			}
			out.close();
			in.close();
			byte[] response = out.toByteArray();
			return response;
		}

		return new byte[0];
	}
}
