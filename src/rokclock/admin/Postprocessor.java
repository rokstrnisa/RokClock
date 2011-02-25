package rokclock.admin;

import static java.lang.System.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import rokclock.Config;

/**
 * Post-processes the gathered time sheets in the hub.
 */
public class Postprocessor {
	/**
	 * This is the pre-agreed name for the file holding the information about
	 * timesheets submitted for a specific week.
	 */
	private static final String submittedFilename = "submitted.txt";
	/**
	 * This is the pre-agreed name for the settings file holding any information
	 * specific to that directory or directories below it.
	 */
	private static final String settingsFilename = "settings.txt";
	/**
	 * A week holds information about how much work was done on particular
	 * project in a specific week.
	 */
	@SuppressWarnings("serial")
	private class Week extends HashMap<String, Float> {
		private float headcount = 0f, reported = 0f, missing = 0f;
		private Map<String, Float> provided = new HashMap<String, Float>();
	}
	/**
	 * Actuals hold information for many weeks of time sheets.
	 */
	@SuppressWarnings("serial")
	private class Actuals extends HashMap<String, Week> {
		private Set<String> allProjects = new TreeSet<String>();
		private Set<String> allUsers = new TreeSet<String>();
	}
	/**
	 * This map holds actuals information for all teams.
	 */
	private HashMap<String, Actuals> teamActuals = new HashMap<String, Actuals>();
	/**
	 * These are the combined actuals for all teams.
	 */
	private Actuals combinedActuals = new Actuals();

	private Postprocessor() throws IOException {
		Config config = new Config();
		String baseAddress = config.getHub();
		List<String> teams = config.fetchTeams();
		for (String team : teams) {
			Actuals actuals = new Actuals();
			teamActuals.put(team, actuals);
			String teamPath = baseAddress + "/raw/" + team;
			Properties teamSettings = new Properties();
			teamSettings.load(new FileInputStream(teamPath + "/" + settingsFilename));
			File teamDir = new File(teamPath);
			for (String weekName : teamDir.list())
				processWeek(teamSettings, actuals, teamPath, weekName);
		}
		computeCombinedActuals();
		computeAllWeeksReported();
		System.out.println("=====================");
		outputTeamTotals();
		System.out.println("=====================");
		outputOverallTotals();
		System.out.println("=====================");
		outputProvidedData();
		System.out.println("=====================");
	}

	private void processWeek(Properties teamSettings, Actuals actuals,
			String teamPath, String weekName) throws IOException {
		String weekPath = teamPath + "/" + weekName;
		String submittedPath = weekPath + "/" + submittedFilename;
		File submittedFile = new File(submittedPath);
		if (!submittedFile.exists())
			return;
		Week week = new Week();
		actuals.put(weekName, week);
		// reading settings.txt
		Properties weekSettings = new Properties(teamSettings);
		try {
			weekSettings.load(new FileInputStream(weekPath + "/" + settingsFilename));
		} catch (IOException e) {
			err.format("WARNING: No settings file found for week %s.", weekPath);
		}
		float headcount = Float.parseFloat(weekSettings.getProperty("headcount"));
		week.headcount = headcount;
		// reading submitted.txt
		Map<String, String> userFiles = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new FileReader(submittedFile));
		String line;
		while ((line = br.readLine()) != null) {
			String[] entry = line.split(",");
			String user = entry[0];
			String logFilename = entry[1];
			if (userFiles.put(logFilename, user) != null)
				err.format(
						"WARNING: Multiple entries for user %s in week '%s'. Using the latest.%n",
						user, weekPath);
		}
		// reading user log files
		for (Entry<String, String> userEntry : userFiles.entrySet()) {
			String user = userEntry.getKey();
			actuals.allUsers.add(user);
			float provided = 0f;
			String logFilename = userEntry.getValue();
			File logFile = new File(weekPath + "/" + logFilename + ".log");
			br = new BufferedReader(new FileReader(logFile));
			while ((line = br.readLine()) != null) {
				String[] entry = line.split(",");
				String project = entry[0];
				float percent = Float.parseFloat(entry[1]);
				Float previousPercent = week.put(project, percent);
				if (previousPercent != null)
					week.put(project, previousPercent + percent);
				provided += percent;
			}
			br.close();
			Float previouslyProvided = week.provided.put(user, provided);
			if (previouslyProvided != null)
				week.provided.put(user, previouslyProvided + provided);
		}
	}

	private void computeCombinedActuals() {
		for (Actuals actuals : teamActuals.values()) {
			for (Entry<String, Week> weekEntry : actuals.entrySet()) {
				String weekName = weekEntry.getKey();
				Week teamWeek = weekEntry.getValue();
				// combine project names
				actuals.allProjects.addAll(teamWeek.keySet());
				// create combined week if non-existent
				Week combinedWeek = combinedActuals.get(weekName);
				if (combinedWeek == null)
					combinedActuals.put(weekName, combinedWeek = new Week());
				// sum user provided
				for (Entry<String, Float> teamUserEntry : teamWeek.provided.entrySet()) {
					String user = teamUserEntry.getKey();
					Float percent = teamUserEntry.getValue();
					Float previousPercent = combinedWeek.provided.put(user, percent);
					if (previousPercent != null)
						combinedWeek.provided.put(user, previousPercent + percent);
				}
				// combine headcount
				combinedWeek.headcount += teamWeek.headcount;
				// combine project entries
				for (Entry<String, Float> projectEntry : teamWeek.entrySet()) {
					String project = projectEntry.getKey();
					float percent = projectEntry.getValue();
					Float previousPercent = combinedWeek.put(project, percent);
					if (previousPercent != null)
						combinedWeek.put(project, previousPercent + percent);
				}
			}
			combinedActuals.allProjects.addAll(actuals.allProjects);
			combinedActuals.allUsers.addAll(actuals.allUsers);
		}
	}

	private void computeAllWeeksReported() {
		for (Actuals actuals : teamActuals.values())
			for (Week week : actuals.values())
				computeWeekReported(week);
		for (Week week : combinedActuals.values())
			computeWeekReported(week);
	}

	private void computeWeekReported(Week week) {
		float reported = 0f;
		for (float percent : week.values())
			reported += percent;
		week.reported = reported;
		week.missing = week.headcount - week.reported;
	}
	
	private void outputTeamTotals() {
		SortedSet<String> weeks = new TreeSet<String>(combinedActuals.keySet());
		// week names
		out.print(",,");
		for (String week : weeks) out.print(week + ",");
		// head counts
		out.print("\n,Headcount->,");
		for (String week : weeks) out.print(combinedActuals.get(week).headcount + ",");
		// reported totals
		out.print("\n,Reported->,");
		for (String week : weeks) out.print(combinedActuals.get(week).reported + ",");
		// missing totals
		out.print("\n,Missing->,");
		for (String week : weeks) out.print(combinedActuals.get(week).missing + ",");
		// team data
		out.println("\nTeam,Code");
		for (Entry<String,Actuals> teamEntry : teamActuals.entrySet()) {
			String team = teamEntry.getKey();
			Actuals actuals = teamEntry.getValue();
			for (String project : actuals.allProjects) {
				out.print(team + "," + project + ",");
				for (String week : weeks) out.print(actuals.get(week).get(project) + ",");
				out.println();
			}
			out.println();
		}
	}

	private void outputOverallTotals() {
		SortedSet<String> weeks = new TreeSet<String>(combinedActuals.keySet());
		// week names
		out.print(",,");
		for (String week : weeks) out.print(week + ",");
		// head counts
		out.print("\n,Headcount->,");
		for (String week : weeks) out.print(combinedActuals.get(week).headcount + ",");
		// reported totals
		out.print("\n,Reported->,");
		for (String week : weeks) out.print(combinedActuals.get(week).reported + ",");
		// missing totals
		out.print("\n,Missing->,");
		for (String week : weeks) out.print(combinedActuals.get(week).missing + ",");
		// team data
		out.println("\n,Code");
		for (String project : combinedActuals.allProjects) {
			out.print("," + project + ",");
			for (String week : weeks) out.print(combinedActuals.get(week).get(project) + ",");
			out.println();
		}
	}

	private void outputProvidedData() {
		SortedSet<String> weeks = new TreeSet<String>(combinedActuals.keySet());
		// week names
		out.print(",");
		for (String week : weeks) out.print(week + ",");
		out.println();
		// user entries
		for (String user : combinedActuals.allUsers) {
			out.print(user + ",");
			for (String week : weeks) {
				Float provided = combinedActuals.get(week).provided.get(user);
				if (provided == null)
					out.print("No data,");
				else if (provided < 1)
					out.print("~,");
				else if (provided > 1)
					out.print("!!,");
				else
					out.print("/,");
			}
			out.println();
		}
	}

	public static void main(String[] args) throws IOException {
		new Postprocessor();
	}
}
