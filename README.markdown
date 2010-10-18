# TIME LOG #

## 1 Purpose ##
This program helps you keep a log of how much time you spend on specific
projects.

## 2 Approach ##
The program displays a small window with a tree of projects and sub-projects at
regular intervals. Each root node should correspond to a specific project that
[your supervisors want] you want to track. You can create any number of
sub-projects by either editing the project file (see section 3.1) and reloading
the program, or right-clicking a node in the user interface. Use middle-click to
expand/collapse a parent node.

Once a node is pressed (left click), the window is minimised (alternative
behaviour is possible --- see below). The window re-appears at the specified
interval (`intervalInSeconds`) in a semi-active state (default colour: blue). If
a node is pressed (left click) within another specified interval
(`waitInSeconds`), the time elapsed since the window appeared will be counted
towards the previous entry (unless specified otherwise with `autoCountTowards`).

If the task should be stopped or changed before the end of the interval, you can
maximise the program (if minimised), and click on the STOP button or a different
task, respectively.

The program generates a simple CSV log, where each entry roughly corresponds to
each node clicked. The order used is:

    <start-time>,<end-time>,<main-project>,<sub-project>,<sub-sub-project>,...

The times are currently displayed using the date format `dd/MM/yyyy HH:mm:ss`.

## 3 Compilation (if not already compiled) ##
Software required:

- JDK 6 (aptitude package: `openjdk-6-jdk`),
- Ant (aptitude package: `ant`).

The program can be compiled with

    ant compile

Since the program is written in Java, it should compile and run on many
platforms.

## 4 Setting up the program ##
The configuration for the program is located in the `config.txt` file. (If
`config.txt` does not exist when the program is started, it is copied from
`config.txt.default`.) In there, you can specify various options.

### 4.1 Configuration options ###

- `title` (default: `Time Log`): The title of the window.

- `locX` (default: `600`): The horizontal starting coordinate of the window.

- `locY` (default: `400`): The vertical starting coordinate of the window.

- `width` (default: `170`): The starting width of the window.

- `height` (default: `480`): The starting height of the window.

- `intervalInSeconds` (default: `3600`): The period (in seconds) after which the
  program will prompt you again.

- `waitInSeconds` (default: `3600`): The period (in seconds) after the prompt in
  which any user interaction will result in automatically counting the time from
  the prompt to the user interaction towards whatever is specified for the
  `autoCountTowards` option.

- `defaultColor` (default: `0,255,0`): The Red-Green-Blue specification for the
  _default colour_ of the project nodes.

- `activeColor` (default: `255,0,0`): The Red-Green-Blue specification for the
  _active colour_ of the project nodes.

- `semiActiveColor` (default: `100,100,200`): The Red-Green-Blue specification
  for the _semi-active colour_ of the project nodes, i.e. the colour for a
  previously active node within the waiting period.

- `behaviour` (default: `minimise`): The window state to go to after the user
  has selected a project. Option `hide` removes the program even from the
  taskbar, while `show` keeps the window visible.

- `autoCountTowards` (default: `previous`): See option `waitInSeconds`. The
  `previous` option counts towards the last selected project. The `unknown`
  option counts towards the "unknown" root project. The `nothing` option does
  not count towards anything.

- `writeTimeouts` (default: `false`): If set to `true`, the program writes a log
  entry whenever the end of a wait period is reached without being interrupted
  by the user. The log entry consists of the start time and the end time equal
  to the end time of the previous log entry, and is titled `(timed out)`.

- `logFilename` (default: `log.txt`): The filename of the log file.

- `projectsFilename` (default: `projects.txt`): The filename of the projects
  file. (If the specified file does not exist once the program starts, it is
  copied from `projects.txt.default`.)

### 4.2 Specifying the projects ###
The projects are specified in the projects file (its path is specified within
the configuration file). The syntax for specifying projects is:

    main_project[{tooltip}]
    				sub_project[{tooltip}]
    								sub_sub_project[{tooltip}]

The top-level projects should be your main projects (projects that project
administration care about). Each name can have its tooltip appended in curly
brackets. See `projects.txt.default` for an example.

## 5 Running the program ##
The program can be run either by compiling the source code yourself, or by
running a JAR (a pre-compiled package).

### 5.1 If source is available ###
Software required:

- JRE 6 (aptitude package: `openjdk-6-jre`),
- Ant (aptitude package: `ant`).

The program can be run with

    ant run

or just

    ant

since `run` the the default target. Both of these options also compile the
program if required.

You can run the program in non-daemon mode using

    ant no-daemon

This will block ant while the program runs, so you can close it with `CTRL-C`.

### 5.2 If a pre-compiled package is available ###
Software required:

- JRE 6 (aptitude package: `openjdk-6-jre`).

On Windows, run

    ./run.bat

On Linux, run

    ./run.sh

The two scripts are identical, and contain a single command

    java -jar TimeLog.jar

## 6 Analysing the logs ##
The logs can be analysed either using a spreadsheet, or with the provided
analyser.

### 6.1 Using a spreadsheet ###
The following instructions work for _Excel 2007_.

#### 6.1.1 Importing ####
- open a spreadsheet;
- select the "Data" tab;
- choose "Get External Data";
- choose "From Text";
- select the log file generated by TimeLog;
- select "Delimited" (leave other settings alone);
- click Next;
- select "Comma" only (leave other settings alone);
- select "General" 'Column data format';
- select where to place the data.

#### 6.1.2 Calculating the time periods ####
- select the date cells;
- right click;
- choose "Format cells...";
- select the "Number" tab;
- choose the "Custom" category;
- type in `dd/mm/yyyy hh:mm:ss` for Type;
- click OK;
- select cell to calculate difference in, e.g. A1 and A2;
- type `=A2-A1`;
- select the cell;
- right click;
- choose "Format cells...";
- choose the "Custom" category;
- type `hh:mm:ss` for Type;
- click OK.

### 6.2 Using the provided log analyser ###
The source code and the pre-compiled JAR both contain the class
`timelog.Analyser`. If you have a pre-compiled JAR available, you can start it
with

    java -cp TimeLog.jar timelog.Analyser <logFilename> [<start date inclusive> <stop date exclusive>]

If you have the source code available, you can start it with

    ant compile
    java -cp bin timelog.Analyser <logFilename> [<start date inclusive> <stop date exclusive>]

### 6.3 Using the GUI analyser ###
The main window of the program now has a "Review & Save" button, which opens up
a new window where the sums for the top-level projects are displayed for any
specified time period. The computed values can be manually modified within the
program. The output file has the CSV format, and contains a line for each
top-project name, together with the fraction of time spent on it.

## 7 Feedback ##
All feedback is much appreciated. Please send it to:
[Rok Strnisa](mailto:rok.strnisa@citrix.com "rok.strnisa@citrix.com")

Thanks.
