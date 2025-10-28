package mprower.javaspark.api;

public class ErrorResponse {
        private final String errorCode;
        private final String message;
        private final String details; // opcional, puede ser null

        public ErrorResponse(String errorCode, String message) {
            this(errorCode, message, null);
        }

        public ErrorResponse(String errorCode, String message, String details) {
            this.errorCode = errorCode;
            this.message = message;
            this.details = details;
        }

        public String getErrorCode() { return errorCode; }
        public String getMessage() { return message; }
        public String getDetails() { return details; }
    }

