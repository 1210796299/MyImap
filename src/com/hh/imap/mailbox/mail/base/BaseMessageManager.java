package com.hh.imap.mailbox.mail.base;

import com.hh.imap.mailbox.HHMailboxManager;
import com.hh.imap.mailbox.mail.model.HHMailBoxMessage;
import org.apache.james.mailbox.MailboxPathLocker;
import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.events.EventBus;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.Mailbox;
import org.apache.james.mailbox.model.MessageAttachment;
import org.apache.james.mailbox.model.MessageId;
import org.apache.james.mailbox.quota.QuotaManager;
import org.apache.james.mailbox.quota.QuotaRootResolver;
import org.apache.james.mailbox.store.*;
import org.apache.james.mailbox.store.mail.model.MailboxMessage;
import org.apache.james.mailbox.store.mail.model.impl.MessageParser;
import org.apache.james.mailbox.store.mail.model.impl.PropertyBuilder;
import org.apache.james.mailbox.store.search.MessageSearchIndex;

import javax.mail.Flags;
import javax.mail.internet.SharedInputStream;
import java.util.Date;
import java.util.List;

/**
 * Abstract base class which should be used from hh implementations.
 *
 * @author oyx
 * @date 2020-1-17 10:22:11
 */
public class BaseMessageManager extends StoreMessageManager {

	public BaseMessageManager(MailboxSessionMapperFactory mapperFactory,
							  MessageSearchIndex index,
							  EventBus eventBus,
							  MailboxPathLocker locker,
							  Mailbox mailbox,
							  QuotaManager quotaManager,
							  QuotaRootResolver quotaRootResolver,
							  MessageParser messageParser,
							  MessageId.Factory messageIdFactory,
							  BatchSizes batchSizes,
							  StoreRightManager storeRightManager) {

		super(HHMailboxManager.DEFAULT_NO_MESSAGE_CAPABILITIES, mapperFactory, index, eventBus, locker, mailbox,
				quotaManager, quotaRootResolver, messageParser, messageIdFactory, batchSizes, storeRightManager, PreDeletionHooks.NO_PRE_DELETION_HOOK);
	}

	@Override
	protected MailboxMessage createMessage(Date internalDate, int size, int bodyStartOctet, SharedInputStream content,
										   final Flags flags, PropertyBuilder propertyBuilder, List<MessageAttachment> attachments) throws MailboxException {

		return new HHMailBoxMessage(getMailboxEntity(), internalDate, size, flags, content, bodyStartOctet, propertyBuilder);
	}


	/**
	 * Support user flags
	 */
	@Override
	protected Flags getPermanentFlags(MailboxSession session) {
		Flags flags = super.getPermanentFlags(session);
		flags.add(Flags.Flag.USER);
		return flags;
	}

}
