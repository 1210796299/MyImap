package com.hh.imap.mailbox.mail.model;

import com.hh.imap.mailbox.mail.base.AbstractHHMailBoxMessage;
import org.apache.commons.io.IOUtils;
import org.apache.james.mailbox.MessageUid;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.Mailbox;
import org.apache.james.mailbox.store.mail.model.MailboxMessage;
import org.apache.james.mailbox.store.mail.model.impl.PropertyBuilder;

import javax.mail.Flags;
import javax.mail.internet.SharedInputStream;
import javax.mail.util.SharedByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * @author oyx
 * @date 2020-01-09 14:32
 */
public class HHStreamingMailBoxMessage extends AbstractHHMailBoxMessage {
	private InputStream body;
	private InputStream header;
	private final SharedInputStream content;

	public HHStreamingMailBoxMessage(Mailbox mailbox, Date internalDate, int size, Flags flags, SharedInputStream content, int bodyStartOctet, PropertyBuilder propertyBuilder) throws MailboxException {
		super(mailbox, internalDate, flags, size, bodyStartOctet, propertyBuilder);
		this.content = content;

		try {
			this.header = getHeaderContent();
			this.body = getBodyContent();

		} catch (IOException e) {
			throw new MailboxException("Unable to parse message", e);
		}
	}

	/**
	 * Create a copy of the given message
	 */
	public HHStreamingMailBoxMessage(Mailbox mailbox, MessageUid uid, MailboxMessage message) throws MailboxException {
		super(mailbox, uid, message);
		try {
			this.content = new SharedByteArrayInputStream(IOUtils.toByteArray(message.getFullContent()));
			this.header = getHeaderContent();
			this.body = getBodyContent();
		} catch (IOException e) {
			throw new MailboxException("Unable to parse message", e);
		}
	}

	@Override
	public InputStream getBodyContent() throws IOException {
		return content.newStream(getBodyStartOctet(), -1);
	}

	@Override
	public InputStream getHeaderContent() throws IOException {
		int headerEnd = getBodyStartOctet() - 2;
		if (headerEnd < 0) {
			headerEnd = 0;
		}
		return content.newStream(0, headerEnd);
	}

	public InputStream getBody() {
		return body;
	}

	public void setBody(InputStream body) {
		this.body = body;
	}

	public InputStream getHeader() {
		return header;
	}

	public void setHeader(InputStream header) {
		this.header = header;
	}

	public SharedInputStream getContent() {
		return content;
	}
}
