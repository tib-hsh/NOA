import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.security.KeyException;
import java.util.ArrayList;

/**
 * Created by charbonn on 20.11.2018.
 */
public class MongoOperation implements PipelineStep
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
	private boolean printError;
	private ArrayList<String> req;
	private int step;
	private String IP;
	private String user;
	private String password;
	private int port;
	private String targetDB;
	private String targetCol;
	private String sourceDB;
	private String sourceCol;

	public MongoOperation(JSONObject o)
	{
		this.type = o.getString("type");
		this.name = o.getString("name");
		this.step = o.getInt("step");
		this.activated = o.getBoolean("activated");
		this.req = new ArrayList<String>();
		JSONArray arr = o.getJSONArray("requrires");
		if (activated)
			logger.info("Loading MongoOperation: [ " + ANSI_YELLOW + name + ANSI_RESET + " ]");
		else
			logger.info("Loading MongoOperation: [ " + ANSI_RED + name + ANSI_RESET + " ]");

		try {
			this.paramset = new ParamSet(o.getJSONArray("params"));
		} catch (KeyException e) {
			e.printStackTrace();
		}
		if (type.equals("mongoDrop") || type.equals("mongoCopy")) {
			targetCol = paramset.get("target_col");
			targetDB = paramset.get("target_db");
		}
		if (type.equals("mongoCopy")) {
			sourceCol = paramset.get("source_col");
			sourceDB = paramset.get("source_db");
		}
		for (int i = 0; i < arr.length(); i++) {
			req.add(arr.getString(i));
		}

	}

	public void execute()
	{
		switch (this.type) {
			case "mongoDrop":

				System.out.print("Droping\t[ " + ANSI_YELLOW + targetCol + ANSI_RESET + " ]  in DB  [ " + ANSI_YELLOW + targetDB + ANSI_RESET + " ] .....");
				MongoOrganizier.Drop(targetCol, targetDB);
				System.out.println(" DONE!!!");
				finished = true;
				break;
			case "mongoCopy":

				System.out.print("Copy \t[ " + ANSI_YELLOW + sourceCol + "." + sourceDB + ANSI_RESET + " ] \u2192");
				System.out.print(" [ " + ANSI_YELLOW + targetCol + "." + targetDB + ANSI_RESET + " ] .....");
				MongoOrganizier.Copy(sourceCol, sourceDB, targetCol, targetDB);
				System.out.println(" DONE!!!");
				finished = true;
				break;
		}
	}

	public boolean wasExecuted()
	{
		return finished;
	}

	@Override
	public boolean isActive()
	{
		return activated;
	}

	public String getStepName()
	{
		return this.name;
	}
}
