// ---------- Crear o editar una meta ----------

const formularioMeta = document.getElementById("formulario-meta");

if (formularioMeta) {

    // Comprobar si estamos editando una meta
    const parametros = new URLSearchParams(window.location.search);
    const idEditar = parametros.get("editar");

    const guardadas = JSON.parse(localStorage.getItem("metas")) || [];

    let metaEditar = null;

    if (idEditar) {

        metaEditar = guardadas.find(function(meta) {
            return meta.id === Number(idEditar);
        });

        // Cargar los datos de la meta en el formulario
        if (metaEditar) {

            document.getElementById("nombre").value = metaEditar.nombre;
            document.getElementById("moneda").value = metaEditar.moneda;
            document.getElementById("monto").value = metaEditar.montoObjetivo;
            document.getElementById("fecha-objetivo").value = metaEditar.fechaObjetivo;

            document.getElementById("boton-meta").textContent = "Guardar cambios";
            document.querySelector(".titulo-pagina").textContent = "Editar meta";

        }

    }

    formularioMeta.addEventListener("submit", function(event) {

        event.preventDefault();

        const nombre = document.getElementById("nombre").value;
        const moneda = document.getElementById("moneda").value;
        const monto = Number(document.getElementById("monto").value);
        const fecha = document.getElementById("fecha-objetivo").value;

        if (metaEditar) {

            // Actualizar los datos de la meta existente
            metaEditar.nombre = nombre;
            metaEditar.moneda = moneda;
            metaEditar.montoObjetivo = monto;
            metaEditar.fechaObjetivo = fecha;

        } else {

            // Crear una meta nueva
            const nuevaMeta = {
                id: Date.now(),
                nombre: nombre,
                moneda: moneda,
                montoObjetivo: monto,
                fechaObjetivo: fecha,
                montoAhorrado: 0
            };

            guardadas.push(nuevaMeta);

        }

        // Guardar los cambios
        localStorage.setItem("metas", JSON.stringify(guardadas));

        // Volver a la pantalla de metas
        window.location.href = "metas.html";

    });

}


// ---------- Datos para mostrar ----------

function formatoMoneda(monto, moneda) {

    return new Intl.NumberFormat("es-AR", {
        style: "currency",
        currency: moneda,
        maximumFractionDigits: 2
    }).format(monto);

}


function formatoFecha(fecha) {

    if (!fecha) {
        return "Sin fecha límite";
    }

    const [año, mes, dia] = fecha.split("-").map(Number);

    return new Date(año, mes - 1, dia).toLocaleDateString("es-AR", {
        month: "short",
        year: "numeric"
    });

}


function escaparHTML(texto) {

    return String(texto).replace(/[&<>"']/g, function(caracter) {
        const caracteres = {
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;"
        };

        return caracteres[caracter];
    });

}


// ---------- Crear una tarjeta de meta ----------

function crearTarjeta(meta) {

    // Calcular el porcentaje de progreso
    const porcentaje = Math.min(
        (meta.montoAhorrado / meta.montoObjetivo) * 100,
        100
    );

    // Calcular cuánto dinero falta
    const faltante = Math.max(
        meta.montoObjetivo - meta.montoAhorrado,
        0
    );

    // Elegir un icono según el nombre
    let icono = "◎";

    if (meta.nombre.toLowerCase().includes("viaje")) {
        icono = "✈";
    }

    return `
        <article class="tarjeta-meta">

            <div class="encabezado-meta">

                <div class="icono-meta">
                    <span>${icono}</span>
                </div>

                <div class="informacion-meta">
                    <h2>${escaparHTML(meta.nombre)}</h2>
                    <p>${meta.fechaObjetivo
                        ? "Fecha objetivo: " + formatoFecha(meta.fechaObjetivo)
                        : "Sin fecha límite"
                    }</p>
                </div>

                <div class="contenedor-opciones-meta">
                    <button class="opciones-meta" type="button" onclick="mostrarOpcionesMeta(${meta.id})">
                        ···
                    </button>
                    <div class="menu-opciones-meta" id="menu-meta-${meta.id}" hidden>
                        <button type="button" onclick="editarMeta(${meta.id})">
                            Editar
                        </button>
                        <button type="button" onclick="eliminarMeta(${meta.id})">
                            Eliminar
                        </button>
                    </div>
                </div>
            </div>

            <div class="montos-meta">

                <p class="monto-ahorrado">
                    ${formatoMoneda(meta.montoAhorrado, meta.moneda)}
                </p>

                <p class="monto-objetivo">
                    de ${formatoMoneda(meta.montoObjetivo, meta.moneda)}
                </p>

            </div>

            <div class="barra-progreso">
                <div class="progreso" style="width: ${porcentaje}%;"></div>
            </div>

            <div class="detalle-progreso">

                <span>${porcentaje.toFixed(0)}% completado</span>

                <span>
                    ${faltante > 0
                        ? "Faltan " + formatoMoneda(faltante, meta.moneda)
                        : "¡Meta alcanzada!"
                    }
                </span>

            </div>

        </article>
    `;

}


// ---------- Mostrar las metas guardadas ----------

const listaMetas = document.getElementById("lista-metas");
const mensajeVacio = document.getElementById("mensaje-vacio");

if (listaMetas) {

    const guardadas = JSON.parse(localStorage.getItem("metas")) || [];

    if (mensajeVacio) {
        mensajeVacio.hidden = guardadas.length > 0;
    }

    // Mostrar las más nuevas primero
    guardadas.sort(function(a, b) {
        return b.id - a.id;
    });

    guardadas.forEach(function(meta) {

        listaMetas.insertAdjacentHTML("beforeend", crearTarjeta(meta));

    });

}

// ---------- Opciones de las metas ----------

function mostrarOpcionesMeta(id) {

    const menu = document.getElementById("menu-meta-" + id);

    menu.hidden = !menu.hidden;

}


// ---------- Editar una meta ----------

function editarMeta(id) {

    // Ir al formulario indicando qué meta queremos editar
    window.location.href = "nuevameta.html?editar=" + id;

}


// ---------- Eliminar una meta ----------

function eliminarMeta(id) {

    const confirmar = confirm("¿Estás seguro de que querés eliminar esta meta?");

    if (!confirmar) {
        return;
    }

    // Obtener las metas guardadas
    let guardadas = JSON.parse(localStorage.getItem("metas")) || [];

    // Eliminar la meta seleccionada
    guardadas = guardadas.filter(function(meta) {
        return meta.id !== id;
    });

    // Guardar la lista actualizada
    localStorage.setItem("metas", JSON.stringify(guardadas));

    // Actualizar la pantalla
    location.reload();

}