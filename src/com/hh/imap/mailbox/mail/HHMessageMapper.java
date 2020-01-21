package com.hh.imap.mailbox.mail;

import com.github.fge.lambdas.Throwing;
import com.github.steveash.guavate.Guavate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.hh.imap.HHMailUtil;
import com.hh.imap.mailbox.HHId;
import com.hh.imap.mailbox.mail.base.AbstractHHMailBoxMessage;
import com.hh.imap.mailbox.mail.model.HHMailBoxMessage;
import com.hh.imap.mailbox.mail.service.HHMailService;
import com.hh.imap.mailbox.mail.service.bean.Mail;
import com.hh.imap.mailbox.mail.service.bean.Mail_Dir;
import com.hh.wframe.service.attach.Attach;
import com.hh.wframe.service.attach.AttachService;
import com.hh.wframe.service.attach.AttachUtil;
import com.hh.wframe.util.PoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.james.mailbox.MessageUid;
import org.apache.james.mailbox.ModSeq;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.*;
import org.apache.james.mailbox.store.FlagsUpdateCalculator;
import org.apache.james.mailbox.store.mail.MessageMapper;
import org.apache.james.mailbox.store.mail.model.MailboxMessage;
import org.apache.james.mailbox.store.mail.utils.ApplicableFlagCalculator;
import org.apache.james.mailbox.store.transaction.NonTransactionalMapper;

import javax.mail.Flags;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author oyx
 * @date 2020-01-09 11:33
 */
public class HHMessageMapper extends NonTransactionalMapper implements MessageMapper {
	private static final int UNLIMIT_MAX_SIZE = -1;
	private static final int UNLIMITED = -1;
	private MessageUtils messageMetadataMapper;
	private HHMailBoxMapper hhMailBoxMapper = new HHMailBoxMapper();


	public HHMessageMapper() {
	}

	public HHMessageMapper(MessageUtils messageMetadataMapper) {
		this.messageMetadataMapper = messageMetadataMapper;
	}

	@Override
	public Iterator<MailboxMessage> findInMailbox(Mailbox mailbox, MessageRange set, FetchType fType, int max) throws MailboxException {
		try {
			List<MailboxMessage> results = null;
			MessageUid from = set.getUidFrom();
			final MessageUid to = set.getUidTo();
			final MessageRange.Type type = set.getType();
			HHId mailboxId = (HHId) mailbox.getMailboxId();
			Mail_Dir mailDir = getDirById((int) mailboxId.getRawId());
			switch (type) {
				case ALL:
					results = findMessagesInMailbox(mailDir);
					break;
				case FROM:
					results = findMessagesInMailboxAfterUID(mailDir, from);
					break;
				case ONE:
					results = findMessagesInMailboxWithUID(mailDir, from);
					break;
				case RANGE:
					results = findMessagesInMailboxBetweenUIDs(mailDir, from, to);
					break;
				default:
			}
			return results.iterator();
		} catch (Exception e) {
			throw new MailboxException("Search of MessageRange " + set + " failed in mailbox " + mailbox, e);
		}
	}

	@Override
	public List<MessageUid> retrieveMessagesMarkedForDeletion(Mailbox mailbox, MessageRange messageRange) throws MailboxException {
		return null;
	}

	@Override
	public long countMessagesInMailbox(Mailbox mailbox) throws MailboxException {
		HHId mailboxId = (HHId) mailbox.getMailboxId();
		return countMessagesInMailbox(mailboxId);
	}

	private long countMessagesInMailbox(HHId mailboxId) throws MailboxException {
		try {
			Mail_Dir mailDir = hhMailBoxMapper.getOne((int) mailboxId.getRawId());
			HHMailService hhMailService = new HHMailService(mailDir.getTab());
			return hhMailService.getCount();
		} catch (Exception e) {
			throw new MailboxException("Count of messages failed in mailbox " + mailboxId, e);
		}
	}

	@Override
	public long countUnseenMessagesInMailbox(Mailbox mailbox) throws MailboxException {
		HHId mailboxId = (HHId) mailbox.getMailboxId();
		return countUnseenMessagesInMailbox(mailboxId);
	}

	private long countUnseenMessagesInMailbox(HHId mailboxId) throws MailboxException {
		try {
			Mail_Dir mailDir = hhMailBoxMapper.getOne((int) mailboxId.getRawId());
			HHMailService hhMailService = new HHMailService(mailDir.getTab());
			return hhMailService.getCount(hhMailService.getMailListSql(-1, false));
		} catch (Exception e) {
			throw new MailboxException("Count of messages failed in mailbox " + mailboxId, e);
		}
	}

	@Override
	public MailboxCounters getMailboxCounters(Mailbox mailbox) throws MailboxException {
		return MailboxCounters.builder()
				.mailboxId(mailbox.getMailboxId())
				.count(countMessagesInMailbox(mailbox))
				.unseen(countUnseenMessagesInMailbox(mailbox))
				.build();
	}

	@Override
	public List<MailboxCounters> getMailboxCounters(Collection<Mailbox> mailboxes) throws MailboxException {
		return mailboxes.stream()
				.map(Throwing.<Mailbox, MailboxCounters>function(this::getMailboxCounters).sneakyThrow())
				.collect(Guavate.toImmutableList());
	}

	@Override
	public void delete(Mailbox mailbox, MailboxMessage mailboxMessage) throws MailboxException {

	}

	@Override
	public Map<MessageUid, MessageMetaData> deleteMessages(Mailbox mailbox, List<MessageUid> list) throws MailboxException {
		return null;
	}

	@Override
	public MessageUid findFirstUnseenMessageUid(Mailbox mailbox) throws MailboxException {
		HHId mailboxId = (HHId) mailbox.getMailboxId();
		Mail_Dir mailDir = null;
		try {
			mailDir = getDirById((int) mailboxId.getRawId());
			return findUnseenMessagesInMailboxOrderByUid(mailDir);
		} catch (Exception e) {
			throw new MailboxException("Search of first unseen message failed in mailbox " + mailbox, e);
		}
	}

	private MessageUid findUnseenMessagesInMailboxOrderByUid(Mail_Dir mailDir) throws Exception {
		HHMailService mailService = new HHMailService(mailDir.getTab());
		List<Mail> mailList = mailService.findUnseenMessagesInMailboxOrderByUid();
		if (mailList != null && mailList.size() > 0) {
			Mail mail = mailList.get(0);
			AttachService attachService = new AttachService(null);
			Attach one = attachService.getOne(mail.getEmlFileId());
			return createMessage(mailDir, mail, one).getUid();
		}
		return null;
	}

	@Override
	public List<MessageUid> findRecentMessageUidsInMailbox(Mailbox mailbox) throws MailboxException {
		HHId mailboxId = (HHId) mailbox.getMailboxId();
		try {
			Mail_Dir mailDir = getDirById((int) mailboxId.getRawId());
			HHMailService mailService = new HHMailService(mailDir.getTab());
			List<String> stringList = mailService.findRecentMessageUIDsInMailbox();
			return mailIdToMsgId(stringList);
		} catch (Exception e) {
			throw new MailboxException("Search of recent messages failed in mailbox " + mailbox, e);
		}
	}

	@Override
	public MessageMetaData add(Mailbox mailbox, MailboxMessage mailboxMessage) throws MailboxException {
		return null;
	}

	@Override
	public Iterator<UpdatedFlags> updateFlags(Mailbox mailbox, FlagsUpdateCalculator flagsUpdateCalculator, MessageRange set) throws MailboxException {
		Iterator<MailboxMessage> messages = findInMailbox(mailbox, set, FetchType.Metadata, UNLIMIT_MAX_SIZE);
		MessageUtils.MessageChangedFlags messageChangedFlags = messageMetadataMapper.updateFlags(mailbox, flagsUpdateCalculator, messages);

		for (MailboxMessage mailboxMessage : messageChangedFlags.getChangedFlags()) {
			try {
				save(mailbox, mailboxMessage);
			} catch (Exception e) {
				throw new MailboxException("updateFlags 失败:" + mailbox);
			}
		}
		return messageChangedFlags.getUpdatedFlags();
	}

	protected MessageMetaData save(Mailbox mailbox, MailboxMessage message) throws Exception {
		HHId mailboxId = (HHId) mailbox.getMailboxId();
		Mail_Dir mailDir = hhMailBoxMapper.getOne((int) mailboxId.getRawId());
		Mailbox currentMailbox = mailDir.toMailbox();
		if (message instanceof AbstractHHMailBoxMessage) {
			((AbstractHHMailBoxMessage) message).setMailbox(currentMailbox);
			updateMessage(mailDir, message);
			return message.metaData();
		} else {
			HHMailBoxMessage persistData = new HHMailBoxMessage(currentMailbox, message.getUid(), message);
			updateMessage(mailDir, message);
			return persistData.metaData();
		}
	}

	private void updateMessage(Mail_Dir mailDir, MailboxMessage message) throws Exception {
		HHMailService mailService = new HHMailService(mailDir.getTab());
		MessageUid uid = message.getUid();
		Mail one = mailService.getOne((int) uid.asLong());
		one.setSeen(message.isSeen());
		mailService.updateMail(one);
	}

	@Override
	public MessageMetaData copy(Mailbox mailbox, MailboxMessage mailboxMessage) throws MailboxException {
		return null;
	}

	@Override
	public MessageMetaData move(Mailbox mailbox, MailboxMessage mailboxMessage) throws MailboxException {
		return null;
	}

	@Override
	public Optional<MessageUid> getLastUid(Mailbox mailbox) throws MailboxException {
		return Optional.empty();
	}

	@Override
	public ModSeq getHighestModSeq(Mailbox mailbox) throws MailboxException {
		return new HHModSeqProvider().highestModSeq(mailbox);
	}

	@Override
	public Flags getApplicableFlag(Mailbox mailbox) throws MailboxException {
		try {
			HHId hhId = (HHId) mailbox.getMailboxId();
			return new ApplicableFlagCalculator(findMessagesInMailbox(getDirById((int) hhId.getRawId())))
					.computeApplicableFlags();
		} catch (Exception e) {
			throw new MailboxException("Search getApplicableFlag of recent messages failed in mailbox " + mailbox, e);
		}
	}

	@Override
	public Iterator<MessageUid> listAllMessageUids(Mailbox mailbox) throws MailboxException {
		Iterator<MailboxMessage> inMailbox = findInMailbox(mailbox, MessageRange.all(), FetchType.Full, UNLIMITED);
		return Iterators.transform(inMailbox, MailboxMessage::getUid);

	}

	@Override
	public void endRequest() {
		try {
			PoolUtil.put();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}


	private List<MailboxMessage> findMessagesInMailbox(Mail_Dir mailDir) throws Exception {
		HHMailService mailService = new HHMailService(mailDir.getTab());
		List<Mail> messagesInMailbox = mailService.findMessagesInMailbox();
		return mailToMessage(mailDir, messagesInMailbox);
	}

	private List<MailboxMessage> findMessagesInMailboxAfterUID(Mail_Dir mailDir, MessageUid from) throws Exception {
		HHMailService mailService = new HHMailService(mailDir.getTab());
		List<Mail> messagesInMailbox = mailService.findMessagesInMailboxAfterUID((int) from.asLong());
		return mailToMessage(mailDir, messagesInMailbox);
	}

	private List<MailboxMessage> findMessagesInMailboxWithUID(Mail_Dir mailDir, MessageUid from) throws Exception {
		HHMailService mailService = new HHMailService(mailDir.getTab());
		List<Mail> messagesInMailbox = mailService.findMessagesInMailboxWithUID((int) from.asLong());
		return mailToMessage(mailDir, messagesInMailbox);
	}

	private List<MailboxMessage> findMessagesInMailboxBetweenUIDs(Mail_Dir mailDir, MessageUid from, MessageUid to) throws Exception {
		HHMailService mailService = new HHMailService(mailDir.getTab());
		List<Mail> mailboxBetweenUIDs = mailService.findMessagesInMailboxBetweenUIDs((int) from.asLong(), (int) to.asLong());
		return mailToMessage(mailDir, mailboxBetweenUIDs);
	}

	private List<MailboxMessage> mailToMessage(Mail_Dir mailDir, List<Mail> messagesInMailbox) throws Exception {
		List<MailboxMessage> mailboxMessages = new ArrayList<>();
		List<Integer> idList = messagesInMailbox.stream().map(Mail::getEmlFileId).filter(id -> id > -1).collect(Collectors.toList());
		if (idList.size() > 0) {
			AttachService attachService = new AttachService(null);
			List<Attach> attachList = attachService.get(Attach.class, String.format("select * from %s where id in (%s)", "attach", StringUtils.join(idList, ",")));
			Map<Integer, Attach> attachMap = attachList.stream().collect(Collectors.toMap(Attach::getId, attach -> attach, (k1, k2) -> k1));
			messagesInMailbox.forEach(mail -> {
				MailboxMessage message = null;
				try {
					message = createMessage(mailDir, mail, attachMap.get(mail.getEmlFileId()));
				} catch (IOException | MailboxException e) {
					e.printStackTrace();
				}
				if (message != null) {
					mailboxMessages.add(message);
				}
			});
		}
		return mailboxMessages;
	}

	public Mail_Dir getDirById(int id) throws Exception {
		return hhMailBoxMapper.getOne(id);
	}

	public MailboxMessage createMessage(Mail_Dir mailDir, Mail mail, Attach eml) throws MailboxException, IOException {
		if (mailDir != null && mail != null && eml != null) {
			try (InputStream is = AttachUtil.read(eml)) {
				return HHMailUtil.createMailBoxMessage(mail, mailDir, is);
			}
		}
		return null;
	}

	private List<MessageUid> mailIdToMsgId(List<String> stringList) {
		ImmutableList.Builder<MessageUid> results = ImmutableList.builder();
		for (String result : stringList) {
			results.add(MessageUid.of(Long.parseLong(result)));
		}
		return results.build();
	}

	public static void main(String[] args) throws Exception {
		HHMessageMapper messageMapper = new HHMessageMapper();
		List<MailboxMessage> messageList = messageMapper.findMessagesInMailbox(messageMapper.getDirById((int) HHId.of(29).getRawId()));
		System.out.println(messageList);
	}

}
