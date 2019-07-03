import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

public class Main {
    static int counter=0;
    private static final int NTHREDS = 10;
    private static ExecutorService executor=Executors.newFixedThreadPool(NTHREDS);
    static boolean tooManyRequests;
    static String outputFolder = "";

    public static void main(String[] args) throws InvalidFileFormatException, IOException {

		File f = new File("config.ini");
		if(f.exists() && !f.isDirectory()) { 
			Wini ini = new Wini(new File("config.ini"));
			outputFolder = ini.get("DEFAULT", "tmp_folder") + "/";
		}
        //System.out.println("Start PaperDownloader on file  "+args[0] );
        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(outputFolder + args[0]))) {
            //Create Task and hand it over to Executor
            stream.forEach(Main::CreateTask);

        } catch (IOException e) {
            e.printStackTrace();
        }
        executor.shutdown();
        // Wait until all threads are finished
        try {
            //Expecting long runtime :D
            executor.awaitTermination(200,TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Finished all threads");
    }
    private static void CreateTask(String s)
    {
        Runnable worker = new Runner(s);
        executor.execute(worker);
    }

}
