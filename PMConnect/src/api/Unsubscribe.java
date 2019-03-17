package api;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@Path("/unsubscribe")
public class Unsubscribe {
	
	JSONObject userdata = new JSONObject();
	JSONParser parser = new JSONParser();
	
	static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://localhost:3306/PMConnect";

	//  Database credentials
	static final String USER = "root";
	static final String PASS = "Cammo9227";
	
	private ArrayList<String> getSubscriptions(String msisdn)
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

	        	ArrayList<String> joSubscriptions = (ArrayList<String>) db_subscriptions.get("subscription");

	        	subscriptions.addAll(joSubscriptions);
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
		return subscriptions;
	}
	
	private String buildForm(ArrayList<String> subscriptions, String msisdn)
	{
		String code = "<!DOCTYPE html>\n" + 
					  "<html>\n" + 
				      "<head>\n" + 
				      "<meta charset=\"UTF-8\">\n" + 
				      "<title>PMConnect</title>\n" + 
				      "</head>\n" + 
				      "<body>\n" +
				      "<h4>Check the boxes you wish to stay subscribed to</h4>\n" +
				      "<form action=\"http://localhost:8080/PMConnect/rest/unsubscribe/confirmation/" + msisdn + "\" method=\"POST\">\n";
		
		for(int i = 0; i < subscriptions.size(); i++)
		{
			code += subscriptions.get(i) + "<input type=\"checkbox\" name=\"" + subscriptions.get(i) + "\"/><br></br>\n";
		}
		
		code += "<input type=\"submit\" value=\"Submit\"/>\n" +
				"</form>\n" +
				"</body>\n" +
				"</html>";
		return code;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/unsubscribed")
	public Response getUser(@FormParam("msisdn") String msisdn) throws URISyntaxException
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
			URI uri = new URI("http://localhost:8080/PMConnect/rest/unsubscribe/unsubscriptions/" + msisdn);
			return Response.temporaryRedirect(uri).build();
		}
		else
		{
			return Response.status(200)  
		            .entity("Invalid number, we cannot unsubscribe you")  
		            .build();  
		}
	}
	
	@POST
	@Path("/unsubscriptions/{msisdn}")
	@Produces({MediaType.TEXT_HTML})
	public String getUserUnsubscriptions(@PathParam("msisdn") String msisdn)
	{
		ArrayList<String> subscriptions = getSubscriptions(msisdn);
		String htmlCode = buildForm(subscriptions, msisdn);
		return htmlCode;
	}
	
	@POST
    @Path("/confirmation/{msisdn}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response returnData(@PathParam("msisdn") String msisdn, MultivaluedMap<String, String> params) {
		
		Connection conn = null;
		Statement stmt = null;
		
		ArrayList<String> subscriptions = new ArrayList<String>();
		
		for(String param : params.keySet()) 
		{	
			subscriptions.add(param);	
        }
		
		try{
			//STEP 2: Register JDBC driver
		    Class.forName(JDBC_DRIVER).newInstance();

		    //STEP 3: Open a connection
		    System.out.println("Connecting to database...");
		    conn = DriverManager.getConnection(DB_URL,USER,PASS);
		    
		    stmt = conn.createStatement();
		    
		    String sql;

		    JSONObject db_subscriptions = new JSONObject();
		        
		    db_subscriptions.put("subscription", subscriptions);  	
		      
		    System.out.println(db_subscriptions.toString());
		    
		    sql = "UPDATE PMConnect.Subscriptions SET subscriptions = \'" +db_subscriptions.toString()+ "\' WHERE msisdn = \'" +msisdn+ "\';";
	        stmt.executeUpdate(sql);
		    
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

}
