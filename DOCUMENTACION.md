# Documentación del proyecto — MascotasPerdidas

## ¿Qué había antes?

Era una plantilla de ejemplo para aplicaciones de servidor web en Kotlin (Spring Boot). Servía para no empezar de cero cuando alguien quería hacer un backend. Tenía código de ejemplo de un cliente ficticio, conexión a base de datos Postgres, Docker, y archivos de configuración con nombres raros llenos de símbolos (`{{ esto }}`).

No tenía nada de Android. Era para servidores, no para celulares.

---

## ¿Qué se quitó?

- Todo el código del servidor (Spring Boot, base de datos, Docker).
- Los archivos de plantilla con los símbolos raros.
- La configuración de Java 21 y las dependencias del servidor.
- Los scripts de prueba del servidor.

---

## ¿Qué se dejó?

- La carpeta `.git` y la conexión al repositorio de GitHub (OmarVillamizar/mascotasperdidas).
- El sistema de construcción Gradle (ya existía, solo se actualizó).
- Los archivos `CLAUDE.md` y `roadmap.md` que escribiste con los requisitos del proyecto.

---

## ¿Qué se creó?

Se armó la base del proyecto Android desde cero encima de esta carpeta:

- Configuración de Gradle para Android con todas las librerías que necesita la app.
- La aplicación mínima que compila y abre una pantalla en blanco (Fase 0 del roadmap).
- Las carpetas vacías donde irá el código de cada parte de la app (`domain/`, `data/`, `app/`).
- Las reglas de calidad de código (detekt + ktlint).
- El `.gitignore` actualizado para que nunca se suba el archivo secreto de Firebase.

---

## ¿Qué sigue?

Seguir el `roadmap.md` fase por fase, en orden:

1. **Fase 1** — Escribir los modelos y reglas del dominio (la parte más importante, sin Android ni Firebase).
2. **Fase 2** — Definir los colores y tipografía de la app.
3. **Fase 3** — Crear datos falsos para poder construir la interfaz sin necesitar Firebase todavía.
4. **Fases 4 al 13** — Construir pantalla por pantalla (Splash, Perfil, OTP, Permisos, Feed, Configuración).
5. **Fase 14 (última)** — Conectar Firebase real. Solo entonces se toca la nube.

La idea es que en ningún momento se mezcle la lógica del negocio con Android o con Firebase. Cada cosa en su lugar.
