package com.hh.imap.mailbox.mail.service;

import com.hh.frame.common.util.db.SqlExeUtil;
import com.hh.frame.common.util.db.SqlQueryUtil;
import com.hh.frame.row.hh.HHRowUtil;
import com.hh.imap.mailbox.mail.service.bean.Mail;
import com.hh.wframe.core.bean.DbQuery;
import com.hh.wframe.service.BaseService;
import com.hh.wframe.service.attach.Attach;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

/**
 * @author oyx
 * @date 2019-6-17   15:50:58
 */
public class HHMailService extends BaseService {

	public HHMailService(String tabName) throws Exception {
		super(null, Mail.class);
		if (StringUtils.isBlank(tabName)) {
			throw new Exception("tabName 为空");
		}
		this.tabName = tabName;
	}

	public Mail getOne(int id) throws Exception {
		return super.getOne(id, Mail.class);
	}

	public List<Mail> getMailList(DbQuery q, int addrId, boolean flag) throws Exception {
		q.setSql(getMailListSql(addrId, flag));
		return super.get(q, Mail.class);
	}

	public List<Mail> getMailListById(String ids) throws Exception {
		String sql = "select * from %s where id in (%s)";
		return HHRowUtil.select(getConn(), Mail.class, sql, tabName, ids);
	}


	public int getCount(String sql) throws Exception {
		return SqlQueryUtil.getCountSql(this.getConn(), sql);
	}

	public int getCount() throws Exception {
		return getCount(this.getSql());
	}

	public String getSql() {
		return String.format("select * from %s where 1=1 ", tabName);
	}

	public String getMailListSql(int addrId, boolean flag) {
		StringBuffer sb = new StringBuffer();
		sb.append(getSql());
		if (addrId > -1) {
			sb.append(" and " + Mail.ADDR_ID + "  = '%s' ");
		}
		if (!flag) {
			sb.append(" and " + Mail.SEEN + "  = false ");
		}
		sb.append(" order by mail_date desc ");
		return String.format(sb.toString(), addrId);
	}

	public int setMailFlag(String id, boolean flag) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("update " + tabName + " set seen = " + flag);
		if (StringUtils.isNoneBlank(id)) {
			sb.append(" where id in (" + id + ") ");
		}
		return SqlExeUtil.executeUpdate(getConn(), sb.toString());
	}

	public int setMailFlagAll(boolean flag) throws Exception {
		return setMailFlag(null, flag);
	}

	public int moveMail(String id, String box) throws Exception {
		StringBuffer columns = new StringBuffer();
		Field[] fields = Mail.class.getFields();
		for (Field field : fields) {
			String fieldName = field.getName();
			if (!fieldName.equalsIgnoreCase(Mail.ID) && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
				columns.append(fieldName + ",");
			}
		}
		columns = columns.deleteCharAt(columns.length() - 1);
		if (id.endsWith(",")) {
			id = id.substring(0, id.length() - 1);
		}
		StringBuffer sql = new StringBuffer();
		sql.append("insert into %s");
		sql.append("( %s ) ( select %s from %s where %s in( %s ))");
		return SqlExeUtil.executeUpdate(getConn(), sql.toString(), box, columns.toString(), columns.toString(), tabName, Mail.ID, id);
	}

	public int addAttach(Attach att) throws Exception {
		int id = super.addRetIntKey(att);
		return id;
	}

	public int addMail(Mail mail) throws Exception {
		return super.addRetIntKey(mail);
	}

	public int updateMail(Mail mail) throws Exception {
		return super.update(mail, new String[]{"id"});
	}

	public int addMail(int attId, Mail mail) throws Exception {
		if (attId <= 0) {
			return 0;
		}
		mail.setEmlFileId(attId);
		mail.setLogFileId(attId);
		return super.addRetIntKey(mail);
	}

	public List<Mail> findMessagesInMailbox() throws Exception {
		String sql = getSql();
		sql += " ORDER BY id ASC ";
		return super.get(Mail.class, sql);
	}

	public List<String> findRecentMessageUIDsInMailbox() throws Exception {
		String sql = "select id from %s order by save_date desc";
		return SqlQueryUtil.selectOneColumn(getConn(), sql, tabName);
	}

	public List<Mail> findUnseenMessagesInMailboxOrderByUid() throws Exception {
		return super.get(Mail.class, getMailListSql(-1, false));
	}

	public List<Mail> findMessagesInMailboxBetweenUIDs(int from, int to) throws Exception {
		String sql = "select * from %s where id BETWEEN %d And %d order by id asc";
		return HHRowUtil.select(getConn(), Mail.class, sql, tabName, from, to);
	}

	public List<Mail> findMessagesInMailboxAfterUID(int id) throws Exception {
		String sql = "select * from %s where id>= %d order by id asc";
		return HHRowUtil.select(getConn(), Mail.class, sql, tabName, id);
	}

	public List<Mail> findMessagesInMailboxWithUID(int id) throws Exception {
		String sql = "select * from %s where id = %d order by id asc";
		return HHRowUtil.select(getConn(), Mail.class, sql, tabName, id);
	}


	public int findMaxId() throws Exception {
		String sql = "select max(id) from %s";
		Map<String, Object> map = SqlQueryUtil.selectOne(getConn(), sql, tabName);
		Object max = map.get("max");
		if (max == null) {
			return -1;
		}
		return Integer.parseInt(max.toString());
	}
}
