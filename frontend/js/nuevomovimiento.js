function cambiarTipo(tipo) {

    const esIngreso = tipo === "ingreso";
    const esGasto = tipo === "gasto";
    const esTransferencia = tipo === "transferencia";

    document.getElementById("tipo").value = tipo;

    // Pestañas activas
    document.getElementById("pestana-ingreso")
        .classList.toggle("activa", esIngreso);

    document.getElementById("pestana-gasto")
        .classList.toggle("activa", esGasto);

    document.getElementById("pestana-transferencia")
        .classList.toggle("activa", esTransferencia);

    // Mostrar u ocultar campos
    document.getElementById("campo-categoria").hidden = esTransferencia;
    document.getElementById("campo-medio").hidden = esTransferencia;
    document.getElementById("campo-destino").hidden = !esTransferencia;

    // Cambiar etiquetas
    document.getElementById("titulo-fondo").textContent =
        esIngreso ? "Fondo de destino" : "Fondo de origen";

    if (esIngreso) {
        document.getElementById("titulo-medio").textContent =
            "Medio de ingreso";
    } else {
        document.getElementById("titulo-medio").textContent =
            "Medio de pago";
    }

    // Categorías
    const categoria = document.getElementById("categoria");

    if (esIngreso) {
        categoria.innerHTML = `
            <option value="" disabled selected>
                Selecciona una categoría
            </option>
            <option value="sueldo">Sueldo y trabajo</option>
            <option value="negocios">Negocios y ventas</option>
            <option value="regalos">Regalos y dinero recibido</option>
            <option value="inversiones">Inversiones</option>
            <option value="alquileres">Alquileres</option>
            <option value="otros">Otros ingresos</option>
        `;
    } else {
        categoria.innerHTML = `
            <option value="" disabled selected>
                Selecciona una categoría
            </option>
            <option value="vivienda">Vivienda</option>
            <option value="alimentacion">Alimentación</option>
            <option value="transporte">Transporte</option>
            <option value="compras">Compras personales</option>
            <option value="salud">Salud</option>
            <option value="educacion">Educación</option>
            <option value="ocio">Ocio y entretenimiento</option>
            <option value="servicios">Servicios y suscripciones</option>
            <option value="viajes">Viajes</option>
            <option value="otros">Otros gastos</option>
        `;
    }

    // Medios
    const medio = document.getElementById("medio");

    if (esIngreso) {
        medio.innerHTML = `
            <option value="" disabled selected>
                Selecciona un medio
            </option>
            <option value="efectivo">Efectivo</option>
            <option value="transferencia">Transferencia</option>
        `;
    } else {
        medio.innerHTML = `
            <option value="" disabled selected>
                Selecciona un medio
            </option>
            <option value="efectivo">Efectivo</option>
            <option value="transferencia">Transferencia</option>
            <option value="tarjeta">Tarjeta</option>
        `;
    }

    // La recurrencia no se aplica a las transferencias
    if (esTransferencia) {
        document.getElementById("es-recurrente").checked = false;
        mostrarRecurrencia();
    }
}


function mostrarRecurrencia() {

    const recurrente = document.getElementById("es-recurrente").checked;

    const datos = document.getElementById("datos-recurrencia");

    datos.hidden = !recurrente;

    document.getElementById("frecuencia").required = recurrente;
    document.getElementById("fecha-inicio").required = recurrente;
    document.getElementById("fecha-fin").required = false;
}