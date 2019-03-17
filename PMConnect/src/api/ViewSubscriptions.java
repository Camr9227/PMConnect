package api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@Path("/subscriptions")
public class ViewSubscriptions 
{
	JSONObject userdata = new JSONObject();
	JSONParser parser = new JSONParser();
	
	static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://localhost:3306/PMConnect";

	//  Database credentials
	static final String USER = "root";
	static final String PASS = "Cammo9227";  // This is a secret, shhhhh!
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/viewsubscriptions")
	public Response getSubscriptions(@FormParam("msisdn") String msisdn)
	{
		boolean valid = false;
		
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
			Connection conn = null;
			Statement stmt = null;
	
			ArrayList<String> subscriptions = new ArrayList<String>();
	
			try{
				Class.forName(JDBC_DRIVER).newInstance();
	
				System.out.println("Connecting to database...");
				conn = DriverManager.getConnection(DB_URL,USER,PASS);
	
				System.out.println("Creating statement...");
				stmt = conn.createStatement();
				String sql;
				sql = "SELECT msisdn,subscriptions FROM PMConnect.Subscriptions WHERE msisdn = \'" + msisdn + "\'";
				ResultSet rs = stmt.executeQuery(sql);
	
				if(rs.next())
				{
					JSONObject db_subscriptions = new JSONObject();
	
					db_subscriptions = (JSONObject) parser.parse(rs.getString("subscriptions"));
	
					subscriptions = (ArrayList<String>) db_subscriptions.get("subscription");
	
				}
	
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
			
			userdata.put("msisdn", msisdn);
			userdata.put("subscription", subscriptions);
			return Response.status(200)  
		            .entity(userdata.toString())  
		            .build();
		}
		else
		{
			return Response.status(200)  
		            .entity("Invalid number, we cannot fetch your subscriptions")  
		            .build();  
		}
	}
}
