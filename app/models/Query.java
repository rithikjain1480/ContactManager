package models;

import java.sql.*;
import java.util.ArrayList;

public class Query {
	
	//SQL commands to create necessary tables
	//create table users (ID int auto_increment, FirstName varchar(255), LastName varchar(255), Email varchar(255), Pass varchar(255), Hours int, primary key(ID), unique(Email));
	//create table contacts (ID int auto_increment, userID int, FirstName varchar(255), LastName varchar(255), Email varchar(255), Phone varchar(255), Birthday date, primary key(ID));
	
	//Validate that the user is in the users table
	public static boolean checkUser(String email,String pass) {
		boolean isValid = false;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/contactManagerDB","root","root");
			PreparedStatement ps = c.prepareStatement("select * from users where email=? and pass=?");
			ps.setString(1, email);
			ps.setString(2, pass);
			ResultSet rs = ps.executeQuery();
			isValid = rs.next();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return isValid;
	}
	
	//Insert new user into the users table
	public static void insertUser(String first, String last, String email, String pass) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/contactManagerDB","root","root");
			PreparedStatement ps = c.prepareStatement("insert into users (FirstName, LastName, Email, Pass, Hours) values (?, ?, ?, ?, ?)");
			
			ps.setString(1, first);
			ps.setString(2, last);
			ps.setString(3, email);
			ps.setString(4, pass);
			ps.setString(5, "12"); //default number of hours before birthday
			
			ps.execute();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//Add a contact into the contacts table and return a string of the contact's birthday
	public static String addContact(String uEmail, String first, String last, String email, String phone, String bday) {
		int cID = 0;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/contactManagerDB","root","root");
			
			PreparedStatement ps = c.prepareStatement("insert into contacts (userID, FirstName, LastName, Email, Phone, Birthday) values (?, ?, ?, ?, ?, ?)");
			
			ps.setString(1, Integer.toString(getID(uEmail)));
			ps.setString(2, first);
			ps.setString(3, last);
			ps.setString(4, email);
			ps.setString(5, phone);
			ps.setString(6, bday);
			
			ps.execute();
			
			ps = c.prepareStatement("select max(ID) from contacts");
			ResultSet rs = ps.executeQuery();
			rs.next();
			cID = rs.getInt(1);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		//System.out.println(bday);
		return bday + "-" + getID(uEmail) + "-" + cID;
	}
	
	//Returns 2D array of all the contacts of a particular user to present in a table
	public static String[][] getResultSet(String email) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/contactManagerDB","root","root");
			PreparedStatement ps = c.prepareStatement("select * from contacts where userID=?");
			ps.setString(1, Integer.toString(getID(email)));
			
			ResultSet rs = ps.executeQuery();
			
			ArrayList<String[]> results = new ArrayList<>();
			String[] temp = new String[5];
			temp[0] = "First Name";
			temp[1] = "Last Name";
			temp[2] = "Email";
			temp[3] = "Phone Number";
			temp[4] = "Birthday";
			results.add(temp);
			
			while(rs.next()) {
				temp = new String[5];
				for(int i=3; i<8; i++) {
					temp[i-3] = rs.getString(i);
				}
				results.add(temp);
			}
			
			String[][] arr = new String[results.size()][results.get(0).length];
			return results.toArray(arr);
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//Changes the number of hours before a birthday the user is sent a reminder email
	public static void updateHours(String email, int hours) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/contactManagerDB","root","root");
			PreparedStatement ps = c.prepareStatement("update users set Hours=? where Email=?");
			
			ps.setString(1, Integer.toString(hours));
			ps.setString(2, email);
			
			ps.execute();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//Helper method to access the user's ID using the user's email
	private static int getID(String email) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/contactManagerDB","root","root");
			PreparedStatement ps = c.prepareStatement("select * from users where email=?");
			ps.setString(1, email);
			ResultSet rs = ps.executeQuery();
			rs.next();
			return rs.getInt(1);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
}