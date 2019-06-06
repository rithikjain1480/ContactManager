package controllers;

import play.*;
import play.mvc.*;
import play.libs.*;

import java.util.*;
import java.util.Date;

import org.apache.commons.mail.*;

import java.sql.*;

import models.*;

public class Application extends Controller implements Runnable {
	
	private static ArrayList<String> bdays = new ArrayList<>(); 
	private static StringTokenizer st;
	private static Calendar cal;
	
	//Gives user the option to log in or sign up
    public static void index() {
        render();
    }
    
    //Generates a sign up page for new users
    public static void signUp() {
    	render();
    }
    
    //Generates a login page for existing users
    public static void loginPage() {
    	render();
    }
    
    //Checks if user's credentials are valid
    //Logs user into account if credentials are valid and returns user to login page if not
    public static void login(String email, String pass) {
    	if(models.Query.checkUser(email, pass)) {
    		String[][] results = models.Query.getResultSet(email);
    		int size = results.length-1;
    		if(results.length==1) {
    			results = null;
    		}
    		
    		render(email, pass, results, size);		
    	}
    	else {
    		loginPage();
    	}
    }
    
    //Creates a new user using the Query class's insertUser(...) method
    public static void createUser(String first, String last, String email, String pass) {
    	models.Query.insertUser(first, last, email, pass);
    	login(email, pass);
    }
    
    //Uses the Query class's addContact(...) method to add a new contact to the contacts table.
    //Adds the contact's birthday to an ArrayList of birthdays
    public static void addContact(String first, String last, String email, String phone, String bday, String uEmail, String uPass) {
		bdays.add(models.Query.addContact(uEmail, first, last, email, phone, bday));
		System.out.println(bdays.size());
		
		//Create new thread to send emails when at least 1 contact has been created by a user
		if(bdays.size()==1) {
			Thread thread = new Thread(new Application());
    		thread.start();
    		System.out.println(bdays.size());
		}
		add(uEmail, uPass);
    }
    
    public static void add(String email, String pass) {
    	render(email, pass);
    }
    
    public static void changeHours(int hours, String email, String pass) {
    	models.Query.updateHours(email, hours);
    	login(email, pass);
    }
    
    //Infinite while loop to continuously check if an email should be sent
    @Override
    public void run() {
    	try {
    		while(true) {
        		int uID, cID, numHours = 0;
            	String to = "", name = "";
            	for(String bday:bdays) {
            		st = new StringTokenizer(bday, "-");
            		cal = Calendar.getInstance();
            		cal.set(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken())-1, Integer.parseInt(st.nextToken()), 0, 34);
            		uID = Integer.parseInt(st.nextToken());
            		//System.out.println("user's ID" + uID);
            		cID = Integer.parseInt(st.nextToken());
            		//System.out.println("contact's ID" + cID);
            		
            		try {
            			Class.forName("com.mysql.jdbc.Driver");
            			Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/contactManagerDB","root","root");
            			PreparedStatement ps = c.prepareStatement("select Email from users where ID=?");
            			ps.setString(1, Integer.toString(uID));
            			
            			ResultSet rs = ps.executeQuery();
            			rs.next();
            			to = rs.getString(1);
            			
            			ps = c.prepareStatement("select Hours from users where ID=?");
            			ps.setString(1, Integer.toString(uID));
            			
            			rs = ps.executeQuery();
            			rs.next();
            			numHours = rs.getInt(1);
            			
            			ps = c.prepareStatement("select FirstName from contacts where ID=?");
            			ps.setString(1, Integer.toString(cID));
            			
            			rs = ps.executeQuery();
            			rs.next();
            			name = rs.getString(1);
            			
            			ps = c.prepareStatement("select LastName from contacts where ID=?");
            			ps.setString(1, Integer.toString(cID));
            			
            			rs = ps.executeQuery();
            			rs.next();
            			name += " " + rs.getString(1);
            			System.out.println(name);
            		}
            		catch(Exception e) {
            			e.printStackTrace();
            		}
            		
            		cal.add(Calendar.HOUR_OF_DAY, numHours*-1);
            		System.out.println(cal.get(Calendar.HOUR_OF_DAY));
            		if(shouldEmail(cal)) {
            			try {
            				//Send email to user
            				Email email = new SimpleEmail();
            				email.setHostName("smtp.gmail.com");
            				email.setSmtpPort(587);
            				//Replace username and password with a Gmail account's username and password
            				email.setAuthenticator(new DefaultAuthenticator("username", "password"));
            				email.setSSL(true);
            				//replace sender@gmail.com with the email address associated with the username and password 
            				email.setFrom("sender@gmail.com");
            				email.setSubject("Upcoming Birthday!");
            				email.setMsg(name + "'s birthday is in " + numHours + " hours. Get a present!");
            				email.addTo(to);
            				email.send();
            			}
            			catch(Exception e) {
            				e.printStackTrace();
            			}
            		}
            	}
            	
            	try {
            		//checks every minute --> for greater efficiency but lower accuracy, can change to every hour and not check minute value in shouldEmail(...)
        			Thread.sleep(60000);
        		} catch (InterruptedException e) {
        			e.printStackTrace();
        		}    		
        	}
    	}
    	//Allows infinite loop to compile and run
    	catch(VerifyError e) {}
    	
    }
    
    //Determine if an email should be sent (certain number of hours before a birthday)
    private boolean shouldEmail(Calendar cal) {
    	Calendar now = Calendar.getInstance();
    	now.setTime(new Date());
    	System.out.println(now.getTime());
    	System.out.println(cal.getTime());
    	return now.get(Calendar.MONTH)==cal.get(Calendar.MONTH) && now.get(Calendar.DATE)==cal.get(Calendar.DATE) && now.get(Calendar.HOUR_OF_DAY)==cal.get(Calendar.HOUR_OF_DAY) && now.get(Calendar.MINUTE)==cal.get(Calendar.MINUTE);
    }

}