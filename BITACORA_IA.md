# Bitácora de uso de inteligencia artificial

**Proyecto:** Miniproyecto 5 — Sistema de Reservaciones de un Hotel  
**Tecnologías:** Java 21, Maven, SQLite (JDBC) y Swing  
**Herramienta de IA utilizada:** ChatGPT (OpenAI)

## Objetivo

Documentar de manera transparente el apoyo de inteligencia artificial durante el análisis, implementación, depuración, documentación y verificación del proyecto. La IA se utilizó como asistente de aprendizaje y revisión; las propuestas se contrastaron con el enunciado, el material de clase y las pruebas del proyecto.

> **Nota de revisión:** Los periodos indicados son aproximados y los prompts están resumidos, no son transcripciones literales. Antes de entregar, verificar fechas y decisiones concretas para que la bitácora coincida con el trabajo realizado.

## Registro de interacciones

| Fecha o periodo aproximado | Herramienta | Propósito y prompt resumido | Sugerencias aceptadas y motivo | Sugerencias rechazadas o ajustadas y motivo |
|---|---|---|---|---|
| 8–10 oct. 2026 | ChatGPT | Explicar y completar E02–E04: interfaz `Reservable`, `NumeroHabitacion` y clase base `Habitacion`. | Validaciones de prefijo, número positivo, invariantes y uso de excepciones; permitieron cumplir el contrato del modelo. | Se corrigieron interpretaciones iniciales de la expresión regular y se mantuvieron las firmas exigidas por el esqueleto. |
| 10–16 oct. 2026 | ChatGPT | Trabajar E05–E08: subtipos, capacidades, huésped, estados y totales de reservación. | Uso de herencia, métodos sobrescritos, retorno covariante, `Externalizable`, máquina de estados y cálculo de importes; se comprobó con pruebas. | Se ajustaron implementaciones y explicaciones para conservar las reglas del PDF y los nombres del proyecto. |
| 16–18 oct. 2026 | ChatGPT | Implementar E09–E12: inventario, tipos internos, reglas funcionales y tareas periódicas. | Colecciones, `Optional`, iteradores, lambdas, referencias a métodos, `Runnable`, `volatile`, interrupción y `join`; se verificó su comportamiento. | Se corrigieron propuestas durante la depuración cuando no coincidían con las pruebas o el código existente. |
| 17–27 oct. 2026 | ChatGPT | Implementar E13–E15: conexión JDBC, transacciones, repositorios SQLite, respaldo, fachada `Hotel` y eventos. | Separación entre servicio y persistencia, operaciones CRUD, consultas parametrizadas y carga desde SQLite; permitieron conservar información al reiniciar. | Se adaptaron ejemplos genéricos a las clases, constructores y repositorios reales del proyecto. |
| 27 oct.–4 nov. 2026 | ChatGPT | Implementar E16 y mejorar la interfaz Swing: reportes, búsquedas, paneles y operaciones del hotel. | Streams, agrupaciones, estadísticas, proyecciones y paneles de habitaciones, huéspedes, reservaciones, reportes y eventos. | Se ajustaron detalles visuales y métodos de actualización para que funcionaran con la implementación existente. |
| 4 nov. 2026 | ChatGPT | Revisar documentación de entrega: README, UML, estructura del proyecto y requisitos de base de datos. | Organización de entregables y revisión de correspondencia entre código y diagramas. | Se pospuso la generación de Javadoc hasta completar primero los comentarios del código. |
| 4–5 nov. 2026 | ChatGPT | Revisar Javadoc de clases, constructores y métodos públicos, archivo por archivo. | Comentarios `@param`, `@return`, `@throws` y explicaciones breves; se conservaron comentarios existentes cuando eran suficientes. | Se corrigieron omisiones detectadas al revisar el código, como métodos `default`, constructores compactos y clases internas. |
| 5 nov. 2026 | ChatGPT | Ejecutar pruebas del modelo y generar documentación HTML mediante Maven. | Ejecución de `PruebasModelo`: **14 pruebas aprobadas, 0 fallidas**. Generación con `mvn javadoc:javadoc`: **BUILD SUCCESS**. | No se consideró suficiente escribir comentarios: también se comprobó la generación de HTML. |

## Ejemplos de prompts resumidos

- «Explícame qué hace este método antes de implementarlo; quiero entenderlo y no solo copiar código».
- «Revisa la etapa del PDF y dime qué falta sin cambiar el esqueleto del proyecto».
- «Ayúdame a interpretar los errores de las pruebas y corregirlos».
- «Revisa todos los métodos públicos de esta clase y escribe el Javadoc que falta, respetando el orden del archivo».
- «Comprueba los requisitos de persistencia, UML, interfaz y documentación antes de la entrega».

## Criterios de aceptación de sugerencias

Se aceptaron propuestas cuando respetaban las firmas y reglas del proyecto, podían explicarse con los conceptos vistos en clase y superaban las pruebas correspondientes. Se modificaron o descartaron sugerencias que omitían métodos, suponían archivos inexistentes o no coincidían con la estructura real del código.

## Verificación y evidencias

- **Pruebas funcionales:** `PruebasModelo` finalizó con **14 aprobadas y 0 fallidas** (5 de noviembre de 2026).
- **Javadoc:** `mvn javadoc:javadoc` terminó con **BUILD SUCCESS**; documentación HTML generada en `target/reports/apidocs/index.html`.
- **Persistencia:** se implementaron repositorios SQLite y respaldo binario; las pruebas del modelo incluyen persistencia tras reinicio y exportación/importación.
- **Interfaz:** se desarrollaron paneles Swing para las operaciones principales del sistema.
- **Entregables adicionales:** README, diagramas UML y datos de ejemplo deben verificarse en la versión definitiva que se suba al repositorio.

## Declaración de responsabilidad

La inteligencia artificial se empleó como herramienta de apoyo para comprender conceptos, generar propuestas de código, detectar omisiones y redactar documentación. La selección, integración, adaptación y validación de las soluciones corresponden a la persona que entrega el proyecto. La bitácora no sustituye el código fuente, las pruebas ni la explicación técnica de las decisiones adoptadas.
