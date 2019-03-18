package pipeline;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import com.splunk.*;

public class splunk {
    private static Service splunkService;
    private static String uName = "NAME";
    private static String pWord = "PASS";
    private static String host = "localhost";
    private static Integer port = 8089;

    public static void main(String[] args) {
        splunkService = connect();
        send("This is a test!");
        search("search * | head 10");

    }

    public static Service connect(){
        //TODO:  Should we set SSL settings here or in overall Java Settings.
        HttpService.setSslSecurityProtocol(SSLSecurityProtocol.TLSv1_2);

        ServiceArgs loginArgs = new ServiceArgs();
        loginArgs.setUsername(uName);
        loginArgs.setPassword(pWord);
        loginArgs.setHost(host);
        loginArgs.setPort(port);

        Service service = Service.connect(loginArgs);

        return service;
    }

    public static void send(String data){
        Index myIndex = splunkService.getIndexes().get("main");

        Args eventArgs = new Args();
        eventArgs.put("sourcetype", "test.log");
        eventArgs.put("host", "local");

        myIndex.submit(eventArgs, data);
    }

    public static void search(String query) {
        JobCollection jobs = splunkService.getJobs();
        Job job = jobs.create(query);

        while (!job.isDone()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        JobResultsArgs resultsArgs = new JobResultsArgs();
        resultsArgs.setOutputMode(JobResultsArgs.OutputMode.JSON);

        try {
            InputStream results = job.getResults(resultsArgs);
            ResultsReaderJson resultsReader = new ResultsReaderJson(results);
            HashMap<String, String> event;
            System.out.println("**************RESULTS**************");
            while ((event = resultsReader.getNextEvent()) != null) {
                for (String key: event.keySet()){
                    System.out.println("   " + key + ":  " + event.get(key));
                }
            }
            resultsReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}