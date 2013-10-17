package ours

/**
 * @author mvolkhart
 */
object Entry {
  def main(args: Array[String]) {

    // TODO remove this and use real args
    val newArgs = Array("-d", "src/main/resources/data.csv")
    val app = new CmdLine
    app.run(newArgs)
  }
}
