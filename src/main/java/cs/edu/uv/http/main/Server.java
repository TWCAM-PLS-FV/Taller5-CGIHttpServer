package cs.edu.uv.http.main;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cs.edu.uv.http.cgiresponse.ThreadCGI;
import cs.edu.uv.http.common.UtilsHTTP;
import cs.edu.uv.http.config.Configurator;

public class Server {
	private static Logger LOG = LoggerFactory.getLogger(Server.class.getName());

	public static final String SERVER_INFO = "Custom Dynamic Server";

	public static void main(String[] args) {
      // Read configuration from file or environment variables
		Configurator config = new Configurator(Arrays.asList("PORT","NTHREADS","PATH_REQUEST","PROCESS","PARAMS","TMP_DIR","CONTENT_TYPE","SERVER_INFO"),"config.ini",null);

		int nThreads = config.getIntProperty("NTHREADS", 50);
		int port = config.getIntProperty("PORT", 8080);
		String PATH_REQUEST = config.getRequiredProperty("PATH_REQUEST");
		String PROCESS = config.getRequiredProperty("PROCESS");
		String PARAMS = config.getRequiredProperty("PARAMS");
		String CONTENT_TYPE=config.getRequiredProperty("CONTENT_TYPE");
		String SERVER_INFO=config.getProperty("SERVER_INFO","Custom Dynamic HTTP Server");
		String TMP_DIR=config.getProperty("TMP_DIR","/tmp");

		// Requested resource is returned without the trailing "/" so I remove it from PATH
		if (PATH_REQUEST.startsWith("/"))
		   PATH_REQUEST=PATH_REQUEST.substring(1);

		ServerSocket s = null;
		try {
			s = new ServerSocket(port);

			ExecutorService ex = Executors.newFixedThreadPool(nThreads);

		   while (true) {
				try {
					Socket canal = s.accept();

					System.out.println("---- New request from : " + canal.getInetAddress().toString());

					InputStream in = canal.getInputStream();
					String request = UtilsHTTP.readLine(in);
					System.out.println("------- " + request);
					String rec = UtilsHTTP.getResource(request);
					LOG.debug("Resource requested: {}", rec);

					System.out.println(PATH_REQUEST);
					// Serve the request if it matches the configured PATH
               if (rec.startsWith(PATH_REQUEST)){
						ex.execute(new ThreadCGI(canal, PROCESS, PARAMS, request,SERVER_INFO, TMP_DIR, CONTENT_TYPE));
					}else{
						UtilsHTTP.writeResponseNotFound(new PrintWriter(canal.getOutputStream()), rec);
					}
				} catch (IOException exc) {
					System.err.println("Error processing request...");
				}
			}
		} catch (Exception excp) {
         excp.printStackTrace();
			try {
				if (s != null)
				   s.close();
			} catch (IOException e) {

			}
		}
	}
}
