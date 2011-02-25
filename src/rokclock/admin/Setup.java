package rokclock.admin;

import java.io.*;
import java.util.List;

import rokclock.Config;

/**
 * Creates "drop box" directories at the hub for users to drop their time
 * sheets.
 */
public class Setup {
	private Setup() throws IOException {
		Config config = new Config();
		String baseAddress = config.getHub();
		List<String> teams = config.fetchTeams();
		for (String team : teams)
			for (int year = 2011; year <= 2012; year++)
				for (int week = 1; week <= 52; week++) {
					String weekPath = String.format("%s/raw/%s/%dwk%02d",
							baseAddress, team, year, week);
					File weekDir = new File(weekPath);
					weekDir.mkdirs();
					// remove all read permissions
					weekDir.setReadable(false, false);
					// add read permission to owner
					weekDir.setReadable(true, true);
					// remove all execute permissions
					weekDir.setExecutable(false, false);
					// add execute permission to owner
					weekDir.setExecutable(true, true);
					// add write permissions to all
					weekDir.setWritable(true, false);
				}
		System.out.println("Done.");		
	}

	public static void main(String[] args) throws IOException {
		new Setup();
	}
}
