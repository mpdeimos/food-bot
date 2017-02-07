package com.mpdeimos.foodbot;

import org.aeonbits.owner.Config;

public interface AppConfig extends Config
{
	@Key("SLACK_CHANNEL")
	@DefaultValue("#general")
	public String slackChannel();

	@Key("SLACK_URL")
	public String slackUrl();

	@Key("PORT")
	@DefaultValue("8080")
	public int serverPort();
}
