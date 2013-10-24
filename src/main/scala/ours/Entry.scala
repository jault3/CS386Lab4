package ours

/**
 * @author mvolkhart
 */
object Entry {
  def main(args: Array[String]) {

    // TODO remove this and use real args
    val newArgs = Array("-o", "/Volumes/home/Dropbox/workspace/dblab4/src/main/resources/owner_data.txt", "-h",
      "/Volumes/home/Dropbox/workspace/dblab4/src/main/resources/owner_has_unit_data.txt", "-n", "/Volumes/home/Dropbox/workspace/dblab4/src/main/resources/unit_data.txt")
    val app = new CmdLine
    app.run(newArgs)
  }
}
