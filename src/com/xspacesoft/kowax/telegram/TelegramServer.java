package com.xspacesoft.kowax.telegram;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.kowalski7cc.botclient.BotClient;
import com.kowalski7cc.botclient.Reciver;
import com.kowalski7cc.botclient.Sender;
import com.kowalski7cc.botclient.chat.Chat;
import com.kowalski7cc.botclient.chat.PrivateChat;
import com.kowalski7cc.botclient.types.Message;
import com.kowalski7cc.botclient.types.User;
import com.xspacesoft.kowax.Core;
import com.xspacesoft.kowax.Logwolf;
import com.xspacesoft.kowax.SystemFolder;
import com.xspacesoft.kowax.apis.PrivilegedAcces;
import com.xspacesoft.kowax.apis.Service;
import com.xspacesoft.kowax.apis.SystemEventsListener;
import com.xspacesoft.kowax.exceptions.MissingPluginCodeException;
import com.xspacesoft.kowax.kernel.PluginBase;
import com.xspacesoft.kowax.kernel.SystemEvent;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.kernel.io.InputReader;
import com.xspacesoft.kowax.kernel.io.OutputWriter;
import com.xspacesoft.kowax.kernel.io.Stdio;
import com.xspacesoft.kowax.shell.CommandRunner;
import com.xspacesoft.kowax.shell.CommandRunner.CommandNotFoundException;
import com.xspacesoft.kowax.shell.Session;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

public class TelegramServer extends PluginBase implements SystemEventsListener, PrivilegedAcces, Service {

	private TokenKey token;
	private TelegramBotShell client;
	private JSONObject configuration;
	private File filePath;
	private String username;
	private TelegramUsersManager telegramUsersManager;

	public TelegramServer() {
		username = null;
	}

	@Override
	public String getAppletName() {
		return "TelegramBot";
	}

	@Override
	public String getAppletVersion() {
		return "1.0";
	}

	@Override
	public String getAppletAuthor() {
		return "Kowalski7cc";
	}

	@Override
	protected void runApplet(String command, Stdio stdio, CommandRunner commandRunner) {
		if(command != null) {
			String[] commands = command.split("\\ ");
			switch(commands[0]) {
			case "settoken":
				if(!commandRunner.isSudo()) {
					stdio.println("You need to be superuser to do this");
					return;
				}
				stdio.print("Insert new token: ");
				String newToken = stdio.scan();
				if(!newToken.matches("([0-9])+(:{1})([A-Za-z0-9_-]{35})")) {
					stdio.println("Invalid token");
					return;
				}
				if(!new BotClient(newToken).isValid()) {
					stdio.print("New API key seems invalid. Save anyway? (y/[n]): ");
					String response = stdio.scan();
					if(!response.equalsIgnoreCase("y")) {
						stdio.println("Token not saved");
					}
				}
				try {
					configuration.put("api_key", newToken);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				saveConfig();
				break;
			case "getusername":
				stdio.println(username==null?
						"You are not connected to Telegram Bot Platform":"System username is @"
						+ username + " (Link: https://t.me/" + username + ")");

				break;
			case "authorize":
				if(!commandRunner.isSudo()) {
					stdio.println("You need to be superuser to do this");
				} else {
					stdio.print("Insert your token: ");
					String response = stdio.scan();
					try {
						this.telegramUsersManager.authorize(Integer.parseInt(response), commandRunner.getUsername());
						JSONObject newUser = new JSONObject();
						newUser.put("id", Long.parseLong(response));
						newUser.put("username", commandRunner.getUsername());
						if(!this.configuration.has("users")) {
							JSONArray usersArray = new JSONArray();
							this.configuration.put("users", usersArray);
						}				
						this.configuration.getJSONArray("users").put(newUser);
						saveConfig();
						stdio.println("User authorized!");
					} catch (NumberFormatException e) {
						stdio.println("Invalid ID");
						return;
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			case "deauthorize":
				if(!commandRunner.isSudo()) {
					stdio.println("You need to be superuser to do this");
				} else {
					try {
						if(!this.configuration.has("users")) {
							JSONArray usersArray = new JSONArray();
							this.configuration.put("users", usersArray);
						} else {
							JSONArray usersArray = this.configuration.getJSONArray("users");
							Long id = null;
							Integer index = null;
							for(int i = 0; i < usersArray.length(); i++) {
								JSONObject cUser = usersArray.getJSONObject(i);
								if(cUser.has("username")) {
									String cUserUsername = cUser.getString("username");
									if(cUserUsername.equals(commandRunner.getUsername())) {
										index = i;
										if(cUser.has("id")) {
											long cId = cUser.getLong("id");
											id = cId;
										}
									}
								}
							}
							if(index!=null)
								usersArray.remove(index);
							if(id!=null)
								telegramUsersManager.deauthorize(id);
							saveConfig();
						}
						saveConfig();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			default:
				stdio.println(printUsage());
			}
		} else {
			stdio.println(printUsage());
		}
	}

	private void saveConfig() {
		try (PrintWriter out = new PrintWriter(filePath)){
			out.print(configuration.toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String printUsage() {
		return "Usage: " + getAppletName() + " (settoken|getusername|authorize|deauthorize)";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SystemEvent[] getEvents() {
		return new SystemEvent[] {SystemEvent.SYSTEM_START};
	}

	@Override
	public void runIntent(SystemEvent event, String extraValue, CommandRunner commandRunner) {
		switch(event) {
		case SYSTEM_START:
			Core.getLogwolf().v("User: " + commandRunner.getUsername());
			break;
		default:
			break;
		}
	}

	@Override
	public void setTokenKey(TokenKey tokenKey) {
		this.token = tokenKey;
	}

	@Override
	public Boolean isServiceRunning() {
		return client==null?false:client.isAlive();
	}

	@Override
	public void startService() {
		Logwolf log = Core.getLogwolf();
		File etc = Core.getSystemFolder(SystemFolder.CONFIGURATIONS, null, token);
		File config = new File(etc, "TelegramBotServer");
		boolean folder = true;
		if(!config.exists() || !config.isDirectory())
			folder = config.mkdirs();
		if(!folder)
			return;
		File configData = new File(config, "config.json");
		this.filePath = configData;
		if(!configData.exists()) {
			log.e("[TelegramServer] - Can't find configuration data, creating new one");
			JSONObject data = new JSONObject();
			try {
				data.put("api_key", "Insert API KEY here");
				data.put("refresh_time", 1);
				data.put("group_support", false);
				JSONArray users = new JSONArray();
				data.put("users", users);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try (PrintWriter out = new PrintWriter(configData)){
				out.print(data.toString());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.e("[TelegramServer] - Please update configuration and restart service");
			this.configuration = data;
		} else {
			log.i("[TelegramServer] - Loading bot platform client configuration");
			StringBuilder stringBuilder = new StringBuilder();
			try(BufferedReader br = new BufferedReader(new FileReader(configData))) {
				String cLine = null;
				while((cLine = br.readLine()) != null) {
					stringBuilder.append(cLine);
				}
			} catch (FileNotFoundException e) {
				log.e("[TelegramServer] - Unknow error");
			} catch (IOException e) {
				log.e("[TelegramServer] - Error during configuration load: " + e.getMessage());
			}
			this.telegramUsersManager = new TelegramUsersManager();
			try {
				JSONObject data = new JSONObject(stringBuilder.toString());
				if(!data.has("api_key")) {
					log.e("[TelegramServer] - API Key is missing from configuration, add and restart service");
					return;
				}
				String api = data.getString("api_key");
				if(!api.matches("([0-9])+(:{1})([A-Za-z0-9_-]{35})")) {
					log.e("[TelegramServer] - Invalid API key");
					return;
				}
				int refresh_time = 1;
				if(data.has("refresh_time"))
				{
					refresh_time = data.getInt("refresh_time");
				}
				Boolean group_support = false;
				if(data.has("group_support")) {
					group_support = data.getBoolean("group_support");
				}
				if(data.has("users")) {
					JSONArray users = data.getJSONArray("users");
					if(users.length()>0) {
						for(int i = 0; i< users.length(); i++) {
							JSONObject cuser = users.getJSONObject(i);
							if(cuser.has("username")&&cuser.has("id")) {
								this.telegramUsersManager.authorize(
										cuser.getLong("id"),
										cuser.getString("username"));
							}
						}
					}
				}
				this.configuration = data;				
				TelegramBotShell telegramBotShell = new TelegramBotShell(token, api, refresh_time,
						group_support, this.telegramUsersManager);
				if(!telegramBotShell.isValid()) {
					log.e("[TelegramServer] - Invalid API Key or not connected to Telegram Platform. Try to relaunch");
					return;
				}
				User me = telegramBotShell.getMe();
				log.i("[TelegramServer] - Username @" + me.getUsername() + " with id " + me.getId());
				username = me.getUsername();
				client = telegramBotShell;
				telegramBotShell.start();
			} catch (JSONException e) {
				log.e("[TelegramServer] - Malformed JSON configuration");
				return;
			}
		}
	}

	@Override
	public void stopService() {
		if(isServiceRunning())
			client.interrupt();
	}

	@Override
	public String getServiceName() {
		return "TelegramBotService";
	}

	private class TelegramBotShell extends Thread {

		private BotClient botClient;
		private TelegramUsersManager telegramUsersManager;
		Logwolf log = Core.getLogwolf();

		public TelegramBotShell(TokenKey tokenKey, String API, int refresh,
				Boolean groups, TelegramUsersManager telegramUsersManager) {
			botClient = new BotClient(API);
			this.telegramUsersManager = telegramUsersManager;
		}

		public Boolean isValid() {
			return botClient.isValid();
		}

		public User getMe() {
			return botClient.getMe();
		}

		@Override
		public void run() {
			setName("TelegramBotClient");
			botClient.getReciver().startPolling();
			Reciver reciver = botClient.getReciver();
			while(!isInterrupted()) {
				while(!reciver.getIncomingQueue().isEmpty()) {
					Message message = reciver.getIncomingQueue().poll();
					User user = message.getFrom();
					Chat chat = message.getChat();
					boolean authorized = telegramUsersManager.isAuthorized(user.getId());
					if(!authorized) {
						botClient.getSender().sendText("Sorry, you are not authorized to use this service.", chat, null);
					} else {
						String username = telegramUsersManager.getUsernameById(user.getId());
						TelegramUserspace userspace = telegramUsersManager.getUserById(user.getId());
						if(userspace==null){
							try {
								userspace = telegramUsersManager.initialiseNewUserspace(user.getId(), username, botClient.getSender());
								botClient.getSender().sendText("Logged in with username " + username, chat, null);
								log.i("[TelegramServer] - User " + username + " connected");
								userspace.commandRunner.sendSystemEvent(SystemEvent.USER_LOGIN_SUCCESS, userspace.session.getUsername(), token, false);
							} catch (Exception e) {
								botClient.getSender().sendText("Error during userspace initialization: " + e.getMessage(), chat, null);
								telegramUsersManager.destroySession(user.getId());
								e.printStackTrace();
								log.i("[TelegramServer] - User " + username + " disconnected");
							}
						} else {
							if(message.getText()==null) {
								userspace.stdio.print("Sorry, unsupported media");
								continue;
							}
							
							if(message.getText().equals("/stop")) {
								userspace.session.setSessionActive(false);
							}
							if(userspace.session.isSessionActive()) {
								try {
									userspace.commandRunner.run(message.getText());
								} catch (IllegalArgumentException e) {
									// Empty line
								} catch (CommandNotFoundException e) {
									userspace.stdio.print("-shell: Command not found.");
								} catch (MissingPluginCodeException e) {
									userspace.stdio.print("-shell: Error launching applet: " + e.toString());
								}
							} else {
								telegramUsersManager.destroySession(user.getId());
								log.i("[TelegramServer] - User " + username + " disconnected");
							}
						}
						if(userspace.session.isSessionActive()) {
							if(userspace.session.isSudo())
								userspace.session.getSockethelper().print("root@kowax:-# ");
							else
								userspace.session.getSockethelper().print(userspace.session.getUsername() + "@kowax:-$ ");
						} else {
							telegramUsersManager.destroySession(user.getId());
							log.i("[TelegramServer] - User " + username + " disconnected");
							userspace.session.getSockethelper().print("logout");
						}
					}
				}
			}
		}
	}

	public class TelegramUsersManager {

		private Map<Long, TelegramUserspace> sessions;
		private Map<Long, String> authorizedUsers;

		public TelegramUsersManager() {
			sessions = new HashMap<Long, TelegramUserspace>();
			authorizedUsers = new HashMap<Long, String>();
		}

		public void destroySession(long id) {
			if(sessions.containsKey(id))
				sessions.remove(id);
		}

		public String getUsernameById(long id) {
			if(authorizedUsers.containsKey(id))
				return authorizedUsers.get(id);
			return null;
		}

		public TelegramUserspace getUserById(long id) {
			if(sessions.containsKey(id))
				return sessions.get(id);
			return null;
		}

		public TelegramUserspace initialiseNewUserspace(long id, String username, Sender sender) throws IOException {
			TelegramUserspace telegramUserspace = new TelegramUserspace(id, username, sender);
			sessions.put(id, telegramUserspace);
			return telegramUserspace;
		}

		public boolean isAuthorized(long id) {
			return authorizedUsers.containsKey(new Long(id));
		}

		public void authorize(long telegramId, String username) {
			authorizedUsers.put(telegramId, username);
		}

		public void deauthorize(long telegramId) {
			authorizedUsers.remove(telegramId);
		}

	}

	public class TelegramUserspace {
		private CommandRunner commandRunner;
		private Stdio stdio;
		private Session session;


		public TelegramUserspace(long destination, String username, Sender sender) throws IOException {
			TelegramWriter telegramWriter = new TelegramWriter(destination, sender);
			TelegramReader telegramReader = new TelegramReader();
			this.stdio = new Stdio(telegramWriter, telegramReader);
			session = new Session(stdio);
			session.setAuthenticated(true);
			session.setUsername(username);
			commandRunner = new CommandRunner(session, token, false);
		}
	}

	public class TelegramWriter implements OutputWriter {

		private long destination;
		private Sender sender;

		public TelegramWriter(long destination, Sender sender) {
			this.destination = destination;
			this.sender = sender;
		}

		@Override
		public void print(String string) {
			sender.sendText(string, new PrivateChat(destination, ""), null);

		}

		@Override
		public void print(int i) {
			print(Integer.toString(i));
		}

		@Override
		public void print(float f) {
			print(Float.toString(f));
		}

		@Override
		public void print(boolean b) {
			print(Boolean.toString(b));
		}

		@Override
		public void print(double d) {
			print(Double.toString(d));
		}

		@Override
		public void print(char c) {
			print(Character.toString(c));
		}

		@Override
		public void print(long l) {
			print(Long.toString(l));
		}

		@Override
		public void println() {
			print("");
		}

		@Override
		public void println(String string) {
			print(string);
		}

		@Override
		public void println(int i) {
			print(i);
		}

		@Override
		public void println(float f) {
			print(f);
		}

		@Override
		public void println(boolean b) {
			print(b);
		}

		@Override
		public void println(double d) {
			print(d);
		}

		@Override
		public void println(char c) {
			print(c);
		}

		@Override
		public void println(long l) {
			print(l);
		}

	}

	public class TelegramReader implements InputReader {

		@Override
		public String next() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String nextLine() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean hasNextLine() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Integer nextInt() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Float nextFloat() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Long nextLong() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Double nextDouble() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Character nextCharacter() {
			// TODO Auto-generated method stub
			return null;
		}

	}

}
