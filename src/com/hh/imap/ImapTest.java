package com.hh.imap;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.james.imap.encode.main.DefaultImapEncoderFactory;
import org.apache.james.imap.main.DefaultImapDecoderFactory;
import org.apache.james.imapserver.netty.IMAPServerFactory;
import org.apache.james.metrics.MySystem;
import org.apache.james.metrics.logger.DefaultMetricFactory;
import org.apache.james.server.core.configuration.Configuration;
import org.apache.james.server.core.configuration.FileConfigurationProvider;
import org.apache.james.server.core.filesystem.FileSystemImpl;
import org.jboss.netty.util.HashedWheelTimer;

/**
 * @author oyx
 * @date 2020-01-03 14:26
 */

public class ImapTest {

	public static void main(String[] args) throws Exception {
		MySystem system = new MySystem();

		Configuration c = Configuration.builder()
				.workingDirectory("../")
				.configurationFromClasspath()
				.build();
		FileSystemImpl fileSystem = new FileSystemImpl(c.directories());
		HierarchicalConfiguration<ImmutableNode> configuration = new FileConfigurationProvider(fileSystem, c).getConfiguration("imapserver");
		DefaultMetricFactory metricFactory = new DefaultMetricFactory();
		system.setConfiguration(configuration);
		system.init();


		IMAPServerFactory serverFactory = new IMAPServerFactory(fileSystem, new DefaultImapDecoderFactory().buildImapDecoder(),
				new DefaultImapEncoderFactory().buildImapEncoder(), system.getDefaultImapProcessorFactory(), metricFactory, new HashedWheelTimer());
		serverFactory.configure(configuration);
		serverFactory.init();
//		imapServer.init();

	}

}
