package lab6

import org.apache.commons.cli.{DefaultParser, CommandLine, Options, Option}
import java.util.{Collections, Scanner}
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
import scala.collection.JavaConversions._
import com.google.common.collect.{HashBasedTable, Table}

object CmdLine {

  var connection: AmazonSimpleDB = null
  val ownerDomain = "lab6.owner"
  val unitDomain = "lab6.unit"

  def main(args: Array[String]) {
    run(args)
  }

  def run(args: Array[String]) {

    val commandLine = setupCommandLine(args)

    // Connect to the database
    connection = connectToAws(commandLine)

    // Prompt for input and wait
    val scan = new Scanner(System.in)
    var done = false
    while (!done) {
      println("Select an option:")
      println("0 - Exit the application.")
      println("1 - Load data.")
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
        case Success(0) => done = true
        case Success(1) => Try(load(commandLine)) match {
          case Failure(ex) => println("There was an error: " + ex.getMessage)
          case Success(_) => println("Loaded the data!")
        }
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
    val selectRequest = new SelectRequest(s"select last_name, first_name, " +
      s"phone_number from `$ownerDomain` where last_name is not null order by last_name")
    val orderedList = new ListBuffer[(String, String)]
    for (item: Item <- connection.select(selectRequest).getItems) {
      var lastName: String = null
      var firstName: String = null
      var phoneNumber: String = null
      println(s"Item: ${item.getName}")
      for (attribute: Attribute <- item.getAttributes) {
        attribute.getName match {
          case "last_name" => lastName = attribute.getValue
          case "first_name" => firstName = attribute.getValue
          case "phone_number" => phoneNumber = attribute.getValue
        }
      }
      orderedList += ((s"$lastName,$firstName", phoneNumber))
    }
    for (entry <- orderedList.sorted) {
      println(entry.toString().replace(",", " | "))
    }
  }

  //fetch all owners that own a user specified amount of weeks
  def doStep3(scan: Scanner) {
    print("Unit name: ")
    val unitName = Try(scan.nextLine).get
    print("Unit number: ")
    val unitNumber = Try(scan.nextLine).get
    print("Weeks owned: ")
    val weeks = Try(scan.nextLine).get

    val ownerRequest = new SelectRequest(s"select last_name, first_name from `$ownerDomain`")
    val unitRequest = new SelectRequest(s"select * from `$unitDomain` where number = '$unitNumber' " +
      s"and name = '$unitName'")

    val ownerMap = new java.util.HashMap[String, Integer]()

    for (item: Item <- connection.select(ownerRequest).getItems) {

      var lastName: String = null
      var firstName: String = null
      val id = Integer.valueOf(item.getName)

      //for each attribute of row
      for (attribute: Attribute <- item.getAttributes) {
        //check the name of the attr and test if it matches
        attribute.getName match {
          case "last_name" => lastName = attribute.getValue
          case "first_name" => firstName = attribute.getValue
        }
      }

      ownerMap.put(s"$lastName,$firstName", id)
    }


    val idList = new util.ArrayList[Integer]()
    for (item: Item <- connection.select(unitRequest).getItems) {

      for (attribute: Attribute <- item.getAttributes) {
        if (attribute.getName.startsWith("week") && !"".equals(attribute.getValue)) {
          idList.add(Integer.valueOf(attribute.getValue))
        }
      }
    }


    val uniqueIds = new util.HashSet[Integer](idList)
    for (id: Integer <- uniqueIds) {
      if (Collections.frequency(idList, id) >= weeks.toInt) {
        for (owner: String <- ownerMap.keySet()) {
          if (id == ownerMap.get(owner)) {
            println(s"| ${owner.replace(",", " | ")} |")
          }
        }

      }
    }
  }

  def doStep4(scan: Scanner) {
    print("Unit name: ")
    val unitName = Try(scan.nextLine).get
    print("Unit number: ")
    val unitNumber = Try(scan.nextLine).get

    val unitRequest = new SelectRequest(s"select * from `$unitDomain` where number = '$unitNumber'" +
      s" and name = '$unitName'")
    val ownerRequest = new SelectRequest(s"select * from `$ownerDomain`")

    val unitTable = convertSelectResultToTable(connection.select(unitRequest))
    val ownerTable = convertSelectResultToTable(connection.select(ownerRequest))

    // Figure out how many weeks each person owns
    // For every row...
    for (row <- unitTable.rowKeySet()) {
      val map = scala.collection.mutable.Map[String, Int]()
      // get the unit name
      val unitName = unitTable.get(row, "name")
      // get the unit number
      val unitNumber = unitTable.get(row, "number")
      // know the unit cost
      val unitCost = unitTable.get(row, "cost").toInt
      // For every week
      for (i <- 1 until 52) {
        // get the owner
        val owner = unitTable.get(row, s"week$i")
        // if the week has an owner
        if (!"".equals(owner)) {
          // update the owner's week count
          val currentNumberOfOwnedWeeks = map.get(owner).getOrElse(0)
          map += owner -> (currentNumberOfOwnedWeeks + 1)
        }
      }
      val costPerWeek = unitCost / 52.0
      print(s"| $unitName | $unitNumber |")
      map.entrySet().foreach {
        entry =>
          print(s" ${ownerTable.get(entry.getKey, "first_name")} ${ownerTable.get(entry.getKey, "last_name")}, ${entry.getValue * costPerWeek} |")
      }
      println()
    }
  }

  //fetch all owners who own one or more weeks and show how many weeks they own it
  def doStep5(scan: Scanner) {
    print("Unit name: ")
    val unitName = Try(scan.nextLine()).get
    print("Unit number: ")
    val unitNumber = Try(scan.nextLine).get

    val ownerRequest = new SelectRequest(s"select * from `$ownerDomain`")
    val unitRequest = new SelectRequest(s"select * from `$unitDomain` where number = '$unitNumber' " +
      s"and name = '$unitName'")

    val ownerTable = convertSelectResultToTable(connection.select(ownerRequest))
    val unitTable = convertSelectResultToTable(connection.select(unitRequest))

    for (row <- unitTable.rowKeySet()) {
      val map = scala.collection.mutable.Map[String, Int]()
      val unitName = unitTable.get(row, "name")
      val unitNumber = unitTable.get(row, "number")

      for (i <- 1 until 52) {
        val owner = unitTable.get(row, s"week$i")

        if (!"".equals(owner)) {
          val currentNumberOfOwnedWeeks = map.get(owner).getOrElse(0)
          map += owner -> (currentNumberOfOwnedWeeks + 1)
        }
      }

      print(s"| $unitName | $unitNumber |")
      map.entrySet().foreach {
        entry =>
          print(s" ${ownerTable.get(entry.getKey, "first_name")} ${ownerTable.get(entry.getKey, "last_name")}, ${entry.getValue} |")
      }
      println()
    }
  }

  //prompt for name, provide all unit names, number and week numbers that the person owns it
  def doStep6(scan: Scanner) {
    print("last name: ")
    val lastName = Try(scan.nextLine()).get
    print("first name: ")
    val firstName = Try(scan.nextLine()).get

    val ownerRequest = new SelectRequest(s"select last_name, first_name " +
      s"from `$ownerDomain` where last_name = '$lastName' and first_name= '$firstName'")
    val unitRequest = new SelectRequest(s"select * from `$unitDomain`")

    val ownerTable = convertSelectResultToTable(connection.select(ownerRequest))
    val unitTable = convertSelectResultToTable(connection.select(unitRequest))

    for (row <- unitTable.rowKeySet()) {
      val map = scala.collection.mutable.Map[String, String]()
      val unitName = unitTable.get(row, "name")
      val unitNumber = unitTable.get(row, "number")

      for (i <- 1 until 52) {
        val owner = unitTable.get(row, s"week$i")

        if (!"".equals(owner)) {
          map += owner -> s"week$i"
        }
      }

      print(s"| $unitName | $unitNumber |")

      map.entrySet().foreach {
        entry =>
          print(s" ${ownerTable.get(entry.getKey, "first_name")} ${ownerTable.get(entry.getKey, "last_name")}, ${entry.getValue} |")
      }
      println()
    }
  }

  //unit name and number, get all owners and their weeks
  def doStep7(scan: Scanner) {
    print("unit name: ")
    val unitName = Try(scan.nextLine()).get
    print("unit number: ")
    val unitNumber = Try(scan.nextLine()).get

    val ownerRequest = new SelectRequest(s"select last_name, first_name from `$ownerDomain`")
    val unitRequest = new SelectRequest(s"select * from `$unitDomain` where name = '$unitName'" +
      s" and number = '$unitNumber'")
    //list of owners (first and last names) and ids
    val ownerMap = new java.util.HashMap[Integer, String]()

    for (item: Item <- connection.select(ownerRequest).getItems) {
      var lastName: String = null
      var firstName: String = null
      val id = Integer.valueOf(item.getName)

      //get the id so that the user name can be displayed after the fetch
      //for each attribute of row
      for (attribute: Attribute <- item.getAttributes) {
        //check the name of the attr and test if it matches
        attribute.getName match {
          case "last_name" => lastName = attribute.getValue
          case "first_name" => firstName = attribute.getValue
          case _ => // Do Nothing
        }
      }

      ownerMap.put(id, s"$lastName,$firstName")
    }

    val sortedWeek = new util.TreeMap[String, Integer]()

    for (item: Item <- connection.select(unitRequest).getItems) {
      for (attribute: Attribute <- item.getAttributes) {
        //get the weeks that have values
        if (attribute.getName.startsWith("week") && !"".equals(attribute.getValue)) {
          //store alphanumerically in a tree
          sortedWeek.put(attribute.getName, Integer.parseInt(attribute.getValue))
        }
      }
    }

    //combine the unit id with the name of the owner
    for (weekName: String <- sortedWeek.keySet()) {
      val unitOwnerId = sortedWeek.get(weekName)
      for (ownerId: Integer <- ownerMap.keySet()) {
        if (unitOwnerId.equals(ownerId)) {
          println(weekName + " | " + ownerMap.get(ownerId).replace(",", " | "))
        }
      }

    }
  }

  //prompt for week and get all owners who own units during that week
  def doStep8(scan: Scanner) {
    print("week number: ")
    val week = Try(scan.nextLine()).get

    val ownerRequest = new SelectRequest(s"select * from `$ownerDomain`")
    val unitRequest = new SelectRequest(s"select * from `$unitDomain` where week$week != ``")

    val ownerTable = convertSelectResultToTable(connection.select(ownerRequest))
    val unitTable = convertSelectResultToTable(connection.select(unitRequest))

    for (row <- unitTable.rowKeySet()) {
      val map = scala.collection.mutable.Map[String, String]()
      val unitName = unitTable.get(row, "name")
      val unitNumber = unitTable.get(row, "number")

      val owner = unitTable.get(row, week)

      if (!"".equals(owner)) {
        map += owner -> week
      }

      print(s"| $unitName | $unitNumber |")

      map.entrySet().foreach {
        entry =>
          print(s" ${ownerTable.get(entry.getKey, "first_name")} ${ownerTable.get(entry.getKey, "last_name")}, ${entry.getValue} |")
      }
      println()
    }
  }

  def load(cmdLine: CommandLine) {

    // Delete existing domains
    connection.deleteDomain(new DeleteDomainRequest(ownerDomain))
    connection.deleteDomain(new DeleteDomainRequest(unitDomain))

    // Create the domains we need
    connection.createDomain(new CreateDomainRequest(ownerDomain))
    connection.createDomain(new CreateDomainRequest(unitDomain))
    println("Created")

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
    retVal
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
    sdb.setRegion(Region.getRegion(Regions.US_EAST_1))
    return sdb
  }

  def convertSelectResultToTable(results: SelectResult): Table[String, String, String] = {
    val retVal = HashBasedTable.create[String, String, String]()
    for (item: Item <- results.getItems) {
      for (attribute: Attribute <- item.getAttributes) {
        retVal.put(item.getName, attribute.getName, attribute.getValue)
      }
    }
    retVal
  }
}
