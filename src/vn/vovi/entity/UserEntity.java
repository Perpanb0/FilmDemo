package vn.vovi.entity;

public class UserEntity {
	private static UserEntity user;

	private String userName;
	private String sessionKey;
	private String catelogyId;

	public UserEntity() {
		this.catelogyId = "0";
	}

	public static UserEntity getInstant() {
		if (user == null) {
			user = new UserEntity();
		}
		return user;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}

	public String getCatelogyId() {
		return catelogyId;
	}

	public void setCatelogyId(String catelogyId) {
		this.catelogyId = catelogyId;
	}

}
