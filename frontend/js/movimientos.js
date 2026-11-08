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