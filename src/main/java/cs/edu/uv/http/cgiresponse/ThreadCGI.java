package cs.edu.uv.http.cgiresponse;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import org.slf4j.*;

import cs.edu.uv.http.common.UtilsHTTP;

/**
 * This class deals with the response when the URL corresponds to a CGI call
 * 
 * @author juan
 * 
 */
public class ThreadCGI implements Runnable {
	private static Logger LOG = LoggerFactory.getLogger(ThreadCGI.class.getName());
	private OutputStream out;
	private Socket canal;
	private String request;
	private String process;
	private String params[];
	private String cType;
	private String sInfo;
	private String tmpPath;

	public ThreadCGI(Socket s, String process, String params, String req, String sInfo, String tmpPath, String cType) {
		canal = s;
		request = req;
		try {
			out = s.getOutputStream();
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
		this.process = process;
		this.params = params.split(" ");
		this.cType = cType;
		this.sInfo = sInfo;
		this.tmpPath = tmpPath;
	}

	public void run() {
		try {

			LOG.info("Dealing with new request in ThreadCGI");

			String requestMethod = UtilsHTTP.getMethod(request);
			String resource = UtilsHTTP.getResource(request);

			ArrayList<String> cmdLine = new ArrayList<String>();
			cmdLine.add(process);

			InputStream in = canal.getInputStream();
			HashMap<String, String> paramsRequest = UtilsHTTP.getParams(in, resource, requestMethod);
			for (String p : params){
				String aux = paramsRequest.get(p);
				// Si no pasan el parámetro ponemos una cadena vacía
				// Esto puede hacer fallar al proceso...
				// Mejora: permitir que se pueda configurar qué parámetros son 
				// opcionales (y así no se añaden)
				if (aux!=null)
				   aux=URLDecoder.decode(aux, "UTF-8");
				else 
				   aux="";
				cmdLine.add(aux);				
			}
				
			UtilsCGI.callProcess(out, cmdLine, sInfo, tmpPath, cType);         


		} catch (Exception ex) {
			UtilsHTTP.writeResponseServerError(new PrintWriter(out), ex);
		} finally {
			try {
				out.flush();
				out.close();
				canal.close();
				LOG.debug("Response has been sent");
			} catch (Exception ex) {
				LOG.error("Excepción", ex);
			}
		}

	}
}
