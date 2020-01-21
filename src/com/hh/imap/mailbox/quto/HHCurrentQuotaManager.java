package com.hh.imap.mailbox.quto;

import org.apache.james.core.quota.QuotaCountUsage;
import org.apache.james.core.quota.QuotaSizeUsage;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.QuotaRoot;
import org.apache.james.mailbox.store.quota.StoreCurrentQuotaManager;

/**
 * @author oyx
 * @date 2020-01-10 10:18
 */
public class HHCurrentQuotaManager implements StoreCurrentQuotaManager {
	public static final long NO_MESSAGES = 0L;
	public static final long NO_STORED_BYTES = 0L;

	public HHCurrentQuotaManager() {
	}

	@Override
	public void increase(QuotaRoot quotaRoot, long l, long l1) throws MailboxException {

	}

	@Override
	public void decrease(QuotaRoot quotaRoot, long l, long l1) throws MailboxException {

	}

	@Override
	public QuotaCountUsage getCurrentMessageCount(QuotaRoot quotaRoot) throws MailboxException {
		return  QuotaCountUsage.count(NO_STORED_BYTES);
	}

	@Override
	public QuotaSizeUsage getCurrentStorage(QuotaRoot quotaRoot) throws MailboxException {
		return QuotaSizeUsage.size(NO_STORED_BYTES);
	}
}
