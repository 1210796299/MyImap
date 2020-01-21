package com.hh.imap.mailbox.mail.service.bean;

import com.hh.frame.row.base.BaseRow;
import com.hh.frame.row.base.hh.HHdbType;

/**
 * 邮箱账号
 * @author oyx
 */
public class Mail_Addr extends BaseRow {

	public static final String ID = "id";
	/**
	 * 账号
	 */
	public static final String NAME = "name";

	/**
	 * 关联usr账号id
	 */
	public static final String USR_ID = "usr_id";

	/**
	 * 是否为默认账号
	 */
	public static final String IS_DEFAULT = "is_def";

	public static final String USR_NAME = "usr_name";
	public static final String USR_PASS = "usr_pass";

	public int getId() {
		return (int) super.get(ID);
	}

	public void setId(int id) {
		super.set(HHdbType.INT4, ID, id);
	}

	public String getName() {
		return (String) super.get(NAME);
	}

	public void setName(String name) {
		super.set(HHdbType.TEXT, NAME, name);
	}


	public int getUsrId() {
		return (int) super.get(USR_ID);
	}

	public void setUsrId(int usrId) {
		super.set(HHdbType.INT4, USR_ID, usrId);
	}

	public boolean getDefault() {
		return (boolean) super.get(IS_DEFAULT);
	}

	public void setDefault(boolean isDefault) {
		super.set(HHdbType.BOOL, IS_DEFAULT, isDefault);
	}

	public String getUsrName() {
		return (String) super.get(USR_NAME);
	}

	public void setUsrName(String name) {
		super.set(HHdbType.TEXT, USR_NAME, name);
	}


	public String getUsrPass() {
		return (String) super.get(USR_PASS);
	}

	public void setUsrPass(String pass) {
		super.set(HHdbType.TEXT, USR_PASS, pass);
	}
}
