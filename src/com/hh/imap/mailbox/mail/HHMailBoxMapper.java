package com.hh.imap.mailbox.mail;

import com.github.steveash.guavate.Guavate;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.hh.frame.common.util.db.SqlExeUtil;
import com.hh.frame.row.base.BaseRow;
import com.hh.frame.row.hh.HHRowUtil;
import com.hh.imap.mailbox.HHId;
import com.hh.imap.mailbox.mail.service.HHUsersService;
import com.hh.imap.mailbox.mail.service.bean.Mail_Addr;
import com.hh.imap.mailbox.mail.service.bean.Mail_Dir;
import com.hh.wframe.core.SessionUser;
import com.hh.wframe.service.BaseService;
import com.hh.wframe.util.PoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.james.core.Username;
import org.apache.james.mailbox.acl.ACLDiff;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.exception.MailboxExistsException;
import org.apache.james.mailbox.exception.MailboxNotFoundException;
import org.apache.james.mailbox.model.Mailbox;
import org.apache.james.mailbox.model.MailboxACL;
import org.apache.james.mailbox.model.MailboxId;
import org.apache.james.mailbox.model.MailboxPath;
import org.apache.james.mailbox.model.search.MailboxQuery;
import org.apache.james.mailbox.store.MailboxExpressionBackwardCompatibility;
import org.apache.james.mailbox.store.mail.MailboxMapper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author oyx
 * @date 2020-01-08 09:50
 */
public class HHMailBoxMapper extends BaseService implements MailboxMapper {
	private static Map<String, String> map = HHMailBoxUtil.boxNameMap;

	public HHMailBoxMapper() {
		super(null, Mail_Dir.class);
	}


	public <T extends BaseRow> HHMailBoxMapper(SessionUser sUser) {
		super(sUser, Mail_Dir.class);
	}

	@Override
	public MailboxId save(Mailbox mailbox) throws MailboxException {

		try {
			if (isPathAlreadyUsedByAnotherMailbox(mailbox)) {
				throw new MailboxExistsException(mailbox.getName());
			}
			Mail_Addr address = new HHUsersService().getAddress(mailbox.getUser().asString());
			Mail_Dir mailDir = new Mail_Dir();
			mailDir.setName(mailbox.getName());
			mailDir.setMailAddrId(address.getId());
			return HHId.of(createMailDir(mailDir));
		} catch (Exception e) {
			throw new MailboxException("Save of mailbox " + mailbox.getName() + " failed", e);
		}
	}

	@Override
	public void delete(Mailbox mailbox) throws MailboxException {

	}

	@Override
	public Mailbox findMailboxByPath(MailboxPath mailboxPath) throws MailboxException, MailboxNotFoundException {

		String user = mailboxPath.getUser().asString();
		String name = getSplitDirName(mailboxPath.getName());
		try {
			String boxName = HHMailBoxUtil.formatMailBoxName(name, false);
			if (StringUtils.isNotBlank(boxName)) {
				name = boxName;
			}
			Mail_Dir mailDir = getOneByUsr(name, user);
			if (mailDir == null) {
				return null;
			}
			String dirName = getUpDirs(mailDir);
			Mailbox mailbox = new Mailbox(mailboxPath, mailDir.getTab().hashCode(), HHId.of(mailDir.getId()));
			mailbox.setName(dirName);
			return mailbox;
		} catch (Exception e) {
			throw new MailboxException("Search of mailbox " + mailboxPath + " failed", e);
		}
	}

	@Override
	public Mailbox findMailboxById(MailboxId id) throws MailboxException, MailboxNotFoundException {
		HHId maiBoxId = (HHId) id;
		try {
			Mail_Dir mailDir = getOne((int) maiBoxId.getRawId());
			String dirName = getUpDirs(mailDir);
			MailboxPath path = new MailboxPath(null, Username.of(mailDir.getAddrName()), dirName);
			return new Mailbox(path, mailDir.hashCode(), maiBoxId);
		} catch (Exception e) {
			throw new MailboxNotFoundException(maiBoxId);
		}
	}

	@Override
	public List<Mailbox> findNonPersonalMailboxes(Username username, MailboxACL.Right right) throws MailboxException {
		return ImmutableList.of();
	}

	@Override
	public List<Mailbox> findMailboxWithPathLike(MailboxQuery.UserBound query) throws MailboxException {
		String pathLike = MailboxExpressionBackwardCompatibility.getPathLike(query);
		try {
			List<Mail_Dir> byLikeName = getByLikeName(getSplitDirName(pathLike), query.getFixedUser().asString());
			List<Mailbox> mailboxes = new ArrayList<>();
			for (Mail_Dir mailDir : byLikeName) {
				String dirName = getUpDirs(mailDir);
				mailDir.setFormatName(dirName);
				mailboxes.add(mailDir.toMailbox());
			}
			return mailboxes;
		} catch (Exception e) {
			throw new MailboxException("Search of mailbox " + query + " failed", e);
		}
	}

	@Override
	public boolean hasChildren(Mailbox mailbox, char c) throws MailboxException, MailboxNotFoundException {
		return false;
	}

	@Override
	public ACLDiff updateACL(Mailbox mailbox, MailboxACL.ACLCommand mailboxACLCommand) throws MailboxException {
		MailboxACL oldACL = mailbox.getACL();
		MailboxACL newACL = mailbox.getACL().apply(mailboxACLCommand);
		mailbox.setACL(newACL);
		return ACLDiff.computeDiff(oldACL, newACL);
	}

	@Override
	public ACLDiff setACL(Mailbox mailbox, MailboxACL mailboxACL) throws MailboxException {
		MailboxACL oldMailboxAcl = mailbox.getACL();
		mailbox.setACL(mailboxACL);
		return ACLDiff.computeDiff(oldMailboxAcl, mailboxACL);
	}

	@Override
	public List<Mailbox> list() throws MailboxException {
		try {
			return HHRowUtil.select(getConn(), Mail_Dir.class, getSql()).stream()
					.map(Mail_Dir::toMailbox)
					.collect(Guavate.toImmutableList());
		} catch (Exception e) {
			throw new MailboxException("Search  of mailboxes list failed", e);
		}
	}


	private boolean isPathAlreadyUsedByAnotherMailbox(Mailbox mailbox) throws MailboxException {
		try {
			Mailbox storedMailbox = findMailboxByPath(mailbox.generateAssociatedPath());
			return !Objects.equal(storedMailbox.getMailboxId(), mailbox.getMailboxId());
		} catch (MailboxNotFoundException e) {
			return false;
		}
	}

	/**
	 * 根据id查找
	 *
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Mail_Dir getOne(int id) throws Exception {
		StringBuffer sb = new StringBuffer(getSql());
		sb.append(" and md.id = %s ");
		return HHRowUtil.selectOne(getConn(), Mail_Dir.class, sb.toString(), id);
	}

	private String getSql() {
		return String.format("select md.*,ma.name addr_name from  %s  md left join mail_addr ma on ma.id =md.mail_addr_id where md.id>-1", tabName);
	}

	/**
	 * 根据邮箱账号id及邮箱名查找
	 *
	 * @param name
	 * @param mailAddrId
	 * @return
	 * @throws Exception
	 */
	public Mail_Dir getOne(String name, int mailAddrId) throws Exception {
		StringBuffer sb = new StringBuffer(getSql());
		sb.append(" and md.name = '%s' and md.mail_addr_id =%s ");
		return HHRowUtil.selectOne(getConn(), Mail_Dir.class, sb.toString(), name, mailAddrId);
	}

	public Mail_Dir getOneByUsr(String name, String addrName) throws Exception {
		StringBuffer sb = new StringBuffer(getSql());
		sb.append(" and md.name = '%s' and ma.name ='%s' ");
		return HHRowUtil.selectOne(getConn(), Mail_Dir.class, sb.toString(), name, addrName);
	}

	public List<Mail_Dir> getByLikeName(String name, String addrName) throws Exception {
		StringBuffer sb = new StringBuffer(getSql());
		sb.append(" and md.name like '%s' and ma.name ='%s' ");
		return HHRowUtil.select(getConn(), Mail_Dir.class, sb.toString(), name, addrName);
	}

	public List<Mail_Dir> getByUsrName(String addrName) throws Exception {
		StringBuffer sb = new StringBuffer(getSql());
		sb.append(" and ma.name ='%s' ");
		return HHRowUtil.select(getConn(), Mail_Dir.class, sb.toString(), addrName);
	}

	/**
	 * 创建邮箱文件夹
	 *
	 * @param mailDir
	 * @return
	 * @throws Exception
	 */
	public int createMailDir(Mail_Dir mailDir) throws Exception {
		int id = -1;
		String boxTabName;
		Connection conn = this.getConn();
		if (mailDir != null && mailDir.getMailAddrId() >= 0) {
			if (StringUtils.isNoneBlank(mailDir.getName())) {
				Mail_Dir one = getOne(mailDir.getName(), mailDir.getMailAddrId());
				if (one != null) {
					throw new Exception(String.format("邮箱:\"%s\" 已存在! ", mailDir.getName()));
				}
			}
			try {
				conn.setAutoCommit(false);
				id = HHRowUtil.insertRetIntKey(conn, mailDir, tabName);
				if (StringUtils.isBlank(mailDir.getTab())) {
					boxTabName = HHMailBoxUtil.MAIL_BOX_TABLE_NAME + id;
					mailDir.setTab(String.format("%s.%s", HHMailBoxUtil.MAIL_SCHEMA_NAME, boxTabName));
					mailDir.setId(id);
					HHRowUtil.update(conn, mailDir, tabName, new String[]{Mail_Dir.ID});
				} else {
					boxTabName = mailDir.getTab();
				}

				int status = createMailBoxTab(boxTabName, conn);
				if (status < 0) {
					throw new Exception("创建box失败");
				}
				conn.commit();
			} catch (Exception e) {
				conn.rollback();
				throw e;
			} finally {
				conn.setAutoCommit(true);
			}
		}
		return id;
	}

	/**
	 * 创建box表
	 *
	 * @param boxName
	 * @return
	 */
	private int createMailBoxTab(String boxName, Connection conn) throws Exception {
		if (StringUtils.isBlank(boxName)) {
			return -1;
		}
		StringBuffer sql = new StringBuffer();
		sql.append("create table %s.%s  (");
		sql.append(" id serial primary key,addr_id  int,size  int,subject  text,from_addr  varchar,to_addr  varchar, ");
		sql.append(" seen  boolean default false,has_attach boolean default false,");
		sql.append(" mail_date  timestamp,save_date  timestamp DEFAULT now(), ");
		sql.append(" eml_file_id    int,log_file_id   int, ");
		sql.append(" foreign key (eml_file_id) references attach (id),");
		sql.append(" foreign key (log_file_id) references attach (id),");
		sql.append(" foreign key (addr_id) references mail_addr (id) )");
		return SqlExeUtil.executeUpdate(conn, sql.toString(), HHMailBoxUtil.MAIL_SCHEMA_NAME, boxName);
	}

	public List<Mail_Dir> getUpDirs(String box) throws Exception {
		StringBuffer sql = new StringBuffer();
		sql.append(" WITH RECURSIVE parents AS  ");
		sql.append(" ( SELECT * FROM %s  WHERE tab ='%s' ");
		sql.append("  UNION  ALL  ");
		sql.append("  SELECT m. * FROM %s m ,parents ");
		sql.append(" WHERE m.id = parents.p_id  AND m.id >= 0)  ");
		sql.append(" SELECT * FROM parents   ");
		List<Mail_Dir> list = HHRowUtil.select(getConn(), Mail_Dir.class, sql.toString(), tabName, box, tabName);
		Collections.reverse(list);
		return list;
	}

	public String getUpDirs(Mail_Dir mailDir) throws Exception {
		String dirName = mailDir.getFormatName();
		if (mailDir.getPId() != -1) {
			List<Mail_Dir> upDirs = getUpDirs(mailDir.getTab());
			StringBuffer sb = new StringBuffer();
			for (Mail_Dir upDir : upDirs) {
				sb.append(upDir.getFormatName()).append(".");
			}
			sb.deleteCharAt(sb.length() - 1);
			dirName = sb.toString();
		}
		return dirName;
	}

	public String getSplitDirName(String name) {
		if (StringUtils.isNotBlank(name) && name.contains(".")) {
			String[] split = name.split("\\.");
			return split[split.length - 1];
		}
		return name;
	}

//	@Override
//	public Connection getConn() throws Exception {
//		return ConnUtil.getHHdbConn("192.168.2.206", 1432, "hhdb", "dba", "123456");
//	}

	public static void main(String[] args) throws Exception {
		MailboxPath path = new MailboxPath("#private", Username.of("oyx@hhxdtech.com.cn"), "测试1");
		Mailbox mailbox = new HHMailBoxMapper().findMailboxByPath(path);
		String name = mailbox.getName();
		System.out.println(name);
		String[] split = name.split("\\.");
		System.out.println(name.split("\\.")[split.length - 1]);
	}

	@Override
	public void endRequest() {
		try {
			PoolUtil.put();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public <T> T execute(Transaction<T> transaction) throws MailboxException {
		return transaction.run();
	}
}
