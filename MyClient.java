import java.io.*;
import java.net.*;

public class MyClient {

	public static void main(String[] args) {
		try {

			// creating socket connection and initializing input and output streams
			Socket s = new Socket("localhost", 50000);
			DataOutputStream dout = new DataOutputStream(s.getOutputStream());
			BufferedReader dis = new BufferedReader(new InputStreamReader(s.getInputStream()));

			// first handshake. Sending hello messages and other standard responses like
			// Auth etc.,
			dout.write(("HELO\n").getBytes());
			// System.out.println((String) dis.readLine());
			dis.readLine();
			dout.flush();

			String username = System.getProperty("user.name");
			dout.write(("AUTH " + username + "\n").getBytes());
			String lastmessage = (String) dis.readLine();
			// System.out.println(lastmessage);
			dout.flush();

			// times represents the number of times the while loop has run. This variable is
			// used to stop running the GETS command after one run of the while loop.
			int times = 0;

			// Initializing this variable inside the while loop messes it up by setting it
			// to an empty string every time the loop runs. So I've kept it outside the
			// loop.
			String largestServer = "";

			int numberOfLargeServers = 0;
			int lastServerUsed = 0;

			// This is where the job scheduling happens. Standard commands like REDY and
			// GETS (which only runs once). Runs till there are no jobs left
			while (!(lastmessage.equals("NONE"))) {

				dout.write(("REDY\n").getBytes());
				String jobs = (String) dis.readLine();
				// System.out.println(jobs);
				dout.flush();

				// If the previous command yields NONE, the following lines of code will break
				// the loop
				if (jobs.equals("NONE")) {
					break;
				}

				// This splits the data recieved from the REDY command to get the jobId
				String[] jobsIndivData = jobs.split(" ");
				int jobId = Integer.parseInt(jobsIndivData[2]);

				// The following if statement checks if the code's already been run. In case it
				// has not, It asks for server info and finds out the largest server
				if (times < 1) {

					dout.write(("GETS ALL\n").getBytes());
					String data = dis.readLine();
					dout.flush();

					dout.write(("OK\n").getBytes());
					dout.flush();

					// Splits the data string to find the nRecs
					String[] indivData = data.split(" ");
					int nRecs = Integer.parseInt(indivData[1]);

					String servers = "";
					String[][] serversList = new String[nRecs][];
					int largestServerCores = 0;

					// Prints out server information and finds out the largest server
					for (int i = 0; i < nRecs; i++) {
						servers = (String) dis.readLine();
						// System.out.println(servers);

						String[] serverIndiv = servers.split(" ");
						serversList[i] = serverIndiv;

						if (Integer.parseInt(serverIndiv[4]) > largestServerCores) {
							largestServer = serverIndiv[0];
							largestServerCores = Integer.parseInt(serverIndiv[4]);

						}

					}

					// System.out.println(largestServer);

					// The following for loop finds out the number of servers present in the largest
					// server type.
					for (int i = 0; i < nRecs; i++) {
						if (serversList[i][0].equals(largestServer)) {
							numberOfLargeServers += 1;
						}
					}

					dout.write(("OK\n").getBytes());
					dis.readLine();
					dout.flush();
				}

				// Checks if the jobs data has the word JOBN in it. If it does, a job is
				// scheduled to the largest server.
				if (jobsIndivData[0].equals("JOBN")) {
					String schedCommand = "SCHD " + jobId + " " + largestServer + " " + lastServerUsed;
					// System.out.println(schedCommand);
					dout.write((schedCommand + "\n").getBytes());
					// System.out.println((String) dis.readLine() + "\n");
					dis.readLine();
					dout.flush();
					lastServerUsed += 1;
					if (lastServerUsed >= numberOfLargeServers) {
						// System.out.println("Server count has been reset");
						lastServerUsed = 0;
					}
				}

				times += 1;
			}

			dout.write(("QUIT\n").getBytes());
			// System.out.println((String) dis.readLine());
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
