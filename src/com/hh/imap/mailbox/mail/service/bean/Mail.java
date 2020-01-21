package com.hh.imap.mailbox.mail.service.bean;

import com.hh.frame.row.base.BaseRow;
import com.hh.frame.row.base.hh.HHdbType;

import java.sql.Timestamp;
import java.util.Date;

/**
 * 邮件信息
 */
public class Mail extends BaseRow {

	public static final String ID = "id";
	public static final String ADDR_ID = "addr_id";
	public static final String SIZE = "size";
	public static final String SUBJECT = "subject";
	public static final String FROM_ADDR = "from_addr";
	public static final String TO_ADDR = "to_addr";
	public static final String SEEN = "seen";
	public static final String HAS_ATTACH = "has_attach";
	public static final String MAIL_DATE = "mail_date";
	public static final String SAVE_DATE = "save_date";
	public static final String EML_FILE_ID = "eml_file_id";
	public static final String LOG_FILE_ID = "log_file_id";

	public int getId() {
		return (int) super.get(ID);
	}

	public void setId(int id) {
		super.set(HHdbType.INT4, ID, id);
	}

	public int getAddrId() {
		Object o = super.get(ADDR_ID);
		if (o == null) {
			return -1;
		}
		return (int) o;
	}

	public void setAddrId(int addr_id) {
		super.set(HHdbType.INT4, ADDR_ID, addr_id);
	}

	public int getSize() {
		Object o = super.get(SIZE);
		if (o == null) {
			return -1;
		}
		return (int) o;
	}

	public void setSize(int size) {
		super.set(HHdbType.INT4, SIZE, size);
	}

	public String getSubject() {
		return (String) super.get(SUBJECT);
	}

	public void setSubject(String subject) {
		super.set(HHdbType.TEXT, SUBJECT, subject);
	}

	public String getFromAddr() {
		return (String) super.get(FROM_ADDR);
	}

	public void setFromAddr(String from_addr) {
		super.set(HHdbType.TEXT, FROM_ADDR, from_addr);
	}

	public String getToAddr() {
		return (String) super.get(TO_ADDR);
	}

	public void setToAddr(String to_addr) {
		super.set(HHdbType.TEXT, TO_ADDR, to_addr);
	}

	public boolean getSeen() {
		return (boolean) super.get(SEEN);
	}

	public void setSeen(boolean seen) {
		super.set(HHdbType.BOOL, SEEN, seen);
	}

	public boolean getHasAttach() {
		return (boolean) super.get(HAS_ATTACH);
	}

	public void setHasAttach(boolean has_attach) {
		super.set(HHdbType.BOOL, HAS_ATTACH, has_attach);
	}

	public Timestamp getMailDate() {
		return (Timestamp) super.get(MAIL_DATE);
	}

	public void setMailDate(Timestamp mail_date) {
		super.set(HHdbType.TIMESTAMP, MAIL_DATE, mail_date);
	}

	public Timestamp getSaveDate() {
		return (Timestamp) super.get(SAVE_DATE);
	}

	public void setSaveDate(Timestamp save_date) {
		super.set(HHdbType.TIMESTAMP, SAVE_DATE, save_date);
	}

	public int getEmlFileId() {
		Object o = super.get(EML_FILE_ID);
		if (o == null) {
			return -1;
		}
		return (int) o;
	}

	public void setEmlFileId(int eml_file_id) {
		super.set(HHdbType.INT4, EML_FILE_ID, eml_file_id);
	}

	public int getLogFileId() {
		Object o = super.get(LOG_FILE_ID);
		if (o == null) {
			return -1;
		}
		return (int) o;
	}

	public void setLogFileId(int log_file_id) {
		super.set(HHdbType.INT4, LOG_FILE_ID, log_file_id);
	}

	public String getHeader() {
		StringBuffer sb = new StringBuffer();
		sb.append("subject:").append(getSubject()).append("\n");
		sb.append("date:").append(new Date(getSaveDate().getTime())).append("\n");
		sb.append("from:").append(getFromAddr()).append("\n");
		sb.append("to:").append(getToAddr()).append("\n");
		return sb.toString();
	}
}
