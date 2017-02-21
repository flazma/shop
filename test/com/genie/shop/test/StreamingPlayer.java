package com.genie.shop.test;

import java.net.URL;
import java.net.URLConnection;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

public class StreamingPlayer {

   public static void main(String[] args) {    
	   
	   
	
	
	   
	   //basic auth
	   String basicAuth = Base64.encodeBase64String("sngPCApp@prod:aad2242c-252a-48ab-88f2-20c058dbe606".getBytes());
	   
	  /* HttpClient client = new HttpClient();
	   
	   AuthPolicy.registerAuthScheme(SecretAuthScheme.NAME, SecretAuthScheme.class);
	   
	   HttpParams params = DefaultHttpParams.getDefaultParams();        
		ArrayList schemes = new ArrayList();
		schemes.add(SecretAuthScheme.NAME);
		schemes.addAll((Collection) params.getParameter(AuthPolicy.AUTH_SCHEME_PRIORITY));
		params.setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, schemes);
	    	        */
	   
	  /* CredentialsProvider provider = new BasicCredentialsProvider();
	   UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("user1", "user1Pass");
	   provider.setCredentials(AuthScope.ANY, credentials);
	   HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
	    
	   HttpResponse response = client.execute(new HttpGet(URL_SECURED_BY_BASIC_AUTHENTICATION));
	   int statusCode = response.getStatusLine().getStatusCode();
	   assertThat(statusCode, equalTo(HttpStatus.SC_OK));
	   */
	   
	   
       Player player = null;
       //String domain = "http://shopstreaming.ktics.co.kr/m/surl:b0bb75b19e6dc041fb8bd01618664120e99e04180fc04595bef7d529b89bfce8faded20f22610c4187d3d6e637d8f125ca3722001d8d9a766b2d2506662a4773?token=a630d84a6ae276277597f8fda60ced04&expr=58245598&sid=86160555&cid=86160555&Cashtype=R&calltype=s&uno=shopstreaming&redirection=no&type=app&block=1";
       String domain = "http://shopstreaming.ktics.co.kr/m/surl%3A390d99f756ee7e6dda957526ce37c603ebb2042225ab9e2c9f83772506b73bba3eb31ded480c8e8d74243217829dcaaaca3722001d8d9a766b2d2506662a4773?token=7e177613c69e4778c72c3f19f0a28422&expr=58246073&sid=86550432&cid=86550432&Cashtype=R&calltype=s&uno=shopstreaming&redirection=no&type=app&block=1";
       try {
          //FileInputStream fis = new FileInputStream("C:\\WEB-Mornings.mp3");
           //BufferedInputStream bis = new BufferedInputStream(fis);
          
          URLConnection urlConnection = new URL ( domain ).openConnection ();
          urlConnection.connect ();
          
           player = new Player(urlConnection.getInputStream ());
       } catch (Exception e) {
           System.out.println(e.getMessage());
       }

       try {
           player.play();
       } catch (JavaLayerException e) {
           System.out.println(e.getMessage());
       }   
   }
   
   
}
