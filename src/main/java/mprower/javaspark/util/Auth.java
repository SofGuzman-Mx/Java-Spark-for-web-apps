package mprower.javaspark.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

public class Auth {

    // Asegurémonos de que la clave secreta y el emisor sean idénticos para generar y verificar.
    private static final String SECRET_KEY = "tu-clave-secreta-muy-larga-y-dificil";
    private static final String ISSUER = "ecommerce-api";
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET_KEY);

    public static String generateToken(int clienteId, String nombre) {
        try {
            long expirationTimeMillis = System.currentTimeMillis() + (24 * 60 * 60 * 1000); // 24 horas

            return JWT.create()
                    .withIssuer(ISSUER)
                    .withClaim("clienteId", clienteId)
                    .withClaim("nombre", nombre)
                    .withIssuedAt(new Date())
                    .withExpiresAt(new Date(expirationTimeMillis))
                    .sign(ALGORITHM);
        } catch (JWTCreationException exception){
            throw new RuntimeException("Error al crear el token JWT", exception);
        }
    }

    public static int verifyTokenAndGetId(String token) throws JWTVerificationException {
        // Creamos un verificador con exactamente el mismo algoritmo y emisor.
        JWTVerifier verifier = JWT.require(ALGORITHM)
                .withIssuer(ISSUER)
                .build();

        // verify() lanzará una excepción si la firma, el emisor o la fecha no son válidos.
        DecodedJWT decodedJWT = verifier.verify(token);

        return decodedJWT.getClaim("clienteId").asInt();
    }
}