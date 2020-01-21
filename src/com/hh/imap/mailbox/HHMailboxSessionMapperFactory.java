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
package com.hh.imap.mailbox;

import com.hh.imap.mailbox.mail.HHMailBoxMapper;
import com.hh.imap.mailbox.mail.HHMessageMapper;
import com.hh.imap.mailbox.mail.HHModSeqProvider;
import com.hh.imap.mailbox.mail.MessageUtils;
import com.hh.imap.mailbox.user.HHSubscriptionMapper;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.store.MailboxSessionMapperFactory;
import org.apache.james.mailbox.store.mail.*;
import org.apache.james.mailbox.store.user.SubscriptionMapper;

/**
 * HH implementation of {@link MailboxSessionMapperFactory}
 *
 * @author oyx
 */
public class HHMailboxSessionMapperFactory extends MailboxSessionMapperFactory {

	private MessageUtils messageMetadataMapper =new MessageUtils(new HHModSeqProvider());

	public HHMailboxSessionMapperFactory() {
	}

	@Override
	public MailboxMapper createMailboxMapper(MailboxSession session) {
		return new HHMailBoxMapper();
	}

	@Override
	public MessageMapper createMessageMapper(MailboxSession session) {
		return new HHMessageMapper(messageMetadataMapper);
	}

	@Override
	public MessageIdMapper createMessageIdMapper(MailboxSession session) throws MailboxException {
		throw new NotImplementedException("not implemented");
	}

	@Override
	public SubscriptionMapper createSubscriptionMapper(MailboxSession session) {
		return new HHSubscriptionMapper();
	}


	@Override
	public AnnotationMapper createAnnotationMapper(MailboxSession session)
			throws MailboxException {
		return null;
	}

	@Override
	public UidProvider getUidProvider() {
		return null;
	}

	@Override
	public ModSeqProvider getModSeqProvider() {
		return null;
	}

}
