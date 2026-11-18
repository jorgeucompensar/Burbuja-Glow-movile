# Configuración de Firebase

La app funciona sin Firebase en modo demo, pero para sincronizar sesión, catálogo, carrito y pedidos sigue estos pasos. Firebase Storage **no es necesario** y no se usa.

## 1. Crear proyecto

1. Crea un proyecto en Firebase Console.
2. Agrega una app Android con package `com.example.burbujaglow`.
3. Descarga `google-services.json` y cópialo en `app/google-services.json`.
4. El plugin `com.google.gms.google-services` se aplica automáticamente solo si ese archivo existe.

## 2. Authentication

1. En `Authentication → Sign-in method`, habilita **Correo electrónico/contraseña**.
2. Registra desde la app a comprador, vendedor o administrador.
3. Para crear el primer administrador:
   - Regístrate con rol **Administrador** y código `BURBUJA-ADMIN`, o
   - crea el usuario en Authentication y agrega `usuarios/{uid}` con `rol: "ADMIN"`, `estado: "Activo"`, `activo: true`.

Alias demo:
- `admin` → `admin@burbujaglow.com`
- `vendedor` → `vendedor@burbujaglow.com`

Contraseña temporal para usuarios creados por el administrador: `Burbuja123*`.

## 3. Firestore

1. Crea la base de datos en modo producción.
2. Publica las reglas de `firestore.rules`.
3. Colecciones usadas:
   - `usuarios/{uid}`
   - `productos/{productoId}`
   - `carritos/{uid}/items/{productoId}`
   - `pedidos/{numero}`
   - `config/{id}`
4. El catálogo inicial se siembra automáticamente desde el panel Admin/Vendedor con **Restaurar demo**, solo si `productos` está vacío.

## 4. Imágenes de productos

Las imágenes no se suben a Firebase Storage. Se guarda únicamente una URL pública en el campo `imagenUrl` de cada producto.

Repositorio de imágenes usado:

```text
https://github.com/jorgeucompensar/Burbuja-Glow-movile
```

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
