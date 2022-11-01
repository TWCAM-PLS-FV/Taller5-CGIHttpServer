package cs.edu.uv.http.cgiresponse;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import cs.edu.uv.http.common.UtilsHTTP;

/**
 * This class offers some helper static methods to execute a process and to
 * obtain the mappings from URL to a process from a file
 * 
 * @author Juan Gutierrez Aguado (Dep. Informatica, Univ. Valencia, Spain)
 */
public class UtilsCGI {
	private static Logger LOG = LoggerFactory.getLogger(UtilsCGI.class.getName());

	/**
	 * Execute a process a writes its output to an OutputStream
	 * 
	 * @param the
	 *                output of the process will be send to this output stream
	 * @param command
	 *                "program arg1 arg2 ... argN" as a List<String>
	 * @throws Exception
	 */
	public static void callProcess(OutputStream out, List<String> command, String sInfo, String tmpPath, String cType)
			throws Exception {
		// Allows to execute a command with arguments
		ProcessBuilder pb = new ProcessBuilder(command);
		LOG.info("Running process {}", command);
		Process p = pb.start();

		// This will allow to read from the output of the process
		InputStream in = p.getInputStream();

		// Write output of the process to temporal file
		String randomFileName = tmpPath + "/" + UUID.randomUUID().toString();
		FileOutputStream outTemp = new FileOutputStream(randomFileName);

		// The output of the process is sent to the client
		int reads = 0;
		byte[] buf = new byte[1400];
		int bytesSent = 0;
		while ((reads = in.read(buf, 0, buf.length)) != -1) {
			outTemp.write(buf, 0, reads);
			bytesSent = bytesSent + reads;
		}
		outTemp.flush();
		outTemp.close();
		in.close();

		boolean finish = p.waitFor(5, TimeUnit.MINUTES);

		int exitValue = p.exitValue();

		LOG.debug("Process {} exit value: {}", command.get(0), exitValue);

		
		PrintWriter pw = new PrintWriter(out);
		if (exitValue == 0) {
			// Send temporal file to client			
			UtilsHTTP.writeResponseLineOK(pw);

			HashMap<String, String> responseCabs = new HashMap<>();
			responseCabs.put("Content-Length", bytesSent + "");
			responseCabs.put("Content-Type", cType);
			responseCabs.put("Server", sInfo);
			responseCabs.put("Date", UtilsHTTP.getDate());

			UtilsHTTP.writeHeaders(pw, responseCabs);

			FileInputStream inTemp = new FileInputStream(randomFileName);
			while ((reads = inTemp.read(buf, 0, buf.length)) != -1) {
				out.write(buf, 0, reads);
				out.flush();
			}
			inTemp.close();
			out.flush();
			out.close();

			LOG.debug("Sent {} bytes to client", bytesSent);

			// Remove temporal file

			Files.delete(Path.of(randomFileName));
		} else {
			LOG.warn("Exit value of the process: {}", exitValue);
         UtilsHTTP.writeResponseServerError(pw, new RuntimeException("Something went wrong. Exit value of the process: " + exitValue));
		}

	   // Remove temporal file
	   Files.delete(Path.of(randomFileName));

		if (finish)
			LOG.debug("The process finished");
		else
			LOG.debug("The process did not finished in 5 minutes");
	}

}
