package com.hh.imap.mailbox.mail;

import com.hh.imap.mailbox.mail.service.bean.MailBoxEnum;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author oyx
 * @date 2020-01-08 14:42
 */
public class HHMailBoxUtil {
	/**
	 * 邮箱表 模式名
	 */
	public static final String MAIL_SCHEMA_NAME = "mail_box";
	/**
	 * 邮箱表前缀 如 box_1
	 */
	public static final String MAIL_BOX_TABLE_NAME = "box_";
	public static final String INBOX = "收件箱";
	public static final String OUTBOX = "已发送";
	public static final String DRAFTBOX = "草稿箱";
	public static final String DELBOX = "已删除";
	public static final String JUNKBOX = "垃圾箱";
	/**
	 * 获取默认邮箱名:Map<"INBOX", "收件箱">
	 */
	static public Map<String, String> boxNameMap = new HashMap<String, String>();
	static public Map<String, String> otherBoxNameMap = new HashMap<String, String>();

	static {
		for (MailBoxEnum boxEnum : MailBoxEnum.values()) {
			Field[] fields = HHMailBoxUtil.class.getFields();
			for (Field field : fields) {
				if (boxEnum.name().equalsIgnoreCase(field.getName())) {
					try {
						boxNameMap.put(boxEnum.name(), field.get(null).toString());
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static String formatMailBoxName(String name, boolean flag) {
		if (StringUtils.isNotBlank(name) && flag && !name.equalsIgnoreCase(MailBoxEnum.INBOX.name())) {
			return boxNameMap.get(name);
		} else if (!flag && !name.equalsIgnoreCase(MailBoxEnum.INBOX.name())) {
			Set<Map.Entry<String, String>> entrySet = boxNameMap.entrySet();
			for (Map.Entry<String, String> entry : entrySet) {
				if (name.contains(entry.getValue())) {
					return entry.getKey();
				}
			}
			if (name.toLowerCase().contains("sent")) {
				return MailBoxEnum.OUTBOX.name();
			}
			if (name.toLowerCase().contains("delete")) {
				return MailBoxEnum.DELBOX.name();
			}
			if (name.toLowerCase().contains("spam")) {
				return MailBoxEnum.JUNKBOX.name();
			}
		}
		return null;
	}


}
