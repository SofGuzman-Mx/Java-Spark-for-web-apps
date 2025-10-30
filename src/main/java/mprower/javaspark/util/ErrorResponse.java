package mprower.javaspark.util;

/**
 * Un objeto de transferencia de datos (DTO) estándar para devolver mensajes de error
 * consistentes en la API.
 */
public class ErrorResponse {
    private final String errorCode;
    private final String message;

    public ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    // --- Getters ---
    // Estos métodos son esenciales para que librerías como Gson puedan
    // serializar este objeto a JSON correctamente.

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}