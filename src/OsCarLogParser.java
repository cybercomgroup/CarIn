import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.lang.StringBuilder;
import java.util.HashMap;
import java.sql.*; // JDBC stuff.


public class OsCarLogParser
{
    public static void main(String[] args) throws Exception
    {
	FileInputStream fs;
	InputStreamReader fr;
	BufferedReader file;
	//If filename is entered, use it. Else, print error
	if ( args.length <= 0 )
	    {
		System.out.println("Enter the relative path to the log file.");
	    }
	else
	    {
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

    private static void error()
    {
	System.out.println("Unknown log format!");
	System.exit(1);
    }
    
    private static void parse(BufferedReader logfile)
    {
	//Do the database thing
	try
	    {
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql://ate.ita.chalmers.se/";
		Properties props = new Properties();
		props.setProperty("user",USERNAME);
		props.setProperty("password",PASSWORD);
		Connection conn = DriverManager.getConnection(url, props);
	    }
	catch( Exception e )
	    {
		System.out.println("Error occured connecting to database. Have you set the right connection settings in the source-code? Do you have network access to the database?");
		e.printStackTrace();
		System.exit(1);
	    }
	
	//Read the file line by line
	try
	    {
		for (String line = logfile.readLine(); line != null; line = logfile.readLine()) {
		    //First check that the line starts correctly with "{"
		    if (line.charAt(0) != '{')
			{
			    error();
			}
		    //Loop through the line setting the data into a buffer.
		    //Every data string into its own place in the hash-map.
		    //Properties are placed in a separate hash-map, so that the varying names of the columns can be handled.
		    HashMap blackboard = new HashMap();
		    HashMap property = new HashMap();
		    HashMap map = blackboard;
		    StringBuilder buffer = new StringBuilder();
		    String key;
		    int blackboardKey = 0;
		    bool inProperties = false;
		    for ( int i = 1; line.length > i; i++ )
			{
			    switch (line.getChar(i))
				{
				case " ":
				    //Blank spaces have no meaning in the log, thus they are ignored
				case '"':
				    //End or start of quote are extraneous, since other symbols declare the start and end of strings
				case ":":
				    //The column name for the data is in the buffer. Save it to key.
				    key = buffer.toString();
				    //Clear the buffer to make space for the data.
				    buffer = new StringBuilder();
				case ",":
				    //Denominates the end of a key-value pair. Save the data into map.
				    map.put(key, buffer.toString());
				    //And clear the buffer
				    buffer = new StringBuilder();
				case "{":
				    //This is a underlying data pair. These are either split and saved with the rest or put into a separate map
				    if (key == "location" )
					{
					    //These are longitude and latitude or 
					    //Pretend everything is normal, the key will be overwritten and the sub data entered with the rest
					}
				    else if ( inProperties )
					{
					    //These are properties. They are to be put into the properties map, which must be cleared.
					    property = new HashMap();
					    map = property;
					    //As above the key will be overwritten and data entered normally
					}
				    else
					{
					    //This should never happen!
					    error();
					}
				case "}":
				    //If this is in properties, commit the map to the database
				    //Else, ignore it
				    if ( inProperties )
					{
					    //Initiate query
					    PreparedStatement query = conn.prepareStatement("INSERT INTO properties VALUES( ?, ?)");
					    for( Map.Entry tmp : map.entrySet() )
						{
						    if (tmp.getKey() == "type")
							{
							    //enter as type in database
							    query.setString(1, tmp.getValue());
							}
						    else
							{
							    //enter as value in database
							    query.setString(2, tmp.getValue());
							}
						}
					    //execute query
					    query.executeQuery();
					}
				case "[":
				    //Start of the underlying properties, just make sure.
				    if (key == "blackboardProperties" )
					{
					    //blackboardProperties MUST be the last entry. Therefore, save the object and get the int key
					    
					    //Initiate query
					    PreparedStatement query = conn.prepareStatement("INSERT INTO blackboards VALUES( ?, ?, ?, ?, ?, ?, ?)");
					    //Set the key
					    query.setString(1, blackboardKey++);
					    //Insert all the data
					    for( Map.Entry tmp : map.entrySet() )
						{
						    switch (tmp.getKey)
							{
							case "itemId":
							    query.setInteger(2, Integer.parseInt(tmp.getValue()));
							case "stationId":
							    query.setInteger(3, Integer.parseInt(tmp.getValue()));
							case "appId":
							    query.setInteger(4, Integer.parseInt(tmp.getValue()));
							case "typeId":
							    query.setString(5, tmp.getValue());
							case "latitude":
							    query.setInteger(6, Integer.parseInt(tmp.getValue()));
							case "longitude":
							    query.setInteger(7, Integer.parseInt(tmp.getValue()));
							case "locationConfidence":
							    query.setInteger(8, Integer.parseInt(tmp.getValue()));
							case "validityDuration":
							    query.setInteger(2, Integer.parseInt(tmp.getValue()));
							case "validityArea":
							    query.setInteger(2, tmp.getValue());
							}
						}
					    //execute query
					    query.executeQuery();//query stuff
					    //Set the inProperties flag and roll along. The first data pair will handle setting the map correctly.
					    inProperties = true;
					}
				    else
					{
					    //This should never happen!
					    error();
					}
				case "]":
				    //It should all be done by now, so ignore
				}
			}
	    }
	catch(Exception e)
	    {
		//System.out.println("Error occured when reading file.");
	    }
    }
}
