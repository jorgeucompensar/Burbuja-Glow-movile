# Configuración de ePayco sandbox

El checkout usa ePayco Smart Checkout v2 dentro de un `WebView`. La app abre la sesión con un `sessionId` creado por el backend; la aprobación real se confirma con el webhook y se consulta desde la app.

Ninguna credencial privada se incluye en la APK: `PUBLIC_KEY`, `PRIVATE_KEY`, `P_KEY` y `P_CUST_ID_CLIENTE` viven solo en el servidor.

## 1. Crear cuenta y credenciales

1. Crea una cuenta ePayco y entra al panel de **pruebas/sandbox**.
2. Copia `public_key` (P_CLI...), `private_key`, `p_key` y `cust_id`.
3. Registra la URL de confirmación del backend en el panel de ePayco si la plataforma lo solicita.

## 2. Backend

El backend está en `server/` y no requiere `npm install`, solo Node 18 o superior.

Variables en `server/.env`:

```text
PORT=3000
BASE_URL=https://tu-backend.onrender.com
EPAYCO_PUBLIC_KEY=P_CLI...
EPAYCO_PRIVATE_KEY=...
EPAYCO_P_KEY=...
EPAYCO_CUST_ID_CLIENTE=...
EPAYCO_TEST_MODE=true
GOOGLE_SERVICE_ACCOUNT_JSON=C:\ruta\segura\service-account.json
FIREBASE_PROJECT_ID=burbuja-glow-1d217
```

`BASE_URL` debe ser HTTPS públicamente accesible para que ePayco pueda llamar al webhook. En local puedes usar `ngrok`.

Endpoints:

```text
GET  /health
POST /api/payments/session
GET  /api/payments/verify?pedidoId=MAK-...
POST /api/payments/epayco/confirmation
GET  /api/payments/epayco/response?ref_payco=...
```

El endpoint de sesión recibe:

```json
{
  "pedidoId": "MAK-...",
  "compradorUid": "UID_DE_FIREBASE_AUTH",
  "items": [{ "productoId": "p_1", "cantidad": 2 }],
  "cliente": {
    "nombre": "María López",
    "email": "maria@email.com",
    "telefono": "3001234567",
    "direccion": "Calle 12 # 34-56",
    "ciudad": "Bogotá",
    "departamento": "Cundinamarca",
    "codigoPostal": "110111",
    "latitud": 4.7,
    "longitud": -74.1
  }
}
```

El servidor recalcula el total desde Firestore, guarda `pagos_pendientes/{pedidoId}` y responde:

```json
{ "sessionId": "id-devuelto-por-epayco", "pedidoId": "MAK-...", "amount": 159800 }
```

## 3. Webhook

El webhook valida la firma con:

```text
SHA256(custId^pKey^x_ref_payco^x_transaction_id^x_amount^x_currency_code)
```

Con una respuesta aprobada, el backend:
1. Crea `pedidos/{pedidoId}` con `estadoPago = "APROBADO"` e `items`.
2. Descuenta stock y aumenta `vendidos` en cada producto.
3. Marca `pagos/{transactionId}` para evitar reprocesos.

Con una respuesta rechazada, crea el pedido como cancelado. La app consulta `verify` cada 2 segundos hasta que el estado sea `APROBADO` o `RECHAZADO`; solo vacía el carrito cuando el backend confirma la aprobación.

## 4. App

En `local.properties`:

```properties
EPAYCO_SESSION_URL=https://tu-backend.onrender.com
EPAYCO_TEST_MODE=true
```

Después de cambiarlo, ejecuta un build limpio para regenerar `BuildConfig`. Si `EPAYCO_SESSION_URL` está vacío, la app muestra un error y no confirma el pedido ePayco.

## 5. Desplegar

En Render:

1. Nuevo **Web Service** apuntando a este repositorio.
2. Runtime: Node. Build command: vacío. Start command: `node index.js`.
3. Root directory: `server`.
4. Agrega las variables de entorno anteriores.
5. Copia la URL HTTPS resultante a `EPAYCO_SESSION_URL` en `local.properties`.

Para prueba local:

```powershell
cd server
$env:BASE_URL="https://abc123.ngrok-free.app"
node index.js
```
