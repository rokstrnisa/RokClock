package rokclock;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * The analyser of the log files. It currently provides a summary for top-level
 * projects for a specified time period.
 */
public class Analyser {
	/**
	 * A platform-independent newline.
	 */
	private final String nl = System.getProperty("line.separator");

	/**
	 * The main method of this analyser, which is used for running it from the
	 * command prompt. As arguments, it expects the name of the log file,
	 * followed by the start date (inclusive) and the stop date (exclusive). The
	 * dates should have the "dd/MM/yyyy" format. The results are written to the
	 * standard output.
	 *
	 * @param args
	 *            The command-line arguments as specified above.
	 * @throws Exception
	 *             Thrown if the processing fails.
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 1 && args.length != 3) {
			System.err.println("Usage: java -cp bin rokclock.Analyser <logFilename> [<start date inclusive> <stop date exclusive>]");
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

	/**
	 * The field that maps top-level project names to the sum of milliseconds
	 * spent on them for a specified time period.
	 */
	private Map<String,Long> sums;
	/**
	 * The start of a specified time period.
	 */
	private Date fromDate;
	/**
	 * The end of a specified time period.
	 */
	private Date toDate;

	/**
	 * This method reads the specified log file for the specified time period.
	 * The processing of individual log entries is delegated to @
	 * #readLogEntry(String)} .
	 *
	 * @param logFilename
	 *            The name of the log file.
	 * @param fromDate
	 *            The start of the time period.
	 * @param toDate
	 *            The end of the time period.
	 * @return The resulting map of results.
	 * @throws IOException
	 *             Thrown if reading or parsing fails.
	 */
	Map<String,Long> processLogFile(String logFilename, Date fromDate, Date toDate) throws IOException {
		sums = new TreeMap<String,Long>();
		this.fromDate = fromDate;
		this.toDate = toDate;
		BufferedReader br = new BufferedReader(new FileReader(logFilename));
		String line;
		int lineNumber = 0;
		while ((line = br.readLine()) != null)
			try {lineNumber++; readLogEntry(line);}
			catch (Exception e) {
				System.err.println("Could not process log entry on line "
						+ lineNumber + ": \"" + line + "\"");
			}
		br.close();
		return sums;
	}

	/**
	 * Parses a single log entry. Fields should be separated by commas; any
	 * spaces around commas are ignored. If the first field is recognised as a
	 * date, the new log format (from,to,project,sub-project,...) is used;
	 * otherwise, the old log format (project,sub-project,from,to) is used. The
	 * recording of data is delegated to
	 * {@link #recordData(String, String, String, String)}.
	 *
	 * @param entry
	 *            A single log entry to process.
	 */
	private void readLogEntry(String entry) {
		String[] fields = entry.split("\\s*,\\s*");
		try { // try new format
			Config.df.parse(fields[0]);
			recordData(entry, fields[2], fields[0], fields[1]);
		} catch (ParseException e) { // old format
			recordData(entry, fields[0], fields[2], fields[3]);
		}
	}

	/**
	 * Records the data from a single log entry, independent from the log
	 * format.
	 *
	 * @param entry
	 *            The whole log entry; used for debugging purposes only.
	 * @param project
	 *            The name of the top-level project.
	 * @param start
	 *            The start of the activity.
	 * @param end
	 *            The end of the activity.
	 */
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

	/**
	 * Outputs results of the analyser to the standard output.
	 */
	private void displayResults() {
		for (Map.Entry<String, Long> entry : sums.entrySet()) {
			String project = entry.getKey();
			long sum = entry.getValue();
			double sumInHours = 1.0 * sum / (1000 * 3600);
			System.out.printf("%s: %.2f" + nl, project, sumInHours);
		}
	}
}
