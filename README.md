# Burbuja Glow - Aplicación móvil Android / Kotlin

Aplicación de comercio electrónico de belleza y cosméticos desarrollada con **Jetpack Compose**, Kotlin, Firebase Authentication/Firestore y checkout real de ePayco en modo sandbox. Corresponde a la Actividad 4 del curso de Desarrollo de Aplicaciones Móviles Nativas de la Fundación Universitaria Compensar.

**Repositorio público en GitHub:** [Burbuja-Glow-movile](https://github.com/jorgeucompensar/Burbuja-Glow-movile)

## Roles y funcionalidades

### Administrador
- Dashboard con métricas y reportes.
- Gestión de usuarios: alta, edición, desactivación y cambio de rol.
- Gestión de productos con imágenes por URL pública.
- Gestión de pedidos y estados.

### Vendedor
- Catálogo con CRUD remoto sobre Firestore.
- Pedidos asignados por `vendedorIds`.
- Perfil editable.

### Comprador
- Catálogo, filtros, detalle de producto y carrito sincronizado.
- Checkout con validación, ubicación opcional y dos métodos de pago:
  - **ePayco**: checkout hosted v2, tarjeta/PSE/Nequi según ePayco; el servidor valida el webhook y confirma el pedido.
  - **Contraentrega**: pedido confirmado y stock descontado en una transacción de Firestore.
- Perfil y recuperación de contraseña.

## Arquitectura

- `app/src/main/java/com/example/burbujaglow`: UI Compose, ViewModels, repositorios y modelos.
- `app/src/main/java/com/example/burbujaglow/data/remote`: sincronización y mappers de Firestore.
- `server`: backend Node sin dependencias externas para sesiones ePayco, webhook y verificación.
- `docs/FIREBASE_SETUP.md`: configuración de Firebase, roles, reglas e imágenes.
- `docs/EPAYCO_SETUP.md`: credenciales, variables de entorno, endpoints y despliegue.

La app nunca guarda tarjetas ni datos bancarios. Las llaves privadas de ePayco y la cuenta de servicio de Firebase viven solo en el backend.

## Configuración local

1. Copia `app/google-services.json` descargado de Firebase (no versionado).
2. Crea `local.properties` con `sdk.dir` y, para pagos:

```properties
EPAYCO_SESSION_URL=https://tu-backend.onrender.com
EPAYCO_TEST_MODE=true
```

3. Configura el backend con `server/.env` usando `server/.env.example` como plantilla.
4. Para builds firmados, configura también en `local.properties`:

```properties
RELEASE_STORE_FILE=C\:\\ruta\\segura\\burbujaglow-release.jks
RELEASE_STORE_PASSWORD=...
RELEASE_KEY_ALIAS=burbujaglow
RELEASE_KEY_PASSWORD=...
```

No versiones `google-services.json`, `local.properties`, `*.jks`, `server/.env` ni credenciales. Las credenciales de la demo se entregan por separado; para usar los alias `admin` y `vendedor` en una copia local, define `DEMO_ADMIN_EMAIL`, `DEMO_VENDOR_EMAIL`, `DEMO_ADMIN_PASSWORD` y `DEMO_VENDOR_PASSWORD` en `local.properties`.

## Build y verificación

```powershell
.\gradlew.bat assembleDebug --console=plain
.\gradlew.bat assembleRelease --console=plain
.\gradlew.bat testDebugUnitTest --console=plain
.\gradlew.bat lintDebug --console=plain
```

El APK debug queda en `app/build/outputs/apk/debug/app-debug.apk` y el release firmado en `app/build/outputs/apk/release/app-release.apk`.

## Documentación del código

Las clases y métodos relevantes incluyen comentarios KDoc en español que explican su propósito y su lógica principal. Se priorizaron autenticación, repositorios, mappers Firestore, pagos ePayco, ViewModels, navegación, checkout y el backend Node. Los comentarios están escritos para que una persona pueda seguir el flujo completo sin depender de conocimiento previo del proyecto.

## Modo sin Firebase

Si no existe `app/google-services.json`, `BuildConfig.FIREBASE_CONFIGURED` es `false` y la app usa un repositorio local en memoria con el catálogo demo y pagos simulados. Para el flujo real de ePayco se requiere Firebase y el backend configurado.
