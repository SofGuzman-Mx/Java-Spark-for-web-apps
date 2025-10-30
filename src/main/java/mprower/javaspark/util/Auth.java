package mprower.javaspark.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

/**
 * Clase de utilidad para manejar la creación y verificación de JSON Web Tokens (JWT).
 * Utilizada para la autenticación y autorización de usuarios.
 */
public class Auth {
    // IMPORTANTE: En una aplicación real, esta clave debe ser secreta y mucho más compleja.
    // Debería cargarse desde una variable de entorno o un archivo de configuración seguro, no estar hardcodeada.
    private static final String SECRET_KEY = "tu-clave-secreta-muy-larga-y-dificil";

    // El algoritmo utilizado para firmar el token. HMAC256 es una elección común y segura.
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET_KEY);

    // El "issuer" (emisor) identifica quién creó el token. Es una buena práctica para la validación.
    private static final String ISSUER = "ecommerce-api";

    /**
     * Genera un nuevo token JWT para un cliente autenticado.
     *
     * @param clienteId El ID único del cliente. Se incluirá en el token para identificar al usuario en futuras peticiones.
     * @param nombre El nombre del cliente. Se puede incluir para propósitos informativos.
     * @return una cadena de texto que representa el token JWT.
     */
    public static String generateToken(int clienteId, String nombre) {
        try {
            // La fecha de expiración se establece en 24 horas a partir del momento de la creación.
            long expirationTimeMillis = System.currentTimeMillis() + (24 * 60 * 60 * 1000); // 24 horas

            return JWT.create()
                    .withIssuer(ISSUER) // Emisor del token
                    .withClaim("clienteId", clienteId) // Payload: información personalizada (ID del cliente)
                    .withClaim("nombre", nombre) // Payload: información personalizada (nombre del cliente)
                    .withIssuedAt(new Date()) // Payload: fecha de emisión
                    .withExpiresAt(new Date(expirationTimeMillis)) // Payload: fecha de expiración
                    .sign(ALGORITHM); // Firma el token con nuestro algoritmo y clave secreta
        } catch (JWTCreationException exception){
            // Si algo sale mal durante la creación del token, lanzamos una excepción de runtime.
            throw new RuntimeException("Error al crear el token JWT", exception);
        }
    }

    /**
     * Verifica la validez de un token JWT y, si es válido, extrae el ID del cliente.
     * La verificación comprueba la firma, el emisor y la fecha de expiración.
     *
     * @param token El token JWT recibido en la cabecera de la petición (sin el prefijo "Bearer ").
     * @return El ID del cliente (clienteId) contenido dentro del token.
     * @throws JWTVerificationException si el token es inválido, ha sido manipulado o ha expirado.
     */
    public static int verifyTokenAndGetId(String token) throws JWTVerificationException {
        // Creamos un verificador configurado con el mismo algoritmo y emisor que usamos para crear el token.
        JWTVerifier verifier = JWT.require(ALGORITHM)
                .withIssuer(ISSUER)
                .build();

        // El método verify() decodifica y valida el token. Si algo falla, lanzará una excepción.
        DecodedJWT decodedJWT = verifier.verify(token);

        // Si la verificación es exitosa, extraemos el 'claim' (información) "clienteId" que guardamos previamente.
        return decodedJWT.getClaim("clienteId").asInt();
    }
}