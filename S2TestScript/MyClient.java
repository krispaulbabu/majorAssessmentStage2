
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

			dout.write(("REDY\n").getBytes());
			dis.readLine();

			String allData = "";
			dout.write(("GETS All\n").getBytes());
			allData = dis.readLine();
			dout.write(("OK\n").getBytes());

			String[] allServers = new String[Integer.parseInt(allData.split(" ")[1])];
			for (int i = 0; i < allServers.length; i++) {
				allServers[i] = dis.readLine();
			}

			dout.write(("OK\n").getBytes());
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
							("GETS Avail " + jobsIndivData[4] + " " + jobsIndivData[5] + " " + jobsIndivData[6] + "\n").getBytes());
					data = dis.readLine();
					dout.write(("OK\n").getBytes());
				}

				else {
					dout.write(("REDY\n").getBytes());
					dis.readLine();
					continue;
				}

				int capableCalled = 0;

				if (data.split(" ")[1].equals("0")) {
					dis.readLine();
					dout.write(
							("GETS Capable " + jobsIndivData[4] + " " + jobsIndivData[5] + " " + jobsIndivData[6] + "\n").getBytes());
					data = dis.readLine();
					dout.write(("OK\n").getBytes());
					capableCalled = 1;
				}

				int serversPresentNo = Integer.parseInt(data.split(" ")[1]);

				String[] serverList = new String[serversPresentNo];
				for (int i = 0; i < serversPresentNo; i++) {
					serverList[i] = dis.readLine();
				}

				if (capableCalled == 1) {
					for (int i = 0; i < serverList.length; i++) {
						String server1Name = serverList[i].split(" ")[0];
						String server1Id = serverList[i].split(" ")[1];
						for (int j = 0; j < allServers.length; j++) {
							String server2Name = allServers[j].split(" ")[0];
							String server2Id = allServers[j].split(" ")[1];

							if (server1Name.equals(server2Name) && server1Id.equals(server2Id)) {
								serverList[i] = allServers[j];
							}
						}
						// System.out.println(serverList[i]);
					}
				}

				String bestServerName = "";
				String bestServerId = "";
				int bestServerCores = 0;

				bestServerName = "";
				bestServerId = "";
				bestServerCores = Integer.MAX_VALUE;

				for (int i = 0; i < serversPresentNo; i++) {
					String currentSserverName = serverList[i].split(" ")[0];
					String currentServerId = serverList[i].split(" ")[1];
					int currentServerCores = Integer.parseInt(serverList[i].split(" ")[4]);

					if (currentServerCores >= neededCores && currentServerCores <= bestServerCores) {
						bestServerName = currentSserverName;
						bestServerId = currentServerId;
						bestServerCores = currentServerCores;
						break;
					}
				}

				dout.write(("OK\n").getBytes());
				dis.readLine();

				// if (!bootingServerNames.isEmpty()) {
				// for (int i = 0; i < bootingServerNames.size(); i++) {
				// dout.write(("LSTJ " + bootingServerNames.get(i) + " " +
				// bootingServerIDs.get(i) + "\n").getBytes());
				// dis.readLine();
				// dout.write(("OK\n").getBytes());
				// dis.readLine();
				// dout.write(("OK\n").getBytes());
				// dis.readLine();
				// }
				// }

				if (jobsIndivData[0].equals("JOBN")) {
					String schedCommand = "SCHD " + jobId + " " + bestServerName + " " + bestServerId;
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
		} catch (

		Exception e) {
			System.out.println(e);
		}
	}
}