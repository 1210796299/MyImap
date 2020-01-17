/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/
package com.hh.imap.mailbox.hh.mail.base;

import com.hh.imap.mailbox.hh.HHMailboxSessionMapperFactory;
import org.apache.james.mailbox.MailboxPathLocker;
import org.apache.james.mailbox.SessionProvider;
import org.apache.james.mailbox.events.EventBus;
import org.apache.james.mailbox.model.MessageId;
import org.apache.james.mailbox.store.*;
import org.apache.james.mailbox.store.mail.model.impl.MessageParser;
import org.apache.james.mailbox.store.quota.QuotaComponents;
import org.apache.james.mailbox.store.search.MessageSearchIndex;

import java.util.EnumSet;

/**
 * HH implementation of {@link StoreMailboxManager}
 *
 * @author oyx
 * @date 2020-1-17 10:21:57
 */
public abstract class AbstractMailboxManager extends StoreMailboxManager {

	public static final EnumSet<MailboxCapabilities> MAILBOX_CAPABILITIES = EnumSet.of(MailboxCapabilities.UserFlag,
			MailboxCapabilities.Namespace,
			MailboxCapabilities.Move,
			MailboxCapabilities.Annotation);

	public AbstractMailboxManager(HHMailboxSessionMapperFactory mailboxSessionMapperFactory,
								  SessionProvider sessionProvider,
								  MailboxPathLocker locker,
								  MessageParser messageParser,
								  MessageId.Factory messageIdFactory,
								  EventBus eventBus,
								  StoreMailboxAnnotationManager annotationManager,
								  StoreRightManager storeRightManager,
								  QuotaComponents quotaComponents,
								  MessageSearchIndex index) {
		super(mailboxSessionMapperFactory, sessionProvider, locker,
				messageParser, messageIdFactory, annotationManager,
				eventBus, storeRightManager, quotaComponents,
				index, MailboxManagerConfiguration.DEFAULT, PreDeletionHooks.NO_PRE_DELETION_HOOK);
	}

	@Override
	public EnumSet<MailboxCapabilities> getSupportedMailboxCapabilities() {
		return MAILBOX_CAPABILITIES;
	}

}
