package background;




import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class DBHelper {
	Connection conn = null; 
	ResultSet rs = null; 
	Statement stmt = null; 
	private String sql = null;
	
	public DBHelper(String dbName){
		try {
			conn = getConnection(dbName);
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
				    ResultSet.CONCUR_UPDATABLE);
			System.out.println("Connect DB is ok!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Connect DB failed!");
			e.printStackTrace();
		}
	}
	
	public Connection getConnection(String dbName) throws SQLException,IOException,Exception { 		
	    Class.forName("org.postgresql.Driver").newInstance();
	    String url="jdbc:postgresql://localhost:5432/"+dbName;  //harddisk数据库名字
	    String user="postgres";//用户名
	    String password="123456";//密码
	    Connection conn=DriverManager.getConnection(url,user,password);
	    return conn;	    
	}
	
	public ResultSet executeQuery(String sqlwords){ 
	   this.sql=sqlwords; 
	   try{ 
	     rs=stmt.executeQuery(sqlwords); 
	   } 
	   catch(SQLException ex){ 
	     System.out.println("Execute Query Sql Failed!:" + ex.getMessage()); 
	   } 
	   return rs; 
	} 
	
	public boolean executeUpdate(String sqlwords){ 
	   this.sql=sqlwords; 
	   try{ 
	     stmt.executeUpdate(sqlwords); 
	     return true; 
	   } 
	   catch(SQLException ex){ 
	     System.err.println("Execute Update Sql Failed!: " + ex.getMessage()); 
	     return false; 
	   } 
	} 
		 
	public boolean execute(String sqlwords) {
		
		this.sql = sqlwords;
		try {
			stmt.execute(sqlwords);
			return true;
		}
		catch(SQLException ex) {
			System.err.println("Execute SQL Failed." + ex.getMessage());
			return false;
		}
	}
		  
	public boolean close(){ 
		try{ 
			if(rs != null){ rs.close(); } 
			if(stmt != null){ stmt.close(); } 
			if(conn != null){ conn.close(); } 
			return true; 
		} 
		catch(Exception e) { 
			System.out.print("Clost Database Connect Failed!:"+e); 
			return false; 
		} 
	} 
	
	public static void main(String[] args){
		DBHelper dbHelper = new DBHelper("postgres");
		ResultSet rs = null;
		
		//dbHelper.execute("drop table data");
		//dbHelper.execute("create table data ( uid int, name char(256), pwd char(256) )");
		//dbHelper.executeUpdate("update data set id='1' where id='9'");
		//dbHelper.executeDelete("delete from data where id='1'");
		
		dbHelper.executeUpdate("delete from data");
		dbHelper.executeUpdate("insert into data (uid, name, pwd) values ('1', 'daiyou', 'shit');");
		rs = dbHelper.executeQuery("select * from data");
		
		
		try {
			while(rs.next()){
				System.out.println(rs.getInt(1));
				System.out.println(rs.getString(2));
				System.out.println(rs.getString(3));
			}
			dbHelper.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
}
