/**
 * Created by charbonn on 15.11.2018.
 */

import org.apache.log4j.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

public class Main
{
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

	public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
	public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
	public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
	public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
	public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
	public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
	public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
	public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";
	public static Pipeline pipe;
	public static MongoOrganizier mon;
	private static Logger logger = Logger.getRootLogger();

	public static void main(String[] args)
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
		Date date = new Date();
		String datum = dateFormat.format(date);

		try {
			SimpleLayout layout = new SimpleLayout();
			ConsoleAppender consoleAppender = new ConsoleAppender(layout);
			logger.addAppender(consoleAppender);
			FileAppender fileAppender = new FileAppender(layout, "logs/" + datum + ".log", true);
			logger.addAppender(fileAppender);
			// ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF:
			logger.setLevel(Level.ALL);
		} catch (Exception ex) {
			System.out.println(ex);
		}

		String pathJson = "location for pipeDefinition";
		String json = null;
		try {
			logger.info("Loading File: [ " + pathJson + " ]");
			json = readPipeline(pathJson);
		} catch (IOException e) {
			logger.error("IO Error. Could not access " + pathJson);
			e.printStackTrace();
		}
		pipe = new Pipeline(json);
		if (!pipe.repeating)
			pipe.execute();
		else {
			Timer t = new Timer();
			int periode = pipe.freqValue;
			if (pipe.freqType.equals("min"))
				periode *= 1000 * 60;
			if (pipe.freqType.equals("hour"))
				periode *= 1000 * 60 * 60;
			if (pipe.freqType.equals("sec"))
				periode *= 1000;
			if (pipe.freqType.equals("day"))
				periode *= 1000 * 60 * 60 * 24;
			if (pipe.freqType.equals("week"))
				periode *= 1000 * 60 * 60 * 24 * 7;
			t.scheduleAtFixedRate(pipe, 0, periode);
		}

	}


	private static String readPipeline(String file) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");

		try {
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}

			return stringBuilder.toString();
		} finally {
			reader.close();
		}
	}

	private static boolean savePipeline(String file) throws IOException, NoSuchMethodException
	{
		throw new NoSuchMethodException("Not yet Implemented!");
	}
}
