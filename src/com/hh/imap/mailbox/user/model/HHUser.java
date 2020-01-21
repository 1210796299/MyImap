package com.hh.imap.mailbox.user.model;

import com.hh.frame.common.util.EncryptUtil;
import org.apache.james.core.Username;
import org.apache.james.user.api.model.User;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;

/**
 * @author oyx
 * @date 2020-01-08 13:44
 */
public class HHUser implements User, Serializable {
	private static final long serialVersionUID = -9050744917742661411L;
	private final Username userName;
	private String hashedPassword;

	public HHUser(Username userName) {
		this.userName = userName;
	}

	public HHUser(Username name, String hashedPassword) {
		this.userName = name;
		this.hashedPassword = hashedPassword;
	}

	@Override
	public Username getUserName() {
		return userName;
	}

	@Override
	public boolean verifyPassword(String pass) {
		try {
			String hashGuess = EncryptUtil.md5(pass);
			return hashedPassword.equalsIgnoreCase(hashGuess);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Security error: " + e);
		}
	}

	@Override
	public boolean setPassword(String newPass) {
		try {
			hashedPassword = EncryptUtil.md5(newPass);
			return true;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Security error: " + e);
		}
	}
	public String getHashedPassword() {
		return hashedPassword;
	}
}
