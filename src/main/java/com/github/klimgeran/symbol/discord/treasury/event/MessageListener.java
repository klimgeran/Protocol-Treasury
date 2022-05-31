package com.github.klimgeran.symbol.discord.treasury.event;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;

import discord4j.core.object.entity.Message;
import io.nem.symbol.sdk.api.AccountRepository;
import io.nem.symbol.sdk.api.RepositoryFactory;
import io.nem.symbol.sdk.infrastructure.vertx.JsonHelperJackson2;
import io.nem.symbol.sdk.infrastructure.vertx.RepositoryFactoryVertxImpl;
import io.nem.symbol.sdk.model.account.AccountInfo;
import io.nem.symbol.sdk.model.account.Address;
import io.nem.symbol.sdk.model.mosaic.ResolvedMosaic;
import io.nem.symbol.sdk.model.transaction.JsonHelper;
import reactor.core.publisher.Mono;

public abstract class MessageListener {
   	
    public Mono<Void> processCommand(Message eventMessage) {
    	/* WebSocketClient client = new ReactorNettyWebSocketClient();
    	 client.execute(URI.create("ws://cola-potatochips:3001/ws"), 
    			 session -> session.receive().map(WebSocketMessage::getPayloadAsText).log().then());//.map(WebSocketMessage::getPayloadAsText).log();
    			 
         */        
         /*client.execute(
           URI.create("ws://cola-potatochips:3001/ws"), 
           session -> session.send(
             Mono.just(session.textMessage("event-spring-reactive-client-websocket")))
             .thenMany(session.receive()
               .map(WebSocketMessage::getPayloadAsText)
               .log())
             .then())
             .block(Duration.ofSeconds(10L));*/
    	
        return Mono.just(eventMessage)
           .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
           .filter(message -> message.getContent().equalsIgnoreCase("!symbol"))
           .flatMap(Message::getChannel)
           .flatMap(channel -> channel.createMessage(getSymbolAccountInfo()))
           .then();
    }
    
    private String getSymbolAccountInfo() {
    	String af = "";
    	BigInteger amount=BigInteger.ZERO;
    	try (final RepositoryFactory repositoryFactory = new RepositoryFactoryVertxImpl(
                "https://conrad.symbolnode.ninja:3001")) {
                final AccountRepository accountRepository = repositoryFactory
                    .createAccountRepository();

                // Replace with an address
                final String rawAddress = "NCHEST-3QRQS4-JZGOO6-4TH7NF-J2A63Y-A7TPM5-PXI";
                                           
                final Address address = Address.createFromRawAddress(rawAddress);
                final AccountInfo accountInfo = accountRepository
                    .getAccountInfo(address).toFuture().get();
                List<ResolvedMosaic> mosaics = accountInfo.getMosaics();
                
                for (ResolvedMosaic resolvedMosaic : mosaics) {
				  if(resolvedMosaic.getIdAsHex().equals("6BED913FA20223F8")) {
					  amount=resolvedMosaic.getAmount();
				  }
				}
                
                final JsonHelper helper = new JsonHelperJackson2();
                
                //af=helper.prettyPrint(accountInfo);
                //System.out.println(af);
                
            } catch (ExecutionException ee) {
            	ee.printStackTrace();
            } catch (InterruptedException ie) {
            	ie.printStackTrace();
            }
    	return "Symbol Treasure: \n\n Address: NCHEST3QRQS4JZGOO64TH7NFJ2A63YA7TPM5PXI\n\nBalance:\n"
              +getPrintableAmount(amount)
              +" XYM"
		      + "\n\n" 
		      + "————————\n"
		      + "Support Our Symbol Nodes: \n"
		      + "\n"
		      + "- conrad.symbolnode.ninja\n"
		      + "- NIS2.host\n"
		      + "- XYM007.host";	
    }
    
    private String getPrintableAmount(BigInteger amount) {
    	String printableAmount = "";
    	String decimalSeparator =".";
    	String thousandsSeparator = ",";
    	
    	java.lang.String samount = amount.toString();
    	int scale=6;
    	int th=(samount.length()-scale)/3;
    	for(int i=0; i<th;i++) {
    		System.out.println(samount.substring(samount.length()-scale-3-3*i, samount.length()-scale-3*i));
    		printableAmount=samount.substring(samount.length()-scale-3-3*i, samount.length()-scale-3*i)+
    				(i==0?"":thousandsSeparator)+printableAmount;
    	}
    	printableAmount=printableAmount+decimalSeparator+samount.substring(samount.length()-6, samount.length());
    	String mostSignificant=samount.substring(0,samount.length()-scale-3*th);
    	printableAmount=mostSignificant+("".equals(mostSignificant)?"":thousandsSeparator)+printableAmount;
    	return printableAmount;
    	
    }
}
