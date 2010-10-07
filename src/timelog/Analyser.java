package timelog;

import java.io.*;
import java.text.*;
import java.util.*;

public class Analyser {
	private final String nl = System.getProperty("line.separator");

	public static void main(String[] args) throws Exception {
		if (args.length != 1 && args.length != 3) {
			System.err.println("Usage: java -cp bin timelog.Analyser <logFilename> [<start date inclusive> <stop date exclusive>]");
			System.exit(1);
		}
		String logFilename = args[0];
		Date fromDate = null, toDate = null;
		String dfS = "dd/MM/yyyy";
		DateFormat df = new SimpleDateFormat(dfS);
		if (3 <= args.length)
			try {
				fromDate = df.parse(args[1]);
				toDate = df.parse(args[2]);
			} catch (ParseException e) {
				System.err.println("Dates should be specified in the following format: " + dfS);
				System.exit(1);
			}
		Analyser a = new Analyser();
		a.processLogFile(logFilename, fromDate, toDate);
		a.displayResults();
	}

	private Map<String,Long> sums;
	private Date fromDate, toDate;

	Map<String,Long> processLogFile(String logFilename, Date fromDate, Date toDate) throws IOException {
		sums = new TreeMap<String,Long>();
		this.fromDate = fromDate;
		this.toDate = toDate;
		BufferedReader br = new BufferedReader(new FileReader(logFilename));
		String line;
		while ((line = br.readLine()) != null)
			readLogEntry(line);
		br.close();
		return sums;
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
			Date startDate = Config.df.parse(start);
			Date endDate = Config.df.parse(end);
			// fit within the specified period
			if (fromDate != null && startDate.before(fromDate))
				startDate = fromDate;
			if (toDate != null && endDate.after(toDate))
				endDate = toDate;
			// ignore if a reverse period
			if (startDate.after(endDate))
				return;
			// calculate and add
			Long sum = sums.get(project);
			if (sum == null)
				sum = 0L;
			sum += (endDate.getTime() - startDate.getTime());
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
