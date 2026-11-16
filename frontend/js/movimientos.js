
const API_URL = "http://localhost:8080/api";

const token = localStorage.getItem("authToken");

let movimientos = [];
let tipoActual = "todos";

const historial = document.getElementById("historial");
const mensajeVacio = document.getElementById("mensaje-vacio");

const NOMBRES_TIPO = {
    INGRESO: "Ingreso",
    GASTO: "Gasto",
    TRANSFERENCIA: "Transferencia"
};

const ICONOS = {
    INGRESO: "▣",
    GASTO: "♜",
    TRANSFERENCIA: "⇄"
};


// ---------- CONSULTAR API ----------

async function cargarMovimientos() {

    if (!token) {
        alert("Tenés que iniciar sesión.");
        window.location.href = "login.html";
        return;
    }

    try {

        const respuesta = await fetch(API_URL + "/movimientos", {
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });

        if (!respuesta.ok) {
            throw new Error("No se pudieron cargar los movimientos.");
        }

        movimientos = await respuesta.json();

        movimientos.sort((a, b) =>
            b.fecha.localeCompare(a.fecha) || b.id - a.id
        );

        mostrarMovimientos();

    } catch (error) {
        console.error(error);
        alert(error.message);
    }
}


// ---------- FORMATO DE FECHA ----------

function aTexto(fecha) {

    const a = fecha.getFullYear();
    const m = String(fecha.getMonth() + 1).padStart(2, "0");
    const d = String(fecha.getDate()).padStart(2, "0");

    return `${a}-${m}-${d}`;
}


function tituloDelDia(fecha) {

    const hoy = new Date();
    const ayer = new Date();

    ayer.setDate(hoy.getDate() - 1);

    const [a, m, d] = fecha.split("-").map(Number);

    const fechaLarga = new Date(a, m - 1, d)
        .toLocaleDateString("es-AR", {
            day: "numeric",
            month: "long"
        })
        .toUpperCase();

    if (fecha === aTexto(hoy)) return "HOY · " + fechaLarga;
    if (fecha === aTexto(ayer)) return "AYER · " + fechaLarga;

    return fechaLarga;
}


// ---------- CREAR TARJETA ----------

function crearTarjeta(mov) {

    const tipo = mov.tipo;

    let signo = "+";

    if (tipo === "GASTO") signo = "-";
    if (tipo === "TRANSFERENCIA") signo = "";

    const moneda = mov.moneda?.simbolo || mov.moneda?.codigo || "";
    const importe = Number(mov.monto).toLocaleString("es-AR", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });

    let titulo = mov.descripcion || NOMBRES_TIPO[tipo];
    let detalle = "";

    if (tipo === "TRANSFERENCIA") {

        detalle =
            (mov.cuenta?.nombre || "Cuenta de origen") +
            " → " +
            (mov.cuentaDestino?.nombre || "Cuenta de destino");

    } else {

        const categoria = mov.categoria?.nombre || NOMBRES_TIPO[tipo];
        const cuenta = mov.cuenta?.nombre || "";
        const medio = mov.medioPago || "";

        detalle = `${categoria} · ${cuenta} · ${medio}`;
    }

    return `
        <article class="tarjeta-movimiento" data-tipo="${tipo.toLowerCase()}">
            <div class="icono-tipo ${tipo.toLowerCase()}">
                <span>${ICONOS[tipo] || ""}</span>
            </div>

            <div class="informacion-movimiento">
                <h3>${titulo}</h3>
                <p>${detalle}</p>
            </div>

            <p class="monto-movimiento ${tipo.toLowerCase()}">
                ${signo} ${moneda} ${importe}
            </p>

            <div class="acciones-movimiento">
                <button type="button" onclick="editarMovimiento(${mov.id})">
                    Editar
                </button>

                <button type="button" onclick="eliminarMovimiento(${mov.id})">
                    Eliminar
                </button>
            </div>
        </article>
    `;
}


// ---------- MOSTRAR HISTORIAL ----------

function mostrarMovimientos() {

    historial.innerHTML = "";

    const busqueda = document
        .getElementById("buscar-movimiento")
        .value
        .toLowerCase();

    const filtrados = movimientos.filter(mov => {

        const coincideTipo =
            tipoActual === "todos" ||
            mov.tipo.toLowerCase() === tipoActual;

        const texto = [
            mov.descripcion,
            mov.categoria?.nombre,
            mov.cuenta?.nombre,
            mov.cuentaDestino?.nombre,
            mov.medioPago
        ].join(" ").toLowerCase();

        return coincideTipo && texto.includes(busqueda);
    });

    mensajeVacio.hidden = filtrados.length > 0;

    let fechaActual = null;
    let lista = null;

    filtrados.forEach(mov => {

        if (mov.fecha !== fechaActual) {

            fechaActual = mov.fecha;

            const grupo = document.createElement("div");
            grupo.className = "grupo-movimientos";

            grupo.innerHTML = `
                <h2 class="fecha-movimientos">
                    ${tituloDelDia(mov.fecha)}
                </h2>

                <div class="lista-movimientos"></div>
            `;

            historial.appendChild(grupo);

            lista = grupo.querySelector(".lista-movimientos");
        }

        lista.insertAdjacentHTML("beforeend", crearTarjeta(mov));
    });
}


// ---------- PESTAÑAS ----------

function cambiarTipo(tipo) {

    tipoActual = tipo;

    document.getElementById("pestana-todos")
        .classList.toggle("activa", tipo === "todos");

    document.getElementById("pestana-ingreso")
        .classList.toggle("activa", tipo === "ingreso");

    document.getElementById("pestana-gasto")
        .classList.toggle("activa", tipo === "gasto");

    document.getElementById("pestana-transferencia")
        .classList.toggle("activa", tipo === "transferencia");

    mostrarMovimientos();
}


// ---------- BUSCADOR ----------

document.getElementById("buscar-movimiento")
    .addEventListener("input", mostrarMovimientos);


// ---------- ELIMINAR ----------

async function eliminarMovimiento(id) {

    if (!confirm("¿Querés eliminar este movimiento?")) {
        return;
    }

    try {

        const respuesta = await fetch(`${API_URL}/movimientos/${id}`, {
            method: "DELETE",
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });

        if (!respuesta.ok) {
            throw new Error("No se pudo eliminar el movimiento.");
        }

        await cargarMovimientos();

    } catch (error) {
        console.error(error);
        alert(error.message);
    }
}


// ---------- EDITAR ----------

function editarMovimiento(id) {

    localStorage.setItem("movimientoEditar", id);

    window.location.href = "nuevomovimiento.html";
}


// ---------- INICIO ----------

document.addEventListener("DOMContentLoaded", cargarMovimientos);
