package com.hh.imap.mailbox.mail.model;

import com.hh.imap.mailbox.HHId;
import org.apache.james.core.Username;
import org.apache.james.mailbox.model.Mailbox;
import org.apache.james.mailbox.model.MailboxPath;

import java.util.Objects;

/**
 * @author oyx
 * @date 2020-01-08 16:16
 */
public class HHMailBox {
	private long mailboxId;
	private String name;
	private long uidValidity;
	private String user;
	private String namespace;
	private long lastUid;
	private long highestModSeq;
	private String tab;

	public static HHMailBox from(Mailbox mailbox) {
		return new HHMailBox(mailbox);
	}

	public HHMailBox(MailboxPath path, long uidValidity) {
		this.name = path.getName();
		this.user = path.getUser().asString();
		this.namespace = path.getNamespace();
		this.uidValidity = uidValidity;
	}

	public HHMailBox(Mailbox mailbox) {
		this(mailbox.generateAssociatedPath(), mailbox.getUidValidity());
	}

	public Mailbox toMailbox() {
		MailboxPath path = new MailboxPath(namespace, Username.of(user), name);
		return new Mailbox(path, uidValidity, new HHId(mailboxId));
	}

	public long getMailboxId() {
		return mailboxId;
	}

	public void setMailboxId(long mailboxId) {
		this.mailboxId = mailboxId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getUidValidity() {
		return uidValidity;
	}

	public void setUidValidity(long uidValidity) {
		this.uidValidity = uidValidity;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getTab() {
		return tab;
	}

	public void setTab(String tab) {
		this.tab = tab;
	}

	public long consumeUid() {
		return ++lastUid;
	}

	public long consumeModSeq() {
		return ++highestModSeq;
	}

	@Override
	public final boolean equals(Object o) {
		if (o instanceof HHMailBox) {
			HHMailBox that = (HHMailBox) o;
			return Objects.equals(this.mailboxId, that.mailboxId);
		}
		return false;
	}

	@Override
	public String toString() {
		return "HHMailBox{" +
				"mailboxId=" + mailboxId +
				", name='" + name + '\'' +
				", uidValidity=" + uidValidity +
				", user='" + user + '\'' +
				'}';
	}

	@Override
	public final int hashCode() {
		return Objects.hash(mailboxId);
	}
}
