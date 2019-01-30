import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyException;
import java.util.ArrayList;

/**
 * Created by charbonn on 15.11.2018.
 */

interface PipelineStep
{
	void execute();

	boolean wasExecuted();

	boolean isActive();

	String getStepName();
}

@lombok.Getter
public class Program implements PipelineStep
{
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
	public boolean remoteExecution = false;
	boolean activated;
	private ParamSet paramset;
	private String name;
	private String desc;
	private String type;
	private String pathTo;
	private String fileName;
	private String outPut;
	private boolean printError;
	private ArrayList<String> req;
	private int step;
	private Process proc = null;
	private InputStream in;
	private InputStream err;
	private String output;
	private String IP;
	private String user;
	private String password;
	private int port;

	public Program(JSONObject o)
	{
		this.remoteExecution = o.getBoolean("remote");
		this.type = o.getString("type");
		this.name = o.getString("name");
		this.desc = o.getString("desc");
		this.pathTo = o.getString("pathTo");
		this.fileName = o.getString("fileName");
		this.outPut = o.getString("output");
		this.step = o.getInt("step");
		this.activated = o.getBoolean("activated");
		this.req = new ArrayList<String>();
		this.printError = o.getBoolean("printError");
		JSONArray arr = o.getJSONArray("requrires");
		if (activated)
			logger.info("Loading Program: [ " + ANSI_YELLOW + name + ANSI_RESET + " ]");
		else
			logger.info("Loading Program: [ " + ANSI_RED + name + ANSI_RESET + " ]");

		for (int i = 0; i < arr.length(); i++) {
			req.add(arr.getString(i));
		}
		try {
			this.paramset = new ParamSet(o.getJSONArray("params"));
		} catch (KeyException e) {
			e.printStackTrace();
		}
		if (remoteExecution) {
			JSONObject con = o.getJSONObject("connection");
			IP = con.getString("remoteIP");
			port = con.getInt("remotePort");
			user = con.getString("remoteUser");
			password = con.getString("remotePw");
		}

	}

	public String getParamsString()
	{
		String ret = "";
		for (String key : paramset.getParams().keySet()) {
			ret += "-" + key + " " + paramset.getParams().get(key) + " ";
		}
		return ret;
	}

	public String getLocation()
	{
		if (!remoteExecution)
			return this.pathTo + "\\" + fileName;
		else
			return this.pathTo + "/" + fileName;
	}

	public String getExection()
	{
		return getLocation() + " " + getParamsString();
	}

	public void execute()
	{
		logger.info("Executing Program: [ " + name + " ]");
		if (remoteExecution) {
			try {

				java.util.Properties config = new java.util.Properties();
				config.put("StrictHostKeyChecking", "no");
				JSch jsch = new JSch();
				Session session = jsch.getSession(user, IP, port);
				session.setPassword(password);
				session.setConfig(config);
				session.connect();
				System.out.println("Connected");

				Channel channel = session.openChannel("exec");
				String command_string = "";
				if (type.equals("py"))
					command_string = "/opt/anaconda3/bin/python3.6 " + getExection();
				else if (type.equals("jar"))
					command_string = "java -jar " + getExection();
				else {
					logger.error("Undefined  type in Programm: " + name);
					throw new Exception("Undefined  type");
				}
				System.out.println("COMMAND: " + command_string);
				((ChannelExec) channel).setCommand(command_string);
				channel.setInputStream(null);
				((ChannelExec) channel).setErrStream(System.err);

				InputStream in = channel.getInputStream();
				channel.connect();
				byte[] tmp = new byte[1024];
				while (true) {
					while (in.available() > 0) {
						int i = in.read(tmp, 0, 1024);
						if (i < 0) break;
						System.out.print(new String(tmp, 0, i));
					}
					if (channel.isClosed()) {
						System.out.println("exit-status: " + channel.getExitStatus());
						break;
					}
					try {
						Thread.sleep(1000);
					} catch (Exception ee) {
					}
				}
				channel.disconnect();
				session.disconnect();
				System.out.println("Remote Execution:  DONE");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else
			try {

				if (VERBOSE > 0) {
					System.out.println("Name: " + this.name);
					System.out.println("Path: " + getLocation());
					System.out.println("Param: " + paramset.getParams());
					System.out.println("Req: " + this.req);
				}
				//check if req are meet to run this prog
				for (String name : req) {
					boolean meet = false;
					for (PipelineStep p : Main.pipe.Progs) {
						if (p.getStepName().equals(name) && p.wasExecuted())
							meet = true;
					}
					if (!meet) {
						logger.error("Requirments not met for Program: " + name);
						throw new IllegalAccessException("Program Requirements not meet!");
					}
				}


				if (type.equals("py"))
					proc = Runtime.getRuntime().exec("python " + getExection());
				else if (type.equals("jar"))
					proc = Runtime.getRuntime().exec("java -jar " + getExection());
				else {
					logger.error("Undefined  type in Programm: " + name);
					throw new Exception("Undefined  type");
				}
				in = proc.getInputStream();
				err = proc.getErrorStream();
				BufferedReader bin = new BufferedReader(new InputStreamReader(in));
				BufferedReader ber = new BufferedReader(new InputStreamReader(err));

				String line = null;
				while ((line = bin.readLine()) != null) {
					System.out.println(ANSI_YELLOW + line);
				}
				if (printError)
					while ((line = ber.readLine()) != null) {
						ERRORFLAG = true;
						System.out.println(ANSI_RED + "ERROR: " + line);
					}
				System.out.println(ANSI_RESET);
			} catch (IOException e) {
				logger.error(e);
				e.printStackTrace();
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
		int exitCode = Integer.MIN_VALUE;
		try {
			exitCode = proc.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.finished = true;
		if (!ERRORFLAG && exitCode == 0)
			logger.info("Finished Program: [ " + name + " ] " + ANSI_GREEN + "without Errors!" + ANSI_RESET);
		else if (ERRORFLAG && exitCode == 0)
			logger.error("Finished Program :[ " + name + " ] " + ANSI_RED + "with minor ERRORS" + ANSI_RESET);
		else
			logger.error(ANSI_RED + "FATAL ERROR:[ " + name + " ] ExitCode: " + exitCode + ANSI_RESET);


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
