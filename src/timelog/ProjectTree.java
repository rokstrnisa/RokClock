package timelog;

import java.util.List;
import java.util.ArrayList;

class ProjectTree {
	class Project {
		private final String name, tooltip;
		private final List<Project> subProjects = new ArrayList<Project>();

		Project(final String name, final String tooltip) {
			this.name = name;
			this.tooltip = tooltip;
		}

		void addSubProject(final Project p) {
			subProjects.add(p);
		}
	}
	
	private final List<Project> projects = new ArrayList<Project>();
	
	void addProject(final Project p) {
		projects.add(p);
	}
}
