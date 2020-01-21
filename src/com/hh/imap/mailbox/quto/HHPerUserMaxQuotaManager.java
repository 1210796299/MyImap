package com.hh.imap.mailbox.quto;

import org.apache.james.core.Domain;
import org.apache.james.core.quota.QuotaCountLimit;
import org.apache.james.core.quota.QuotaSizeLimit;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.Quota;
import org.apache.james.mailbox.model.QuotaRoot;
import org.apache.james.mailbox.quota.MaxQuotaManager;

import java.util.Map;
import java.util.Optional;

/**
 * @author oyx
 * @date 2020-01-10 10:20
 */
public class HHPerUserMaxQuotaManager implements MaxQuotaManager {
	public HHPerUserMaxQuotaManager() {
	}

	@Override
	public void setMaxStorage(QuotaRoot quotaRoot, QuotaSizeLimit quotaSizeLimit) throws MailboxException {

	}

	@Override
	public void setMaxMessage(QuotaRoot quotaRoot, QuotaCountLimit quotaCountLimit) throws MailboxException {

	}

	@Override
	public void removeMaxMessage(QuotaRoot quotaRoot) throws MailboxException {

	}

	@Override
	public void removeMaxStorage(QuotaRoot quotaRoot) throws MailboxException {

	}

	@Override
	public void setGlobalMaxStorage(QuotaSizeLimit quotaSizeLimit) throws MailboxException {

	}

	@Override
	public void removeGlobalMaxStorage() throws MailboxException {

	}

	@Override
	public void setGlobalMaxMessage(QuotaCountLimit quotaCountLimit) throws MailboxException {

	}

	@Override
	public void removeGlobalMaxMessage() throws MailboxException {

	}

	@Override
	public Optional<QuotaSizeLimit> getGlobalMaxStorage() throws MailboxException {
		return Optional.empty();
	}

	@Override
	public Optional<QuotaCountLimit> getGlobalMaxMessage() throws MailboxException {
		return Optional.empty();
	}

	@Override
	public Map<Quota.Scope, QuotaCountLimit> listMaxMessagesDetails(QuotaRoot quotaRoot) {
		return null;
	}

	@Override
	public Map<Quota.Scope, QuotaSizeLimit> listMaxStorageDetails(QuotaRoot quotaRoot) {
		return null;
	}

	@Override
	public Optional<QuotaCountLimit> getDomainMaxMessage(Domain domain) {
		return Optional.empty();
	}

	@Override
	public void setDomainMaxMessage(Domain domain, QuotaCountLimit quotaCountLimit) throws MailboxException {

	}

	@Override
	public void removeDomainMaxMessage(Domain domain) throws MailboxException {

	}

	@Override
	public void setDomainMaxStorage(Domain domain, QuotaSizeLimit quotaSizeLimit) throws MailboxException {

	}

	@Override
	public Optional<QuotaSizeLimit> getDomainMaxStorage(Domain domain) {
		return Optional.empty();
	}

	@Override
	public void removeDomainMaxStorage(Domain domain) throws MailboxException {

	}
}
