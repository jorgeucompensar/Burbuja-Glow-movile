# Backend de pagos Burbuja Glow

Servidor Node sin dependencias externas que crea sesiones de ePayco Smart Checkout, valida el webhook y escribe el resultado en Firestore.

## Variables de entorno

Copia `.env.example` y completa:

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

`EPAYCO_PRIVATE_KEY`, `EPAYCO_P_KEY` y el JSON de service account nunca se incluyen en la app ni en Git.

## Endpoints

- `GET /health`
- `POST /api/payments/session`
- `GET /api/payments/verify?pedidoId=MAK-...`
- `POST /api/payments/epayco/confirmation`
- `GET /api/payments/epayco/response?ref_payco=...`

## Ejecutar local

```powershell
$env:BASE_URL="https://abc123.ngrok-free.app"
$env:EPAYCO_PUBLIC_KEY="P_CLI..."
$env:EPAYCO_PRIVATE_KEY="..."
$env:EPAYCO_P_KEY="..."
$env:EPAYCO_CUST_ID_CLIENTE="..."
$env:GOOGLE_SERVICE_ACCOUNT_JSON="C:\ruta\segura\service-account.json"
node index.js
```

## Desplegar en Render

1. Crea un servicio Web Service desde este repositorio.
2. Runtime: Node. Build command: vacío. Start command: `node index.js`.
3. Agrega las variables de entorno anteriores en Render.
4. Usa la URL HTTPS de Render como `EPAYCO_SESSION_URL` en `local.properties`.
