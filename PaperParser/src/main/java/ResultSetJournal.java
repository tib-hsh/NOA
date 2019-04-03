import lombok.Getter;
import lombok.Setter;
import metadata.Author;
import metadata.Citation;
import metadata.ID;
import metadata.PublicationDate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by charbonn on 04.11.2016.
 */
@Getter
@Setter
public class ResultSetJournal
{
	List<Author> Authors = new ArrayList<>();
	List<String> Sections = new ArrayList<>();
	String Body;
	String Title;
	Integer bodyLength=-1;
	Integer abstractLength=-1;
	String Abstract;
	String journalDOI ="";
	String journalName;
	String publicationYear;
	String PmcID;
	String PublisherId;
	//String XMLPathYear;
	String XMLPathComplete;
	String Volume;
	String Issue;
	String Pages;
	List<String> Keywords = new ArrayList<>();
	String License;
	String Publisher;
	File file;
	boolean hasFormula;
	String Error="";
	List <Citation> Bibliography = new ArrayList<>();
	List<Result> resultList = new ArrayList<>();
	PublicationDate fullDate;
	String source;
}
