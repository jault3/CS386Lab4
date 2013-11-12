package lab6

import org.apache.commons.cli.{DefaultParser, CommandLine, Options, Option}
import java.util.Scanner
import scala.util.{Failure, Try, Success}
import com.amazonaws.services.simpledb.{AmazonSimpleDBClient, AmazonSimpleDB}
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.{Regions, Region}
import com.amazonaws.services.simpledb.model._
import au.com.bytecode.opencsv.CSVReader
import java.io.FileReader
import scala.collection.mutable.ListBuffer
import java.util
import scala.collection.JavaConverters._

object CmdLine {

  var connection: AmazonSimpleDB = null

  def main(args: Array[String]) {
    run(args)
  }

  def run(args: Array[String]) {

    val commandLine = setupCommandLine(args)

    // Connect to the database
    connection = connectToAws(commandLine)

    Try(load(commandLine)) match {
      case Failure(ex) => println("There was an error: " + ex.getMessage)
    }

    // Prompt for input and wait
    val scan = new Scanner(System.in)
    var done = false
    while (!done) {
      println("Select an option:")
      println("1 - Exit the application.")
      println("2 - List names and phone numbers of owners.")
      println("3 - View minimum weeks owned.")
      println("4 - List maintenance shares.")
      println("5 - List owners for unit.")
      println("6 - User lookup. Shows all shares a person owns.")
      println("7 - Show owners for unit by week.")
      println("8 - Show ownerships for specified week.")
      val i = Try(Integer.parseInt(scan.nextLine()))
      //if (scan.hasNextLine) scan.nextLine()
      i match {
        case Success(1) => done = true
        case Success(2) => doStep2()
        case Success(3) => doStep3(scan)
        case Success(4) => doStep4(scan)
        case Success(5) => doStep5(scan)
        case Success(6) => doStep6(scan)
        case Success(7) => doStep7(scan)
        case Success(8) => doStep8(scan)
        case _ =>
          println("Not a valid option")
      }

      // TODO: there is a bug here that causes scanner to require extra line breaks before
      // responding to a request.
      if (scan.hasNextLine) scan.nextLine()
    }
  }

  def doStep2() {
//    val results = connection.createStatement().executeQuery("SELECT last_name, first_name, phone_number FROM owner ORDER BY last_name, first_name;")
//    println("last name | first name | phone number")
//    while (results.next()) {
//      print(results.getString("last_name"))
//      print(" | ")
//      print(results.getString("first_name"))
//      print(" | ")
//      println(results.getString("phone_number"))
//    }
  }

  def doStep3(scan: Scanner) {
    print("Unit name: ")
    val unitName = Try(scan.nextLine)
    print("Unit number: ")
    val unitNumber = Try(scan.nextInt)
    print("Weeks owned: ")
    val weeks = Try(scan.nextInt)

//    val prep = connection.prepareStatement("SELECT last_name, first_name FROM owner, owner_has_unit WHERE unit_name = ? AND unit_number = ? AND owner_id = id GROUP BY last_name, first_name HAVING COUNT(DISTINCT week_number) >= ?;")
//    prep.setString(1, unitName.get)
//    prep.setInt(2, unitNumber.get)
//    prep.setInt(3, weeks.get)
//    val results = prep.executeQuery
//    println("last name | first name")
//    while (results.next()) {
//      print(results.getString("last_name"))
//      print(" | ")
//      println(results.getString("first_name"))
//    }
  }

  def doStep4(scan: Scanner) {
    print("Unit name: ")
    val unitName = Try(scan.nextLine)
    print("Unit number: ")
    val unitNumber = Try(scan.nextInt)

//    val prep = connection.prepareStatement("SELECT last_name, first_name, CAST(cost/(SELECT COUNT(DISTINCT week_number) FROM unit, owner, owner_has_unit WHERE unit_name = ? AND unit_number = ? AND owner.id = owner_id AND unit.name = unit_name AND unit.number = unit_number)*COUNT(DISTINCT week_number) AS DECIMAL(40,2)) AS share FROM unit, owner, owner_has_unit WHERE unit_name = ? AND unit_number = ? AND owner.id = owner_id AND unit.name = unit_name AND unit.number = unit_number GROUP BY last_name, first_name;")
//    prep.setString(1, unitName.get)
//    prep.setInt(2, unitNumber.get)
//    prep.setString(3, unitName.get)
//    prep.setInt(4, unitNumber.get)
//    val results = prep.executeQuery
//    println("last name | first name | share owed")
//    while (results.next) {
//      print(results.getString("last_name"))
//      print(" | ")
//      print(results.getString("first_name"))
//      print(" | ")
//      println(results.getDouble("share"))
//    }
  }

  def doStep5(scan: Scanner) {
    print("Unit name: ")
    val unitName = Try(scan.nextLine())
//    val prep = connection.prepareStatement("SELECT o.last_name, o.first_name, count(*) weeks_owned FROM owner o, owner_has_unit u WHERE o.id = u.owner_id AND u.unit_name = ? GROUP BY o.last_name HAVING weeks_owned >= 1 ORDER BY o.last_name, o.first_name;")
//    prep.setString(1, unitName.get)
//    val results = prep.executeQuery()
//    println("last name | first name | weeks owned")
//    while (results.next) {
//      print(results.getString("last_name"))
//      print(" | ")
//      print(results.getString("first_name"))
//      print(" | ")
//      println(results.getString("weeks_owned"))
//    }
  }

  def doStep6(scan: Scanner) {
    print("last name: ")
    val lastName = Try(scan.nextLine())
    print("first name: ")
    val firstName = Try(scan.nextLine())
//    val prep = connection.prepareStatement("SELECT ohu.unit_name,ohu.unit_number, GROUP_CONCAT(ohu.week_number SEPARATOR ', ') AS weeks FROM owner o, owner_has_unit ohu WHERE o.last_name = ? AND o.first_name = ? AND o.id = ohu.owner_id GROUP BY ohu.unit_name, ohu.unit_number ORDER BY ohu.unit_name, ohu.unit_number, ohu.week_number;")
//    prep.setString(1, lastName.get)
//    prep.setString(2, firstName.get)
//    val results = prep.executeQuery()
//    println("unit name | unit number | weeks owned")
//    while (results.next) {
//      print(results.getString("unit_name"))
//      print(" | ")
//      print(results.getInt("unit_number"))
//      print(" | ")
//      println(results.getString("weeks"))
//    }
  }

  def doStep7(scan: Scanner) {
    print("unit name: ")
    val unitName = Try(scan.nextLine())
    print("unit number: ")
    val unitNumber = Try(scan.nextInt())
//    val prep = connection.prepareStatement("SELECT o.last_name, o.first_name, ohu.week_number FROM owner o, owner_has_unit ohu WHERE ohu.owner_id = o.id AND ohu.unit_name = ? AND ohu.unit_number = ? ORDER BY ohu.week_number;")
//    prep.setString(1, unitName.get)
//    prep.setInt(2, unitNumber.get)
//    val results = prep.executeQuery()
//    println("week | last name | first name")
//    while (results.next) {
//      print(results.getInt("week_number"))
//      print(" | ")
//      print(results.getString("last_name"))
//      print(" | ")
//      println(results.getString("first_name"))
//    }
  }

  def doStep8(scan: Scanner) {
    print("week number: ")
    val week = Try(scan.nextInt())
//    val prep = connection.prepareStatement("SELECT o.last_name, o.first_name, ohu.unit_name, ohu.unit_number FROM owner o, owner_has_unit ohu WHERE ohu.owner_id = o.id AND ohu.week_number = ? ORDER BY ohu.unit_name, ohu.unit_number, o.last_name, o.first_name;")
//    prep.setInt(1, week.get)
//    val results = prep.executeQuery()
//    println("unit name | unit number | last name | first name")
//    while (results.next) {
//      print(results.getString("unit_name"))
//      print(" | ")
//      print(results.getInt("unit_number"))
//      print(" | ")
//      print(results.getString("last_name"))
//      print(" | ")
//      println(results.getString("first_name"))
//    }
  }

  def load(cmdLine: CommandLine) {

    val ownerDomain = "lab6.owner"
    val unitDomain = "lab6.unit"

    // Delete existing domains
    connection.deleteDomain(new DeleteDomainRequest(ownerDomain))
    connection.deleteDomain(new DeleteDomainRequest(unitDomain))

    // Create the domains we need
    connection.createDomain(new CreateDomainRequest(ownerDomain))
    connection.createDomain(new CreateDomainRequest(unitDomain))

    // Populate domains
    connection.batchPutAttributes(new BatchPutAttributesRequest(ownerDomain,
      readData(cmdLine.getOptionValue('o'))))
    connection.batchPutAttributes(new BatchPutAttributesRequest(unitDomain,
      readData(cmdLine.getOptionValue('u'))))
  }

  def readData(file: String): util.ArrayList[ReplaceableItem] = {

    // Read the whole file. It's short so no memory issues.
    val reader = new CSVReader(new FileReader(file))
    val entries = reader.readAll()
    reader.close()

    // The first entry is the columns
    val columns = entries.get(0)

    val retVal = new util.ArrayList[ReplaceableItem]()

    // For every every entry...
    for (j <- 1 to entries.size() - 1) {
      val entry: Array[String] = entries.get(j)
      val attributes = new ListBuffer[ReplaceableAttribute]

      // For every column...
      for (i <- 1 to columns.length - 1) {

        // Create and add an attribute.
        attributes += new ReplaceableAttribute(columns(i), entry(i), true);
      }
      retVal.add(new ReplaceableItem(entry(0)).withAttributes(attributes.asJava))
    }
    return retVal
  }

  def setupCommandLine(args: Array[String]): CommandLine = {
    val accessKey = Option.builder("a").required(true).argName("access").hasArg(true)
      .desc("AWS access key").longOpt("access").build()
    val secretKey = Option.builder("s").required(true).argName("secret").hasArg(true)
      .desc("AWS secret key").longOpt("secret").build()
    val owners = Option.builder("o").required(true).argName("file").hasArg(true)
      .desc("owner data").longOpt("owner").build()
    val units = Option.builder("u").required(true).argName("file").hasArg(true)
      .desc("units data").longOpt("unit").build()

    val options = new Options().addOption(accessKey).addOption(secretKey).addOption(owners)
      .addOption(units)
    val parser = new DefaultParser
    parser.parse(options, args)
  }

  def connectToAws(cmdLine: CommandLine): AmazonSimpleDB = {
    val credentials = new BasicAWSCredentials(cmdLine.getOptionValue('a'),
      cmdLine.getOptionValue('s'))
    val sdb = new AmazonSimpleDBClient(credentials)
    sdb.setRegion(Region.getRegion(Regions.US_WEST_2))
    return sdb
  }
}
