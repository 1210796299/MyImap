package com.hh.imap.mailbox.user.model;

import java.io.Serializable;

/**
 * @author oyx
 * @date 2020-01-10 14:33
 */
public class HHSubscription implements Serializable {
	private static final long serialVersionUID = -3999316539387118808L;
	private long id;
	private String username;
	private String mailbox;

	public HHSubscription() {
	}

	public HHSubscription(String username, String mailbox) {
		this.username = username;
		this.mailbox = mailbox;
	}

	public HHSubscription(long id, String username, String mailbox) {
		this.id = id;
		this.username = username;
		this.mailbox = mailbox;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getMailbox() {
		return mailbox;
	}

	public void setMailbox(String mailbox) {
		this.mailbox = mailbox;
	}

	@Override
	public String toString() {
		return "HHSubscription{" +
				"id=" + id +
				", username='" + username + '\'' +
				", mailbox='" + mailbox + '\'' +
				'}';
	}
}
