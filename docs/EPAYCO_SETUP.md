# Configuración de ePayco sandbox

El checkout usa ePayco v2 dentro de un `WebView`. La app carga `https://checkout.epayco.co/checkout-v2.js` y abre la sesión con un `sessionId`.

> La creación de la sesión requiere credenciales server-to-server. La llave privada o el `apiKey` de ePayco **no** deben incluirse en la app ni en `local.properties`.

## 1. Obtener credenciales

1. Crea una cuenta ePayco y entra al panel de pruebas/sandbox.
2. Copia el `public_key` (P_CLI...) y el `cust_id`.
3. Mantén `test = true` para sandbox.

## 2. Endpoint de sesión

Implementa un endpoint propio, por ejemplo `POST https://tu-servidor.com/api/epayco/session`, que reciba:

```json
{
  "numeroPedido": "MAK-10482",
  "descripcion": "Pedido Burbuja Glow",
  "amount": 159800,
  "currency": "COP",
  "name": "María López",
  "email": "maria@email.com",
  "publicKey": "P_CLI...",
  "custId": "123456",
  "test": true
}
```

El servidor debe crear la sesión con las credenciales privadas de ePayco y responder:

```json
{ "sessionId": "id-devuelto-por-epayco" }
```

También se aceptan `id_session` o `session_id` como nombres del campo.

## 3. local.properties

```properties
EPAYCO_PUBLIC_KEY=P_CLI...
EPAYCO_CUST_ID=123456
EPAYCO_TEST_MODE=true
EPAYCO_SESSION_URL=https://tu-servidor.com/api/epayco/session
```

`local.properties` no se versiona. Después de cambiarlo, ejecuta un build limpio para regenerar `BuildConfig`.

## 4. Flujo en la app

1. Checkout valida datos de contacto y envío.
2. Al seleccionar **ePayco**, la app pide el `sessionId` al endpoint configurado.
3. Se abre el WebView con checkout v2.
4. El JavaScript avisa a la app con `AndroidPago.resultado("APROBADO" | "ERROR")`.
5. Con `APROBADO`, se guarda el pedido y se actualiza `estadoPago`.

Si `EPAYCO_SESSION_URL` o `EPAYCO_PUBLIC_KEY` están vacíos, la app muestra un error y no confirma el pedido.
