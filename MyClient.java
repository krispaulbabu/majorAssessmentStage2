
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class MyClient {

	public static void main(String[] args) {
		try {

			Socket s = new Socket("localhost", 50000);
			DataOutputStream dout = new DataOutputStream(s.getOutputStream());
			BufferedReader dis = new BufferedReader(new InputStreamReader(s.getInputStream()));

			dout.write(("HELO\n").getBytes());
			dis.readLine();

			String username = System.getProperty("user.name");
			dout.write(("AUTH " + username + "\n").getBytes());
			dis.readLine();

			while (true) {

				dout.write(("REDY\n").getBytes());
				String jobs = (String) dis.readLine();

				if (jobs.equals("NONE")) {
					break;
				}

				String[] jobsIndivData = jobs.split(" ");
				int jobId = Integer.parseInt(jobsIndivData[2]);
				int neededCores = Integer.parseInt(jobsIndivData[4]);

				String data = "";
				if (!jobsIndivData[0].equals("JCPL")) {
					dout.write(
							("GETS Capable " + jobsIndivData[4] + " " + jobsIndivData[5] + " " + jobsIndivData[6] + "\n").getBytes());
					data = dis.readLine();
				}

				else {
					dout.write(("REDY\n").getBytes());
					dis.readLine();
					continue;
				}

				dout.write(("OK\n").getBytes());

				int serversPresentNo = Integer.parseInt(data.split(" ")[1]);

				ArrayList<String> bootingServerNames = new ArrayList<>();
				ArrayList<String> bootingServerIDs = new ArrayList<>();


				for (int i = 0; i < serversPresentNo - 1; i++) {
					String currentServer=dis.readLine();
					String currentServername= currentServer.split(" ")[0];
					String currentServerId=currentServer.split(" ")[1];
					String currentServerCores=currentServer.split(" ")[4];

					String nextServer= dis.readLine(); 
					String nextServerName= nextServer.split(" ")[0];
					String nextServerId=nextServer.split(" ")[1];
					String nextServerCores=nextServer.split(" ")[4];

					if(currentServerCores>nex){

					}

				}
					System.out.println("\n\n");

					System.out.println(minServerName + " " + minServerId + " " + minServerCores);
					System.out.println(neededCores);
					System.out.println("\n\n");

					dout.write(("OK\n").getBytes());
					dis.readLine();

					if (!bootingServerNames.isEmpty()) {
						for (int i = 0; i < bootingServerNames.size(); i++) {
							dout.write(("LSTJ " + bootingServerNames.get(i) + " " + bootingServerIDs.get(i) + "\n").getBytes());
							dis.readLine();
							dout.write(("OK\n").getBytes());
							dis.readLine();
							dout.write(("OK\n").getBytes());
							dis.readLine();
						}
					}

					if (jobsIndivData[0].equals("JOBN")) {
						String schedCommand = "SCHD " + jobId + " " + minServerName + " " + minServerId;
						dout.write((schedCommand + "\n").getBytes());
						dis.readLine();
					}

				}
				dout.write(("QUIT\n").getBytes());
				dis.readLine();
				dout.flush();
	
				dout.close();
				dis.close();
				s.close();
			}catch(

	Exception e)
	{
		System.out.println(e);
	}
}
}