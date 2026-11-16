
const API_URL = "http://localhost:8080/api";

const token = localStorage.getItem("authToken");

let cuentas = [];
let fondos = [];
let categorias = [];

// ---------- PETICIONES AL BACKEND ----------

async function consultarAPI(ruta) {
    const respuesta = await fetch(API_URL + ruta, {
        headers: {
            "Authorization": `Bearer ${token}`
        }
    });

    if (!respuesta.ok) {
        throw new Error("No se pudieron cargar los datos.");
    }

    return await respuesta.json();
}

async function enviarAPI(ruta, metodo, datos) {
    const respuesta = await fetch(API_URL + ruta, {
        method: metodo,
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify(datos)
    });

    if (!respuesta.ok) {
        const error = await respuesta.text();
        throw new Error(error || "No se pudo guardar el movimiento.");
    }

    return await respuesta.json();
}


// ---------- CARGAR DATOS ----------

async function cargarDatos() {

    if (!token) {
        alert("Tenés que iniciar sesión.");
        window.location.href = "login.html";
        return;
    }

    try {
        [cuentas, fondos, categorias] = await Promise.all([
            consultarAPI("/cuentas"),
            consultarAPI("/fondos"),
            consultarAPI("/categorias")
        ]);

        cargarMonedas();
        cargarCuentas();
        cargarFondos();

    } catch (error) {
        console.error(error);
        alert("No se pudieron cargar las cuentas, fondos y categorías.");
    }
}


// ---------- MONEDAS ----------

function cargarMonedas() {

    const select = document.getElementById("moneda");

    const monedas = [];

    cuentas.forEach(cuenta => {
        if (
            cuenta.moneda &&
            !monedas.some(m => m.id === cuenta.moneda.id)
        ) {
            monedas.push(cuenta.moneda);
        }
    });

    select.innerHTML = `
        <option value="" disabled selected>Selecciona una moneda</option>
    `;

    monedas.forEach(moneda => {
        select.innerHTML += `
            <option value="${moneda.id}">
                ${moneda.codigo}
            </option>
        `;
    });
}


// ---------- CUENTAS ----------

function cargarCuentas() {

    const select = document.getElementById("cuenta");
    const selectDestino = document.getElementById("cuenta-destino");

    select.innerHTML = `
        <option value="" disabled selected>Selecciona una cuenta</option>
    `;

    selectDestino.innerHTML = `
        <option value="" disabled selected>Selecciona una cuenta</option>
    `;

    cuentas.forEach(cuenta => {

        const moneda = cuenta.moneda?.codigo || "";

        const opcion = `
            <option value="${cuenta.id}">
                ${cuenta.nombre} (${moneda})
            </option>
        `;

        select.innerHTML += opcion;
        selectDestino.innerHTML += opcion;
    });
}


// ---------- FONDOS ----------

function cargarFondos() {

    const select = document.getElementById("fondo");

    select.innerHTML = `
        <option value="" disabled selected>Selecciona un fondo</option>
    `;

    fondos.forEach(fondo => {
        select.innerHTML += `
            <option value="${fondo.id}">
                ${fondo.nombre}
            </option>
        `;
    });
}


// ---------- CATEGORÍAS ----------

function cargarCategorias(tipo) {

    const select = document.getElementById("categoria");

    select.innerHTML = `
        <option value="" disabled selected>Selecciona una categoría</option>
    `;

    categorias
        .filter(categoria => categoria.tipo === tipo.toUpperCase())
        .forEach(categoria => {
            select.innerHTML += `
                <option value="${categoria.id}">
                    ${categoria.nombre}
                </option>
            `;
        });
}


// ---------- CAMBIAR TIPO ----------

function cambiarTipo(tipo) {

    const esIngreso = tipo === "ingreso";
    const esGasto = tipo === "gasto";
    const esTransferencia = tipo === "transferencia";

    document.getElementById("tipo").value = tipo.toUpperCase();

    document.getElementById("pestana-ingreso")
        .classList.toggle("activa", esIngreso);

    document.getElementById("pestana-gasto")
        .classList.toggle("activa", esGasto);

    document.getElementById("pestana-transferencia")
        .classList.toggle("activa", esTransferencia);

    document.getElementById("campo-categoria").hidden = esTransferencia;
    document.getElementById("campo-medio").hidden = esTransferencia;
    document.getElementById("campo-cuenta-destino").hidden = !esTransferencia;
    document.getElementById("campo-recurrente").hidden = esTransferencia;

    document.getElementById("categoria").required = !esTransferencia;
    document.getElementById("medio").required = !esTransferencia;
    document.getElementById("cuenta-destino").required = esTransferencia;

    document.getElementById("titulo-cuenta").textContent =
        esIngreso ? "Cuenta de destino" : "Cuenta de origen";

    document.getElementById("titulo-medio").textContent =
        esIngreso ? "Medio de ingreso" : "Medio de pago";

    if (!esTransferencia) {
        cargarCategorias(tipo);
    }

    const medio = document.getElementById("medio");

    if (esIngreso) {
        medio.innerHTML = `
            <option value="" disabled selected>Selecciona un medio</option>
            <option value="EFECTIVO">Efectivo</option>
            <option value="TRANSFERENCIA">Transferencia</option>
        `;
    } else {
        medio.innerHTML = `
            <option value="" disabled selected>Selecciona un medio</option>
            <option value="EFECTIVO">Efectivo</option>
            <option value="TRANSFERENCIA">Transferencia</option>
            <option value="TARJETA">Tarjeta</option>
        `;
    }

    if (esTransferencia) {
        document.getElementById("es-recurrente").checked = false;
        mostrarRecurrencia();
    }
}


// ---------- FILTRAR CUENTAS SEGÚN MONEDA ----------

document.getElementById("moneda").addEventListener("change", function () {

    const monedaId = Number(this.value);

    const cuenta = document.getElementById("cuenta");
    const destino = document.getElementById("cuenta-destino");

    cuenta.value = "";
    destino.value = "";

    Array.from(cuenta.options).forEach(opcion => {
        if (opcion.value) {
            const cuentaDatos = cuentas.find(c => c.id === Number(opcion.value));
            opcion.hidden = cuentaDatos.moneda.id !== monedaId;
        }
    });

    Array.from(destino.options).forEach(opcion => {
        if (opcion.value) {
            const cuentaDatos = cuentas.find(c => c.id === Number(opcion.value));
            opcion.hidden = cuentaDatos.moneda.id !== monedaId;
        }
    });
});


// ---------- RECURRENCIA ----------

function mostrarRecurrencia() {

    const recurrente = document.getElementById("es-recurrente").checked;

    document.getElementById("datos-recurrencia").hidden = !recurrente;

    document.getElementById("frecuencia").required = recurrente;
    document.getElementById("fecha-inicio").required = recurrente;
}


// ---------- GUARDAR MOVIMIENTO ----------

const formulario = document.querySelector(".formulario");

formulario.addEventListener("submit", async function (evento) {

    evento.preventDefault();

    const tipo = document.getElementById("tipo").value;
    const esTransferencia = tipo === "TRANSFERENCIA";
    const esRecurrente = document.getElementById("es-recurrente").checked;

    const monedaId = Number(document.getElementById("moneda").value);
    const cuentaId = Number(document.getElementById("cuenta").value);
    const fondoId = Number(document.getElementById("fondo").value);

    const cuentaSeleccionada = cuentas.find(c => c.id === cuentaId);

    if (!cuentaSeleccionada || cuentaSeleccionada.moneda.id !== monedaId) {
        alert("La moneda debe coincidir con la moneda de la cuenta.");
        return;
    }

    const movimiento = {
        tipo: tipo,
        monto: Number(document.getElementById("importe").value),
        moneda: { id: monedaId },
        cuenta: { id: cuentaId },
        fondo: { id: fondoId },
        fecha: document.getElementById("fecha").value,
        descripcion: document.getElementById("descripcion").value
    };

    if (esTransferencia) {

        const cuentaDestinoId = Number(
            document.getElementById("cuenta-destino").value
        );

        const cuentaDestino = cuentas.find(c => c.id === cuentaDestinoId);

        if (!cuentaDestino || cuentaDestinoId === cuentaId) {
            alert("Seleccioná una cuenta de destino diferente.");
            return;
        }

        if (cuentaDestino.moneda.id !== monedaId) {
            alert("Las dos cuentas deben tener la misma moneda.");
            return;
        }

        movimiento.cuentaDestino = { id: cuentaDestinoId };

    } else {

        movimiento.categoria = {
            id: Number(document.getElementById("categoria").value)
        };

        movimiento.medioPago = document.getElementById("medio").value;
    }

    try {

        if (esRecurrente) {

            const recurrente = {
                tipo: tipo,
                monto: movimiento.monto,
                moneda: movimiento.moneda,
                categoria: movimiento.categoria,
                cuenta: movimiento.cuenta,
                fondo: movimiento.fondo,
                frecuencia: document.getElementById("frecuencia").value,
                fechaInicio: document.getElementById("fecha-inicio").value,
                fechaFin: document.getElementById("fecha-fin").value || null,
                descripcion: movimiento.descripcion
            };

            await enviarAPI("/movimientos-recurrentes", "POST", recurrente);

        } else {

            await enviarAPI("/movimientos", "POST", movimiento);
        }

        window.location.href = "movimientos.html";

    } catch (error) {
        console.error(error);
        alert("No se pudo guardar: " + error.message);
    }
});


// ---------- INICIO ----------

document.addEventListener("DOMContentLoaded", async function () {
    await cargarDatos();
    cambiarTipo("ingreso");

    document.getElementById("fecha").value =
        new Date().toISOString().split("T")[0];
});
