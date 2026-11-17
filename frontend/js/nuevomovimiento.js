const API_URL = "http://localhost:8080/api";
const token = localStorage.getItem("authToken");

let monedas = [];
let cuentas = [];
let fondos = [];
let categorias = [];
let excepcionesSeleccionadas = [];

// ---------- PETICIONES AL BACKEND ----------

async function consultarAPI(ruta) {
    const respuesta = await fetch(`${API_URL}${ruta}`, {
        headers: {
            "Authorization": `Bearer ${token}`
        }
    });

    if (!respuesta.ok) {
        throw new Error(`No se pudieron cargar los datos de ${ruta}.`);
    }

    return await respuesta.json();
}

async function enviarAPI(ruta, metodo, datos) {
    const respuesta = await fetch(`${API_URL}${ruta}`, {
        method: metodo,
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify(datos)
    });

    if (!respuesta.ok) {
        const texto = await respuesta.text();
        let mensaje = texto;
        try {
            const json = JSON.parse(texto);
            mensaje = json.message || json.mensaje || texto;
        } catch (_) {}
        throw new Error(mensaje || "No se pudo guardar el movimiento.");
    }

    const contenido = await respuesta.text();
    return contenido ? JSON.parse(contenido) : null;
}

// ---------- CARGAR DATOS INICIALES ----------

async function cargarDatos() {
    if (!token) {
        alert("Tenés que iniciar sesión.");
        window.location.href = "iniciarsesion.html";
        return;
    }

    try {
        [monedas, cuentas, fondos, categorias] = await Promise.all([
            consultarAPI("/monedas"),
            consultarAPI("/cuentas"),
            consultarAPI("/fondos"),
            consultarAPI("/categorias")
        ]);

        cargarMonedas();
        cargarCuentas();
        cargarFondos();

    } catch (error) {
        console.error(error);
        alert("No se pudieron cargar las monedas, cuentas, fondos y categorías.");
    }
}

// ---------- MONEDAS (DESDE GET /api/monedas) ----------

function cargarMonedas() {
    const select = document.getElementById("moneda");

    select.innerHTML = `<option value="" disabled selected>Moneda</option>`;

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

    const placeholder = `<option value="" disabled selected>Selecciona una cuenta</option>`;
    select.innerHTML = placeholder;
    selectDestino.innerHTML = placeholder;

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

function filtrarCuentasPorMoneda(monedaId) {
    const cuenta = document.getElementById("cuenta");
    const destino = document.getElementById("cuenta-destino");

    const cuentaActual = cuentas.find(c => c.id === Number(cuenta.value));
    if (cuentaActual && cuentaActual.moneda.id !== monedaId) {
        cuenta.value = "";
    }

    const destinoActual = cuentas.find(c => c.id === Number(destino.value));
    if (destinoActual && destinoActual.moneda.id !== monedaId) {
        destino.value = "";
    }

    Array.from(cuenta.options).forEach(opcion => {
        if (opcion.value) {
            const cuentaDatos = cuentas.find(c => c.id === Number(opcion.value));
            opcion.hidden = cuentaDatos && cuentaDatos.moneda.id !== monedaId;
        }
    });

    Array.from(destino.options).forEach(opcion => {
        if (opcion.value) {
            const cuentaDatos = cuentas.find(c => c.id === Number(opcion.value));
            opcion.hidden = cuentaDatos && cuentaDatos.moneda.id !== monedaId;
        }
    });
}

document.getElementById("moneda").addEventListener("change", function () {
    filtrarCuentasPorMoneda(Number(this.value));
});

// Si elegís primero una cuenta, selecciona automáticamente su moneda
document.getElementById("cuenta").addEventListener("change", function () {
    const cuentaDatos = cuentas.find(c => c.id === Number(this.value));
    if (cuentaDatos && cuentaDatos.moneda) {
        document.getElementById("moneda").value = cuentaDatos.moneda.id;
        filtrarCuentasPorMoneda(cuentaDatos.moneda.id);
    }
});

// ---------- FONDOS ----------

function cargarFondos() {
    const select = document.getElementById("fondo");
    const selectDestino = document.getElementById("destino");

    const placeholder = `<option value="" disabled selected>Selecciona un fondo</option>`;
    select.innerHTML = placeholder;
    selectDestino.innerHTML = placeholder;

    fondos.forEach(fondo => {
        const etiqueta = fondo.esMeta ? `${fondo.nombre} (Meta)` : fondo.nombre;
        const opcion = `<option value="${fondo.id}">${etiqueta}</option>`;
        select.innerHTML += opcion;
        selectDestino.innerHTML += opcion;
    });

    const general = fondos.find(f => f.esFondoGeneral);
    if (general) {
        select.value = general.id;
        selectDestino.value = general.id;
    }
}

// ---------- CATEGORÍAS (DESDE GET /api/categorias) ----------

function cargarCategorias(tipo) {
    const select = document.getElementById("categoria");
    const tipoUpper = tipo.toUpperCase();

    select.innerHTML = `<option value="" disabled selected>Selecciona una categoría</option>`;

    categorias
        .filter(categoria => categoria.tipo === tipoUpper)
        .forEach(categoria => {
            select.innerHTML += `
                <option value="${categoria.id}">
                    ${categoria.nombre}
                </option>
            `;
        });
}

// ---------- CAMBIAR TIPO (INGRESO / GASTO / TRANSFERENCIA) ----------

function cambiarTipo(tipo) {
    const esIngreso = tipo === "ingreso";
    const esGasto = tipo === "gasto";
    const esTransferencia = tipo === "transferencia";

    document.getElementById("tipo").value = tipo.toUpperCase();

    document.getElementById("pestana-ingreso").classList.toggle("activa", esIngreso);
    document.getElementById("pestana-gasto").classList.toggle("activa", esGasto);
    document.getElementById("pestana-transferencia").classList.toggle("activa", esTransferencia);

    document.getElementById("campo-categoria").hidden = esTransferencia;
    document.getElementById("campo-cuenta-destino").hidden = !esTransferencia;
    document.getElementById("campo-destino").hidden = !esTransferencia;
    document.getElementById("campo-recurrente").hidden = esTransferencia;

    document.getElementById("categoria").required = !esTransferencia;
    document.getElementById("cuenta-destino").required = esTransferencia;
    document.getElementById("destino").required = esTransferencia;

    document.getElementById("titulo-cuenta").textContent =
        esIngreso ? "Cuenta de destino" : "Cuenta de origen";

    document.getElementById("titulo-fondo").textContent =
        esTransferencia ? "Fondo de origen" : "Fondo";

    if (!esTransferencia) {
        cargarCategorias(tipo);
    }

    if (esTransferencia) {
        document.getElementById("es-recurrente").checked = false;
        mostrarRecurrencia();
    }
}

// ---------- RECURRENCIA Y MÚLTIPLES EXCEPCIONES ----------

function mostrarRecurrencia() {
    const recurrente = document.getElementById("es-recurrente").checked;

    document.getElementById("datos-recurrencia").hidden = !recurrente;
    document.getElementById("frecuencia").required = recurrente;
    document.getElementById("fecha-inicio").required = recurrente;

    if (recurrente && !document.getElementById("fecha-inicio").value) {
        document.getElementById("fecha-inicio").value =
            document.getElementById("fecha").value || new Date().toISOString().split("T")[0];
    }
}

function renderizarExcepciones() {
    const contenedor = document.getElementById("lista-excepciones");
    if (!contenedor) return;

    contenedor.innerHTML = excepcionesSeleccionadas.map(fecha => `
        <span style="display: inline-flex; align-items: center; gap: 6px; padding: 4px 10px; border-radius: 999px; background: rgba(0,0,0,0.08); font-size: 0.85rem;">
            ${fecha}
            <button type="button" onclick="quitarExcepcion('${fecha}')" style="border: none; background: none; cursor: pointer; font-weight: bold;">×</button>
        </span>
    `).join("");
}

function quitarExcepcion(fecha) {
    excepcionesSeleccionadas = excepcionesSeleccionadas.filter(f => f !== fecha);
    renderizarExcepciones();
}

const btnAgregarExcepcion = document.getElementById("boton-agregar-excepcion");
if (btnAgregarExcepcion) {
    btnAgregarExcepcion.addEventListener("click", () => {
        const input = document.getElementById("fecha-excepcion");
        const fecha = input.value;

        if (!fecha) {
            alert("Seleccioná una fecha antes de agregar la excepción.");
            return;
        }

        if (excepcionesSeleccionadas.includes(fecha)) {
            alert("Esa fecha ya está agregada como excepción.");
            return;
        }

        excepcionesSeleccionadas.push(fecha);
        excepcionesSeleccionadas.sort();
        input.value = "";
        renderizarExcepciones();
    });
}

// ---------- GUARDAR O ACTUALIZAR MOVIMIENTO ----------

const formulario = document.getElementById("formulario-movimiento");

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
        medioPago: null,
        fecha: document.getElementById("fecha").value,
        descripcion: document.getElementById("descripcion").value.trim() || null
    };

    if (esTransferencia) {
        const cuentaDestinoId = Number(document.getElementById("cuenta-destino").value);
        const fondoDestinoId = Number(document.getElementById("destino").value);
        const cuentaDestino = cuentas.find(c => c.id === cuentaDestinoId);

        if (!cuentaDestino || cuentaDestinoId === cuentaId) {
            alert("Seleccioná una cuenta de destino diferente a la de origen.");
            return;
        }

        if (cuentaDestino.moneda.id !== monedaId) {
            alert("Las dos cuentas deben tener la misma moneda.");
            return;
        }

        movimiento.cuentaDestino = { id: cuentaDestinoId };
        movimiento.fondoDestino = { id: fondoDestinoId };
    } else {
        movimiento.categoria = {
            id: Number(document.getElementById("categoria").value)
        };
    }

    try {
        const idEditar = localStorage.getItem("movimientoEditar");

        if (idEditar) {
            await enviarAPI(`/movimientos/${idEditar}`, "PUT", movimiento);
            localStorage.removeItem("movimientoEditar");
        } else if (esRecurrente) {
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

            const creado = await enviarAPI("/movimientos-recurrentes", "POST", recurrente);

            if (creado?.id && excepcionesSeleccionadas.length > 0) {
                for (const fechaEx of excepcionesSeleccionadas) {
                    await enviarAPI(`/movimientos-recurrentes/${creado.id}/excepciones`, "POST", {
                        fecha: fechaEx
                    });
                }
            }
        } else {
            await enviarAPI("/movimientos", "POST", movimiento);
        }

        window.location.href = "movimientos.html";

    } catch (error) {
        console.error(error);
        alert("No se pudo guardar: " + error.message);
    }
});

// ---------- INICIO (Y PRECARGA SI ES EDICIÓN) ----------

document.addEventListener("DOMContentLoaded", async function () {
    await cargarDatos();

    const idEditar = localStorage.getItem("movimientoEditar");

    if (idEditar) {
        document.querySelector(".titulo-pagina").textContent = "Editar movimiento";
        document.getElementById("boton-movimiento").textContent = "Guardar cambios";
        document.getElementById("campo-recurrente").hidden = true;

        try {
            const lista = await consultarAPI("/movimientos");
            const mov = lista.find(m => m.id === Number(idEditar));

            if (mov) {
                cambiarTipo(mov.tipo.toLowerCase());
                document.getElementById("moneda").value = mov.moneda.id;
                filtrarCuentasPorMoneda(mov.moneda.id);

                document.getElementById("importe").value = mov.monto;
                document.getElementById("cuenta").value = mov.cuenta?.id || "";
                if (mov.cuentaDestino) {
                    document.getElementById("cuenta-destino").value = mov.cuentaDestino.id;
                }
                if (mov.fondo) {
                    document.getElementById("fondo").value = mov.fondo.id;
                }
                if (mov.fondoDestino) {
                    document.getElementById("destino").value = mov.fondoDestino.id;
                }
                if (mov.categoria) {
                    document.getElementById("categoria").value = mov.categoria.id;
                }
                document.getElementById("fecha").value = mov.fecha;
                document.getElementById("descripcion").value = mov.descripcion || "";
            }
        } catch (e) {
            console.error(e);
        }
    } else {
        cambiarTipo("ingreso");
        document.getElementById("fecha").value = new Date().toISOString().split("T")[0];
    }
});