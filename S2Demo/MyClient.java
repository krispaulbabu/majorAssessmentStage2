
import java.io.*;
import java.net.*;

public class MyClient {

	public static void main(String[] args) {
		try {

			// Establish the socket connection between the client and the server
			Socket socket = new Socket("localhost", 50000);
			DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
			BufferedReader dis = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			dout.write(("HELO\n").getBytes());
			dis.readLine();

			String username = System.getProperty("user.name");
			dout.write(("AUTH " + username + "\n").getBytes());
			dis.readLine();

			dout.write(("REDY\n").getBytes());
			dis.readLine();

			// Uses the gets all command to get all the servers present.
			String allData = "";
			dout.write(("GETS All\n").getBytes());
			allData = dis.readLine();
			dout.write(("OK\n").getBytes());

			// Store all servers in an array called allServers
			String[] allServers = new String[Integer.parseInt(allData.split(" ")[1])];
			for (int i = 0; i < allServers.length; i++) {
				allServers[i] = dis.readLine();
			}

			dout.write(("OK\n").getBytes());
			dis.readLine();

			// Starting the while loop
			while (true) {

				dout.write(("REDY\n").getBytes());
				String jobs = (String) dis.readLine();

				// Break the loop if no jobs have been returned.
				if (jobs.equals("NONE")) {
					break;
				}

				// job recieved is split into an array and cores needed and the job id is
				// recorded
				String[] jobsIndivData = jobs.split(" ");
				int jobId = Integer.parseInt(jobsIndivData[2]);
				int neededCores = Integer.parseInt(jobsIndivData[4]);

				// Use the gets avail command to find out what servers are available for a
				// particular job. Also checks if the
				// Server has sent a "JCPL".
				String data = "";
				if (!jobsIndivData[0].equals("JCPL")) {
					dout.write(
							("GETS Avail " + jobsIndivData[4] + " " + jobsIndivData[5] + " " + jobsIndivData[6] + "\n").getBytes());
					data = dis.readLine();
					dout.write(("OK\n").getBytes());
				}

				// If the server has sent a JCPL, then the client skips the rest of the code and
				// restarts the loop
				else {
					dout.write(("REDY\n").getBytes());
					dis.readLine();
					continue;
				}

				// a boolean variable to record if the GETS Capable command has been called
				int capableCalled = 0;

				// If there are no servers present when using the GETS Avail command (all
				// servers are busy), then the client
				// uses the GETS Capable command, which returns a list of servers that will
				// eventually be ready.
				if (data.split(" ")[1].equals("0")) {
					dis.readLine();
					dout.write(
							("GETS Capable " + jobsIndivData[4] + " " + jobsIndivData[5] + " " + jobsIndivData[6] + "\n").getBytes());
					data = dis.readLine();
					dout.write(("OK\n").getBytes());
					capableCalled = 1;
				}

				// When GETS Capable is called, the initial cores are not returned. So the
				// client runs through the list of servers
				// returned by gets capable and changes it (finds the right core count) by
				// comparing it to the server list that was
				// returned by the GETS all command
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
					}
				}

				// Finds the server that can best suit the needs of the given job by making sure
				// the server has a higher core count
				// than the cores needed by the job and a lesser core count than any of the
				// other servers
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

				// Once the right server has been found the job is scheduled to this server
				if (jobsIndivData[0].equals("JOBN")) {
					String schedCommand = "SCHD " + jobId + " " + bestServerName + " " + bestServerId;
					dout.write((schedCommand + "\n").getBytes());
					dis.readLine();
				}

			}

			// When the loop is broken, the client sends a QUIT command.
			dout.write(("QUIT\n").getBytes());
			dis.readLine();
			dout.flush();

			dout.close();
			dis.close();
			socket.close();
		} catch (

		Exception e) {
			System.out.println(e);
		}
	}
}