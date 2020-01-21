package com.hh.imap.mailbox.mail.base;

import com.github.steveash.guavate.Guavate;
import com.google.common.base.Objects;
import com.hh.imap.mailbox.HHId;
import com.hh.imap.mailbox.user.model.HHProperty;
import com.hh.imap.mailbox.user.model.HHUserFlag;
import org.apache.james.mailbox.MessageUid;
import org.apache.james.mailbox.ModSeq;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.*;
import org.apache.james.mailbox.store.mail.model.DefaultMessageId;
import org.apache.james.mailbox.store.mail.model.FlagsFactory;
import org.apache.james.mailbox.store.mail.model.MailboxMessage;
import org.apache.james.mailbox.store.mail.model.Property;
import org.apache.james.mailbox.store.mail.model.impl.MessageParser;
import org.apache.james.mailbox.store.mail.model.impl.PropertyBuilder;
import org.apache.james.mime4j.MimeException;

import javax.mail.Flags;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author oyx
 * @date 2020-01-17 10:24
 */
public abstract class AbstractHHMailBoxMessage implements MailboxMessage {
	private static final String TOSTRING_SEPARATOR = " ";

	public static class MailboxIdUidKey implements Serializable {

		private static final long serialVersionUID = -8166642631962864094L;

		public MailboxIdUidKey() {
		}

		/**
		 * The value for the mailbox field
		 */
		public long mailbox;

		/**
		 * The value for the uid field
		 */
		public long uid;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (mailbox ^ (mailbox >>> 32));
			result = prime * result + (int) (uid ^ (uid >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final MailboxIdUidKey other = (MailboxIdUidKey) obj;
			if (mailbox != other.mailbox) {
				return false;
			}
			if (uid != other.uid) {
				return false;
			}
			return true;
		}

	}

	private Mailbox mailbox;
	private long uid;
	private long modSeq;
	private Date internalDate;
	private boolean deleted = false;
	private boolean draft = false;
	private boolean answered = false;
	private boolean seen = false;
	private boolean recent = false;
	private boolean flagged = false;
	private String mediaType;
	private String subType;
	private int bodyStartOctet;
	private long contentOctets;
	private Long textualLineCount;
	private List<HHProperty> properties;
	private List<HHUserFlag> userFlags;

	public AbstractHHMailBoxMessage() {
	}

	public AbstractHHMailBoxMessage(Mailbox mailbox, Date internalDate, Flags flags, long contentOctets,
									int bodyStartOctet, PropertyBuilder propertyBuilder) {
		this.mailbox = mailbox;
		this.internalDate = internalDate;
		userFlags = new ArrayList<>();

		setFlags(flags);
		this.contentOctets = contentOctets;
		this.bodyStartOctet = bodyStartOctet;
		this.textualLineCount = propertyBuilder.getTextualLineCount();
		this.mediaType = propertyBuilder.getMediaType();
		this.subType = propertyBuilder.getSubType();
		final List<Property> properties = propertyBuilder.toProperties();
		this.properties = new ArrayList<>(properties.size());
		int order = 0;
		for (Property property : properties) {
			this.properties.add(new HHProperty(property, order++));
		}
	}

	/**
	 * Constructs a copy of the given message. All properties are cloned except
	 * mailbox and UID.
	 *
	 * @param mailbox  new mailbox
	 * @param uid      new UID
	 * @param original message to be copied, not null
	 */
	public AbstractHHMailBoxMessage(Mailbox mailbox, MessageUid uid, MailboxMessage original)
			throws MailboxException {
		super();
		this.mailbox = mailbox;
		this.uid = uid.asLong();
		this.userFlags = new ArrayList<>();
		setFlags(original.createFlags());

		// A copy of a message is recent
		// See MAILBOX-85
		this.recent = true;

		this.contentOctets = original.getFullContentOctets();
		this.bodyStartOctet = (int) (original.getFullContentOctets() - original.getBodyOctets());
		this.internalDate = original.getInternalDate();

		PropertyBuilder pBuilder = new PropertyBuilder(original.getProperties());
		this.textualLineCount = original.getTextualLineCount();
		this.mediaType = original.getMediaType();
		this.subType = original.getSubType();
		final List<Property> properties = pBuilder.toProperties();
		this.properties = new ArrayList<>(properties.size());
		int order = 0;
		for (Property property : properties) {
			this.properties.add(new HHProperty(property, order++));
		}
	}

	@Override
	public void setFlags(Flags flags) {
		answered = flags.contains(Flags.Flag.ANSWERED);
		deleted = flags.contains(Flags.Flag.DELETED);
		draft = flags.contains(Flags.Flag.DRAFT);
		flagged = flags.contains(Flags.Flag.FLAGGED);
		recent = flags.contains(Flags.Flag.RECENT);
		seen = flags.contains(Flags.Flag.SEEN);

		String[] userFlags = flags.getUserFlags();
		this.userFlags.clear();
		for (String flag : userFlags) {
			this.userFlags.add(new HHUserFlag(flag));
		}
	}

	public Mailbox getMailbox() {
		return mailbox;
	}

	public void setMailbox(Mailbox mailbox) {
		this.mailbox = mailbox;
	}


	@Override
	public ModSeq getModSeq() {
		return ModSeq.of(modSeq);
	}

	@Override
	public void setModSeq(ModSeq modSeq) {
		this.modSeq = modSeq.asLong();
	}

	@Override
	public boolean isAnswered() {
		return answered;
	}

	@Override
	public boolean isDeleted() {
		return deleted;
	}

	@Override
	public boolean isDraft() {
		return draft;
	}

	@Override
	public boolean isFlagged() {
		return flagged;
	}

	@Override
	public boolean isRecent() {
		return recent;
	}

	@Override
	public Date getInternalDate() {
		return internalDate;
	}

	@Override
	public String getSubType() {
		return subType;
	}

	@Override
	public MessageId getMessageId() {
		return new DefaultMessageId();
	}

	@Override
	public MessageUid getUid() {
		return MessageUid.of(uid);
	}

	@Override
	public HHId getMailboxId() {
		return (HHId) getMailbox().getMailboxId();
	}

	@Override
	public InputStream getFullContent() throws IOException {
		return new SequenceInputStream(getHeaderContent(), getBodyContent());
	}

	protected int getBodyStartOctet() {
		return bodyStartOctet;
	}

	@Override
	public long getBodyOctets() {
		return getFullContentOctets() - getBodyStartOctet();
	}

	@Override
	public Long getTextualLineCount() {
		return textualLineCount;
	}

	@Override
	public long getHeaderOctets() {
		return bodyStartOctet;
	}

	@Override
	public long getFullContentOctets() {
		return contentOctets;
	}

	@Override
	public String getMediaType() {
		return mediaType;
	}

	@Override
	public boolean isSeen() {
		return seen;
	}

	@Override
	public void setUid(MessageUid uid) {
		this.uid = uid.asLong();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getMailboxId().getRawId(), uid);
	}

	@Override
	public final Flags createFlags() {
		return FlagsFactory.createFlags(this, createUserFlags());
	}

	protected String[] createUserFlags() {
		return userFlags.stream()
				.map(HHUserFlag::getName)
				.toArray(String[]::new);
	}

	@Override
	public List<Property> getProperties() {
		return properties.stream()
				.map(HHProperty::toProperty)
				.collect(Guavate.toImmutableList());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AbstractHHMailBoxMessage) {
			AbstractHHMailBoxMessage other = (AbstractHHMailBoxMessage) obj;
			return Objects.equal(getMailboxId(), other.getMailboxId())
					&& Objects.equal(uid, other.getUid());
		}
		return false;
	}

	@Override
	public ComposedMessageIdWithMetaData getComposedMessageIdWithMetaData() {
		return ComposedMessageIdWithMetaData.builder()
				.modSeq(getModSeq())
				.flags(createFlags())
				.composedMessageId(new ComposedMessageId(mailbox.getMailboxId(), getMessageId(), MessageUid.of(uid)))
				.build();
	}

	@Override
	public List<MessageAttachment> getAttachments() {
		try {
			return new MessageParser().retrieveAttachments(getFullContent());
		} catch (MimeException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean hasAttachment() {
		return !getAttachments().isEmpty();
	}

	@Override
	public String toString() {
		return "message("
				+ "mailboxId = " + this.getMailboxId() + TOSTRING_SEPARATOR
				+ "uid = " + this.uid + TOSTRING_SEPARATOR
				+ "internalDate = " + this.internalDate + TOSTRING_SEPARATOR
				+ "answered = " + this.answered + TOSTRING_SEPARATOR
				+ "deleted = " + this.deleted + TOSTRING_SEPARATOR
				+ "draft = " + this.draft + TOSTRING_SEPARATOR
				+ "flagged = " + this.flagged + TOSTRING_SEPARATOR
				+ "recent = " + this.recent + TOSTRING_SEPARATOR
				+ "seen = " + this.seen + TOSTRING_SEPARATOR
				+ " )";
	}
}
