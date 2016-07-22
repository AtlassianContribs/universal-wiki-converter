package com.atlassian.uwc.ui;

import java.io.File;
import java.util.Date;

public class Attachment {

	private String name;
	private File file;
	private String user;
	private Date created;
	private String comment;

	public Attachment(String name, File file, String user, Long epoch, String comment) {
		this.name=name;
		this.file=file;
		this.user=user;
		if (epoch != null) this.created=new Date(epoch*1000);
		this.comment=comment;
	}

	public Attachment(File file) {
		this.file = file;
	}
	public Attachment(File file, String name) {
		this.file = file;
		this.name = name;
	}

	public String getName() {
		if (this.name != null) return this.name;
		return this.file.getName();
	}
	public void setName(String name) {
		this.name = name;
	}
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public String getUser()
	{
		return user;
	}

	public Date getCreated()
	{
		return created;
	}

	public String getComment()
	{
		return comment;
	}

}
