# Balance — Aplicación de Gestión de Finanzas Personales

Aplicación web para organizar, visualizar y planificar finanzas personales: gestión de múltiples cuentas y monedas, fondos y metas de ahorro, registro de ingresos, gastos y transferencias, movimientos recurrentes automáticos, métricas visuales y sistema de suscripción (**Gratuito** y **Premium**).

---

## Arquitectura y Tecnologías

El proyecto está dividido en dos módulos independientes dentro de la carpeta raíz `balance/`:

```text
balance/
├── backend/    # API REST en Java 21 + Spring Boot 4.1.2 + Spring Security (JWT) + JPA/Hibernate
└── frontend/   # Interfaz web en HTML5, CSS3 y JavaScript Vanilla
```

### Stack técnico
- **Backend:** Java 21, Spring Boot 4.1.2, Spring Web MVC, Spring Data JPA, Spring Security, JJWT `0.13.0`.
- **Base de datos:** MySQL 8+ (`balance_db`).
- **Frontend:** HTML5, CSS3, JavaScript Vanilla (ES6+), servido en puerto `5500`.
- **Zona horaria estandarizada:** `America/Argentina/Buenos_Aires` (JVM, Jackson, Hibernate y frontend).

---

## Requisitos Previos

Antes de ejecutar la aplicación, asegurate de tener instalado:

1. **Java JDK 21** (verificar con `java -version`).
2. **Maven 3.9+** o el wrapper incluido (`./mvnw` / `mvnw.cmd`).
3. **MySQL Server 8.0+** corriendo en `localhost:3306`.
4. **VS Code con la extensión Live Server** (o cualquier servidor estático local en el puerto `5500`).

---

## Paso 1: Configurar la Base de Datos (MySQL)

1. Iniciá el servicio de MySQL en tu máquina.
2. Conectate a MySQL (por consola o MySQL Workbench) y creá la base de datos vacía:

```sql
CREATE DATABASE balance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

> **Nota sobre tablas y datos iniciales:** No hace falta ejecutar scripts SQL manuales. Al iniciar el backend por primera vez, Hibernate (`spring.jpa.hibernate.ddl-auto=update`) crea automáticamente todas las tablas y `DataInitializer` precarga:
> - **Monedas:** `ARS`, `USD`, `EUR`, `MXN`, `CLP`, `COP`.
> - **Planes:** `GRATIS` ($0) y `PREMIUM` ($4.999/mes · $36.000/año).
> - **Categorías globales:** 9 de gastos y 7 de ingresos.

---

## Paso 2: Configurar las Variables de Entorno

El backend utiliza **3 variables de entorno obligatorias** definidas en `backend/src/main/resources/application.properties` para no exponer credenciales ni claves secretas en el código fuente:

| Variable | Descripción | Ejemplo |
| :--- | :--- | :--- |
| `DB_USERNAME` | Usuario de conexión a MySQL | `root` |
| `DB_PASSWORD` | Contraseña del usuario de MySQL | `tu_contraseña_mysql` |
| `JWT_SECRET` | Clave secreta para firmar tokens JWT (**mínimo 32 caracteres / 256 bits**) | `BalanceClaveSecretaSuperSeguraJWT2026!` |

> **Importante sobre `JWT_SECRET`:** `jjwt 0.13.0` exige una clave HMAC-SHA256 de al menos **32 caracteres**. Si la clave es más corta, el inicio de sesión fallará con `WeakKeyException`.

### Opción A — Configurarlas desde IntelliJ IDEA (recomendado)
1. Abrí `Run > Edit Configurations...`.
2. Seleccioná la configuración de `BalanceBackendApplication`.
3. En el campo **Environment variables**, agregá:
   ```text
   DB_USERNAME=root;DB_PASSWORD=tu_contraseña_mysql;JWT_SECRET=BalanceClaveSecretaSuperSeguraJWT2026!
   ```
4. Guardá los cambios con **Apply** y **OK**.

### Opción B — Configurarlas desde la terminal antes de ejecutar Maven

**En Windows (PowerShell):**
```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="tu_contraseña_mysql"
$env:JWT_SECRET="BalanceClaveSecretaSuperSeguraJWT2026!"
```

**En Windows (CMD):**
```cmd
set DB_USERNAME=root
set DB_PASSWORD=tu_contraseña_mysql
set JWT_SECRET=BalanceClaveSecretaSuperSeguraJWT2026!
```

**En Linux / macOS (Bash / Zsh):**
```bash
export DB_USERNAME="root"
export DB_PASSWORD="tu_contraseña_mysql"
export JWT_SECRET="BalanceClaveSecretaSuperSeguraJWT2026!"
```

---

## Paso 3: Ejecutar el Backend

1. Entrá a la carpeta `backend`:
   ```bash
   cd backend
   ```
2. Ejecutá la aplicación con Maven (usando la misma terminal donde definiste las variables de entorno, o desde tu IDE):
   ```bash
   # Linux / macOS
   ./mvnw spring-boot:run

   # Windows
   .\mvnw.cmd spring-boot:run
   ```
3. Verificá en la consola que el servidor levante sin errores en **`http://localhost:8080`**.

---

## Paso 4: Ejecutar el Frontend

1. Abrí la carpeta `frontend` en **Visual Studio Code**.
2. Asegurate de tener instalada la extensión **Live Server** (Ritwick Dey).
3. Hacé clic derecho sobre `frontend/htmls/cuenta.html` (o sobre `frontend/index.html`, que redirige automáticamente a `cuenta.html` si no hay sesión iniciada) y seleccioná **"Open with Live Server"**.
4. El navegador abrirá la aplicación en **`http://127.0.0.1:5500`** o **`http://localhost:5500`**.

> **Importante sobre CORS:** `SecurityConfig.java` permite peticiones únicamente desde `http://127.0.0.1:5500` y `http://localhost:5500`. Si abrís los archivos HTML con doble clic (`file://`) o en otro puerto, el navegador bloqueará las llamadas a la API.

---

## Funcionalidades Principales y Reglas de Negocio

- **Cuentas y Patrimonio:** Soporte multimoneda con conversión automática a la moneda base del usuario. Al eliminar una cuenta, su saldo se descuenta del patrimonio y de los fondos asignados, conservando el historial de movimientos.
- **Fondos y Metas de Ahorro:** Cada usuario cuenta con un fondo **General** automático y puede crear fondos personalizados o metas con monto objetivo, moneda propia y fecha límite. Al eliminar un fondo o meta, su saldo regresa automáticamente al fondo **General**.
- **Movimientos y Recurrencia:** Registro de ingresos, gastos y transferencias entre cuentas/fondos. Los movimientos recurrentes (`DIARIA`, `SEMANAL`, `MENSUAL`) admiten fecha de fin y fechas de excepción, y se generan automáticamente tanto por tarea programada (`@Scheduled` a las `00:05` hora Argentina) como al consultar el historial.
- **Planes de Suscripción:**
  - **Plan Gratuito:** Hasta **3 metas de ahorro**, hasta **3 movimientos recurrentes** e historial de movimientos de los **últimos 3 meses**.
  - **Plan Premium:** Metas ilimitadas, movimientos recurrentes ilimitados e **historial completo**.

---

## Solución de Problemas Frecuentes

- **`Access denied for user` / error de conexión a MySQL:** Verificá que `DB_USERNAME` y `DB_PASSWORD` coincidan con tu usuario local de MySQL y que la base `balance_db` exista.
- **Error `WeakKeyException` al iniciar sesión:** `JWT_SECRET` tiene menos de 32 caracteres. Asignale una cadena de 32 o más caracteres y reiniciá el backend.
- **Error de CORS en la consola del navegador:** Asegurate de estar accediendo desde `http://127.0.0.1:5500` o `http://localhost:5500` con Live Server y no abriendo el `.html` directamente desde el explorador de archivos.
