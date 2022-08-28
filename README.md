# Losing the Thread #

*Project undertaken in fulfillment of MSc Computer Science, 
Birkbeck, University of London*

## Instructions for use ##

### 1. Make a jar file containing your multithreaded application in IntelliJ ###

(JetBrains reference: https://www.jetbrains.com/help/idea/compiling-applications.html#package_into_jar)

1. Open **File > Project Structure**
2. Under **Project Settings**, navigate to **Artifacts**
3. Click the '+' symbol at the top of the left pane
4. Select **JAR > From modules with dependiencies...**
5. In the 'Module' field, select the module containing the application on which you want to run _Losing the Thread_
6. In the 'Main Class' field, select the class whose main method is run to start your application
7. Press OK to exit the Create JAR from Modules window
8. Press OK to exit the Project Structure window
9. Finally, navigate to the **Build** tab on the main toolbar, select **Build Artifacts** and navigate to the JAR you just created. Press build



### 2. Run the agent.jar

Type the following command into the Terminal (substituting the sample paths for the paths from Content root):

`java -javaagent:path/to/agent.jar -jar path/to/your.jar`

_(Note: to get the path to your JAR file, navigate to the file in your **Project Directory**, right click on it and select **Copy Path/Reference... > Path From Content Root**)_