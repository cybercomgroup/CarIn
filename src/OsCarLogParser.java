import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;


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
			return;
		    }
		
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
    }

    private static void parse(BufferedReader logfile)
    {
	//Do the database thing

	//Read the file line by line
	try
	    {
		for (String line = logfile.readLine(); line != null; line = logfile.readLine()) {
		    System.out.println(line);
		}
	    }
	catch(Exception e)
	    {
		//System.out.println("Error occured when reading file.");
	    }
    }
}
