package com.xspacesoft.kowax.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.xspacesoft.kowax.apis.SystemEvent;
import com.xspacesoft.kowax.apis.SystemEventsListener;
import com.xspacesoft.kowax.kernel.ShellPlugin;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.shell.CommandRunner;

public class Fortune extends ShellPlugin implements SystemEventsListener {
	
	private final static String[] PHRASES = {
		"All diseases run into one, old age. --Ralph Waldo Emerson",
		"Anger and intolerance are the enemies of correct understanding. --Mohandas Gandhi",
		"An unjust peace is better than a just war. --Cicerone",
		"I saw the angel in the marble and carved until I set him free. --Michelangelo",
		"I paint objects as I think them, not as I see them. --Pablo Picasso",
		"Dream as if you have forever. Live as if you only have today. --James Dean",
		"In dreams and in love there are no impossibilities. --Janos Arnay",
		"The secret of success is to know something nobody else knows. --Aristotle Onassis",
		"Misfortune shows those who are not really friends. --Aristotle Onassis",
		"Doubt is the father of invention. -Galileo Galilei",
		"Love comes unseen; we only see it go. --Austin Dobson",
		"You are the only person on earth who can use your ability. --Zig Ziglar",
		"A leader is one who knows the way, goes the way, and shows the way. --John C. Maxwell",
		"There is just one life for each of us: our own. --Euripide",
		"Be yourself; everyone else is already taken. --Oscar Wilde",
		"Everything has its beauty, but not everyone sees it. --Confucius",
		"All our dreams can come true, if we have the courage to pursue them. --Walt Disney"
	};
	
	private Integer lastRand = null;
	private List<String> usersBlacklist;

	@Override
	public SystemEvent[] getEvents() {
		return new SystemEvent[] {SystemEvent.USER_LOGIN};
	}

	@Override
	public void runIntent(SystemEvent event, String extraValue, CommandRunner commandRunner) {
		if (usersBlacklist == null) {
			// TODO Load blacklist form ./etc/Fortune/blacklist.cfg
			usersBlacklist = new ArrayList<String>();
		}
		switch(event) {
		case USER_LOGIN:
			if((extraValue==null)||(!usersBlacklist.contains(extraValue)))
				showFortune(commandRunner.getSocketHelper());
		default: return;
		}
	}

	@Override
	public String getAppletName() {
		return "Fortune";
	}

	@Override
	public String getAppletVersion() {
		return "1.0";
	}

	@Override
	public String getAppletAuthor() {
		return "Kowalski";
	}

	@Override
	protected void runApplet(String command, Stdio stdio, CommandRunner commandRunner) {
		if(command==null) {
			showFortune(stdio);
			return;
		}
		if (usersBlacklist == null) {
			// TODO Load blacklist form ./etc/Fortune/blacklist.cfg
			usersBlacklist = new ArrayList<String>();
		}	
		switch (command) {
		case "blacklist":
			usersBlacklist.add(commandRunner.getUsername());
			break;
		case "unblacklist":
			usersBlacklist.remove(commandRunner.getUsername());
			break;
		default:
			stdio.println(getHint());
			break;
		}
	}

	@Override
	public String getDescription() {
		return "Print a random, hopefully interesting, adage";
	}

	@Override
	public String getHint() {
		return "Usage: Fortune [blacklist|unblacklist]";
	}
	
	private void showFortune(Stdio stdio) {
		Random random = new Random();
		int newRandom = random.nextInt(PHRASES.length);
		if(lastRand!=null) {
			while(lastRand.intValue()==newRandom){
				newRandom = random.nextInt(PHRASES.length);
			}
		}
		String randomSentence = PHRASES[newRandom];
		lastRand = new Integer(newRandom);
		stdio.println(randomSentence);
		stdio.println();
	}
}
