package ours

import java.io.{InputStreamReader, BufferedReader, FileReader, File}
import au.com.bytecode.opencsv.CSVReader
import java.sql.{Connection, DriverManager}
import org.apache.commons.cli.{DefaultParser, CommandLine, Options, Option}
import java.util.Scanner
import scala.util.{Success, Try}

/**
 * @author mvolkhart
 */
class CmdLine {

  private var connection :Connection = null

  def run(args: Array[String]) {

    val commandLine = setupCommandLine(args)

    // Connect to the database
    val connStr = parseConnStringFromCmdLine(commandLine)
    classOf[com.mysql.jdbc.Driver]
    connection = DriverManager.getConnection(connStr)

    // Add the new data
    // TODO restore call
//    load(new File(commandLine.getOptionValue('d')))

    // Prompt for input and wait
    val scan = new Scanner(System.in)
    var done = false
    while (!done) {
      println("Select an option:")
      println("0 - exit the application.")
      println("1 - List names and phone numbers of owners.")
      println("2 - View minimum weeks owned.")
      println("3 - List maintenance shares.")
      println("4 - List owners for unit.")
      println("5 - User lookup. Shows all shares a person owns.")
      println("6 - Show year for unit.")
      val i = Try(scan.nextInt())
      i match {
        case Success(0) => done = true
        case Success(1) =>
          connection.createStatement().execute("SELECT last_name, first_name, phone_number FROM owner ORDER BY last_name, first_name;")
        case Success(2) =>
        case Success(3) =>
        case Success(4) =>
        case Success(5) =>
        case Success(6) =>
        case _ =>
          println("Not a valid option")
          if (scan.hasNextLine) scan.nextLine()
      }
    }

    closeConnection()
  }

  def load(toImport: File): Either[String, String] = {

    // Create the tables, dropping any old
//    val reader = new BufferedReader(new FileReader("src/main/resources/create_schema.sql"))
    val reader = new BufferedReader(new InputStreamReader(this.getClass
      .getClassLoader.getResourceAsStream("create_schema.sql")))
    val wholeFile = new StringBuffer
    var currentLine: String = ""
    while(currentLine != null) {
      wholeFile.append(currentLine)
      wholeFile.append(" ")
      currentLine = reader.readLine()
    }
    connection.prepareCall(wholeFile.toString).execute
//    for (query <- wholeFile.split(';')) {
//      println(query + ";")
//      connection.createStatement().execute(query + ";")
//    }

    val csvReader = new CSVReader(new FileReader(toImport))
    val rows = csvReader.readAll()
    Left("fail")
    Right("pass")









//    CSVReader reader = null;
//    try {
//      reader = new CSVReader(new FileReader(toImport));
//    } catch (FileNotFoundException e) {
//      return new Status(Status.FAIL, "File not found.");
//    }
//    try {
//      List<String[]> rows = reader.readAll();
//    } catch (IOException e) {
//      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//    }
//    return new Status(Status.PASS, "File successfully imported.");
  }

  def getView() = View.MENU

  def closeConnection() {
    connection.close()
  }

  def setupCommandLine(args: Array[String]): CommandLine = {
    val username = Option.builder("u").required(false).argName("name").hasArg(true)
      .desc("username for the database").longOpt("user").build()
    val password = Option.builder("p").required(false).argName("secret").hasArg(true)
      .desc("password for the database").longOpt("password").build()
    val owners = Option.builder("d").required(true).argName("file").hasArg(true)
      .desc("csv of data").longOpt("data").build()

    val options = new Options().addOption(username).addOption(password).addOption(owners)
    val parser = new DefaultParser
    parser.parse(options, args)
  }

  def parseConnStringFromCmdLine(cmdLine: CommandLine): String = {
    val template = "jdbc:mysql://localhost:3306?user=%s&password=%s"

    val username = cmdLine.getOptionValue('u', "root")
    val password = cmdLine.getOptionValue('p', "")
    String.format(template, username, password)
  }
}
