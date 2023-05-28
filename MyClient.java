
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.stream.Stream;

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
			dis.readLine();

			String username = System.getProperty("user.name");
			dout.write(("AUTH " + username + "\n").getBytes());
			String lastmessage = (String) dis.readLine();

			// This is where the job scheduling happens. Standard commands like REDY and
			// GETS (which only runs once). Runs till there are no jobs left
			while (!(lastmessage.equals("NONE"))) {

				dout.write(("REDY\n").getBytes());
				String jobs = (String) dis.readLine();
				System.out.println(jobs);

				// If the previous command yields NONE, the following lines of code will break
				// the loop
				if (jobs.equals("NONE")) {
					break;
				}

				// This splits the data recieved from the REDY command to get the jobId
				String[] jobsIndivData = jobs.split(" ");
				int jobId = Integer.parseInt(jobsIndivData[2]);

				int neededCores = Integer.parseInt(jobsIndivData[4]);

				// The following if statement checks if the code's already been run. In case it
				// has not, It asks for server info and finds out the largest server

				dout.write(
						("GETS Capable " + jobsIndivData[4] + " " + jobsIndivData[5] + " " + jobsIndivData[6] + "\n").getBytes());
				String data = dis.readLine();

				System.out.println(data);

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

				// Checks if the jobs data has the word JOBN in it. If it does, a job is
				// scheduled to the largest server.

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