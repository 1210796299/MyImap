package com.hh.imap.mailbox.mail;

import com.hh.imap.mailbox.HHId;
import com.hh.imap.mailbox.mail.model.HHMailBox;
import com.hh.imap.mailbox.mail.service.HHMailService;
import com.hh.imap.mailbox.mail.service.bean.Mail_Dir;
import org.apache.james.mailbox.ModSeq;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.Mailbox;
import org.apache.james.mailbox.model.MailboxId;
import org.apache.james.mailbox.store.mail.ModSeqProvider;

/**
 * @author oyx
 * @date 2020-01-09 15:39
 */
public class HHModSeqProvider implements ModSeqProvider {
	public HHModSeqProvider() {
	}

	@Override
	public ModSeq nextModSeq(Mailbox mailbox) throws MailboxException {
		HHId mailboxId = (HHId) mailbox.getMailboxId();
		return highestModSeq(mailboxId);
	}

	@Override
	public ModSeq nextModSeq(MailboxId mailboxId) throws MailboxException {
		return nextModSeq((HHId) mailboxId);
	}

	@Override
	public ModSeq highestModSeq(Mailbox mailbox) throws MailboxException {
		HHId mailboxId = (HHId) mailbox.getMailboxId();
		return highestModSeq(mailboxId);
	}

	@Override
	public ModSeq highestModSeq(MailboxId mailboxId) throws MailboxException {
		return highestModSeq((HHId) mailboxId);
	}

	private ModSeq nextModSeq(HHId mailboxId) throws MailboxException {
		Mailbox mailbox = new HHMailBoxMapper().findMailboxById(mailboxId);
		HHMailBox hhMailBox = new HHMailBox(mailbox);
		long modSeq = hhMailBox.consumeModSeq();
		return ModSeq.of(modSeq);
	}

	private ModSeq highestModSeq(HHId mailboxId) throws MailboxException {
		try {
			Mail_Dir mailDir = new HHMailBoxMapper().getOne((int) mailboxId.getRawId());
			HHMailService mailService = new HHMailService(mailDir.getTab());
			int maxId = mailService.findMaxId();
			return maxId > 0 ? ModSeq.of(maxId) : ModSeq.of(0);
		} catch (Exception e) {
			throw new MailboxException("Unable to get highest mod-sequence for mailbox " + mailboxId.serialize(), e);
		}
	}
}
