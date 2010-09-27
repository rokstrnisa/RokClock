TIME LOG
========

1. Purpose
----------
This program helps you keep a log of how much time you spend on specific
projects.

2. Approach
-----------
The program displays a small window of buttons at regular intervals. Each button
corresponds to a specific project that [your supervisors want] you want to
track. Once the button is pressed, the window is minimised (or disappears,
depending on the configuration).

The window re-appears at the specified interval. If a button is pressed within
the specified interval, the time elapsed since the window appeared will be
counted towards the previous entry.

If the task should be stopped or changed before the end of the interval, you can
maximise the program and then click on the STOP button or a different task,
respectively.

The program generates a simple CSV log, where each entry roughly corresponds to
each button click.

2. Compilation
--------------
Software required: JDK 6, Ant.

The program can be compiled with

    ant compile

Since the program is written in Java, it should compile and run on many
platforms.

3. Setting up the program
-------------------------
The configuration for the program is located in the "config.txt" file. In there,
you can specify various options including the time interval in seconds (default:
intervalInSeconds=3600), the file containing the project and sub-project names
(default: projectsFilename=projects.txt), and the output file for the log
entries (default: logFilename=log.txt).

3.1 Specifying the projects
---------------------------
Each line should begin with the name of a main project (a project that project
administration might care about). Each main project name can be followed by a
colon and a comma-separated list of sub-project names (mini projects that
mainly you care about logging individually). For example, to define a project
"A" with sub-projects "x", "y", and "z", the project file should contain the
line:

    A: x, y, z

Each name can have its tooltip appended in curly brackets, e.g.:

    A{A's tooltip}: x, y{y's tooltip}, z

3.2 Alternative behaviour
-------------------------
Some window managers do not support minimisation of windows (default:
behaviour=minimise). In that case, specifying "behaviour=hide" in the
"config.txt" file will instead completely hide the window. However, this does
make it more difficult to show the window again before the end of the specified
interval (e.g. to stop/change the logging). In this case, you might want to use
the non-daemon mode to run the program (see below).

4. Running the program
----------------------
Software required: JRE 6 (included with JDK 6), Ant.

The program can be run with

    ant run

or just

    ant

since "run" the the default target. Both of these options also compile the
program if required.

You can run the program in non-daemon mode using

    ant no-daemon

This will block ant while the program runs, so you can close it with CTRL-C.

5. Feedback
-----------
All feedback is much appreciated. Please send it to:

  [Rok Strnisa](mailto:rok.strnisa@citrix.com "Rok Strnisa")

Thanks.
