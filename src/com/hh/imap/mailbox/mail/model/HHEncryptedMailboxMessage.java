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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * @author oyx
 * @date 2020-01-16 14:36
 */
public class HHEncryptedMailboxMessage extends AbstractHHMailBoxMessage {
	private byte[] body;
	private byte[] header;

	public HHEncryptedMailboxMessage() {
	}

	public HHEncryptedMailboxMessage(Mailbox mailbox, Date internalDate, int size, Flags flags, SharedInputStream content, int bodyStartOctet, PropertyBuilder propertyBuilder) throws MailboxException {
		super(mailbox, internalDate, flags, size, bodyStartOctet, propertyBuilder);
		try {
			int headerEnd = bodyStartOctet;
			if (headerEnd < 0) {
				headerEnd = 0;
			}
			this.header = IOUtils.toByteArray(content.newStream(0, headerEnd));
			this.body = IOUtils.toByteArray(content.newStream(getBodyStartOctet(), -1));

		} catch (IOException e) {
			throw new MailboxException("Unable to parse message", e);
		}
	}

	/**
	 * Create a copy of the given message
	 */
	public HHEncryptedMailboxMessage(Mailbox mailbox, MessageUid uid, MailboxMessage message) throws MailboxException {
		super(mailbox, uid, message);
		try {
			this.body = IOUtils.toByteArray(message.getBodyContent());
			this.header = IOUtils.toByteArray(message.getHeaderContent());
		} catch (IOException e) {
			throw new MailboxException("Unable to parse message", e);
		}
	}

	@Override
	public InputStream getBodyContent() throws IOException {
		return new ByteArrayInputStream(body);
	}

	@Override
	public InputStream getHeaderContent() throws IOException {
		return new ByteArrayInputStream(header);
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}

	public byte[] getHeader() {
		return header;
	}

	public void setHeader(byte[] header) {
		this.header = header;
	}
}
