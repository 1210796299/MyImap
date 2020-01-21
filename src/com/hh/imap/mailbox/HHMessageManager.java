package com.hh.imap.mailbox;

import com.hh.imap.mailbox.mail.base.BaseMessageManager;
import com.hh.imap.mailbox.mail.model.HHEncryptedMailboxMessage;
import com.hh.imap.mailbox.mail.model.HHStreamingMailBoxMessage;
import org.apache.james.mailbox.MailboxPathLocker;
import org.apache.james.mailbox.events.EventBus;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.Mailbox;
import org.apache.james.mailbox.model.MessageAttachment;
import org.apache.james.mailbox.model.MessageId;
import org.apache.james.mailbox.quota.QuotaManager;
import org.apache.james.mailbox.quota.QuotaRootResolver;
import org.apache.james.mailbox.store.BatchSizes;
import org.apache.james.mailbox.store.MailboxSessionMapperFactory;
import org.apache.james.mailbox.store.StoreRightManager;
import org.apache.james.mailbox.store.mail.model.MailboxMessage;
import org.apache.james.mailbox.store.mail.model.impl.MessageParser;
import org.apache.james.mailbox.store.mail.model.impl.PropertyBuilder;
import org.apache.james.mailbox.store.search.MessageSearchIndex;

import javax.mail.Flags;
import javax.mail.internet.SharedInputStream;
import java.util.Date;
import java.util.List;

/**
 * @author oyx
 * @date 2020-01-10 10:33
 */
public class HHMessageManager extends BaseMessageManager {
	private final AdvancedFeature feature;

	public enum AdvancedFeature {
		None,
		Streaming,
		Encryption
	}

	public HHMessageManager(MailboxSessionMapperFactory mapperFactory, MessageSearchIndex index, EventBus eventBus, MailboxPathLocker locker, Mailbox mailbox, QuotaManager quotaManager, QuotaRootResolver quotaRootResolver, MessageParser messageParser, MessageId.Factory messageIdFactory, BatchSizes batchSizes, StoreRightManager storeRightManager, AdvancedFeature f) {
		super(mapperFactory, index, eventBus, locker, mailbox, quotaManager, quotaRootResolver, messageParser, messageIdFactory, batchSizes, storeRightManager);
		this.feature = f;
	}

	@Override
	protected MailboxMessage createMessage(Date internalDate, int size, int bodyStartOctet, SharedInputStream content, Flags flags, PropertyBuilder propertyBuilder, List<MessageAttachment> attachments) throws MailboxException {
		switch (feature) {
			case Streaming:
				return new HHStreamingMailBoxMessage(getMailboxEntity(), internalDate, size, flags, content, bodyStartOctet, propertyBuilder);
			case Encryption:
				return new HHEncryptedMailboxMessage(getMailboxEntity(), internalDate, size, flags, content, bodyStartOctet, propertyBuilder);
			default:
				return super.createMessage(internalDate, size, bodyStartOctet, content, flags,  propertyBuilder, attachments);
		}

	}
}
