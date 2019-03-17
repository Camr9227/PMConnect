package api;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;

import java.sql.*;

@Path("/subscribe")
public class Subscribe 
{	
	JSONObject userdata = new JSONObject();
	
	JSONParser parser = new JSONParser();
	
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://localhost:3306/PMConnect";

	//  Database credentials
	static final String USER = "root";
	static final String PASS = "Cammo9227";
	
	Connection conn = null;
	Statement stmt = null;
	
	private ArrayList<String> addToDatabase(String msisdn, String subscription)
	{
		ArrayList<String> subscriptions = new ArrayList<String>();
		subscriptions.add(subscription);
		
		try{
			//STEP 2: Register JDBC driver
		    Class.forName(JDBC_DRIVER).newInstance();

		    //STEP 3: Open a connection
		    System.out.println("Connecting to database...");
		    conn = DriverManager.getConnection(DB_URL,USER,PASS);

		    //STEP 4: Execute a query
		    System.out.println("Creating statement...");
		    stmt = conn.createStatement();
		    String sql;
		    sql = "SELECT msisdn,subscriptions FROM PMConnect.Subscriptions WHERE msisdn = \'" + msisdn + "\'";
		    ResultSet rs = stmt.executeQuery(sql);

		    JSONObject db_subscriptions = new JSONObject();
		    
		    //STEP 5: Extract data from result set
		    if(rs.next())
		    {
		    
		    	//Retrieve by column name
		        String db_msisdn  = rs.getString("msisdn");
		        db_subscriptions = (JSONObject) parser.parse(rs.getString("subscriptions"));
		        System.out.println(db_subscriptions.toString());

		        ArrayList<String> joSubscriptions = (ArrayList<String>) db_subscriptions.get("subscription");
		        System.out.println(joSubscriptions);
		        subscriptions.addAll(joSubscriptions);
		        
		       	db_subscriptions.put("subscription", subscriptions);
		       	
		       	
		       	sql = "UPDATE PMConnect.Subscriptions SET subscriptions = \'" +db_subscriptions.toString()+ "\' WHERE msisdn = \'" +msisdn+ "\';";
		        stmt.executeUpdate(sql);
		     
		    }
		    else
		    {
		    	System.out.println("yee");
	        	db_subscriptions.put("subscription", subscriptions);
	        	sql = "INSERT INTO PMConnect.Subscriptions VALUES (\'"+msisdn+"\',\'"+db_subscriptions.toString()+"\');";
	        	stmt.executeUpdate(sql);
		    }
		    //STEP 6: Clean-up environment
		    rs.close();
		    stmt.close();
		    conn.close();
		}catch(SQLException se){
			//Handle errors for JDBC
		    se.printStackTrace();
		}catch(Exception e){
			//Handle errors for Class.forName
		    e.printStackTrace();
		}finally{
		    //finally block used to close resources
		    try{
		    	if(stmt!=null)
		            stmt.close();
		    }catch(SQLException se2){
		      }// nothing we can do
		    try{
		    	if(conn!=null)
		            conn.close();
		    }catch(SQLException se){
		    	se.printStackTrace();
		    }//end finally try
		}//end try
		return subscriptions;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/subscription")  
    public Response addUser(@FormParam("msisdn") String msisdn, 
    		@FormParam("subscription") String subscription) 
	{
		boolean valid = false;
		ArrayList<String> subscriptions = new ArrayList<String>();
		subscriptions.add(subscription);
		
		if(msisdn.length() == 13)
		{
			msisdn = "0" + msisdn.substring(3);
		}
		
		if(msisdn.length() == 11 && msisdn.charAt(0) == '0')
		{
			valid = true;
		}
		else
		{
			valid = false;
		}
		
		if(valid)
		{
			System.out.println("about to add to db");
			subscriptions = addToDatabase(msisdn, subscription);
			userdata.put("msisdn", msisdn);
			userdata.put("subscription", subscriptions);
			System.out.println("valid");
			return Response.status(200)  
		            .entity(userdata.toString())  
		            .build();
		}
		else
		{
			System.out.println("invalid");
			return Response.status(200)  
		            .entity("Invalid number, you have not been subscribed")  
		            .build();  
		}
    }  
}
