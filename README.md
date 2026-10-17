# Reto 2 — Aplicación de conversión de divisas

Aplicación móvil desarrollada en **Kotlin y Jetpack Compose** que permite a los usuarios autenticarse, consultar y convertir divisas, registrar operaciones y consultar su historial. También incluye funcionalidades de perfil de usuario y auditoría de operaciones para el administrador.

## 📱 Capturas de pantalla

En esta sección se muestran las principales pantallas de la aplicación. Para agregar las imágenes, guárdalas dentro de la carpeta `screenshots` del proyecto utilizando los nombres indicados.

### 1. Inicio de sesión

<img width="1917" height="1020" alt="image" src="https://github.com/user-attachments/assets/99927b0e-337b-4def-9aa5-e8beedb32b3b" />


### 2. Pantalla principal y conversión de divisas
<img width="1917" height="1018" alt="image" src="https://github.com/user-attachments/assets/c474e6b4-2264-4a28-b55c-64aedb4cd588" />



### 3. Perfil de usuario
<img width="1917" height="1021" alt="image" src="https://github.com/user-attachments/assets/1d6b040b-72ef-493f-8857-3d721e473352" />



### 4. Historial de operaciones
<img width="1917" height="1020" alt="image" src="https://github.com/user-attachments/assets/7a38412f-54ef-4a9c-afdf-0c759a9b23e2" />



### 5. Panel de administración y auditoría
<img width="1917" height="1013" alt="image" src="https://github.com/user-attachments/assets/bc89a49f-3988-4256-bdc2-69fa0eeb4eb5" />

### 6. Base de datos firebase donde se alamcena el usuario y las operaciones
<img width="1917" height="971" alt="image" src="https://github.com/user-attachments/assets/d333fac4-b620-42f1-b48c-5174df5c1029" />


<img width="1917" height="967" alt="image" src="https://github.com/user-attachments/assets/e08273b4-fb99-43ff-a0cb-15070d93743c" />

### 7. Operación Rechazada de divisa
<img width="1911" height="1017" alt="image" src="https://github.com/user-attachments/assets/6ae68a12-7946-42b3-9495-bd66335ef90a" />

### 8. Auditoria pendiente de administrador 
<img width="1917" height="1020" alt="image" src="https://github.com/user-attachments/assets/6588fe4e-7987-401c-b815-d784fac41dc1" />






---

## ✨ Funcionalidades

- **Autenticación:** inicio de sesión y gestión de usuarios mediante Firebase Authentication.
- **Conversión de divisas:** consulta y conversión de monedas mediante servicios externos.
- **Registro de operaciones:** almacenamiento de las operaciones realizadas.
- **Historial:** consulta de las operaciones registradas.
- **Perfil de usuario:** visualización y gestión de la información personal.
- **Foto de perfil:** carga y almacenamiento de imágenes mediante Cloudinary.
- **Auditoría administrativa:** revisión y aprobación o rechazo de operaciones pendientes.
- **Control de fechas:** registro y seguimiento de las fechas de las operaciones.
- **Validaciones:** comprobaciones para mejorar el funcionamiento de la aplicación.

## 🛠️ Tecnologías utilizadas

- Kotlin
- Android Studio
- Jetpack Compose
- Material 3
- Firebase Authentication
- Cloud Firestore
- Cloudinary
- Retrofit y Gson
- OkHttp
- Coil
- Coroutines
- JUnit
- Finnhub API
- Frankfurter API

## 📋 Requisitos

Para ejecutar el proyecto necesitas:

- Android Studio.
- JDK compatible con la configuración de Gradle del proyecto.
- Android SDK.
- Una cuenta y un proyecto configurado en Firebase.
- Credenciales de Cloudinary para la carga de imágenes.
- Una clave de Finnhub API, configurada según las propiedades utilizadas por el proyecto.

## 🚀 Instalación y configuración

### 1. Clonar el repositorio

```bash
git clone https://github.com/kmigi0211k/AplicacionReto2.git
```

Abre el proyecto descargado desde Android Studio.

### 2. Configurar Firebase

1. Crea un proyecto en Firebase.
2. Registra una aplicación Android con el identificador `com.example.reto2`.
3. Descarga el archivo `google-services.json`.
4. Coloca el archivo dentro de la carpeta `app/`.
5. Habilita los métodos de autenticación utilizados por la aplicación.
6. Configura Cloud Firestore y sus reglas de seguridad.

### 3. Configurar las credenciales

Configura las propiedades locales necesarias en el archivo `local.properties`, ubicado en la raíz del proyecto.

```properties
sdk.dir=RUTA_DE_TU_ANDROID_SDK
FINNHUB_API_KEY=TU_CLAVE_DE_FINNHUB
CLOUDINARY_CLOUD_NAME=TU_CLOUD_NAME
CLOUDINARY_UPLOAD_PRESET=TU_UPLOAD_PRESET
```

**Importante:** reemplaza los valores de ejemplo por tus propias credenciales. No publiques claves privadas ni contraseñas en GitHub.

### 4. Ejecutar la aplicación

Sincroniza Gradle en Android Studio, selecciona un emulador o conecta un dispositivo Android y ejecuta la aplicación.

También puedes compilar el proyecto desde PowerShell, en la raíz del repositorio:

```powershell
.\gradlew.bat assembleDebug
```

Para ejecutar las pruebas automatizadas:

```powershell
.\gradlew.bat test
```

## 📂 Estructura del proyecto

La organización exacta puede variar según los paquetes actuales del proyecto.

```text
Reto2/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/example/reto2/
│           └── res/
├── screenshots/
│   ├── login.png
│   ├── inicio.png
│   ├── perfil.png
│   ├── historial.png
│   └── administrador.png
├── README.md
├── build.gradle.kts
├── settings.gradle.kts
└── local.properties
```

El archivo `local.properties` normalmente se mantiene fuera del repositorio porque contiene rutas locales y puede incluir credenciales.

## 🔐 Seguridad

- No compartir contraseñas ni credenciales privadas.
- Mantener las claves y configuraciones sensibles fuera del repositorio público.
- Configurar reglas de seguridad adecuadas en Cloud Firestore.
- Verificar los permisos para que las funciones administrativas solo estén disponibles para usuarios autorizados.

- ## 🧪 Pruebas

El proyecto cuenta con configuración para ejecutar pruebas automatizadas mediante JUnit.

Para ejecutar las pruebas desde la terminal, utiliza:

```powershell
.\gradlew.bat test
```

Para verificar que la aplicación compile correctamente:

```powershell
.\gradlew.bat assembleDebug
```

Los resultados deben comprobarse después de ejecutar cada comando.

## 📖 Flujo de funcionamiento

1. El usuario inicia sesión en la aplicación.
2. Accede a la pantalla principal.
3. Selecciona las divisas y consulta la conversión.
4. Registra las operaciones disponibles.
5. Consulta su historial y la información de su perfil.
6. El administrador revisa las operaciones pendientes y realiza las acciones autorizadas.

## 🔮 Mejoras futuras

- Ampliar la cobertura de pruebas automatizadas.
- Mejorar la experiencia de usuario y la accesibilidad.
- Incorporar mensajes de error más descriptivos.
- Optimizar el manejo de errores de conexión y de los servicios externos.
- Continuar mejorando la seguridad y las reglas de acceso a los datos.



## 👨‍💻 Autor

**Juan Camilo Giraldo Lizcano**

Repositorio: [AplicacionReto2 en GitHub](https://github.com/kmigi0211k/AplicacionReto2)
# 📱 Integración de Cámara y Asistente por Voz

<p align="center">
  <strong>Mejorando la interacción del usuario en una aplicación Android</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Android_Studio-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=white" alt="Android Studio">
  <img src="https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
</p>

## 📌 Descripción

Este desarrollo incorpora dos funcionalidades a la aplicación móvil: la captura de fotografías mediante la cámara del dispositivo y el ingreso de información a través de comandos de voz.

El objetivo es ofrecer una interacción más sencilla y práctica durante el uso de la aplicación.

## 📸 1. Integración de cámara

Se implementó el acceso a la cámara del dispositivo para capturar fotografías desde la aplicación.

**Funcionalidades principales:**
- Apertura de la cámara.
- Captura de fotografías.
- Retorno a la pantalla anterior mediante la navegación de la aplicación.

## 🎙️ 2. Asistente por voz

Se desarrolló un asistente que reconoce instrucciones habladas y facilita el ingreso de datos en las operaciones de compra y venta de divisas.

**Funcionalidades principales:**
- Reconocimiento de comandos de voz.
- Identificación del tipo de operación: compra o venta.
- Interpretación de montos y monedas mencionados.
- Autocompletado de los campos del formulario.

## 🛠️ Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Kotlin | Desarrollo de la aplicación |
| Android Studio | Entorno de desarrollo |
| Jetpack Compose | Construcción de la interfaz |
| Reconocimiento de voz de Android | Captura e interpretación de comandos hablados |

## 🎯 Objetivo

Facilitar el uso de la aplicación mediante funciones que permitan capturar fotografías e ingresar información por voz, mejorando la interacción con los formularios de operaciones de divisas.

---

<p align="center">
  <em>Proyecto académico de Desarrollo de Software</em>
</p>

