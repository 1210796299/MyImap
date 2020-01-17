package com.hh.imap;

import org.apache.james.mailbox.MailboxManager;
import org.apache.james.mailbox.MailboxPathLocker;
import org.apache.james.mailbox.events.EventBus;
import org.apache.james.mailbox.model.Mailbox;
import org.apache.james.mailbox.model.MessageId;
import org.apache.james.mailbox.quota.QuotaManager;
import org.apache.james.mailbox.quota.QuotaRootResolver;
import org.apache.james.mailbox.store.*;
import org.apache.james.mailbox.store.mail.model.impl.MessageParser;
import org.apache.james.mailbox.store.search.MessageSearchIndex;

import java.util.EnumSet;

/**
 * @author oyx
 * @date 2020-01-15 14:59
 */
public class HHStoreMessageManager extends StoreMessageManager {
	public HHStoreMessageManager(EnumSet<MailboxManager.MessageCapabilities> messageCapabilities, MailboxSessionMapperFactory mapperFactory, MessageSearchIndex index, EventBus eventBus, MailboxPathLocker locker, Mailbox mailbox, QuotaManager quotaManager, QuotaRootResolver quotaRootResolver, MessageParser messageParser, MessageId.Factory messageIdFactory, BatchSizes batchSizes, StoreRightManager storeRightManager, PreDeletionHooks preDeletionHooks) {
		super(messageCapabilities, mapperFactory, index, eventBus, locker, mailbox, quotaManager, quotaRootResolver, messageParser, messageIdFactory, batchSizes, storeRightManager, preDeletionHooks);
	}

}
