package com.github.klimgeran.symbol.discord.treasury.event;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import discord4j.core.object.entity.Message;
import io.nem.symbol.sdk.api.AccountRepository;
import io.nem.symbol.sdk.api.RepositoryFactory;
import io.nem.symbol.sdk.infrastructure.vertx.RepositoryFactoryVertxImpl;
import io.nem.symbol.sdk.model.account.AccountInfo;
import io.nem.symbol.sdk.model.account.Address;
import io.nem.symbol.sdk.model.mosaic.ResolvedMosaic;
import reactor.core.publisher.Mono;

public abstract class MessageListener {
	
	private final String TREASURY = "!treasury";
	private final String SYMBOL = " symbol";
	private final String NEM = " nem";
	private final String ALL = " all";
	
   	
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
           .filter(message -> message.getContent().toLowerCase().startsWith(TREASURY))
           .flatMap(Message::getChannel)
           .flatMap(channel -> channel.createMessage(getAccountInfo(eventMessage.getContent().toLowerCase())))
           .then();
    }
    
    private String getAccountInfo(String msg) {
    	String subcommand=msg.substring(msg.lastIndexOf(TREASURY)+9);
    	if(SYMBOL.equals(subcommand)) {
    		return getSymbolAccountInfo();
    	} else if(NEM.equals(subcommand)) {
    		return getNEMAccountInfo();
    	} 
    	return getSymbolAccountInfo()+"\n\n\n"+
    			   getNEMAccountInfo();
    	
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
		     // + "\n"
		      + "- conrad.symbolnode.ninja\n"
		      + "- NIS2.host\n"
		      + "- XYM007.host";	
    }

    private String getNEMAccountInfo() {
    	BigInteger amount=BigInteger.ZERO;
    	WebClient client = WebClient.create();

    	WebClient.ResponseSpec responseSpec = client.get()
    	    .uri("http://88.99.87.132:7890/account/get?address=NCHESTYVD2P6P646AMY7WSNG73PCPZDUQNSD6JAK")
    	    .retrieve();
    	String responseBody = responseSpec.bodyToMono(String.class).block();

    	ObjectMapper mapper = new ObjectMapper();
    	try {
    	    JsonNode neoJsonNode = mapper.readTree(responseBody);
    	    amount=neoJsonNode.get("account").get("balance").bigIntegerValue();
    	    
    	} catch (JsonMappingException jme) {
    		jme.printStackTrace();
    	} catch (JsonProcessingException jpe) {
    		jpe.printStackTrace();
    	}
    	
    	return "NEM Treasure: \n\n Address: NCHESTYVD2P6P646AMY7WSNG73PCPZDUQNSD6JAK\n\nBalance:\n"
	        +getPrintableAmount(amount)
	        +" XEM"
		      + "\n\n" 
		      + "————————\n"
		      + "Support Our Symbol Nodes: \n"
		      //+ "\n"
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
