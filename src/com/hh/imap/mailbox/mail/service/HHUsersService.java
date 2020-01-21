package com.hh.imap.mailbox.mail.service;

import com.hh.frame.common.util.db.SqlExeUtil;
import com.hh.frame.common.util.db.SqlQueryUtil;
import com.hh.frame.row.hh.HHRowUtil;
import com.hh.imap.mailbox.mail.service.bean.Mail_Addr;
import com.hh.wframe.core.SessionUser;
import com.hh.wframe.service.BaseService;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author oyx
 * @date 2019-6-17 15:50:58
 */
public class HHUsersService extends BaseService {
	public HHUsersService() {
		super(null, Mail_Addr.class);
	}

	public HHUsersService(SessionUser sUser) {
		super(sUser, Mail_Addr.class);
	}

	public int addOne(Mail_Addr addr) throws Exception {
		int id = -1;
		if (addr.getDefault()) {
			updateDef(addr.getUsrId());
		}
		id = HHRowUtil.insertRetIntKey(getConn(), addr, tabName);
		return id;
	}

	public Mail_Addr getOne(int id) throws Exception {
		return super.getOne(id, Mail_Addr.class);
	}

	public int count() throws Exception {
		return SqlQueryUtil.getCountSql(getConn(), "select count(*) from %s", tabName);
	}

	public List<Mail_Addr> getAddress(int ustId) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("select * from " + tabName + " where " + Mail_Addr.USR_ID + " =%d  ");
		List<Mail_Addr> mailAddrList = HHRowUtil.select(getConn(), Mail_Addr.class, sb.toString(), ustId);
		return mailAddrList;
	}

	public Mail_Addr getAddress(String name) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("select * from " + tabName + " where " + Mail_Addr.NAME + " ='%s'  ");
		Mail_Addr mailAddr = HHRowUtil.selectOne(getConn(), Mail_Addr.class, sb.toString(), name);
		return mailAddr;
	}

	public Mail_Addr getAddressByName(String name) throws Exception {
		return getAddressInfo(Mail_Addr.NAME, name);
	}

	public Mail_Addr getAddressInfo(String col, String value) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("select m.*,u.name as usr_name,u.pass as usr_pass from %s m,usr u where m.usr_id=u.id and m.%s='%s' ");
		return HHRowUtil.selectOne(getConn(), Mail_Addr.class, sb.toString(), tabName, col, value);
	}

	public List<Mail_Addr> getAll(String usrNo) throws Exception {
		String sql = String.format(" select m.*,u.name as usr_name from %s m,usr u where m.usr_id=u.id ", tabName);
		if (StringUtils.isNoneBlank(usrNo)) {
			sql += String.format(" and u.name='%s'", usrNo);
		}
		return HHRowUtil.select(getConn(), Mail_Addr.class, sql);
	}

	public int updateDef(int usrId) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("update %s set %s = %s where %s =%d and %s =%s");
		return SqlExeUtil.executeUpdate(getConn(), sb.toString(), tabName, Mail_Addr.IS_DEFAULT, false,
				Mail_Addr.USR_ID, usrId, Mail_Addr.IS_DEFAULT, true);
	}

	public int setDef(int id) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("update %s set %s = %s where %s =%d ");
		return SqlExeUtil.executeUpdate(getConn(), sb.toString(), tabName, Mail_Addr.IS_DEFAULT, true, Mail_Addr.ID,
				id);
	}

	@Override
	public int del(int id) throws Exception {
		return super.del(id);
	}

//	@Override
//	public Connection getConn() throws Exception {
//		return ConnUtil.getHHdbConn("192.168.2.206", 1432, "james", "dba", "123456");
//	}
}
