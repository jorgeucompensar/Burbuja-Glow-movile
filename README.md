# Burbuja Glow - Aplicación Móvil (Android / Kotlin)

Aplicación móvil desarrollada en **Android Studio** utilizando XML, Kotlin y recursos centralizados, correspondiente a la **Actividad 4** del curso de Desarrollo de Aplicaciones Móviles Nativas de la Fundación Universitaria Compensar.

## Descripción del Proyecto
Burbuja Glow es una plataforma de comercio electrónico orientada al sector de belleza y cosméticos, estructurada bajo un modelo multi-rol para garantizar la gestión completa de procesos comerciales.

## Roles Implementados y Pantallas
1. Administrador
   - Dashboard principal de control.
   - Gestión de usuarios (Listado, creación, edición y eliminación).
   - Gestión y control de productos.
   - Reporte de ventas y estadísticas.

2. Vendedo
   - Panel de productos (CRUD completo).
   - Historial de pedidos recibidos.
   - Perfil de vendedor.

3. Comprador
   - Pantalla de inicio (*Splash Screen* / Bienvenida).
   - Catálogo general de productos.
   - Detalle de producto.
   - Carrito de compras y pasarela de pagos simulada.
   - Perfil de usuario y recuperación de contraseña.

Componentes Transversales y Técnicos
- Autenticación (incluyendo simulación biométrica).
- Navegación optimizada mediante Drawer / Bottom Navigation.
- Centralización estricta de estilos, *drawables*, colores y recursos en la carpeta `res/values`.
