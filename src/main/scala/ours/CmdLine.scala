package ours

import java.io.{InputStreamReader, BufferedReader, FileReader, File}
import au.com.bytecode.opencsv.CSVReader
import java.sql.{ResultSet, Connection, DriverManager}
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
    connection.setClientInfo("autoReconnect", "true")

    // Add the new data
    // TODO restore call
//    load(new File(commandLine.getOptionValue('d')))

    // Prompt for input and wait
    val scan = new Scanner(System.in)
    var done = false
    while (!done) {
      println("Select an option:")
      println("1 - exit the application.")
      println("2 - List names and phone numbers of owners.")
      println("3 - View minimum weeks owned.")
      println("4 - List maintenance shares.")
      println("5 - List owners for unit.")
      println("6 - User lookup. Shows all shares a person owns.")
      println("7 - Show owners for unit by week.")
      println("8 - Show ownerships for specified week.")
      val i = Try(scan.nextInt)
      if (scan.hasNextLine) scan.nextLine()
      i match {
        case Success(1) => done = true
        case Success(2) => do2()
        case Success(3) => do3(scan)
        case Success(4) => do4(scan)
        case Success(5) => do5(scan)
        case Success(6) => do6(scan)
        case Success(7) => do7(scan)
        case Success(8) => do8(scan)
        case _ =>
          println("Not a valid option")
      }
      println("")
      if (scan.hasNextLine) scan.nextLine()
    }

    closeConnection()
  }

  def do2() {
    val results = connection.createStatement().executeQuery("SELECT last_name, first_name, phone_number FROM owner ORDER BY last_name, first_name;")
    println("last name | first name | phone number")
    while (results.next()) {
      print(results.getString("last_name"))
      print(" | ")
      print(results.getString("first_name"))
      print(" | ")
      println(results.getString("phone_number"))
    }
  }

  def do3(scan :Scanner) {
    print("Unit name: ")
    val unitName = Try(scan.nextLine)
    print("Unit number: ")
    val unitNumber = Try(scan.nextInt)
    print("Weeks owned: ")
    val weeks = Try(scan.nextInt)

    val prep = connection.prepareStatement("SELECT last_name, first_name FROM owner, owner_has_unit WHERE unit_name = ? AND unit_number = ? AND owner_id = id GROUP BY last_name, first_name HAVING COUNT(DISTINCT week_number) >= ?;")
    prep.setString(1, unitName.get)
    prep.setInt(2, unitNumber.get)
    prep.setInt(3, weeks.get)
    val results = prep.executeQuery
    println("last name | first name")
    while (results.next()) {
      print(results.getString("last_name"))
      print(" | ")
      println(results.getString("first_name"))
    }
  }

  def do4(scan :Scanner) {
    print("Unit name: ")
    val unitName = Try(scan.nextLine)
    print("Unit number: ")
    val unitNumber = Try(scan.nextInt)

    val prep = connection.prepareStatement("SELECT last_name, first_name, CAST(cost/(SELECT COUNT(DISTINCT week_number) FROM unit, owner, owner_has_unit WHERE unit_name = ? AND unit_number = ? AND owner.id = owner_id AND unit.name = unit_name AND unit.number = unit_number)*COUNT(DISTINCT week_number) AS DECIMAL(40,2)) AS share FROM unit, owner, owner_has_unit WHERE unit_name = ? AND unit_number = ? AND owner.id = owner_id AND unit.name = unit_name AND unit.number = unit_number GROUP BY last_name, first_name;")
    prep.setString(1, unitName.get)
    prep.setInt(2, unitNumber.get)
    prep.setString(3, unitName.get)
    prep.setInt(4, unitNumber.get)
    val results = prep.executeQuery
    println("last name | first name | share owed")
    while (results.next) {
      print(results.getString("last_name"))
      print(" | ")
      print(results.getString("first_name"))
      print(" | ")
      println(results.getDouble("share"))
    }
  }

  def do5(scan :Scanner) {
    print("Unit name: ")
    val unitName = Try(scan.nextLine())
    val prep = connection.prepareStatement("select o.last_name, o.first_name, count(*) weeks_owned from owner o, owner_has_unit u where o.id = u.owner_id and u.unit_name = ? group by o.last_name having weeks_owned >= 1 order by o.last_name, o.first_name;")
    prep.setString(1, unitName.get)
    val results = prep.executeQuery()
    println("last name | first name | weeks owned")
    while (results.next) {
      print(results.getString("last_name"))
      print(" | ")
      print(results.getString("first_name"))
      print(" | ")
      println(results.getString("weeks_owned"))
    }
  }

  def do6(scan :Scanner) {
    print("last name: ")
    val lastName = Try(scan.nextLine())
    print("first name: ")
    val firstName = Try(scan.nextLine())
    val prep = connection.prepareStatement("select ohu.unit_name,ohu.unit_number, GROUP_CONCAT(ohu.week_number SEPARATOR ', ') AS weeks from owner o, owner_has_unit ohu where o.last_name = ? AND o.first_name = ? and o.id = ohu.owner_id GROUP BY ohu.unit_name, ohu.unit_number order by ohu.unit_name, ohu.unit_number, ohu.week_number;")
    prep.setString(1, lastName.get)
    prep.setString(2, firstName.get)
    val results = prep.executeQuery()
    println("unit name | unit number | weeks owned")
    while (results.next) {
      print(results.getString("unit_name"))
      print(" | ")
      print(results.getInt("unit_number"))
      print(" | ")
      println(results.getString("weeks"))
    }
  }

  def do7(scan :Scanner) {
    print("unit name: ")
    val unitName = Try(scan.nextLine())
    print("unit number: ")
    val unitNumber = Try(scan.nextInt())
    val prep = connection.prepareStatement("select o.last_name, o.first_name, ohu.week_number from owner o, owner_has_unit ohu where ohu.owner_id = o.id and ohu.unit_name = ? and ohu.unit_number = ? order by ohu.week_number;")
    prep.setString(1, unitName.get)
    prep.setInt(2, unitNumber.get)
    val results = prep.executeQuery()
    println("week | last name | first name")
    while (results.next) {
      print(results.getInt("week_number"))
      print(" | ")
      print(results.getString("last_name"))
      print(" | ")
      println(results.getString("first_name"))
    }
  }

  def do8(scan :Scanner) {
    print("week number: ")
    val week = Try(scan.nextInt())
    val prep = connection.prepareStatement("select o.last_name, o.first_name, ohu.unit_name, ohu.unit_number from owner o, owner_has_unit ohu where ohu.owner_id = o.id and ohu.week_number = ? order by ohu.unit_name, ohu.unit_number, o.last_name, o.first_name;")
    prep.setInt(1, week.get)
    val results = prep.executeQuery()
    println("unit name | unit number | last name | first name|")
    while (results.next) {
      print(results.getString("unit_name"))
      print(" | ")
      print(results.getInt("unit_number"))
      print(" | ")
      print(results.getString("last_name"))
      print(" | ")
      println(results.getString("first_name"))
    }
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
    val template = "jdbc:mysql://localhost:3306/lab4?user=%s&password=%s"

    val username = cmdLine.getOptionValue('u', "root")
    val password = cmdLine.getOptionValue('p', "")
    String.format(template, username, password)
  }
}
