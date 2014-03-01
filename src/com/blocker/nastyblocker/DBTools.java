package com.blocker.nastyblocker;

//DBTools.java



import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//SQLiteOpenHelper helps you open or create a database

public class DBTools  extends SQLiteOpenHelper {

	// Context : provides access to application-specific resources and classes
	
	public DBTools(Context applicationcontext) {
		
		// Call use the database or to create it
		
     super(applicationcontext, "contactbook.db", null, 1);
     
 }
	
	// onCreate is called the first time the database is created
	
	public void onCreate(SQLiteDatabase database) {
		/* DATABASE_CREATE = "create table "
      + TABLE_COMMENTS + "(" + COLUMN_ID
      + " integer primary key autoincrement, " + COLUMN_COMMENT
      + " text not null);";*/
		// How to create a table in SQLite
		// Make sure you don't put a ; at the end of the query
		
		
		String query = "CREATE TABLE contacts ( contactNumber uniqueTEXT NOT NULL PRIMARY KEY, name TEXT)";
		
		// Executes the query provided as long as the query isn't a select
		// or if the query doesn't return any data
		
     try{database.execSQL(query);
     }
     catch(Exception e){
    	 e.printStackTrace();
    	 Log.i("nothing is being created", "database creation failed");
     }
     finally{
    	 
     }

	}

	// onUpgrade is used to drop tables, add tables, or do anything 
	// else it needs to upgrade
	// This is droping the table to delete the data and then calling
	// onCreate to make an empty table
	
	public void onUpgrade(SQLiteDatabase database, int version_old, int current_version) {
		String query = "DROP TABLE IF EXISTS contacts";
		
		// Executes the query provided as long as the query isn't a select
		// or if the query doesn't return any data
		
		database.execSQL(query);
     onCreate(database);
	}
	
	public void insertContact(String number,String name) throws Exception {
		
		// Open a database for reading and writing
		
		SQLiteDatabase database = this.getWritableDatabase();
		
		// Stores key value pairs being the column name and the data
		// ContentValues data type is needed because the database
		// requires its data type to be passed
		
		ContentValues values = new ContentValues();
		
		values.put("contactNumber",number);
		values.put("name", name);
		
		// Inserts the data in the form of ContentValues into the
		// table name provided
		//if number already exist throw an Exception
		/*if(this.getName(number)!=null)
		{
			throw new Exception("contactNotFound");
		}*/
		try{
			database.insertOrThrow("contacts", null, values);
		}
		catch(Exception e)
		{
			throw new Exception("contact already exists");
		}
		
		// Release the reference to the SQLiteDatabase object
		
		database.close();
	}
	public String getName(String number){
		String columns[]={"contactNumber","name"};
		SQLiteDatabase database = this.getReadableDatabase();
		Cursor c= database.query("contacts", columns, "contactNumber="+number, null, null, null, null);
		if(c!=null){
			c.moveToFirst();
			return c.getString(1);
		}
		return null;
	}
	public int updateContact(String number,String name) {
		
		// Open a database for reading and writing
		
		SQLiteDatabase database = this.getWritableDatabase();	
		
		// Stores key value pairs being the column name and the data
		
	    ContentValues values = new ContentValues();
	    
	    values.put("contactNumber",number);
		values.put("name", name);
	    
		// update(TableName, ContentValueForTable, WhereClause, ArgumentForWhereClause)
		
	    return database.update("contacts", values, "contactNumber="+number, null);
	}
	
	// Used to delete a contact with the matching contactId
	
	public void deleteContact(String contactNumber) {

		// Open a database for reading and writing
		
		SQLiteDatabase database = this.getWritableDatabase();
		
		String deleteQuery = "DELETE FROM  contacts where contactNumber='"+contactNumber +"'";
		
		// Executes the query provided as long as the query isn't a select
		// or if the query doesn't return any data
		
		database.execSQL(deleteQuery);
	}
	
	public ArrayList<String> getAllNumbers() {
		
		// ArrayList that contains every row in the database
		// and each row key / value stored in a HashMap
		
		ArrayList<String> contactArrayList= new ArrayList<String>();
		
		String selectQuery = "SELECT  contactNumber FROM contacts";
		
		// Open a database for reading and writing
		
	    SQLiteDatabase database = this.getWritableDatabase();
	    
	    // Cursor provides read and write access for the 
	    // data returned from a database query
	    
	    // rawQuery executes the query and returns the result as a Cursor
	    
	    Cursor cursor = database.rawQuery(selectQuery, null);	
	    
	    // Move to the first row
	   
	    if (cursor.moveToFirst()) {
	    	 while(!cursor.isAfterLast()){
	        	
	        	contactArrayList.add(cursor.getString(0));
	        	cursor.moveToNext();
	        }
	    }
	    // return contact list
	    return contactArrayList;
	}	
}
