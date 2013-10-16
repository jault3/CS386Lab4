/**
 * @author Jay Urbain
 *
 * 10/5/2011, updated 10/3/2012
 * Simple MySQL jdbc example show how to conect to a database,
 * create a statement, issue a query, and process a results
 * set.
 * Query is first issued with standard java.sql.Statement, then
 * with java.sql.PrepatedStatement
 */

package db;

import com.mysql.jdbc.ResultSetMetaData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

//import javax.sql.DataSource;


public class JDBCDemo {
	// MySQL
	static String dbdriver = "com.mysql.jdbc.Driver"; 
	static String dburl = "jdbc:mysql://localhost"; 
	//static String dburl = "jdbc:mysql://ec2-107-20-10-136.compute-1.amazonaws.com";
	static String dbname = "video";
	//DataSource ds = null;

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		//scanner.useDelimiter("\n");
		
		System.out.print("Enter login name: ");
		String login = scanner.nextLine();
		
		// Note: password will be echoed to console; 
		System.out.print("Enter password: ");
		String password = scanner.nextLine();
		//String password = PasswordField.readPassword("Enter password: ");
		
		System.out.println("Connecting as user '"+login+"' . . .");

		// Load the JDBC driver.
		// Library (.jar file) must be added to project build path.
		try {
			Class.forName(dbdriver);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		}

		// Connect to the database
	    // You can also connect with Properties for user and password as follows
//		java.util.Properties p = new Properties();
//	    p.put("user", login);
//	    p.put("password", password);
//	    Connection connection = DriverManager.getConnection(dbdriver, p);
		
		Connection connection = null;
		try {
			connection = DriverManager.getConnection((dburl + "/" + dbname), login, password);
			connection.setClientInfo("autoReconnect", "true");
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}

//		Note: could also pass multiple database parameters when creating Connection using Properties
//		Properties props = new Properties();
//		props.setProperty("user", login);
//		props.setProperty("password",password);
//		props.setProperty("autoReconnect",true);
//		Connection connection = DriverManager.getConnection(url,props);

		
		String name = null;
		System.out.print("Enter actor name: ");
		while( (name = scanner.nextLine()).length() > 0) {
		
			// Set up query.
			// First we'll do standard query
			String query = 	
				"select va.name, vr.title as 'Movie Title' " +
				"from Video_Actors va, Video_Recordings vr, Video_Actors_has_Video_Recordings vahvr " +
				"where va.id=vahvr.Video_Actors_id " +
                "and vahvr.Video_Recordings_id=vr.id " +
				"and va.name='"+name+"'";
			
			ResultSet results = null;
			Statement statement = null;
			try {
				statement = connection.createStatement();
				results = statement.executeQuery(query);
				ResultSetMetaData resultSetMetaData = (ResultSetMetaData) results.getMetaData();
				System.out.print("Actor Name");
				System.out.print("|");
				System.out.println("Movie Title");
				while(results.next()) {
					System.out.print(results.getString(1));
					System.out.print("|");
					System.out.println(results.getString("Movie Title"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
				System.exit(0);
			} finally {
				try {
					results.close();
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
					System.exit(0);
				}
			}
	
			// Next will issue the same query using a prepatedStatement
			// Table and column names are quoted, even though it is not
			// necessary if all are in lower case.
			query = 	
				"select va.name as Actor, vr.title as 'Movie Title' " +
                "from Video_Actors va, Video_Recordings vr, Video_Actors_has_Video_Recordings vahvr " +
				"where va.id=vahvr.Video_Actors_id " +
                "and vahvr.Video_Recordings_id=vr.id " +
				"and va.name=?";

            // For each row in the result set, print some columns.
			// Note: query can use "AS" to rename columns as needed.
			PreparedStatement preparedStatement = null;
			try {
				preparedStatement = connection.prepareStatement(query);
				preparedStatement.setString(1,name);
				results = preparedStatement.executeQuery();
				System.out.print("\n\n");
				System.out.print("Actor");
				System.out.print("|");
				System.out.println("Movie Title");
				while(results.next()) {
					System.out.print(results.getString("Actor"));
					System.out.print("|");
					System.out.println(results.getString("Movie Title"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
				System.exit(0);
			} finally {
				try {
					results.close();
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
					System.exit(0);
				}
			}
			
			System.out.print("\nEnter actor name: ");
		}

		// Close the database connection.
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("Done");
		
	}
}

