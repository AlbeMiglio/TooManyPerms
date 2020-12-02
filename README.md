# TooManyPerms
[![Build Status](https://travis-ci.com/AlbeMiglio/TooManyPerms.svg?branch=master)](https://travis-ci.com/AlbeMiglio/TooManyPerms)
[![License](https://img.shields.io/badge/license-GNU%20General%20Public%20License%20v3.0-brightgreen)](https://github.com/AlbeMiglio/TooManyPerms/blob/master/LICENSE)
![GitHub All Releases](https://img.shields.io/github/downloads/AlbeMiglio/TooManyPerms/total?color=brightgreen)
[![Discord](https://img.shields.io/discord/618742870035398684?logo=Join%20on%20Discord)](https://discord.gg/XuBvVG8)
[![](https://jitpack.io/v/AlbeMiglio/TooManyPerms.svg)](https://jitpack.io/#AlbeMiglio/TooManyPerms)
[![Rating](https://img.shields.io/spiget/rating/53086?label=Rating%20on%20SpigotMC)](http://www.spigotmc.org/resources/53086/)
[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=PXWGWLK6C4D2A&source=url)

## Import with Maven
- To hook this plugin into your project with Maven, you just need to add to your pom.xml the repositories and dependencies below:
```	
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>

	<dependency>
	    <groupId>com.github.AlbeMiglio</groupId>
	    <artifactId>TooManyPerms</artifactId>
	    <version>1.0.7</version>
	</dependency>
```
## Configuration files
These are the main configurations of the plugin. You can fully customize them as far as you prefer:
- Config File ([config.yml](src/main/resources/config.yml))
- Messages File ([messages.yml](src/main/resources/messages.yml))
- Permissions File ([permissions.yml](src/main/resources/permissions.yml))
- Punishments File ([punishments.yml](src/main/resources/punishments.yml))

## Developer API
The plugin provides 3 different events, which are called when a player has got something he shouldn't have.
  
- UnfairOpDetectedEvent: it's called when someone is OP and is not allowed.
```
@EventHandler  
public void onUnfairOpDetected(UnfairOpDetectedEvent event) {  
  Player player =  event.getPlayer();  
}  
```
  
- UnfairPermsDetectedEvent: it's called when someone has a certain perm and shouldn't.
```
@EventHandler  
public void onUnfairPermsDetected(UnfairPermsDetectedEvent event) {  
  Player player =  event.getPlayer();  
  String permission = event.getPermission();  
}  
```  
  
- UnfairGroupsDetectedEvent: it's called when someone is in a certain group and shouldn't.
```
@EventHandler  
public void onUnfairGroupsDetected(UnfairGroupsDetectedEvent event) {  
  Player player =  event.getPlayer();  
  String group = event.getGroup();  
}  
```
