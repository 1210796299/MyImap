package com.hh.imap.mailbox.user;

import com.hh.imap.mailbox.mail.HHMailBoxMapper;
import com.hh.imap.mailbox.mail.service.bean.MailBoxEnum;
import com.hh.imap.mailbox.mail.service.bean.Mail_Dir;
import org.apache.james.core.Username;
import org.apache.james.mailbox.exception.SubscriptionException;
import org.apache.james.mailbox.store.transaction.NonTransactionalMapper;
import org.apache.james.mailbox.store.user.SubscriptionMapper;
import org.apache.james.mailbox.store.user.model.Subscription;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyx
 * @date 2020-01-10 14:36
 */
public class HHSubscriptionMapper extends NonTransactionalMapper implements SubscriptionMapper {
	public HHSubscriptionMapper() {
	}

	/**
	 * Saves the given subscription.
	 *
	 * @param subscription not null
	 */
	@Override
	public void save(Subscription subscription) throws SubscriptionException {

	}

	/**
	 * Finds subscriptions for the given user.
	 *
	 * @param user not null
	 * @return not null
	 */
	@Override
	public List<Subscription> findSubscriptionsForUser(Username user) throws SubscriptionException {
		HHMailBoxMapper mailBoxMapper = new HHMailBoxMapper();
		try {
			List<Subscription> subscriptions = new ArrayList<>();
			List<Mail_Dir> mailDirList = mailBoxMapper.getByUsrName(user.asString());
			for (Mail_Dir mailDir : mailDirList) {
				if (!mailDir.getName().equalsIgnoreCase(MailBoxEnum.INBOX.name())) {
					if (mailDir.getPId() != -1) {
						HHMailBoxMapper boxMapper = new HHMailBoxMapper();
						String upDirs = boxMapper.getUpDirs(mailDir);
						mailDir.setFormatName(upDirs);
					}
					subscriptions.add(new Subscription(user, mailDir.getFormatName()));
				}
			}
			return subscriptions;
		} catch (Exception e) {
			throw new SubscriptionException(e);
		}
	}

	/**
	 * Deletes the given subscription.
	 *
	 * @param subscription not null
	 */
	@Override
	public void delete(Subscription subscription) throws SubscriptionException {

	}

	/**
	 * IMAP Request was complete. Cleanup all Request scoped stuff
	 */
	@Override
	public void endRequest() {

	}

}
