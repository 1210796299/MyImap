package com.hh.imap.mailbox.user;

import com.hh.frame.common.util.EncryptUtil;
import com.hh.imap.mailbox.mail.base.HHTransactionalMapper;
import com.hh.imap.mailbox.mail.service.HHUsersService;
import com.hh.imap.mailbox.mail.service.bean.Mail_Addr;
import com.hh.imap.mailbox.user.model.HHUser;
import com.hh.wframe.service.login.LoginService;
import com.hh.wframe.service.login.Usr;
import com.hh.wframe.util.PoolUtil;
import org.apache.james.core.MailAddress;
import org.apache.james.core.Username;
import org.apache.james.domainlist.api.DomainList;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.user.api.UsersRepositoryException;
import org.apache.james.user.api.model.User;
import org.apache.james.user.lib.AbstractUsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * @author oyx
 * @date 2020-01-08 10:02
 */
public class HHUsersRepository extends AbstractUsersRepository implements HHTransactionalMapper {
	private static final Logger LOGGER = LoggerFactory.getLogger(HHUsersRepository.class);
	private HHUsersService hhUsersService;

	public HHUsersRepository(DomainList domainList) {
		super(domainList);
		hhUsersService = new HHUsersService();
	}


	@Override
	protected void doAddUser(Username username, String password) throws UsersRepositoryException {
		Username lowerCasedUsername = Username.of(username.asString().toLowerCase(Locale.US));
		if (contains(lowerCasedUsername)) {
			throw new UsersRepositoryException(lowerCasedUsername.asString() + " already exists.");
		}
		try {
			String name = lowerCasedUsername.asString();
			Usr usr = new Usr();
			usr.setName(lowerCasedUsername.asString());
			usr.setPass(EncryptUtil.md5(password));
			LoginService loginService = new LoginService(null);
			int usrId = loginService.add(usr);
			Mail_Addr mailAddr = new Mail_Addr();
			mailAddr.setDefault(true);
			mailAddr.setName(name);
			mailAddr.setUsrId(usrId);
			hhUsersService.addOne(mailAddr);
		} catch (Exception e) {
			LOGGER.debug("Failed to save user", e);
		}

	}

	@Override
	public User getUserByName(Username username) throws UsersRepositoryException {
		try {
			Mail_Addr mailAddr = hhUsersService.getAddressByName(username.asString());
			if (mailAddr != null) {
				return new HHUser(Username.of(mailAddr.getName()), mailAddr.getUsrPass());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				PoolUtil.put();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public void updateUser(User user) throws UsersRepositoryException {

	}

	@Override
	public void removeUser(Username username) throws UsersRepositoryException {

	}

	@Override
	public boolean contains(Username username) throws UsersRepositoryException {
		String address = username.asString().toLowerCase();
		try {
			return hhUsersService.getAddress(address) != null;
		} catch (Exception e) {
			LOGGER.debug("Failed to find user", e);
			throw new UsersRepositoryException("Failed to find user" + username.asString(), e);
		}
	}

	@Override
	public boolean test(Username name, String password) throws UsersRepositoryException {
		try {
			final User user = getUserByName(name);
			final boolean result;
			result = user != null && user.verifyPassword(password);
			return result;
		} finally {
			try {
				PoolUtil.put();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public int countUsers() throws UsersRepositoryException {
		try {
			return hhUsersService.count();
		} catch (Exception e) {
			LOGGER.debug("Failed to find user", e);
			throw new UsersRepositoryException("Failed to count users", e);
		}
	}

	@Override
	public Iterator<Username> list() throws UsersRepositoryException {
		try {
			List<Mail_Addr> mailAddrList = hhUsersService.get(Mail_Addr.class);
			List<Username> userNames = new ArrayList<>();
			for (Mail_Addr mailAddr : mailAddrList) {
				userNames.add(Username.of(mailAddr.getName()));
			}
			return userNames.iterator();
		} catch (Exception e) {
			LOGGER.debug("Failed to find user", e);
			throw new UsersRepositoryException("Failed to list users", e);
		}
	}

	@Override
	public Username getUser(MailAddress mailAddress) throws UsersRepositoryException {
		return null;
	}


	/**
	 * IMAP Request was complete. Cleanup all Request scoped stuff
	 */
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
