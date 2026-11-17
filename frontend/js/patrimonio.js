const API_URL = "http://localhost:8080/api";
const token = localStorage.getItem("authToken");

let monedas = [];
let cuentas = [];
let fondos = [];
let patrimonioActual = null;
let cotizacionesActuales = null;
let periodoSeleccionado = "1A";

const NOMBRES_TIPO_CUENTA = {
    EFECTIVO: "Efectivo",
    BANCO: "Banco",
    BILLETERA_VIRTUAL: "Billetera virtual",
    TARJETA_CREDITO: "Tarjeta de crédito",
    INVERSIONES: "Inversiones"
};

const ICONOS_TIPO_CUENTA = {
    EFECTIVO: "▣",
    BANCO: "🏛",
    BILLETERA_VIRTUAL: "◈",
    TARJETA_CREDITO: "▭",
    INVERSIONES: "↗"
};

function formatoMoneda(monto, moneda = "ARS") {
    return new Intl.NumberFormat("es-AR", {
        style: "currency",
        currency: moneda,
        maximumFractionDigits: 2
    }).format(Number(monto) || 0);
}

async function consultarAPI(ruta) {
    const respuesta = await fetch(`${API_URL}${ruta}`, {
        headers: { "Authorization": `Bearer ${token}` }
    });
    if (!respuesta.ok) {
        throw new Error(`Error al consultar ${ruta}`);
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
        body: datos ? JSON.stringify(datos) : null
    });

    if (!respuesta.ok) {
        const texto = await respuesta.text();
        let mensaje = texto;
        try {
            const json = JSON.parse(texto);
            mensaje = json.message || json.mensaje || texto;
        } catch (_) {}
        throw new Error(mensaje || "Error en la operación.");
    }

    const contenido = await respuesta.text();
    try {
        return contenido ? JSON.parse(contenido) : null;
    } catch (_) {
        return contenido;
    }
}

// ---------- PESTAÑAS ----------

function cambiarPestanaPatrimonio(pestana) {
    document.getElementById("tab-resumen").classList.toggle("activa", pestana === "resumen");
    document.getElementById("tab-cuentas").classList.toggle("activa", pestana === "cuentas");
    document.getElementById("tab-fondos").classList.toggle("activa", pestana === "fondos");

    document.getElementById("vista-resumen").hidden = pestana !== "resumen";
    document.getElementById("vista-cuentas").hidden = pestana !== "cuentas";
    document.getElementById("vista-fondos").hidden = pestana !== "fondos";
}

// ---------- CARGA INICIAL ----------

async function cargarTodoPatrimonio() {
    if (!token) {
        alert("Tenés que iniciar sesión.");
        window.location.href = "iniciarsesion.html";
        return;
    }

    try {
        [monedas, cuentas, fondos, patrimonioActual, cotizacionesActuales] = await Promise.all([
            consultarAPI("/monedas"),
            consultarAPI("/cuentas"),
            consultarAPI("/fondos"),
            consultarAPI("/patrimonio"),
            consultarAPI("/cotizaciones").catch(() => null)
        ]);

        poblarSelectMonedasCuenta();
        renderizarResumen();
        renderizarCuentas();
        renderizarFondos();
        await cargarEvolucion(periodoSeleccionado);

    } catch (error) {
        console.error(error);
        alert("No se pudo cargar la información de patrimonio.");
    }
}

// ---------- VISTA 1: RESUMEN Y GRÁFICOS ----------

function renderizarResumen() {
    if (!patrimonioActual) return;

    const monedaBase = patrimonioActual.monedaBase || "ARS";

    document.getElementById("patrimonio-total-monto").textContent =
        formatoMoneda(patrimonioActual.totalConvertido, monedaBase);

    document.getElementById("patrimonio-moneda-base").textContent =
        `Equivalente en ${monedaBase}`;

    const textoCot = document.getElementById("texto-cotizaciones");
    if (cotizacionesActuales?.ultimaActualizacion) {
        const fecha = new Date(cotizacionesActuales.ultimaActualizacion);
        textoCot.textContent = isNaN(fecha)
            ? `Cotizaciones actualizadas: ${cotizacionesActuales.ultimaActualizacion}`
            : `Cotizaciones actualizadas: ${fecha.toLocaleString("es-AR", {
                day: "numeric", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit"
            })}`;
    } else {
        textoCot.textContent = "Cotizaciones al día";
    }

    renderizarDistribucionMoneda();
}

function renderizarDistribucionMoneda() {
    const barra = document.getElementById("barra-distribucion");
    const lista = document.getElementById("lista-monedas");

    const totales = patrimonioActual.totalesPorMoneda || {};
    const monedaBase = patrimonioActual.monedaBase || "ARS";
    const rates = cotizacionesActuales?.rates || {};
    const entradas = Object.entries(totales).filter(([, monto]) => Number(monto) > 0);

    if (entradas.length === 0) {
        barra.innerHTML = `<div class="segmento ars" style="width: 100%;"></div>`;
        lista.innerHTML = `<p>Todavía no tenés saldo en cuentas incluidas en tu patrimonio.</p>`;
        return;
    }

    const items = entradas.map(([codigo, saldoOriginal]) => {
        let enBase = Number(saldoOriginal);
        if (codigo !== monedaBase && rates[codigo] && rates[monedaBase]) {
            enBase = (Number(saldoOriginal) / Number(rates[codigo])) * Number(rates[monedaBase]);
        }
        return { codigo, saldoOriginal: Number(saldoOriginal), enBase };
    });

    const sumaBase = items.reduce((acc, it) => acc + it.enBase, 0) || 1;
    items.sort((a, b) => b.enBase - a.enBase);

    const clasesColor = ["ars", "usd", "otras"];

    barra.innerHTML = items.map((it, idx) => {
        const pct = ((it.enBase / sumaBase) * 100).toFixed(1);
        const clase = clasesColor[idx] || "otras";
        return `<div class="segmento ${clase}" style="width: ${pct}%;"></div>`;
    }).join("");

    lista.innerHTML = items.map((it, idx) => {
        const pct = Math.round((it.enBase / sumaBase) * 100);
        const clase = clasesColor[idx] || "otras";
        return `
            <div class="moneda-patrimonio">
                <div class="identificacion-moneda">
                    <span class="indicador-moneda ${clase}"></span>
                    <p>${it.codigo}</p>
                </div>
                <p class="porcentaje-moneda">${pct}%</p>
                <p class="saldo-moneda">${formatoMoneda(it.saldoOriginal, it.codigo)}</p>
            </div>
        `;
    }).join("");
}

async function cambiarPeriodoEvolucion(periodo) {
    periodoSeleccionado = periodo;

    document.querySelectorAll(".periodo-evolucion").forEach(btn => {
        btn.classList.toggle("activo", btn.dataset.periodo === periodo);
    });

    document.getElementById("titulo-evolucion").textContent = `Evolución (${periodo})`;

    await cargarEvolucion(periodo);
}

async function cargarEvolucion(periodo) {
    const hoy = new Date();
    const mesActual = hoy.getMonth() + 1;
    const anioActual = hoy.getFullYear();

    try {
        if (periodo === "1M") {
            const est = await consultarAPI(`/estadisticas?mes=${mesActual}&anio=${anioActual}`);
            let acumulado = 0;
            const serie = (est.evolucionDiaria || []).map(d => {
                acumulado += Number(d.ingresos || 0) - Number(d.gastos || 0);
                return acumulado;
            });
            dibujarCurvaSVG(serie);
        } else {
            const cantidadMeses = periodo === "6M" ? 6 : 12;
            const consultas = [];

            for (let i = cantidadMeses - 1; i >= 0; i--) {
                const fecha = new Date(anioActual, mesActual - 1 - i, 1);
                const m = fecha.getMonth() + 1;
                const a = fecha.getFullYear();
                consultas.push(
                    consultarAPI(`/estadisticas?mes=${m}&anio=${a}`)
                        .then(res => Number(res.resumen?.saldo || 0))
                        .catch(() => 0)
                );
            }

            const saldosMensuales = await Promise.all(consultas);
            let acumulado = 0;
            const serie = saldosMensuales.map(saldoMes => {
                acumulado += saldoMes;
                return acumulado;
            });
            dibujarCurvaSVG(serie);
        }
    } catch (e) {
        console.error("Error cargando evolución:", e);
    }
}

function dibujarCurvaSVG(valores) {
    const polyline = document.getElementById("linea-evolucion");
    const polygon = document.getElementById("area-evolucion");
    const grupoPuntos = document.getElementById("puntos-evolucion");

    if (!polyline || !polygon || !grupoPuntos || valores.length === 0) return;

    const min = Math.min(...valores, 0);
    const max = Math.max(...valores, 1);
    const rango = (max - min) || 1;

    const ancho = 320;
    const yBase = 110;
    const yTope = 15;

    const puntos = valores.map((val, idx) => {
        const x = (idx / Math.max(valores.length - 1, 1)) * ancho;
        const y = yBase - ((val - min) / rango) * (yBase - yTope);
        return { x: Number(x.toFixed(1)), y: Number(y.toFixed(1)) };
    });

    const cadenaPuntos = puntos.map(p => `${p.x},${p.y}`).join(" ");

    polyline.setAttribute("points", cadenaPuntos);
    polygon.setAttribute("points", `0,${yBase} ${cadenaPuntos} ${ancho},${yBase}`);

    // Dibujar hasta 12 puntos visibles sobre la línea
    const paso = Math.max(1, Math.floor(puntos.length / 8));
    grupoPuntos.innerHTML = puntos
        .filter((_, i) => i % paso === 0 || i === puntos.length - 1)
        .map(p => `<circle cx="${p.x}" cy="${p.y}" r="3" class="punto-grafico"/>`)
        .join("");
}

// ---------- VISTA 2: MIS CUENTAS (ABM) ----------

function poblarSelectMonedasCuenta() {
    const select = document.getElementById("cuenta-moneda");
    select.innerHTML = `<option value="" disabled selected>Selecciona una moneda</option>`;
    monedas.forEach(m => {
        select.innerHTML += `<option value="${m.id}">${m.codigo} - ${m.nombre}</option>`;
    });
}

function renderizarCuentas() {
    const contenedor = document.getElementById("lista-cuentas-patrimonio");
    const vacio = document.getElementById("mensaje-cuentas-vacio");

    vacio.hidden = cuentas.length > 0;

    contenedor.innerHTML = cuentas.map(c => {
        const tipoTexto = NOMBRES_TIPO_CUENTA[c.tipo] || c.tipo;
        const icono = ICONOS_TIPO_CUENTA[c.tipo] || "$";
        const codigoMoneda = c.moneda?.codigo || "ARS";
        const notaPatrimonio = c.incluirEnPatrimonio ? "" : " · Excluida del patrimonio";

        return `
            <article class="tarjeta-fondo">
                <div class="icono-fondo">
                    <span>${icono}</span>
                </div>
                <div class="informacion-fondo">
                    <h3>${c.nombre}</h3>
                    <p>${tipoTexto} (${codigoMoneda})${notaPatrimonio}</p>
                    <div style="display: flex; gap: 10px; margin-top: 6px;">
                        <button type="button" class="boton-secundario" style="padding: 4px 10px; font-size: 0.8rem;" onclick="abrirEditarCuenta(${c.id})">
                            Editar
                        </button>
                        <button type="button" class="boton-peligro" style="padding: 4px 10px; font-size: 0.8rem;" onclick="eliminarCuenta(${c.id})">
                            Eliminar
                        </button>
                    </div>
                </div>
                <p class="saldo-fondo">${formatoMoneda(c.saldoActual, codigoMoneda)}</p>
            </article>
        `;
    }).join("");
}

const formCuenta = document.getElementById("form-cuenta");

document.getElementById("btn-mostrar-form-cuenta").addEventListener("click", () => {
    formCuenta.reset();
    document.getElementById("cuenta-id").value = "";
    document.getElementById("campo-cuenta-saldo").hidden = false;
    document.getElementById("cuenta-incluir").checked = true;
    document.getElementById("btn-guardar-cuenta").textContent = "Crear cuenta";
    formCuenta.hidden = false;
});

document.getElementById("btn-cancelar-cuenta").addEventListener("click", () => {
    formCuenta.hidden = true;
});

function abrirEditarCuenta(id) {
    const cuenta = cuentas.find(c => c.id === id);
    if (!cuenta) return;

    document.getElementById("cuenta-id").value = cuenta.id;
    document.getElementById("cuenta-nombre").value = cuenta.nombre;
    document.getElementById("cuenta-tipo").value = cuenta.tipo;
    document.getElementById("cuenta-moneda").value = cuenta.moneda.id;
    document.getElementById("cuenta-incluir").checked = cuenta.incluirEnPatrimonio;

    // En edición el saldo se modifica mediante movimientos, no directo
    document.getElementById("campo-cuenta-saldo").hidden = true;
    document.getElementById("btn-guardar-cuenta").textContent = "Guardar cambios";
    formCuenta.hidden = false;
}

formCuenta.addEventListener("submit", async (e) => {
    e.preventDefault();

    const id = document.getElementById("cuenta-id").value;
    const payload = {
        nombre: document.getElementById("cuenta-nombre").value.trim(),
        tipo: document.getElementById("cuenta-tipo").value,
        moneda: { id: Number(document.getElementById("cuenta-moneda").value) },
        incluirEnPatrimonio: document.getElementById("cuenta-incluir").checked
    };

    if (!id) {
        payload.saldoActual = Number(document.getElementById("cuenta-saldo").value || 0);
    }

    try {
        if (id) {
            await enviarAPI(`/cuentas/${id}`, "PUT", payload);
        } else {
            await enviarAPI("/cuentas", "POST", payload);
        }

        formCuenta.hidden = true;
        await cargarTodoPatrimonio();
    } catch (error) {
        alert("No se pudo guardar la cuenta: " + error.message);
    }
});

async function eliminarCuenta(id) {
    if (!confirm("¿Querés eliminar esta cuenta? (Recordá que debe tener saldo $ 0)")) {
        return;
    }

    try {
        await enviarAPI(`/cuentas/${id}`, "DELETE");
        await cargarTodoPatrimonio();
    } catch (error) {
        alert("No se pudo eliminar: " + error.message);
    }
}

// ---------- VISTA 3: MIS FONDOS (ABM) ----------

function renderizarFondos() {
    const contenedor = document.getElementById("lista-fondos-patrimonio");

    contenedor.innerHTML = fondos.map(f => {
        const subtitulo = f.esFondoGeneral
            ? "Fondo principal (automático)"
            : (f.esMeta ? "Meta de ahorro" : "Fondo personalizado");

        const botones = f.esFondoGeneral
            ? ""
            : `
                <div style="display: flex; gap: 10px; margin-top: 6px;">
                    <button type="button" class="boton-secundario" style="padding: 4px 10px; font-size: 0.8rem;" onclick="abrirEditarFondo(${f.id})">
                        Editar
                    </button>
                    <button type="button" class="boton-peligro" style="padding: 4px 10px; font-size: 0.8rem;" onclick="eliminarFondo(${f.id})">
                        Eliminar
                    </button>
                </div>
            `;

        return `
            <article class="tarjeta-fondo">
                <div class="icono-fondo">
                    <span>${f.esMeta ? "◎" : "▣"}</span>
                </div>
                <div class="informacion-fondo">
                    <h3>${f.nombre}</h3>
                    <p>${subtitulo}</p>
                    ${botones}
                </div>
            </article>
        `;
    }).join("");
}

const formFondo = document.getElementById("form-fondo");

document.getElementById("btn-mostrar-form-fondo").addEventListener("click", () => {
    formFondo.reset();
    document.getElementById("fondo-id").value = "";
    document.getElementById("btn-guardar-fondo").textContent = "Crear fondo";
    formFondo.hidden = false;
});

document.getElementById("btn-cancelar-fondo").addEventListener("click", () => {
    formFondo.hidden = true;
});

function abrirEditarFondo(id) {
    const fondo = fondos.find(f => f.id === id);
    if (!fondo || fondo.esFondoGeneral) return;

    document.getElementById("fondo-id").value = fondo.id;
    document.getElementById("fondo-nombre").value = fondo.nombre;
    document.getElementById("btn-guardar-fondo").textContent = "Guardar cambios";
    formFondo.hidden = false;
}

formFondo.addEventListener("submit", async (e) => {
    e.preventDefault();

    const id = document.getElementById("fondo-id").value;
    const existente = id ? fondos.find(f => f.id === Number(id)) : null;

    const payload = {
        nombre: document.getElementById("fondo-nombre").value.trim(),
        esMeta: existente ? existente.esMeta : false,
        montoObjetivo: existente ? existente.montoObjetivo : null,
        fechaObjetivo: existente ? existente.fechaObjetivo : null,
        colorIcono: existente ? existente.colorIcono : null
    };

    try {
        if (id) {
            await enviarAPI(`/fondos/${id}`, "PUT", payload);
        } else {
            await enviarAPI("/fondos", "POST", payload);
        }

        formFondo.hidden = true;
        await cargarTodoPatrimonio();
    } catch (error) {
        alert("No se pudo guardar el fondo: " + error.message);
    }
});

async function eliminarFondo(id) {
    if (!confirm("¿Querés eliminar este fondo? El saldo asignado volverá automáticamente al fondo General.")) {
        return;
    }

    try {
        await enviarAPI(`/fondos/${id}`, "DELETE");
        await cargarTodoPatrimonio();
    } catch (error) {
        alert("No se pudo eliminar el fondo: " + error.message);
    }
}

document.addEventListener("DOMContentLoaded", cargarTodoPatrimonio);