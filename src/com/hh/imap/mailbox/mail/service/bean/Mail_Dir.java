package com.hh.imap.mailbox.mail.service.bean;

import com.hh.frame.row.base.BaseRow;
import com.hh.frame.row.base.hh.HHdbType;
import com.hh.imap.mailbox.HHId;
import com.hh.imap.mailbox.mail.HHMailBoxUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.james.core.Username;
import org.apache.james.mailbox.model.Mailbox;
import org.apache.james.mailbox.model.MailboxConstants;
import org.apache.james.mailbox.model.MailboxPath;

/**
 * 邮箱文件夹
 *
 * @author oyx
 */
public class Mail_Dir extends BaseRow {
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String TAB = "tab";
	public static final String MAIL_ADDR_ID = "mail_addr_id";
	public static final String P_ID = "p_id";
	public static final String UID_VALIDITY = "uid_validity";
	public static final String ADDR_NAME = "addr_name";
	public String formatName;

	public int getId() {
		return (int) super.get(ID);
	}

	public void setId(int id) {
		super.set(HHdbType.INT4, ID, id);
	}

	public String getName() {
		return (String) super.get(NAME);
	}

	public String getFormatName() {
		if (StringUtils.isNotBlank(formatName)) {
			return formatName;
		}
		String boxName = HHMailBoxUtil.formatMailBoxName(getName(), true);
		if (StringUtils.isNotBlank(boxName)) {
			return boxName;
		}
		return getName();
	}

	public void setFormatName(String formatName) {
		this.formatName = formatName;
	}

	public void setName(String name) {
		super.set(HHdbType.TEXT, NAME, name);
	}

	public String getTab() {
		return (String) super.get(TAB);
	}

	public void setTab(String tab) {
		super.set(HHdbType.TEXT, TAB, tab);
	}

	public int getMailAddrId() {
		return (int) super.get(MAIL_ADDR_ID);
	}

	public void setMailAddrId(int mail_addr_id) {
		super.set(HHdbType.INT4, MAIL_ADDR_ID, mail_addr_id);
	}

	public int getPId() {
		return (int) super.get(P_ID);
	}

	public void setPId(int p_id) {
		super.set(HHdbType.INT4, P_ID, p_id);
	}

	public long getUidValIdIty() {
		return (long) super.get(UID_VALIDITY);
	}

	public void setUidValidity(long uidValidity) {
		super.set(HHdbType.NUMBER, UID_VALIDITY, uidValidity);
	}

	public String getAddrName() {
		return (String) super.get(ADDR_NAME);
	}

	public void setAddrName(String addrName) {
		super.set(HHdbType.TEXT, ADDR_NAME, addrName);
	}

	public Mailbox toMailbox() {
		MailboxPath path = new MailboxPath(MailboxConstants.USER_NAMESPACE, Username.of(getAddrName()), getFormatName());
		return new Mailbox(path, getTab().hashCode(), HHId.of(getId()));
	}
}
