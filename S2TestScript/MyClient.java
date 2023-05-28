import java.io.*;
import java.net.*;

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
					dout.write(("REDY \n").getBytes());
					dis.readLine();
					continue;
				}

				dout.write(("OK\n").getBytes());

				int serversPresentNo = Integer.parseInt(data.split(" ")[1]);

				String serverRead = (String) dis.readLine();

				String minServerName = serverRead.split(" ")[0];
				int minServerCores = Integer.parseInt(serverRead.split(" ")[4]);
				int minServerId = Integer.parseInt(serverRead.split(" ")[1]);

				for (int i = 0; i < serversPresentNo - 1; i++) {
					serverRead = (String) dis.readLine();

					String currentServerName = serverRead.split(" ")[0];
					int currentServerCores = Integer.parseInt(serverRead.split(" ")[4]);
					int currentServerId = Integer.parseInt(serverRead.split(" ")[1]);

					if (currentServerCores >= neededCores && currentServerCores <= minServerCores) {
						minServerCores = currentServerCores;
						minServerName = currentServerName;
						minServerId = currentServerId;
					}
				}

				dout.write(("OK\n").getBytes());
				dis.readLine();

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
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}