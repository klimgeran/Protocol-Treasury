package org.github.klimgeran.discordsymbolprotocol.treasury;


public class TheBot {

	  public static void main(String[] args) {
	    DiscordClientBuilder builder = new DiscordClientBuilder("TOKEN HERE");
	    DiscordClient client = builder.build();

	    client.getEventDispatcher().on(ReadyEvent.class)
	        .subscribe(event -> {
	          User self = event.getSelf();
	          System.out.println(String.format("Logged in as %s#%s", self.getUsername(), self.getDiscriminator()));
	        });

	    client.getEventDispatcher().on(MessageCreateEvent.class)
	        .map(MessageCreateEvent::getMessage)
	        .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
	        .filter(message -> message.getContent().orElse("").equalsIgnoreCase("!Symbol"))
	        .flatMap(Message::getChannel)
	        .flatMap(channel -> channel.createMessage("letsgo!"))
	        .subscribe();

	    client.login().block();
	  }
	}