import java.net.InetAddress;

public class NetworkChecker {

	
	public static void main(String[] args){
		
			

			boolean apiReachable = false;
			boolean cdnReachable = false;
			
			try{
				apiReachable = InetAddress.getByName("api-shop.genie.co.kr").isReachable(2*1000);
				
				if (apiReachable){
					System.out.println("\napi is reacheable!");
				}else{
					System.out.println("##############network is checking please...!!!!##############");
				}
				
				
				cdnReachable = InetAddress.getByName("shopstreaming.ktics.co.kr").isReachable(2*1000);
				
				if (cdnReachable){
					System.out.println("\ncdn is reacheable!");
				}else{
					System.out.println("##############network is checking please...!!!!##############");
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}
	}
}
