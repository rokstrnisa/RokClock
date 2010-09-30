package timelog;

import java.io.*;
import java.text.ParseException;
import java.util.*;

public class Analyser {
	private final String nl = System.getProperty("line.separator");

	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.err.println("Please specify the log file.");
			System.exit(1);
		}
		new Analyser().processLogFile(args[0]);
	}

	private Map<String,Long> sums = new TreeMap<String,Long>();

	private void processLogFile(String logFilename) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(logFilename));
		String line;
		while ((line = br.readLine()) != null)
			readLogEntry(line);
		br.close();
		displayResults();
	}

	private void readLogEntry(String entry) {
		String[] fields = entry.split("\\s*,\\s*");
		try { // try new format
			Config.df.parse(fields[0]);
			recordData(entry, fields[2], fields[0], fields[1]);
		} catch (ParseException e) { // old format
			recordData(entry, fields[0], fields[2], fields[3]);
		}
	}

	private void recordData(String entry, String project, String start, String end) {
		try {
			long startTime = Config.df.parse(start).getTime();
			long endTime = Config.df.parse(end).getTime();
			Long sum = sums.get(project);
			if (sum == null)
				sum = 0L;
			sum += (endTime - startTime);
			sums.put(project, sum);
		} catch (ParseException e) {
			System.err.println("Could not parse log entry: " + entry);
		}
	}

	private void displayResults() {
		for (Map.Entry<String, Long> entry : sums.entrySet()) {
			String project = entry.getKey();
			long sum = entry.getValue();
			double sumInHours = 1.0 * sum / (1000 * 3600);
			System.out.printf("%s: %.2f" + nl, project, sumInHours);
		}
	}
}
