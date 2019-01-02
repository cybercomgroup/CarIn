import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.lang.StringBuilder;
import java.util.HashMap;
import java.sql.*; // JDBC stuff.
import java.util.Properties;


public class OsCarLogParser
{
    static String USERNAME = "";
    static String PASSWORD = "";

    //Reads input from command line and opens the local logfile to be parsed.
    //Hands over to parse() while keeping track of used resources and closes when parse() is done.
    public static void main(String[] args) throws Exception
    {
	FileInputStream fs = null;
	InputStreamReader fr = null;
	BufferedReader file = null;
	//If filename is entered, use it. Else, print error
	if ( args.length <= 2 )
	    {
		System.out.println("Enter the relative path to the log file, the username and the password.");
	    }
	else
	    {
		USERNAME = args[1];
		PASSWORD = args[2];
		
		try
		    {
			//open the file in an easily used format
			fs = new FileInputStream(args[0]);
		        fr = new InputStreamReader(fs);
			file = new BufferedReader(fr);
			
			parse(file);
		    }
		catch(Exception e)
		    {
			System.out.println("Error occured opening file. Please enter an existing, readable file.");
			e.printStackTrace();
		    }
		finally
		    {
			// releases system resources associated with this stream
			if(file!=null)
			    {
				file.close();
			    }
			if(fr!=null)
			    {
				fr.close();
			    }
			if(fs!=null)
			    {
				fs.close();
			    }	
		    }
		return;
	    }
    }

    //An error macro for parse errors
    private static void error()
    {
	System.out.println("Unknown log format!");
	System.exit(1);
    }

    //The method that does the rest, could be split into database and parse.
    private static void parse(BufferedReader logfile)
    {
	//Do the database thing
	Connection conn = null;
	try
	    {
		//Note that this will fail if the driver jar isn't in the classpath
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql://localhost/";
		Properties props = new Properties();
		props.setProperty("user",USERNAME);
		props.setProperty("password",PASSWORD);
		conn = DriverManager.getConnection(url, props);
	    }
	catch( Exception e )
	    {
		System.out.println("Error occured connecting to database. Have you set the right connection settings in the source-code? Do you have network access to the database?");
		e.printStackTrace();
		System.exit(1);
	    }

	if (conn == null)
	    {
		error();
	    }

	System.out.println("Database connected");
	
	//Read the file line by line, parsing each line
	try
	    {
		HashMap<String,String> blackboard = new HashMap<String,String>();
		HashMap<String,String> property = new HashMap<String,String>();
		HashMap<String,String> map = blackboard;
		StringBuilder buffer = new StringBuilder();
		String key = new String();
		Boolean inProperties = false;
		        
		int linenr = 0;
		for (String line = logfile.readLine(); line != null; line = logfile.readLine()) {
		    System.out.println(++linenr);
		    //First check that the line starts correctly with "{"
		    if (line.charAt(0) != '{')
			{
			    error();
			}
		    //Loop through the line setting the data into a buffer.
		    //Every data string into its own place in the hash-map.
		    //Properties are placed in a separate hash-map, so that the varying names of the columns can be handled.
		    blackboard = new HashMap<String,String>();
		    property = new HashMap<String,String>();
		    map = blackboard;
		    buffer = new StringBuilder();
		    key = new String();
		    inProperties = false;
		    for ( int i = 1; line.length() > i; i++ )
			{
			    char buff = line.charAt(i);
			    switch (buff)
				{
				case ' ':
				    //Blank spaces have no meaning in the log, thus they are ignored
				    break;
				case '"':
				    //End or start of quote are extraneous, since other symbols declare the start and end of strings
				    break;
				case ':':
				    //The column name for the data is in the buffer. Save it to key.
				    key = buffer.toString();
				    //Clear the buffer to make space for the data.
				    buffer = new StringBuilder();
				    break;
				case ',':
				    //Denominates the end of a key-value pair. Save the data into map.
				    map.put(key, buffer.toString());
				    //And clear the buffer
				    buffer = new StringBuilder();
				    break;
				case '{':
				    //This is a underlying data pair. These are either split and saved with the rest or put into a separate map
				    if (key.equals("location") )
					{
					    //These are longitude and latitude or 
					    //Pretend everything is normal, the key will be overwritten and the sub data entered with the rest
					}
				    else if ( inProperties )
					{
					    //These are properties. They are to be put into the properties map, which must be cleared.
					    property = new HashMap<String,String>();
					    map = property;
					    //As above the key will be overwritten and data entered normally
					}
				    else
					{
					    //This should never happen!
					    System.out.println("Misplaced {! character " + i);
					    error();
					}
				    break;
				case '}':
				    //If this is in properties, commit the map to the database
				    //Else, ignore it
				    if ( inProperties )
					{
					    //Bind the last given value into the map
					    map.put(key, buffer.toString());
					    //And clear the buffer
					    buffer = new StringBuilder();
					    
					    //Initiate query
					    PreparedStatement query = conn.prepareStatement("INSERT INTO properties VALUES(?, ?, ?)");
					    query.setInt(1, linenr);
					    for( HashMap.Entry<String, String> tmp : map.entrySet() )
						{
						    if (tmp.getKey().equals("type"))
							{
							    //enter as type in database
				       			    query.setString(2, tmp.getValue());
							}
						    else
							{
							    //enter as value in database
							    query.setString(3, tmp.getValue());
							}
						}
					    //execute query
					    query.executeUpdate();
					}
				    break;
				case '[':
				    //Start of the underlying properties, just make sure.
				    if (key.equals("blackboardProperties") )
					{
					    //blackboardProperties MUST be the last entry. Therefore, save the object and get the int key
					    
					    //Initiate query
					    PreparedStatement query = conn.prepareStatement("INSERT INTO blackboard VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
					    //Set the key
					    query.setInt(1, linenr);
					    //Insert all the data
					    for( HashMap.Entry<String, String> tmp : map.entrySet() )
						{
						    switch (tmp.getKey())
							{
							case "itemId":
							    query.setString(2, tmp.getValue());
							    break;
							case "stationId":
							    query.setInt(3, Integer.parseInt(tmp.getValue()));
							    break;
							case "appId":
							    query.setInt(4, Integer.parseInt(tmp.getValue()));
							    break;
							case "typeId":
							    query.setString(5, tmp.getValue());
							    break;
							case "latitude":
							    query.setFloat(6, Float.parseFloat(tmp.getValue()));
							    break;
							case "longitude":
							    query.setFloat(7, Float.parseFloat(tmp.getValue()));
							    break;
							case "locationConfidence":
							    query.setFloat(8, Float.parseFloat(tmp.getValue()));
							    break;
							case "validityDuration":
							    query.setFloat(9, Float.parseFloat(tmp.getValue()));
							    break;
							case "validityArea":
							    //query.setInt(10, Integer.parseInt(tmp.getValue()));
							    query.setInt(10, 0);
							    break;
							}
						}
					    //execute query
					    query.executeUpdate();//query stuff
					    //query.
					    //Set the inProperties flag and roll along. The first data pair will handle setting the map correctly.
					    inProperties = true;
					}
				    else
					{
					    //This should never happen!
					    System.out.println("Misplaced [!");
					    error();
					}
				    break;
				case ']':
				    //Mark that we are leaving properties
				    if (inProperties)
					{
					    inProperties = false;
					}
				    break;
				default:
				    buffer.append(line.charAt(i));
				    break;
					    
				}
			}
		}
	    }
	catch(Exception e)
	    {
		e.printStackTrace();
		System.out.println("Error occured when reading file.");
	    }
    }
}
