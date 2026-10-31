# Workout Tracker

App Android **offline** para seguir tu progreso en gym, calistenia o cualquier disciplina basada en repeticiones y peso.

## Instalar

1. Descarga el `.apk` más reciente desde [Releases](../../releases).
2. Ábrelo en el teléfono y permite "instalar apps desconocidas" si Android lo pide.
3. Las versiones nuevas se instalan **encima** de la anterior y conservan los datos (todas usan la misma firma).

## Versiones

| Versión | Contenido |
|---|---|
| v0.1 | Crear ejercicios con disciplina, registrar series (reps + peso opcional), ver y borrar (con deshacer) las series del día |
| v0.2 | Historial por día y por tipo de entrenamiento |
| v0.3 | Metas con barra de progreso y % |
| v0.4 | Récords personales y gráfico de evolución |
| v0.5 | Exportar / importar respaldo |
| v0.6 | Rutinas, repetir último entreno, temporizador de descanso |
| v1.0 | Funcionalidad completa → fase de UI/UX |

## Stack

Kotlin · Jetpack Compose · Room (SQLite local). GitHub Actions compila y prueba en cada push; los tags `v*` publican un Release con el APK.

## Firma

El APK se firma con un keystore que **no** está en el repo. Se configura con dos secrets del repositorio
(Settings → Secrets and variables → Actions):

- `KEYSTORE_BASE64`: el archivo `.jks` codificado en base64.
- `KEYSTORE_PASSWORD`: su contraseña (alias `workout`).

Guarda una copia del `.jks` y la contraseña: si se pierden, las versiones nuevas no podrán instalarse encima de las viejas.
