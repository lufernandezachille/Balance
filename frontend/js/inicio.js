const API_URL = "http://localhost:8080/api";
const token = localStorage.getItem("authToken");

if (!token) {
    window.location.replace("htmls/cuenta.html");
}

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

async function cargarInicio() {
    if (!token) {
        window.location.href = "htmls/cuenta.html";
        return;
    }

    try {
        const [perfil, patrimonio, estadisticas, movimientos] = await Promise.all([
            consultarAPI("/usuarios/perfil"),
            consultarAPI("/patrimonio"),
            consultarAPI("/estadisticas"),
            consultarAPI("/movimientos")
        ]);

        const monedaBase = perfil.codigoMoneda || patrimonio.monedaBase || "ARS";

        // 1. Saludo
        const tituloSaludo = document.querySelector(".saludo-inicio h1");
        if (tituloSaludo) {
            tituloSaludo.textContent = `Hola, ${perfil.nombre} 👋🏻`;
        }

        // 2. Patrimonio total, ingresos y gastos del mes
        const patrimonioMonto = document.querySelector(".tarjeta-patrimonio-inicio h2");
        if (patrimonioMonto) {
            patrimonioMonto.textContent = formatoMoneda(patrimonio.totalConvertido, monedaBase);
        }

        const variacionTexto = document.querySelector(".variacion-patrimonio");
        if (variacionTexto) {
            variacionTexto.textContent = `Moneda base: ${monedaBase}`;
        }

        const resumenCols = document.querySelectorAll(".resumen-ingresos-gastos h3");
        const ingresosMes = Number(estadisticas.resumen?.ingresos || 0);
        const gastosMes = Number(estadisticas.resumen?.gastos || 0);
        const balanceMes = Number(estadisticas.resumen?.saldo || 0);

        if (resumenCols[0]) resumenCols[0].textContent = `+${formatoMoneda(ingresosMes, monedaBase)}`;
        if (resumenCols[1]) resumenCols[1].textContent = `-${formatoMoneda(gastosMes, monedaBase)}`;

        // 3. Balance mensual y Metas activas
        const balanceTexto = document.querySelector(".balance-mensual h2");
        if (balanceTexto) {
            const signo = balanceMes >= 0 ? "+" : "-";
            balanceTexto.textContent = `${signo}${formatoMoneda(Math.abs(balanceMes), monedaBase)}`;
        }

        const metasTexto = document.querySelector(".metas-ahorro h2");
        if (metasTexto) {
            const cantMetas = (estadisticas.metas || []).length;
            metasTexto.textContent = cantMetas === 1 ? "1 activa" : `${cantMetas} activas`;
        }

        // 4. Últimos movimientos (los 3 más recientes)
        renderizarUltimosMovimientos(movimientos);

        // 5. Gráfico de barras (Ingresos vs Gastos agrupado en 6 bloques del mes)
        renderizarGraficoBarras(estadisticas.evolucionDiaria || []);

    } catch (error) {
        console.error(error);
        if (error.message.includes("401") || error.message.includes("403")) {
            localStorage.removeItem("authToken");
            window.location.href = "htmls/iniciarsesion.html";
        }
    }
}

function renderizarUltimosMovimientos(movimientos) {
    const contenedor = document.querySelector(".lista-ultimos-movimientos");
    if (!contenedor) return;

    if (!movimientos || movimientos.length === 0) {
        contenedor.innerHTML = `<p>Todavía no registraste movimientos.</p>`;
        return;
    }

    const ordenados = [...movimientos].sort(
        (a, b) => b.fecha.localeCompare(a.fecha) || b.id - a.id
    );

    const ultimos = ordenados.slice(0, 3);

    contenedor.innerHTML = ultimos.map(mov => {
        const tipoLower = mov.tipo.toLowerCase();
        const signo = mov.tipo === "INGRESO" ? "+" : (mov.tipo === "GASTO" ? "-" : "");
        const icono = mov.tipo === "INGRESO" ? "▣" : (mov.tipo === "GASTO" ? "♜" : "⇄");
        const moneda = mov.moneda?.simbolo || mov.moneda?.codigo || "$";
        const titulo = mov.descripcion || mov.categoria?.nombre || mov.tipo;
        const subtitulo = mov.tipo === "TRANSFERENCIA"
            ? `${mov.fecha} · ${mov.cuenta?.nombre || ""} → ${mov.cuentaDestino?.nombre || ""}`
            : `${mov.fecha} · ${mov.medioPago || mov.categoria?.nombre || ""}`;

        const importe = Number(mov.monto).toLocaleString("es-AR", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });

        return `
            <article class="movimiento-inicio">
                <div class="icono-movimiento-inicio ${tipoLower}">
                    <span>${icono}</span>
                </div>
                <div class="informacion-movimiento-inicio">
                    <h3>${titulo}</h3>
                    <p>${subtitulo}</p>
                </div>
                <p class="monto-inicio ${tipoLower}">${signo} ${moneda} ${importe}</p>
            </article>
        `;
    }).join("");
}

function renderizarGraficoBarras(evolucionDiaria) {
    const grafico = document.querySelector(".grafico-inicio");
    if (!grafico || evolucionDiaria.length === 0) return;

    // Agrupar los días del mes en 6 bloques de ~5 días para mantener las 6 columnas del diseño
    const bloques = [];
    const tamBloque = Math.ceil(evolucionDiaria.length / 6);

    for (let i = 0; i < evolucionDiaria.length; i += tamBloque) {
        const slice = evolucionDiaria.slice(i, i + tamBloque);
        const ing = slice.reduce((acc, d) => acc + Number(d.ingresos || 0), 0);
        const gas = slice.reduce((acc, d) => acc + Number(d.gastos || 0), 0);
        bloques.push({ ing, gas });
    }

    const maxValor = Math.max(
        ...bloques.map(b => Math.max(b.ing, b.gas)),
        1
    );

    grafico.innerHTML = bloques.map(b => {
        const altIng = Math.max(Math.round((b.ing / maxValor) * 100), b.ing > 0 ? 8 : 2);
        const altGas = Math.max(Math.round((b.gas / maxValor) * 100), b.gas > 0 ? 8 : 2);
        return `
            <div class="grupo-barras">
                <div class="barra ingreso" style="height: ${altIng}%;"></div>
                <div class="barra gasto" style="height: ${altGas}%;"></div>
            </div>
        `;
    }).join("");
}

document.addEventListener("DOMContentLoaded", cargarInicio);