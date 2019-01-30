import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.security.KeyException;
import java.util.ArrayList;

import static java.lang.System.currentTimeMillis;


/**
 * Created by charbonn on 20.11.2018.
 */
public class FSOperation implements PipelineStep
{
	public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
	public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
	public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
	public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
	public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
	public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
	public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
	public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";
	private static Logger logger = Logger.getRootLogger();
	private final String ANSI_RESET = "\u001B[0m";
	private final String ANSI_BLACK = "\u001B[30m";
	private final String ANSI_RED = "\u001B[31m";
	private final String ANSI_GREEN = "\u001B[32m";
	private final String ANSI_YELLOW = "\u001B[33m";
	private final String ANSI_BLUE = "\u001B[34m";
	private final String ANSI_PURPLE = "\u001B[35m";
	private final String ANSI_CYAN = "\u001B[36m";
	private final String ANSI_WHITE = "\u001B[37m";
	public int VERBOSE = 1;
	public boolean finished = false;
	public boolean ERRORFLAG = false;
	boolean activated;
	private ParamSet paramset;
	private String name;
	private String desc;
	private String type;
	private ArrayList<String> req;
	private int step;
	private String IP;
	private String user;
	private String password;
	private int port;
	private String target;
	private String source;
	private boolean timestamp;

	public FSOperation(JSONObject o)
	{
		this.type = o.getString("type");
		this.timestamp = o.getBoolean("timestamp");
		this.name = o.getString("name");
		this.step = o.getInt("step");
		this.activated = o.getBoolean("activated");
		this.req = new ArrayList<String>();
		JSONArray arr = o.getJSONArray("requrires");
		if (activated)
			logger.info("Loading FS-Operation: [ " + ANSI_YELLOW + name + ANSI_RESET + " ]");
		else
			logger.info("Loading FS-Operation: [ " + ANSI_RED + name + ANSI_RESET + " ]");

		try {
			this.paramset = new ParamSet(o.getJSONArray("params"));
		} catch (KeyException e) {
			e.printStackTrace();
		}
		if (type.equals("FSRename")) {
			source = paramset.get("source");
			target = paramset.get("target");

		}
		for (int i = 0; i < arr.length(); i++) {
			req.add(arr.getString(i));
		}

	}

	@Override
	public void execute()
	{
		switch (this.type) {
			case "FSRename":
				System.out.print("Rename \t[ " + ANSI_YELLOW + source + ANSI_RESET + " ] \u2192");
				String ne = Rename(source, target, timestamp);
				System.out.println(" [ " + ANSI_YELLOW + target + ne + ANSI_RESET + " ]");

				finished = true;
				break;
		}
	}

	public String Rename(String source, String target, boolean timestamp)
	{
		long time = currentTimeMillis();
		if (timestamp)
			new File(source).renameTo(new File(target + "_" + time));
		else
			new File(source).renameTo(new File(target));
		if (timestamp)
			return "_" + time;
		else
			return "";

	}

	@Override
	public boolean wasExecuted()
	{
		return finished;
	}

	@Override
	public boolean isActive()
	{
		return activated;
	}

	@Override
	public String getStepName()
	{
		return this.name;
	}
}
