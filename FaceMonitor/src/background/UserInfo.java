package background;

public class UserInfo {

	private String strName;
	private String strPassword;
	public UserInfo() {
		strName = null;
		strPassword = null;
	}
	
	public UserInfo(String name,String pass) {
		this.strName = name;
		this.strPassword = pass;
	}
	
	public String getName() {
		return strName;
	}
	public String getPassword() {
		return strPassword;
	}
}
