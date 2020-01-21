package com.hh.imap.mailbox.mail.base;

import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.store.transaction.Mapper;

/**
 * @author oyx
 * @date 2020-01-16 15:55
 */
public interface HHTransactionalMapper extends Mapper {

	/**
	 * IMAP Request was complete. Cleanup all Request scoped stuff
	 */
	@Override
	public void endRequest();

	@Override
	public <T> T execute(Transaction<T> transaction) throws MailboxException;

}
