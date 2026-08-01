# Identidad del examen

> ⏱️ Este archivo debe ir en tu **primer commit**, hecho **antes de que termine la sesión
> sincrónica** del examen. Toma menos de 5 minutos y es lo único con horario fijo:
> después trabajas a tu ritmo hasta el plazo del día siguiente.
>
> Completa **todos** los campos. El código del examen ya viene escrito: no lo cambies.

- **Nombre completo:** ALEX CANCHIGNIA
- **Cédula:** 1753484623
- **NRC:** 30405
- **Código del examen:** `AGSK-2026`
- **Fecha y hora de inicio:** 2026-07-30 20:00

---

## Mi semilla personal

Sean `NN` los **dos últimos dígitos** de mi cédula. No hay cálculos: se copian dígitos y
se busca en una tabla.

| # | Parámetro | Cómo se obtiene | Mi valor |
|:---:|-----------|-----------------|----------|
| — | `NN` | los 2 últimos dígitos de mi cédula | `23` |
| 1 | Nombre de la tabla | `tbl_productos_base_` + `NN` | `tbl_productos_base_23` |
| 2 | Puerto | `81` + `NN` | `8123` |
| 3 | Categoría | según el **último dígito** (tabla de abajo) | Café |
| — | Audiencia para el prompt de IA | según la categoría | cafeterías de especialidad |

**Tabla de categorías:**

| Cédula termina en | Categoría | Audiencia |
|:---:|-----------|-----------|
| 0 o 1 | Cacao | exportadores europeos |
| 2 o 3 | Café | cafeterías de especialidad |
| 4 o 5 | Banano | supermercados mayoristas |
| 6 o 7 | Flores | floristerías premium |
| 8 o 9 | Quinua | tiendas de alimentación saludable |

> ✅ **Autocomprobación:** mi puerto empieza por `81` y termina con los mismos dos
> dígitos que el nombre de mi tabla.
>
> ⚠️ **La categoría no va en el nombre de la tabla.** La tabla es
> `tbl_productos_base_` + mis dos dígitos. La categoría define los **productos que
> siembro dentro** y la **audiencia del prompt de IA**.

**Valores fijos, iguales para todo el curso:** base de datos `agrosmart_db`,
**3 productos válidos** y **2 inválidos**.

---

## Defensa oral

- **Enlace al video: https://drive.google.com/file/d/1jR4EysUpL-Fj_RMQLGbXlEEmo6cb0-u9/view?usp=sharing**
- **Plataforma: Drive institucional**                  <!-- Drive institucional / OneDrive / YouTube no listado -->
- **Acceso verificado en incógnito:**    Sí
- **Duración real: 4:36 **

---

## Declaración

Declaro que este repositorio es de mi autoría individual, que sustentaré oralmente cada
decisión de diseño que contiene, y que el historial de commits refleja fielmente el
proceso de desarrollo que realicé.

**Firma:** ALEX CANCHIGNIA
