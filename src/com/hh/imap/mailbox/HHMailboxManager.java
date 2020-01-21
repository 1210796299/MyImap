package com.hh.imap.mailbox;

import com.hh.imap.mailbox.mail.base.AbstractMailboxManager;
import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.SessionProvider;
import org.apache.james.mailbox.events.EventBus;
import org.apache.james.mailbox.model.Mailbox;
import org.apache.james.mailbox.model.MessageId;
import org.apache.james.mailbox.store.JVMMailboxPathLocker;
import org.apache.james.mailbox.store.StoreMailboxAnnotationManager;
import org.apache.james.mailbox.store.StoreMessageManager;
import org.apache.james.mailbox.store.StoreRightManager;
import org.apache.james.mailbox.store.mail.model.impl.MessageParser;
import org.apache.james.mailbox.store.quota.QuotaComponents;
import org.apache.james.mailbox.store.search.MessageSearchIndex;

/**
 * @author oyx
 * @date 2020-01-10 10:26
 */
public class HHMailboxManager extends AbstractMailboxManager {

	public HHMailboxManager(HHMailboxSessionMapperFactory mapperFactory,
							SessionProvider sessionProvider,
							MessageParser messageParser,
							MessageId.Factory messageIdFactory,
							EventBus eventBus,
							StoreMailboxAnnotationManager annotationManager,
							StoreRightManager storeRightManager,
							QuotaComponents quotaComponents,
							MessageSearchIndex index) {
		super(mapperFactory, sessionProvider, new JVMMailboxPathLocker(), messageParser, messageIdFactory, eventBus, annotationManager, storeRightManager, quotaComponents, index);
	}

	protected HHMessageManager.AdvancedFeature getAdvancedFeature() {
		return HHMessageManager.AdvancedFeature.None;
	}

	@Override
	protected StoreMessageManager createMessageManager(Mailbox mailboxRow, MailboxSession session) {
		return new HHMessageManager(getMapperFactory(),
				getMessageSearchIndex(),
				getEventBus(),
				getLocker(),
				mailboxRow,
				getQuotaComponents().getQuotaManager(),
				getQuotaComponents().getQuotaRootResolver(),
				getMessageParser(),
				getMessageIdFactory(),
				configuration.getBatchSizes(),
				getStoreRightManager(), getAdvancedFeature());
	}

}
