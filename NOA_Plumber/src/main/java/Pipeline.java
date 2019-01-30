import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.TimerTask;

/**
 * Created by charbonn on 16.11.2018.
 */
public class Pipeline extends TimerTask
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
	public boolean repeating;
	public String freqType;
	public int freqValue;
	public ArrayList<PipelineStep> Progs;
	private String name;
	private String desc;
	private String fileName;
	private String lastRun;
	public MongoOrganizier mon;

	private String mongoIP;
	private int mongoPort;
	private String mongoDefaultDB;

	private int VERBOSE = 2;

	public Pipeline(String json)
	{
		Progs = new ArrayList<PipelineStep>();
		JSONObject obj = new JSONObject(json);
		this.name = obj.getString("name");
		this.repeating = obj.getBoolean("repeating");
		this.freqType = obj.getString("freqType");
		this.freqValue = obj.getInt("freqValue");
		this.mongoPort=obj.getInt("mongoPort");
		this.mongoIP=obj.getString("mongoIP");
		this.mongoDefaultDB=obj.getString("mongoDefaultDB");
		mon = MongoOrganizier.getInstance(mongoIP, mongoPort, mongoDefaultDB);
		if (repeating)
			logger.info("Init Pipeline: [ " + ANSI_CYAN + name + ANSI_RESET + " ]   every " + freqValue + " " + freqType + " \n");
		else
			logger.info("Init Pipeline: [ " + ANSI_CYAN + name + ANSI_RESET + " ]\n");

		JSONArray arr = obj.getJSONArray("program");
		logger.info("-------------- Prog Loading START --------------");
		for (int i = 0; i < arr.length(); i++) {
			JSONObject o = arr.getJSONObject(i);
			String type = o.getString("type");
			PipelineStep p;
			if (type.equals("jar") || type.equals("py"))
				p = new Program(o);
			else if (type.contains("mongo"))
				p = new MongoOperation(o);
				//if(type.contains("FS"))
			else
				p = new FSOperation(o);
			Progs.add(p);
		}
		logger.info("-------------- Prog Loading END --------------\n");
	}

	public void executeProg(int index)
	{
		if (index > Progs.size())
			throw new IndexOutOfBoundsException("Index out of Range");
		Progs.get(index).execute();
	}

	public void execute()
	{
		logger.info("-------------- Prog Execution START --------------");

		for (PipelineStep p : Progs) {
			if (p.isActive()) {
				p.execute();
			}
		}
		logger.info("-------------- Prog Execution END --------------\n");

	}

	@Override
	public void run()
	{
		logger.info(ANSI_YELLOW_BACKGROUND + ANSI_BLACK + "-------------- Run Timed Execution -------------- " + ANSI_RESET + Instant.now() + "\n");
		execute();
	}
}
