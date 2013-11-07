package lab6

import java.sql.DriverManager
import org.apache.commons.cli.{DefaultParser, CommandLine, Options, Option}
import java.util.Scanner
import scala.util.{Success, Try}
import com.amazonaws.services.simpledb.{AmazonSimpleDBClient, AmazonSimpleDB}
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.{Regions, Region}
import com.amazonaws.services.simpledb.model.{ReplaceableAttribute, ReplaceableItem, CreateDomainRequest, DeleteDomainRequest}
import au.com.bytecode.opencsv.CSVReader
import java.io.FileReader
import scala.collection.mutable.ListBuffer

object CmdLine {

  var connection: AmazonSimpleDB = null

  def main(args: Array[String]) {
    run(args)
  }

  def run(args: Array[String]) {

    val commandLine = setupCommandLine(args)

    // Connect to the database
    val connection = connectToAws(commandLine)

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
      val i = Try(scan.nextInt)
      if (scan.hasNextLine) scan.nextLine()
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

  def doStep3(scan: Scanner) {
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

  def doStep4(scan: Scanner) {
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

  def doStep5(scan: Scanner) {
    print("Unit name: ")
    val unitName = Try(scan.nextLine())
    val prep = connection.prepareStatement("SELECT o.last_name, o.first_name, count(*) weeks_owned FROM owner o, owner_has_unit u WHERE o.id = u.owner_id AND u.unit_name = ? GROUP BY o.last_name HAVING weeks_owned >= 1 ORDER BY o.last_name, o.first_name;")
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

  def doStep6(scan: Scanner) {
    print("last name: ")
    val lastName = Try(scan.nextLine())
    print("first name: ")
    val firstName = Try(scan.nextLine())
    val prep = connection.prepareStatement("SELECT ohu.unit_name,ohu.unit_number, GROUP_CONCAT(ohu.week_number SEPARATOR ', ') AS weeks FROM owner o, owner_has_unit ohu WHERE o.last_name = ? AND o.first_name = ? AND o.id = ohu.owner_id GROUP BY ohu.unit_name, ohu.unit_number ORDER BY ohu.unit_name, ohu.unit_number, ohu.week_number;")
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

  def doStep7(scan: Scanner) {
    print("unit name: ")
    val unitName = Try(scan.nextLine())
    print("unit number: ")
    val unitNumber = Try(scan.nextInt())
    val prep = connection.prepareStatement("SELECT o.last_name, o.first_name, ohu.week_number FROM owner o, owner_has_unit ohu WHERE ohu.owner_id = o.id AND ohu.unit_name = ? AND ohu.unit_number = ? ORDER BY ohu.week_number;")
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

  def doStep8(scan: Scanner) {
    print("week number: ")
    val week = Try(scan.nextInt())
    val prep = connection.prepareStatement("SELECT o.last_name, o.first_name, ohu.unit_name, ohu.unit_number FROM owner o, owner_has_unit ohu WHERE ohu.owner_id = o.id AND ohu.week_number = ? ORDER BY ohu.unit_name, ohu.unit_number, o.last_name, o.first_name;")
    prep.setInt(1, week.get)
    val results = prep.executeQuery()
    println("unit name | unit number | last name | first name")
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

  def load(cmdLine: CommandLine) {

    val ownerDomain = "lab6.owner"
    val unitDomain = "lab6.unit"
    val hasDomain: String = "lab6.owner_has_unit"

    // Delete existing domains
    connection.deleteDomain(new DeleteDomainRequest(ownerDomain))
    connection.deleteDomain(new DeleteDomainRequest(unitDomain))
    connection.deleteDomain(new DeleteDomainRequest(hasDomain))
    
    // Create the domains we need
    connection.createDomain(new CreateDomainRequest(ownerDomain))
    connection.createDomain(new CreateDomainRequest(unitDomain))
    connection.createDomain(new CreateDomainRequest(hasDomain))
    
    // Populate domains
    connection.batchPutAttributes(ownerDomain, populateOwner(cmdLine.getOptionValue('o')))

    // Create the database and the tables
    connection.createStatement().execute("DROP SCHEMA IF EXISTS lab4 ;")
    connection.createStatement().execute("CREATE SCHEMA IF NOT EXISTS lab4 DEFAULT CHARACTER SET latin1 ;")
    connection.createStatement().execute("CREATE TABLE IF NOT EXISTS lab4.owner (\n  id INT NOT NULL AUTO_INCREMENT,\n  first_name VARCHAR(255) NULL,\n  phone_number VARCHAR(12) NULL,\n  last_name VARCHAR(255) NULL,\n  PRIMARY KEY (id))\nENGINE = InnoDB;")
    connection.createStatement().execute("CREATE TABLE IF NOT EXISTS lab4.unit (\n  name VARCHAR(255) NOT NULL,\n  number INT NOT NULL,\n  minimum INT NULL,\n  cost INT NULL,\n  PRIMARY KEY (name, number))\nENGINE = InnoDB;")
    connection.createStatement().execute("CREATE TABLE IF NOT EXISTS lab4.owner_has_unit (\n  owner_id INT NOT NULL,\n  unit_name VARCHAR(255) NOT NULL,\n  unit_number INT NOT NULL,\n  week_number INT NOT NULL,\n  PRIMARY KEY (owner_id, unit_name, unit_number, week_number),\n  INDEX fk_owner_has_unit_unit1_idx (unit_name ASC, unit_number ASC),\n  INDEX fk_owner_has_unit_owner_idx (owner_id ASC),\n  CONSTRAINT fk_owner_has_unit_owner\n    FOREIGN KEY (owner_id)\n    REFERENCES lab4.owner (id)\n    ON DELETE NO ACTION\n    ON UPDATE NO ACTION,\n  CONSTRAINT fk_owner_has_unit_unit1\n    FOREIGN KEY (unit_name , unit_number)\n    REFERENCES lab4.unit (name , number)\n    ON DELETE NO ACTION\n    ON UPDATE NO ACTION)\nENGINE = InnoDB;")

    // Switch to using the new database
    connection.close()
    connection = DriverManager.getConnection(parseConnStringFromCmdLine(cmdLine, true))
    connection.setClientInfo("autoReconnect", "true")

    // Import owner data
    var prep = connection.prepareStatement("LOAD DATA LOCAL INFILE ? INTO TABLE lab4.owner FIELDS TERMINATED BY '\\t' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '\\n' IGNORE 1 LINES (id, first_name, phone_number, @var) SET last_name = TRIM(TRAILING '\r' FROM @var);")
    prep.setString(1, cmdLine.getOptionValue('o'))
    prep.execute()

    // Import unit data
    prep = connection.prepareStatement("LOAD DATA LOCAL INFILE ? INTO TABLE lab4.unit FIELDS TERMINATED BY '\\t' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '\\n' IGNORE 1 LINES (name, number, minimum, @var) SET cost = TRIM(TRAILING '\r' FROM @var);")
    prep.setString(1, cmdLine.getOptionValue('n'))
    prep.execute()

    // Import has data
    prep = connection.prepareStatement("LOAD DATA LOCAL INFILE ? INTO TABLE lab4.owner_has_unit FIELDS TERMINATED BY '\\t' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '\\n' IGNORE 1 LINES (owner_id, unit_name, unit_number, @var) SET week_number = TRIM(TRAILING '\r' FROM @var);")
    prep.setString(1, cmdLine.getOptionValue('h'))
    prep.execute()

  }

  def setupCommandLine(args: Array[String]): CommandLine = {
    val accessKey = Option.builder("a").required(true).argName("access").hasArg(true)
      .desc("AWS access key").longOpt("access").build()
    val secretKey = Option.builder("s").required(true).argName("secret").hasArg(true)
      .desc("AWS secret key").longOpt("secret").build()
    val owners = Option.builder("o").required(true).argName("file").hasArg(true)
      .desc("owner data").longOpt("owner").build()
    val has = Option.builder("h").required(true).argName("file").hasArg(true)
      .desc("has data").longOpt("has").build()
    val units = Option.builder("n").required(true).argName("file").hasArg(true)
      .desc("units data").longOpt("unit").build()

    val options = new Options().addOption(accessKey).addOption(secretKey).addOption(owners)
      .addOption(has).addOption(units)
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
