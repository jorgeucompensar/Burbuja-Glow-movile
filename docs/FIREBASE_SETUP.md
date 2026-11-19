# Configuración de Firebase

La app funciona sin Firebase en modo demo, pero para sincronizar sesión, catálogo, carrito y pedidos sigue estos pasos. Firebase Storage **no es necesario** y no se usa.

## 1. Crear proyecto

1. Crea un proyecto en Firebase Console.
2. Agrega una app Android con package `com.example.burbujaglow`.
3. Descarga `google-services.json` y cópialo en `app/google-services.json`.
4. El plugin `com.google.gms.google-services` se aplica automáticamente solo si ese archivo existe.

## 2. Authentication

1. En `Authentication → Sign-in method`, habilita **Correo electrónico/contraseña**.
2. Crea el primer administrador manualmente:
   1. En `Authentication → Users → Add user`, usa `burbujaglow18@gmail.com` y una contraseña segura.
   2. Copia el UID generado.
   3. En `Firestore Database → Data → usuarios`, crea un documento con ese UID como ID y estos campos:

```text
id: 1
nombre: "Administrador"
correo: "burbujaglow18@gmail.com"
telefono: ""
rol: "ADMIN"
estado: "Activo"
uid: "<UID>"
usuarioAlias: "admin"
direccion: ""
ciudad: "Bogotá"
latitud: 0
longitud: 0
```

3. El registro público desde la app solo permite `COMPRADOR` y `VENDEDOR`; un administrador autenticado puede crear usuarios desde el panel.
4. Los roles canónicos son `ADMIN`, `VENDEDOR` y `COMPRADOR`.

Alias de inicio de sesión:
- `admin` → `burbujaglow18@gmail.com`
- `vendedor` → `vendedor@burbujaglow.com`

La contraseña es la que definas en Firebase Authentication; las contraseñas `admin12345` y `vendedor12345` solo aplican al modo demo sin Firebase.

## 3. Firestore

1. Crea la base de datos en modo producción.
2. Publica el contenido final de `firestore.rules` en `Firestore Database → Rules`.
3. Las reglas exigen sesión activa, usan `usuarios/{uid}.rol` como fuente de autoridad, permiten crear perfiles propios con `COMPRADOR` o `VENDEDOR` y dejan la gestión de usuarios a `ADMIN`.
4. Colecciones usadas:
   - `usuarios/{uid}`
   - `productos/{productoId}`
   - `carritos/{uid}/items/{productoId}`
   - `pedidos/{numero}`
   - `config/{id}`
5. El catálogo inicial se siembra automáticamente al entrar al panel Admin si `productos` está vacío. También puedes forzarlo con **Restaurar demo**, que solo escribe si la colección está vacía para no sobrescribir cambios existentes.

## 4. Imágenes de productos

Las imágenes no se suben a Firebase Storage. Se guarda únicamente una URL pública en el campo `imagenUrl` de cada producto.

Repositorio de imágenes usado:

```text
https://github.com/jorgeucompensar/Burbuja-Glow-movile
```

Los primeros 8 productos del catálogo demo ya usan URLs Raw de este repositorio. Los 16 restantes usan la imagen incluida en la APK como respaldo local: Firestore guarda `imagenUrl` vacía para esos productos y la app conserva su `drawable` por `id` al sincronizar.

Para crear una URL:

1. Sube la imagen al repositorio, por ejemplo en la raíz o en `imagenes-productos/`.
2. Abre el archivo en GitHub.
3. Haz clic en **Raw**.
4. Copia la URL; debe empezar por `raw.githubusercontent.com`.

Ejemplo válido:

```text
https://raw.githubusercontent.com/jorgeucompensar/Burbuja-Glow-movile/main/Balacas.png
```

También puedes pegar el enlace normal de GitHub con `?raw=true`; la app lo convierte automáticamente a Raw.

Requisitos:

- El repositorio debe ser público.
- La rama debe ser `main`.
- Usa imágenes JPG, PNG o WebP por HTTPS.
- Si cambias el nombre o la ruta del archivo, actualiza la `imagenUrl` del producto.

## 5. Verificación

```powershell
.\gradlew.bat assembleDebug --console=plain
.\gradlew.bat testDebugUnitTest --console=plain
.\gradlew.bat lintDebug --console=plain
```

Si no hay `google-services.json`, `BuildConfig.FIREBASE_CONFIGURED` es `false` y la app usa el repositorio local en memoria.
