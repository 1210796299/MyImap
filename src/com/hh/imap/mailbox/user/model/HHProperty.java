package com.hh.imap.mailbox.user.model;

import org.apache.james.mailbox.store.mail.model.Property;

import java.util.Objects;

/**
 * @author oyx
 * @date 2020-01-17 10:12
 */
public class HHProperty {
	private long id;
	private int line;
	private String localName;
	private String namespace;
	private String value;

	public HHProperty() {
	}

	/**
	 * Constructs a property.
	 *
	 * @param localName not null
	 * @param namespace not null
	 * @param value     not null
	 */
	public HHProperty(String namespace, String localName, String value, int order) {
		super();
		this.localName = localName;
		this.namespace = namespace;
		this.value = value;
		this.line = order;
	}

	/**
	 * Constructs a property cloned from the given.
	 *
	 * @param property not null
	 */
	public HHProperty(Property property, int order) {
		this(property.getNamespace(), property.getLocalName(), property.getValue(), order);
	}

	@Override
	public final boolean equals(Object o) {
		if (o instanceof HHProperty) {
			HHProperty that = (HHProperty) o;

			return Objects.equals(this.id, that.id);
		}
		return false;
	}

	@Override
	public final int hashCode() {
		return Objects.hash(id);
	}

	public Property toProperty() {
		return new Property(namespace, localName, value);
	}

	/**
	 * Constructs a <code>String</code> with all attributes in name = value
	 * format.
	 *
	 * @return a <code>String</code> representation of this object.
	 */
	@Override
	public String toString() {
		return "HHProperty ( " + "id = " + this.id + " " + "localName = " + this.localName + " "
				+ "namespace = " + this.namespace + " " + "value = " + this.value + " )";
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public String getLocalName() {
		return localName;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
