package com.hh.imap;

import com.hh.imap.mailbox.hh.mail.model.HHMailBoxMessage;
import com.hh.imap.mailbox.hh.mail.service.bean.Mail;
import com.hh.imap.mailbox.hh.mail.service.bean.Mail_Dir;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.TeeInputStream;
import org.apache.james.mailbox.MessageUid;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.store.mail.model.MailboxMessage;
import org.apache.james.mailbox.store.mail.model.impl.PropertyBuilder;
import org.apache.james.mailbox.store.streaming.CountingInputStream;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.message.DefaultBodyDescriptorBuilder;
import org.apache.james.mime4j.message.HeaderImpl;
import org.apache.james.mime4j.message.MaximalBodyDescriptor;
import org.apache.james.mime4j.stream.EntityState;
import org.apache.james.mime4j.stream.MimeConfig;
import org.apache.james.mime4j.stream.MimeTokenStream;
import org.apache.james.mime4j.stream.RecursionMode;
import org.apache.james.util.BodyOffsetInputStream;
import sun.security.action.GetPropertyAction;

import javax.mail.Flags;
import javax.mail.util.SharedFileInputStream;
import java.io.*;
import java.security.AccessController;
import java.util.Date;
import java.util.UUID;

/**
 * @author oyx
 */
public class HHMailUtil {

	private static final File tmpdir = new File(AccessController.doPrivileged(new GetPropertyAction("java.io.tmpdir")));

	private static final File imapPath;

	static {
		imapPath = new File(tmpdir.getAbsolutePath(), "HH_IMAP_TEMP");
		if (!imapPath.exists()) {
			imapPath.mkdirs();
		}
	}


	/**
	 * 将HHMail转换为MailboxMessage
	 *
	 * @param mail    邮件
	 * @param mailDir 邮箱文件夹
	 * @param msgIn   邮件源文件流
	 * @return
	 * @throws IOException
	 * @throws MailboxException
	 */
	public static MailboxMessage createMailBoxMessage(Mail mail, Mail_Dir mailDir, InputStream msgIn) throws IOException, MailboxException {
		File file = null;
		SharedFileInputStream contentIn = null;
		file = File.createTempFile("imap", ".msg", imapPath);
		try (FileOutputStream out = new FileOutputStream(file);BufferedOutputStream bufferedOut = new BufferedOutputStream(out); BufferedInputStream tmpMsgIn = new BufferedInputStream(new TeeInputStream(msgIn, bufferedOut)); BodyOffsetInputStream bIn = new BodyOffsetInputStream(tmpMsgIn)) {
			final	MimeTokenStream parser = getParser(bIn);
			readHeader(parser);
			final	MaximalBodyDescriptor descriptor = (MaximalBodyDescriptor) parser.getBodyDescriptor();
			final	MediaType mediaType = getMediaType(descriptor);
			final	PropertyBuilder propertyBuilder = getPropertyBuilder(descriptor, mediaType.mediaType, mediaType.subType);
			setTextualLinesCount(parser, mediaType.mediaType, propertyBuilder);

			consumeStream(bufferedOut, tmpMsgIn);
			int bodyStartOctet = getBodyStartOctet(bIn);

			final int size = (int) file.length();
			Flags flags = new Flags();
			if (mail.getSeen()) {
				flags.add(Flags.Flag.SEEN);
			}
			contentIn = new SharedFileInputStream(file);
			Date internalDate = new Date(mail.getSaveDate().getTime());
			HHMailBoxMessage hhMailBoxMessage = new HHMailBoxMessage(mailDir.toMailbox(), internalDate, size, flags, contentIn, bodyStartOctet, propertyBuilder);
			hhMailBoxMessage.setUid(MessageUid.of(mail.getId()));
			return hhMailBoxMessage;
		} catch (IOException | MimeException e) {
			throw new MailboxException("无法解析邮件:", e);
		} finally {
			if (contentIn != null) {
				contentIn.close();
			}
			if (file.exists()) {
				FileUtils.deleteQuietly(file);
			}
			file.deleteOnExit();
		}
	}


	public ByteArrayOutputStream parse(InputStream in) throws Exception {
		ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
		int ch;
		while ((ch = in.read()) != -1) {
			swapStream.write(ch);
		}
		return swapStream;
	}

	private static MimeTokenStream getParser(BodyOffsetInputStream bIn) {
		final MimeTokenStream parser = new MimeTokenStream(MimeConfig.PERMISSIVE, new DefaultBodyDescriptorBuilder());

		parser.setRecursionMode(RecursionMode.M_NO_RECURSE);
		parser.parse(bIn);
		return parser;
	}

	private static HeaderImpl readHeader(MimeTokenStream parser) throws IOException, MimeException {
		final HeaderImpl header = new HeaderImpl();

		EntityState next = parser.next();
		while (next != EntityState.T_BODY && next != EntityState.T_END_OF_STREAM && next != EntityState.T_START_MULTIPART) {
			if (next == EntityState.T_FIELD) {
				header.addField(parser.getField());
			}
			next = parser.next();
		}
		return header;
	}

	private static MediaType getMediaType(MaximalBodyDescriptor descriptor) {
		final String mediaTypeFromHeader = descriptor.getMediaType();
		if (mediaTypeFromHeader == null) {
			return new MediaType("text", "plain");
		} else {
			return new MediaType(mediaTypeFromHeader, descriptor.getSubType());
		}
	}

	private static PropertyBuilder getPropertyBuilder(MaximalBodyDescriptor descriptor, String mediaType, String subType) {
		final PropertyBuilder propertyBuilder = new PropertyBuilder();
		propertyBuilder.setMediaType(mediaType);
		propertyBuilder.setSubType(subType);
		propertyBuilder.setContentID(descriptor.getContentId());
		propertyBuilder.setContentDescription(descriptor.getContentDescription());
		propertyBuilder.setContentLocation(descriptor.getContentLocation());
		propertyBuilder.setContentMD5(descriptor.getContentMD5Raw());
		propertyBuilder.setContentTransferEncoding(descriptor.getTransferEncoding());
		propertyBuilder.setContentLanguage(descriptor.getContentLanguage());
		propertyBuilder.setContentDispositionType(descriptor.getContentDispositionType());
		propertyBuilder.setContentDispositionParameters(descriptor.getContentDispositionParameters());
		propertyBuilder.setContentTypeParameters(descriptor.getContentTypeParameters());
		// Add missing types
		final String codeset = descriptor.getCharset();
		if (codeset == null) {
			if ("TEXT".equalsIgnoreCase(mediaType)) {
				propertyBuilder.setCharset("us-ascii");
			}
		} else {
			propertyBuilder.setCharset(codeset);
		}
		final String boundary = descriptor.getBoundary();
		if (boundary != null) {
			propertyBuilder.setBoundary(boundary);
		}
		return propertyBuilder;
	}

	private static void setTextualLinesCount(MimeTokenStream parser, String mediaType, PropertyBuilder propertyBuilder) throws IOException, MimeException {
		EntityState next;
		if ("text".equalsIgnoreCase(mediaType)) {
			final CountingInputStream bodyStream = new CountingInputStream(parser.getInputStream());
			bodyStream.readAll();
			long lines = bodyStream.getLineCount();
			bodyStream.close();
			next = parser.next();
			if (next == EntityState.T_EPILOGUE) {
				final CountingInputStream epilogueStream = new CountingInputStream(parser.getInputStream());
				epilogueStream.readAll();
				lines += epilogueStream.getLineCount();
				epilogueStream.close();

			}
			propertyBuilder.setTextualLineCount(lines);
		}
	}


	private static void consumeStream(BufferedOutputStream bufferedOut, BufferedInputStream tmpMsgIn) throws IOException {
		byte[] discard = new byte[4096];
		while (tmpMsgIn.read(discard) != -1) {
			// consume the rest of the stream so everything get copied to
			// the file now
			// via the TeeInputStream
		}
		bufferedOut.flush();
	}

	private static int getBodyStartOctet(BodyOffsetInputStream bIn) {
		int bodyStartOctet = (int) bIn.getBodyStartOffset();
		if (bodyStartOctet == -1) {
			bodyStartOctet = 0;
		}
		return bodyStartOctet;
	}


	private static class MediaType {
		final String mediaType;
		final String subType;

		private MediaType(String mediaType, String subType) {
			this.mediaType = mediaType;
			this.subType = subType;
		}
	}

	public static Integer getUUIDInOrderId() {
		Integer orderId = UUID.randomUUID().toString().hashCode();
		orderId = orderId < 0 ? -orderId : orderId;
		return orderId;
	}

}
