function cambiarTipo(tipo) {

    // Pestañas
    document.getElementById("pestana-todos")
        .classList.toggle("activa", tipo === "todos");

    document.getElementById("pestana-ingreso")
        .classList.toggle("activa", tipo === "ingreso");

    document.getElementById("pestana-gasto")
        .classList.toggle("activa", tipo === "gasto");

    document.getElementById("pestana-transferencia")
        .classList.toggle("activa", tipo === "transferencia");


    // Movimientos
    const movimientos = document.querySelectorAll(".tarjeta-movimiento");

    movimientos.forEach(function(movimiento) {

        const tipoMovimiento = movimiento.dataset.tipo;

        if (tipo === "todos" || tipoMovimiento === tipo) {
            movimiento.hidden = false;
        } else {
            movimiento.hidden = true;
        }

    });

}

// ---------- Datos para mostrar ----------
const NOMBRES_CATEGORIA = {
    ingreso: {
        sueldo: "Sueldo y trabajo",
        negocios: "Negocios y ventas",
        regalos: "Regalos y dinero recibido",
        inversiones: "Inversiones",
        alquileres: "Alquileres",
        otros: "Otros ingresos"
    },
    gasto: {
        vivienda: "Vivienda",
        alimentacion: "Alimentación",
        transporte: "Transporte",
        compras: "Compras personales",
        salud: "Salud",
        educacion: "Educación",
        ocio: "Ocio y entretenimiento",
        servicios: "Servicios y suscripciones",
        viajes: "Viajes",
        otros: "Otros gastos"
    }
};

const NOMBRES_FONDO = {
    efectivo: "Efectivo",
    banco: "Cuenta bancaria",
    billetera: "Billetera virtual",
    otro: "Otro"
};

const ICONOS = { ingreso: "▣", gasto: "♜", transferencia: "⇄" };


// ---------- Funciones auxiliares ----------
function aTexto(fecha) {
    const a = fecha.getFullYear();
    const m = String(fecha.getMonth() + 1).padStart(2, "0");
    const d = String(fecha.getDate()).padStart(2, "0");
    return a + "-" + m + "-" + d;
}

function tituloDelDia(fecha) {
    const hoy = new Date();
    const ayer = new Date();
    ayer.setDate(hoy.getDate() - 1);

    const [a, m, d] = fecha.split("-").map(Number);
    const fechaLarga = new Date(a, m - 1, d)
        .toLocaleDateString("es-AR", { day: "numeric", month: "long" })
        .toUpperCase();

    if (fecha === aTexto(hoy)) return "HOY · " + fechaLarga;
    if (fecha === aTexto(ayer)) return "AYER · " + fechaLarga;
    return fechaLarga;
}

function crearTarjeta(mov) {
    let signo = "+";
    if (mov.tipo === "gasto") signo = "-";
    if (mov.tipo === "transferencia") signo = "";

    const simbolo = mov.moneda === "ARS" ? "$" : mov.moneda;
    const importe = mov.importe.toLocaleString("es-AR", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });

    let titulo;
    let detalle;
    if (mov.tipo === "transferencia") {
        titulo = "Transferencia";
        detalle = NOMBRES_FONDO[mov.fondo] + " → " + NOMBRES_FONDO[mov.destino];
    } else {
        titulo = NOMBRES_CATEGORIA[mov.tipo][mov.categoria];
        const medio = mov.medio.charAt(0).toUpperCase() + mov.medio.slice(1);
        detalle = NOMBRES_FONDO[mov.fondo] + " · " + medio;
    }

    return `
        <article class="tarjeta-movimiento" data-tipo="${mov.tipo}">
            <div class="icono-tipo ${mov.tipo}">
                <span>${ICONOS[mov.tipo]}</span>
            </div>
            <div class="informacion-movimiento">
                <h3>${titulo}</h3>
                <p>${detalle}</p>
            </div>
            <p class="monto-movimiento ${mov.tipo}">${signo} ${simbolo} ${importe}</p>
        </article>
    `;
}


// ---------- Mostrar los movimientos ----------
const historial = document.getElementById("historial");
const mensajeVacio = document.getElementById("mensaje-vacio");

const guardados = JSON.parse(localStorage.getItem("movimientos")) || [];

mensajeVacio.hidden = guardados.length > 0;

// Más recientes primero (por fecha, y a igual fecha, el último cargado)
guardados.sort(function (a, b) {
    return b.fecha.localeCompare(a.fecha) || b.id - a.id;
});

let fechaActual = null;
let lista = null;

guardados.forEach(function (mov) {
    if (mov.fecha !== fechaActual) {
        fechaActual = mov.fecha;

        const grupo = document.createElement("div");
        grupo.className = "grupo-movimientos";
        grupo.innerHTML = `
            <h2 class="fecha-movimientos">${tituloDelDia(mov.fecha)}</h2>
            <div class="lista-movimientos"></div>
        `;
        historial.appendChild(grupo);
        lista = grupo.querySelector(".lista-movimientos");
    }

    lista.insertAdjacentHTML("beforeend", crearTarjeta(mov));
});