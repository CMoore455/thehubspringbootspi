package moore.christian.thehub;

import javax.persistence.EntityNotFoundException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


public class ExceptionHandling extends ResponseEntityExceptionHandler{
	
	@ExceptionHandler(value= {IllegalArgumentException.class})
	protected ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest req) {
		TheHubResponseError err = new TheHubResponseError("BAD ARGUMENT", "");
		return handleExceptionInternal(ex, err, new HttpHeaders(), HttpStatus.BAD_REQUEST, req);
	}
	
	@ExceptionHandler(value= {EntityNotFoundException.class})
	protected ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException ex, WebRequest req){
		TheHubResponseError err = new TheHubResponseError("ENTITY NOT FOUND", "");
		return handleExceptionInternal(ex, err, new HttpHeaders(), HttpStatus.NOT_FOUND, req);
		
	}
	
	@ExceptionHandler(value={NullPointerException.class})
	protected ResponseEntity<Object> handleEntityNullPointerException(EntityNotFoundException ex, WebRequest req){
		TheHubResponseError err = new TheHubResponseError("ENTITY NOT FOUND", "");
		return handleExceptionInternal(ex, err, new HttpHeaders(), HttpStatus.BAD_REQUEST, req);
	}
	
//	javax.persistence.EntityNotFoundException
	
	public static class TheHubResponseError {
		private String reasonCode;
		
		private String message;
		
		public TheHubResponseError() {}
		
		public TheHubResponseError(String reason, String msg) {
			this.setReasonCode(reason);
			this.setMessage(msg);
		}

		public String getReasonCode() {
			return reasonCode;
		}

		public void setReasonCode(String reasonCode) {
			this.reasonCode = reasonCode;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}
}

