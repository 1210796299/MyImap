package com.hh.imap.mailbox.user.model;

/**
 * @author oyx
 * @date 2020-01-17 10:06
 */
public class HHUserFlag {
	private long id;
	private String name;

	public HHUserFlag() {

	}

	/**
	 * Constructs a User Flag.
	 *
	 * @param name not null
	 */
	public HHUserFlag(String name) {
		super();
		this.name = name;
	}

	/**
	 * Constructs a User Flag, cloned from the given.
	 *
	 * @param flag not null
	 */
	public HHUserFlag(HHUserFlag flag) {
		this(flag.getName());
	}


	/**
	 * Gets the name.
	 *
	 * @return not null
	 */
	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
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
		final HHUserFlag other = (HHUserFlag) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Constructs a <code>String</code> with all attributes
	 * in name = value format.
	 *
	 * @return a <code>String</code> representation
	 * of this object.
	 */
	@Override
	public String toString() {
		return "HHUserFlag ( "
				+ "id = " + this.id + " "
				+ "name = " + this.name
				+ " )";
	}
}
