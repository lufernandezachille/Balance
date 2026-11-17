const API_URL = "http://localhost:8080/api";
const token = localStorage.getItem("authToken");

function verificarSesion() {
    if (!token) {
        alert("Tenés que iniciar sesión.");
        window.location.href = "iniciarsesion.html";
        return false;
    }
    return true;
}

// ---------- Formatos ----------

function formatoMoneda(monto, moneda = "ARS") {
    return new Intl.NumberFormat("es-AR", {
        style: "currency",
        currency: moneda,
        maximumFractionDigits: 2
    }).format(monto || 0);
}

function formatoFecha(fecha) {
    if (!fecha) return "Sin fecha límite";
    const [anio, mes, dia] = fecha.split("-").map(Number);
    return new Date(anio, mes - 1, dia).toLocaleDateString("es-AR", {
        day: "numeric",
        month: "short",
        year: "numeric"
    });
}

function escaparHTML(texto) {
    return String(texto).replace(/[&<>"']/g, c => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    }[c]));
}

// ---------- Crear o editar una meta (nuevameta.html) ----------

const formularioMeta = document.getElementById("formulario-meta");

if (formularioMeta && verificarSesion()) {
    const parametros = new URLSearchParams(window.location.search);
    const idEditar = parametros.get("editar");

    // Cargar moneda base del usuario para mostrarla en el select
    fetch(`${API_URL}/usuarios/perfil`, {
        headers: { "Authorization": `Bearer ${token}` }
    })
        .then(r => r.ok ? r.json() : null)
        .then(usuario => {
            if (usuario && usuario.codigoMoneda) {
                const selectMoneda = document.getElementById("moneda");
                selectMoneda.innerHTML = `<option value="${usuario.codigoMoneda}" selected>${usuario.codigoMoneda}</option>`;
            }
        })
        .catch(() => {});

    // Si estamos editando, traer la meta desde GET /api/metas/{id}
    if (idEditar) {
        document.getElementById("boton-meta").textContent = "Guardar cambios";
        document.querySelector(".titulo-pagina").textContent = "Editar meta";

        fetch(`${API_URL}/metas/${idEditar}`, {
            headers: { "Authorization": `Bearer ${token}` }
        })
            .then(async res => {
                if (!res.ok) throw new Error("No se pudo cargar la meta.");
                return res.json();
            })
            .then(meta => {
                document.getElementById("nombre").value = meta.nombre || "";
                document.getElementById("monto").value = meta.montoObjetivo || "";
                document.getElementById("fecha-objetivo").value = meta.fechaObjetivo || "";
                if (meta.moneda) {
                    const selectMoneda = document.getElementById("moneda");
                    selectMoneda.innerHTML = `<option value="${meta.moneda}" selected>${meta.moneda}</option>`;
                }
            })
            .catch(err => {
                alert(err.message);
                window.location.href = "metas.html";
            });
    }

    formularioMeta.addEventListener("submit", async function (event) {
        event.preventDefault();

        const payload = {
            nombre: document.getElementById("nombre").value.trim(),
            montoObjetivo: Number(document.getElementById("monto").value),
            fechaObjetivo: document.getElementById("fecha-objetivo").value,
            colorIcono: null
        };

        const url = idEditar ? `${API_URL}/metas/${idEditar}` : `${API_URL}/metas`;
        const metodo = idEditar ? "PUT" : "POST";

        try {
            const respuesta = await fetch(url, {
                method: metodo,
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify(payload)
            });

            if (!respuesta.ok) {
                const textoError = await respuesta.text();
                throw new Error(textoError || "No se pudo guardar la meta.");
            }

            window.location.href = "metas.html";
        } catch (error) {
            alert(error.message);
        }
    });
}

// ---------- Listado de metas (metas.html) ----------

function crearTarjeta(meta) {
    const porcentaje = Math.min(Number(meta.porcentajeProgreso || 0), 100);
    const faltante = Math.max(Number(meta.montoRestante || 0), 0);

    let icono = "◎";
    if (meta.nombre.toLowerCase().includes("viaje")) icono = "✈";

    let textoEstado = "";
    if (meta.estado === "CUMPLIDA") {
        textoEstado = "¡Meta alcanzada!";
    } else if (meta.estado === "VENCIDA") {
        textoEstado = `Vencida · Faltan ${formatoMoneda(faltante, meta.moneda)}`;
    } else {
        textoEstado = `Faltan ${formatoMoneda(faltante, meta.moneda)}`;
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
                    ${formatoMoneda(meta.montoAcumulado, meta.moneda)}
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
                <span>${textoEstado}</span>
            </div>
        </article>
    `;
}

const listaMetas = document.getElementById("lista-metas");
const mensajeVacio = document.getElementById("mensaje-vacio");

async function cargarMetas() {
    if (!listaMetas || !verificarSesion()) return;

    try {
        const respuesta = await fetch(`${API_URL}/metas`, {
            headers: { "Authorization": `Bearer ${token}` }
        });

        if (!respuesta.ok) throw new Error("No se pudieron cargar las metas.");

        const metas = await respuesta.json();

        listaMetas.innerHTML = "";
        if (mensajeVacio) mensajeVacio.hidden = metas.length > 0;

        metas.sort((a, b) => b.id - a.id);
        metas.forEach(meta => {
            listaMetas.insertAdjacentHTML("beforeend", crearTarjeta(meta));
        });
    } catch (error) {
        console.error(error);
        alert(error.message);
    }
}

function mostrarOpcionesMeta(id) {
    const menu = document.getElementById("menu-meta-" + id);
    if (menu) menu.hidden = !menu.hidden;
}

function editarMeta(id) {
    window.location.href = "nuevameta.html?editar=" + id;
}

async function eliminarMeta(id) {
    if (!confirm("¿Estás seguro de que querés eliminar esta meta? El dinero asignado volverá al fondo General.")) {
        return;
    }

    try {
        const respuesta = await fetch(`${API_URL}/metas/${id}`, {
            method: "DELETE",
            headers: { "Authorization": `Bearer ${token}` }
        });

        if (!respuesta.ok) throw new Error("No se pudo eliminar la meta.");

        await cargarMetas();
    } catch (error) {
        alert(error.message);
    }
}

document.addEventListener("DOMContentLoaded", cargarMetas);