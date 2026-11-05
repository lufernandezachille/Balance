const lista = document.getElementById("lista-movimientos");
const mensajeVacio = document.getElementById("mensaje-vacio");

const movimientos = JSON.parse(localStorage.getItem("movimientos")) || [];

mensajeVacio.hidden = movimientos.length > 0;

movimientos.reverse().forEach(function (mov) {
    const item = document.createElement("li");
    const signo = mov.tipo === "gasto" ? "-" : "+";

    item.textContent =
        mov.fecha + " · " + mov.tipo + " · " +
        signo + mov.importe + " " + mov.moneda;

    lista.appendChild(item);
});