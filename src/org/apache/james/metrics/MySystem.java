package org.apache.james.metrics;

import com.hh.imap.mailbox.hh.HHMailboxManager;
import com.hh.imap.mailbox.hh.HHMailboxSessionMapperFactory;
import com.hh.imap.mailbox.hh.mail.base.AbstractMailboxManager;
import com.hh.imap.mailbox.hh.quto.HHCurrentQuotaManager;
import com.hh.imap.mailbox.hh.quto.HHPerUserMaxQuotaManager;
import com.hh.imap.mailbox.hh.user.HHUsersRepository;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.james.adapter.mailbox.store.UserRepositoryAuthenticator;
import org.apache.james.adapter.mailbox.store.UserRepositoryAuthorizator;
import org.apache.james.core.Domain;
import org.apache.james.imap.api.process.ImapProcessor;
import org.apache.james.imap.processor.main.DefaultImapProcessorFactory;
import org.apache.james.mailbox.SubscriptionManager;
import org.apache.james.mailbox.acl.GroupMembershipResolver;
import org.apache.james.mailbox.acl.MailboxACLResolver;
import org.apache.james.mailbox.acl.SimpleGroupMembershipResolver;
import org.apache.james.mailbox.acl.UnionMailboxACLResolver;
import org.apache.james.mailbox.events.InVMEventBus;
import org.apache.james.mailbox.events.delivery.InVmEventDelivery;
import org.apache.james.mailbox.store.*;
import org.apache.james.mailbox.store.event.MailboxAnnotationListener;
import org.apache.james.mailbox.store.extractor.DefaultTextExtractor;
import org.apache.james.mailbox.store.mail.model.DefaultMessageId;
import org.apache.james.mailbox.store.mail.model.impl.MessageParser;
import org.apache.james.mailbox.store.quota.DefaultUserQuotaRootResolver;
import org.apache.james.mailbox.store.quota.ListeningCurrentQuotaUpdater;
import org.apache.james.mailbox.store.quota.QuotaComponents;
import org.apache.james.mailbox.store.quota.StoreQuotaManager;
import org.apache.james.mailbox.store.search.MessageSearchIndex;
import org.apache.james.mailbox.store.search.SimpleMessageSearchIndex;
import org.apache.james.metrics.logger.DefaultMetricFactory;
import org.apache.james.user.lib.AbstractUsersRepository;

/**
 * @author oyx
 * @date 2020-01-06 17:37
 */
public class MySystem {
	private static SimpleDomainList domainList = null;
	private static final Domain DOMAIN = Domain.of("hh.com");
	private AbstractUsersRepository usersRepository;
	protected Authorizator authorizator;
	protected Authenticator authenticator;


	private HHPerUserMaxQuotaManager maxQuotaManager;
	private ImapProcessor defaultImapProcessorFactory;
	private AbstractMailboxManager mailboxManager;
	private HierarchicalConfiguration<ImmutableNode> configuration;


	public void init() throws Exception {
		try {
			usersRepository = getUsersRepository();
			usersRepository.configure(configuration);
		} catch (ConfigurationException e) {
			throw new RuntimeException(e);
		}
		authenticator = new UserRepositoryAuthenticator(usersRepository);
		authorizator = new UserRepositoryAuthorizator(usersRepository);

		HHMailboxSessionMapperFactory mapperFactory = new HHMailboxSessionMapperFactory();

		MailboxACLResolver aclResolver = new UnionMailboxACLResolver();
		GroupMembershipResolver groupMembershipResolver = new SimpleGroupMembershipResolver();
		MessageParser messageParser = new MessageParser();

		InVMEventBus eventBus = new InVMEventBus(new InVmEventDelivery(new DefaultMetricFactory()));
		StoreRightManager storeRightManager = new StoreRightManager(mapperFactory, aclResolver, groupMembershipResolver, eventBus);
		StoreMailboxAnnotationManager annotationManager = new StoreMailboxAnnotationManager(mapperFactory, storeRightManager);
		SessionProviderImpl sessionProvider = new SessionProviderImpl(authenticator, authorizator);
		DefaultUserQuotaRootResolver quotaRootResolver = new DefaultUserQuotaRootResolver(sessionProvider, mapperFactory);
		HHCurrentQuotaManager currentQuotaManager = new HHCurrentQuotaManager();
		maxQuotaManager = new HHPerUserMaxQuotaManager();
		StoreQuotaManager storeQuotaManager = new StoreQuotaManager(currentQuotaManager, maxQuotaManager);
		ListeningCurrentQuotaUpdater quotaUpdater = new ListeningCurrentQuotaUpdater(currentQuotaManager, quotaRootResolver, eventBus, storeQuotaManager);
		QuotaComponents quotaComponents = new QuotaComponents(maxQuotaManager, storeQuotaManager, quotaRootResolver);
		MessageSearchIndex index = new SimpleMessageSearchIndex(mapperFactory, mapperFactory, new DefaultTextExtractor());

		mailboxManager = new HHMailboxManager(mapperFactory, sessionProvider, messageParser, new DefaultMessageId.Factory(),
				eventBus, annotationManager, storeRightManager, quotaComponents, index);

		eventBus.register(quotaUpdater);
		eventBus.register(new MailboxAnnotationListener(mapperFactory, sessionProvider));

		SubscriptionManager subscriptionManager = new StoreSubscriptionManager(mapperFactory);

		defaultImapProcessorFactory =
				DefaultImapProcessorFactory.createDefaultProcessor(
						mailboxManager,
						eventBus,
						subscriptionManager,
						storeQuotaManager,
						quotaRootResolver,
						new DefaultMetricFactory());
	}


	public ImapProcessor getDefaultImapProcessorFactory() {
		return defaultImapProcessorFactory;
	}

	public AbstractUsersRepository getUsersRepository() throws Exception {
		domainList = new SimpleDomainList();
		domainList.addDomain(DOMAIN);

		HHUsersRepository repos = new HHUsersRepository(domainList);

		return repos;
	}

	public HierarchicalConfiguration<ImmutableNode> getConfiguration() {
		return configuration;
	}

	public void setConfiguration(HierarchicalConfiguration<ImmutableNode> configuration) {
		this.configuration = configuration;
	}

	public static void main(String[] args) throws Exception {
		MySystem system = new MySystem();
		system.init();
	}
}
