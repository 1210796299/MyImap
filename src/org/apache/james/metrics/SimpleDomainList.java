package org.apache.james.metrics;


import com.google.common.collect.ImmutableList;
import org.apache.james.core.Domain;
import org.apache.james.domainlist.api.DomainList;
import org.apache.james.domainlist.api.DomainListException;

import java.util.LinkedList;
import java.util.List;

/**
 * @author oyx
 * @date 2020-01-07 11:37
 * Simplest implementation for ManageableDomainList
 */
public class SimpleDomainList implements DomainList {

	private final List<Domain> domains = new LinkedList<>();

	@Override
	public boolean containsDomain(Domain domain) {
		return domains.contains(domain);
	}

	@Override
	public List<Domain> getDomains() {
		return ImmutableList.copyOf(domains);
	}

	@Override
	public void addDomain(Domain domain) throws DomainListException {
		if (domains.contains(domain)) {
			throw new DomainListException("Domain " + domain + " already exist");
		}
		domains.add(domain);
	}

	@Override
	public void removeDomain(Domain domain) throws DomainListException {
		if (!domains.remove(domain)) {
			throw new DomainListException("Domain " + domain + " does not exist");
		}
	}

	@Override
	public Domain getDefaultDomain() {
		return Domain.LOCALHOST;
	}
}
