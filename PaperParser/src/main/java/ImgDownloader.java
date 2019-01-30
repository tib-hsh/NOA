/**
 * Created by charbonn on 29.01.2019.
 */
public class ImgDownloader
{
	String rootPath = "C:\\_NOA\\DownloadRoot\\";
	    /*
	    TODO:
	    Fill with logic for a cleaner image Downloading process
	     */
	public void getTIBPathFrom(Result r)
	{
		String path = rootPath + "\\"+
				r.getRsj().getPublicationYear();

	}
}
